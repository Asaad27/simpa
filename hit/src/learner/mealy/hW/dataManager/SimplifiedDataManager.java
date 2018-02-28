package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import automata.mealy.Mealy;
import automata.State;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

public class SimplifiedDataManager {
	public static SimplifiedDataManager instance;// TODO either make a proper
													// singleton either do
													// something else
	private MealyDriver driver;
	private LmTrace trace;
	private LmTrace globalTrace;
	private final List<InputSequence> W; // Characterization set
	public final InputSequence h;
	private final ArrayList<String> I;// Input Symbols
	private Map<List<OutputSequence>, FullyQualifiedState> Q;// known states
	private Set<FullyQualifiedState> notFullyKnownStates;// Fully qualified
															// states with
															// undefined
															// transitions
	private LmConjecture conjecture;
	private FullyQualifiedState currentState;

	private Map<OutputSequence, FullyQualifiedState> hResponse2State;
	private Map<OutputSequence, List<OutputSequence>> hResponse2Wresponses;
	private HomingSequenceChecker hChecker;

	private Collection<TraceTree> expectedTraces;
	
	protected Collection<FullyQualifiedState> identifiedFakeStates=new ArrayList<>();

	public Collection<FullyQualifiedState> getIdentifiedFakeStates() {
		return identifiedFakeStates;
	}
	public int getHResponsesNb(){
		return hResponse2State.keySet().size();
	}

	public FullyQualifiedState getState(OutputSequence hResponse) {
		assert (hResponse.getLength() == h.getLength());
		FullyQualifiedState s = hResponse2State.get(hResponse);
		if (s == null && W.size() == 0) {
			s = getFullyQualifiedState(new ArrayList<OutputSequence>());
		}
		return s;
	}

	private List<OutputSequence> getOrCreateWResponseAfterHresponse(
			OutputSequence hR) {
		List<OutputSequence> wR = hResponse2Wresponses.get(hR);
		if (wR != null)
			return wR;
		wR = new ArrayList<OutputSequence>();
		hResponse2Wresponses.put(hR, wR);
		return wR;

	}

	public InputSequence getMissingInputSequence(OutputSequence hResponse) {
		assert (getState(hResponse) == null);
		List<OutputSequence> wRs = getOrCreateWResponseAfterHresponse(hResponse);
		return W.get(wRs.size());
	}

	public FullyQualifiedState addWresponseAfterH(OutputSequence hResponse,
			InputSequence w, OutputSequence wResponse) {
		List<OutputSequence> wRs = getOrCreateWResponseAfterHresponse(hResponse);
		assert (wRs.size() < W.size()) : "all responses already known";
		assert (w.getLength() == wResponse.getLength());
		assert (w.equals(W.get(wRs.size())));
		wRs.add(wResponse);
		if (wRs.size() == W.size()) {
			FullyQualifiedState q = getFullyQualifiedState(wRs);
			hResponse2State.put(hResponse, q);
			LogManager.logInfo("We found that the response " + hResponse
					+ " to homing sequence lead in state " + q);
			return q;
		}
		return null;
	}


	public SimplifiedDataManager(MealyDriver driver, List<InputSequence> W,
			InputSequence h, LmTrace globalTrace) {
		this.trace = new LmTrace();
		this.globalTrace = globalTrace;
		this.W = W;
		this.h = h;
		this.I = new ArrayList<String>(driver.getInputSymbols());
		this.driver = driver;
		Q = new HashMap<List<OutputSequence>, FullyQualifiedState>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new LmConjecture(driver);
		instance = this;
		hResponse2State = new HashMap<>();
		hResponse2Wresponses = new HashMap<>();
		currentState = null;
		expectedTraces = new ArrayList<>();
		hChecker = new HomingSequenceChecker(h);
	}
	
	private void extendTrace(String input, String output){
		trace.append(input, output);
		globalTrace.append(input, output);
	}
	
	public String walkWithoutCheck(String input, String output){
		extendTrace(input, output);
		String expectedOutput=null;
		if (currentState != null) {
			FullyKnownTrace transition = currentState.getKnownTransition(input);
			if (transition != null) {
				expectedOutput=transition.getTrace().getOutput(0);
				currentState = transition.getEnd();
			} else
				currentState = null;
		}
		return expectedOutput;
	}
	public OutputSequence walkWithoutCheck(LmTrace seq){
		OutputSequence outputs=new OutputSequence();
		for (int i=0;i<seq.size();i++)
			outputs.addOutput(walkWithoutCheck(seq.getInput(i),seq.getOutput(i)));
		return outputs;
	}
	

	public String apply(String input) {
		LogManager.logInfo("expected Traces are " + expectedTraces);
		String output = driver.execute(input);
		extendTrace(input, output);
		// check for Non-Determinism after homing sequence
		hChecker.applyInput(input, output);

		// checking the compatibility with K
		if (currentState != null) {
			String expectedOutput = currentState.getExpectedKOutput(input);
			if (expectedOutput != null)
				if (!expectedOutput.equals(output))
					throw new InconsistancyWithConjectureAtEndOfTraceException(
							input, expectedOutput, null, output);
		}

		// checking the compatibility with expected traces.
		Collection<TraceTree> nextExpectedTraces = new ArrayList<TraceTree>();
		for (TraceTree traceTree : expectedTraces) {
			if (traceTree == null)
				continue;
			String expectedOutput = traceTree.getOutput(input);
			if (expectedOutput != null && !expectedOutput.equals(output))
				LogManager.logWarning("ignored ND : "
						+ new InconsistancyWithConjectureAtEndOfTraceException(
								input, expectedOutput, traceTree, output));
			nextExpectedTraces.add(traceTree.getSubTreeRO(input));
		}
		expectedTraces = nextExpectedTraces;

		// updating the currentState in conjecture
		if (currentState != null) {
			FullyKnownTrace transition = currentState.getKnownTransition(input);
			if (transition != null) {
				if (!transition.getTrace().getOutput(0).equals(output)) {
					throw new InconsistancyWithConjectureAtEndOfTraceException(
							input, transition.getTrace().getOutput(0), null,
							output);
				}
				currentState = transition.getEnd();
				if (currentState != null)
					LogManager
							.logInfo("According to conjecture, we are now in state "
									+ currentState);
				expectedTraces.add(currentState.getExpectedTraces());
				LogManager.logInfo("adding expected traces "
						+ currentState.getExpectedTraces());
			} else
				currentState = null;
		}

		return output;
	}

	public OutputSequence apply(InputSequence inputs) {
		OutputSequence outputs = new OutputSequence();
		for (String input : inputs.sequence)
			outputs.addOutput(apply(input));
		return outputs;
	}

	public String getK() {
		StringBuilder s = new StringBuilder("{");
		for (FullyQualifiedState q : Q.values()) {
			for (PartiallyKnownTrace k : q.getK())
				s.append(k);
		}
		s.append("}");
		return s.toString();
	}

	public String getV() {
		StringBuilder VString = new StringBuilder();
		for (FullyQualifiedState q : Q.values()) {
			for (FullyKnownTrace v : q.getVerifiedTrace()) {
				VString.append("(" + v.getStart() + ", " + v.getTrace() + ", "
						+ v.getEnd() + "), ");
			}
		}
		return "{" + VString + "}";
	}

	public boolean addPartiallyKnownTrace(FullyQualifiedState start,
			LmTrace transition, LmTrace print) {
		assert transition.size() > 0;
		assert (W.contains(print.getInputsProjection()) || (W.size() == 0 && print
				.size() == 0));
		return start.addPartiallyKnownTrace(transition, print);
	}

	protected void addFullyKnownTrace(FullyKnownTrace v) {
		v.getStart().addFullyKnownTrace(v);
	}

	public FullyQualifiedState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(FullyQualifiedState currentState) {
		assert (this.currentState == null || currentState == this.currentState);
		this.currentState = currentState;
	}

	public int traceSize() {
		return trace.size();
	}

	public LmTrace getSubtrace(int start, int end) {
		return trace.subtrace(start, end);
	}

	/**
	 * get an existing or create a new FullyQualifiedState
	 * 
	 * @param wResponses
	 * @return
	 */
	public FullyQualifiedState getFullyQualifiedState(
			List<OutputSequence> WResponses) {
		if (Q.containsKey(WResponses))
			return Q.get(WResponses);
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I,
				conjecture.addState());
		notFullyKnownStates.add(newState);
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < W.size(); i++) {
			s.append(new LmTrace(W.get(i), WResponses.get(i)) + ", ");
		}
		LogManager.logInfo("New state discovered : "
				+ newState.toStringWithMatching() + " (" + s + ")");
		Q.put(WResponses, newState);
		return newState;
	}

	public List<InputSequence> getW() {
		return W;
	}

	/**
	 * get elements which are not in K (ie we don't know the result of
	 * tr_s(t↓I.w)
	 * 
	 * @param s
	 *            a state
	 * @param t
	 *            a transition(of length 1 or more) typically an input symbol or
	 *            an element of W
	 * @return a set Z s.t. \forall w \in Z, (s, t, w) \notin W
	 */
	public List<InputSequence> getwNotInK(FullyQualifiedState s, LmTrace t) {
		assert s != null;
		assert t != null;
		return s.getwNotInK(t);
	}

	/**
	 * get transitions for which we do not know the output (ie we don't know the
	 * result of tr_s(x)
	 * 
	 * @param s
	 * @return a set X s.t. \forall x \in X (s,x) \notin R
	 */
	public Set<String> getxNotInR(FullyQualifiedState s) {
		return s.getUnknowTransitions();
	}

	/**
	 * check if the automata is fully known.
	 * 
	 * @return true if all states are fully known.
	 */
	public Boolean isFullyKnown() {
		return notFullyKnownStates.isEmpty();
	}

	protected void setKnownState(FullyQualifiedState s) {
		notFullyKnownStates.remove(s);
	}

	/**
	 * find a shortest path alpha to a FullyQualifiedState with unknown outputs.
	 * 
	 * @param s
	 *            the start state
	 * @return an empty list if the state himself has unknown outputs
	 */
	public InputSequence getShortestAlpha(FullyQualifiedState s) {
		assert s != null;
		class Node {
			public InputSequence path;
			public FullyQualifiedState end;

			public boolean equals(Object o) {
				if (o instanceof Node)
					return equals((Node) o);
				return false;
			}

			public boolean equals(Node o) {
				return path.equals(o.path) && end.equals(o.end);
			}

			public String toString() {
				return path.toString() + "→" + end.toString();
			}
		}
		class PathComparator implements Comparator<Node> {
			@Override
			public int compare(Node o1, Node o2) {
				int diff = o1.path.getLength() - o2.path.getLength();
				return diff;
			}
		}
		PriorityQueue<Node> paths = new PriorityQueue<Node>(10,
				new PathComparator());
		Node firstNode = new Node();
		firstNode.end = s;
		firstNode.path = new InputSequence();
		paths.add(firstNode);
		List<FullyQualifiedState> reachedStates = new ArrayList<FullyQualifiedState>();
		while (!paths.isEmpty()) {
			firstNode = paths.poll();
			if (reachedStates.contains(firstNode.end))
				continue;
			reachedStates.add(firstNode.end);
			if (!firstNode.end.getUnknowTransitions().isEmpty()) {
				LogManager.logInfo("chosen alpha is " + firstNode.path
						+ " that lead in " + firstNode.end);
				return firstNode.path;
			}
			for (FullyKnownTrace t : firstNode.end.getVerifiedTrace()) {
				Node childNode = new Node();
				childNode.end = t.getEnd();
				childNode.path = new InputSequence();
				childNode.path.addInputSequence(firstNode.path);
				childNode.path.addInputSequence(t.getTrace()
						.getInputsProjection());
				paths.add(childNode);
			}
		}
		throw new ConjectureNotConnexException(reachedStates,
				notFullyKnownStates);
	}

	public Collection<FullyQualifiedState> getStates() {
		return Q.values();
	}

	public LmConjecture getConjecture() {
		return conjecture;
	}

	public LmTrace getTrace() {
		return trace;
	}

	/**
	 * 
	 * @param WResponses
	 * @return states in driver matching this WResponses (or null if driver is
	 *         not available)
	 */
	public List<State> getDriverStates(List<OutputSequence> WResponses) {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
			Mealy automata = tDriver.getAutomata();
			List<State> states = new ArrayList<>();
			for (State s : automata.getStates()) {
				boolean stateMatch = true;
				for (int i = 0; i < W.size(); i++) {
					if (!automata.apply(W.get(i), s).equals(WResponses.get(i))) {
						stateMatch = false;
						break;
					}

				}
				if (stateMatch)
					states.add(s);
			}
			return states;
		}
		return null;
	}

	public void exportConjecture() {
		StringBuilder s = new StringBuilder();
		int node_n = 0;
		for (Map.Entry<OutputSequence, FullyQualifiedState> e : hResponse2State
				.entrySet()) {
			s.append("\t unknown_" + node_n
					+ " [shape=none,label=\"homing\",fontcolor=blue];\n");
			s.append("\t unknown_" + node_n + " -> " + e.getValue()
					+ " [style=dashed,color=blue,fontcolor=blue,label=\""
					+ new LmTrace(h, e.getKey()) + "\"]" + "\n");
			node_n++;
		}
		for (FullyQualifiedState q : Q.values()) {
			q.getExpectedTraces().exportToDot(s, q.toString());
		}
		conjecture.exportToDot(s.toString());
	}

	/**
	 * Check for inconsistencies between h and conjecture. The inconsistency
	 * detected here is a difference between a response from a state and a
	 * response recorded in mapping. This function also check partial mapping.
	 * 
	 * Characterization of states might be not consistent with built
	 * transitions, in this case, thrown exception can have an empty distinction
	 * sequence.
	 * 
	 * @param s
	 *            the starting state from which homing sequence will be applied
	 * @return true if the state match a recorded mapping, or null if the
	 *         mapping is not recorded
	 * @throws InconsistancyHMappingAndConjectureException
	 *             if an inconsistency is discovered, either from complete or
	 *             partial mapping.
	 */
	public Boolean isCompatibleWithHMapping(State s)
			throws InconsistancyHMappingAndConjectureException {
		OutputSequence output = conjecture.apply(h, s);
		State targetState = conjecture.applyGetState(h, s);
		List<OutputSequence> partialMapping = hResponse2Wresponses.get(output);
		if (partialMapping != null) {
			for (int i = 0; i < partialMapping.size(); i++) {
				assert partialMapping.get(i) != null;
				OutputSequence targetResponse = conjecture.apply(W.get(i),
						targetState);
				OutputSequence traceResponse = partialMapping.get(i);
				if (!targetResponse.equals(traceResponse)) {
					FullyQualifiedState mappedState = hResponse2State
							.get(output);

					throw new InconsistancyHMappingAndConjectureException(s,
							targetState, output, mappedState, W.get(i),
							targetResponse, traceResponse);
				}
			}
			if (partialMapping.size() == W.size()) {
				FullyQualifiedState mappedState = hResponse2State.get(output);
				assert mappedState != null;
				if (mappedState.getState() == targetState)
					return true;
				else
					throw new InconsistancyHMappingAndConjectureException(s,
							targetState, output, mappedState, null, null, null);
			}
		} else {
			assert hResponse2State.get(output) == null;
		}
		return null;
	}
}
