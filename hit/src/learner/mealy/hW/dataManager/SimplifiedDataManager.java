package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import automata.mealy.Mealy;
import automata.State;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public class SimplifiedDataManager {
	public static SimplifiedDataManager instance;// TODO either make a proper
													// singleton either do
													// something else
	private MealyDriver driver;
	private LmTrace trace;
	private LmTrace globalTrace;
	private final DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W; // Characterization
																										// set
	public final GenericInputSequence h;
	private final ArrayList<String> I;// Input Symbols
	private Map<Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence>, FullyQualifiedState> Q;// known
																															// states
	private Set<FullyQualifiedState> notFullyKnownStates;// Fully qualified
															// states with
															// undefined
															// transitions
	private LmConjecture conjecture;
	private FullyQualifiedState currentState;

	private Map<GenericOutputSequence, FullyQualifiedState> hResponse2State;
	private Map<GenericOutputSequence, Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence>> hResponse2Wresponses;
	private GenericHomingSequenceChecker hChecker;

	private Collection<TraceTree> expectedTraces;
	
	private Map<GenericOutputSequence, List<HZXWSequence>> hZXWSequences;
	private List<LocalizedHZXWSequence> readyForReapplyHZXWSequence = new ArrayList<>();

	protected Collection<FullyQualifiedState> identifiedFakeStates=new ArrayList<>();

	public Collection<FullyQualifiedState> getIdentifiedFakeStates() {
		return identifiedFakeStates;
	}
	public int getHResponsesNb(){
		return hResponse2State.keySet().size();
	}

	public FullyQualifiedState getState(GenericOutputSequence hResponse) {
		assert hResponse.checkCompatibilityWith(h);
		FullyQualifiedState s = hResponse2State.get(hResponse);
		if (s == null && W.isEmpty()) {
			s = getFullyQualifiedState(W.getEmptyCharacterization());
		}
		return s;
	}

	private Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> getOrCreateWResponseAfterHresponse(
			GenericOutputSequence hR) {
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wR = hResponse2Wresponses
				.get(hR);
		if (wR != null)
			return wR;
		wR = W.getEmptyCharacterization();
		hResponse2Wresponses.put(hR, wR);
		return wR;

	}

	public GenericInputSequence getMissingInputSequence(
			GenericOutputSequence hResponse) {
		assert (getState(hResponse) == null);
		Iterator<? extends GenericInputSequence> it = getOrCreateWResponseAfterHresponse(
				hResponse).unknownPrints().iterator();
		return it.next();
	}

	public FullyQualifiedState addWresponseAfterH(
			GenericOutputSequence hResponse, GenericInputSequence w,
			GenericOutputSequence wResponse) {
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wRs = getOrCreateWResponseAfterHresponse(
				hResponse);
		assert !wRs.isComplete() : "all responses already known";
		assert (wResponse.checkCompatibilityWith(w));
		assert wRs.getUnknownPrints().contains(w);
		wRs.addPrint(w, wResponse);
		if (wRs.isComplete()) {
			FullyQualifiedState q = getFullyQualifiedState(wRs);
			hResponse2State.put(hResponse, q);
			LogManager.logInfo("We found that the response " + hResponse
					+ " to homing sequence lead in state " + q);
			List<HZXWSequence> sequences = hZXWSequences.get(hResponse);
			if (sequences != null) {
				for (HZXWSequence seq : sequences) {
					LocalizedHZXWSequence localizedSeq = new LocalizedHZXWSequence(
							seq);
					if (q.addLocalizedHZXWSequence(localizedSeq)) {
						readyForReapplyHZXWSequence.add(localizedSeq);
					}
				}
			}
			return q;
		}
		return null;
	}

	public SimplifiedDataManager(MealyDriver driver,
			DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W,
			GenericInputSequence h, LmTrace globalTrace,
			Map<GenericOutputSequence, List<HZXWSequence>> hZXWSequences,
			GenericHomingSequenceChecker hChecker) {
		this.trace = new LmTrace();
		this.globalTrace = globalTrace;
		this.W = W;
		this.h = h;
		this.I = new ArrayList<String>(driver.getInputSymbols());
		this.driver = driver;
		this.hZXWSequences = hZXWSequences;
		Q = new HashMap<>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new LmConjecture(driver);
		instance = this;
		hResponse2State = new HashMap<>();
		hResponse2Wresponses = new HashMap<>();
		currentState = null;
		expectedTraces = new ArrayList<>();
		assert hChecker.getH().equals(h);
		this.hChecker = hChecker;
	}
	
	private void extendTrace(String input, String output){
		trace.append(input, output);
		globalTrace.append(input, output);
	}
	
	public String walkWithoutCheck(String input, String output,
			List<GenericHNDException> hExceptions) {
		extendTrace(input, output);
		String expectedOutput = null;
		// check for Non-Determinism after homing sequence
		try {
			hChecker.apply(input, output);
		} catch (GenericHNDException e) {
			if (hExceptions != null)
				hExceptions.add(e);
		}
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

	public OutputSequence walkWithoutCheck(LmTrace seq,
			List<GenericHNDException> hExceptions) {
		OutputSequence outputs = new OutputSequence();
		for (int i = 0; i < seq.size(); i++)
			outputs.addOutput(walkWithoutCheck(seq.getInput(i),
					seq.getOutput(i), hExceptions));
		return outputs;
	}
	

	public String apply(String input) {
		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			LogManager.logInfo("expected Traces are ", expectedTraces);
		String output = driver.execute(input);
		extendTrace(input, output);
		// check for Non-Determinism after homing sequence
		hChecker.apply(input, output);

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
				throw new InconsistancyWithConjectureAtEndOfTraceException(
						input, expectedOutput, traceTree, output);
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
				if (Options.LOG_LEVEL == LogLevel.ALL)
					LogManager.logInfo("adding expected traces ",
							currentState.getExpectedTraces());
			} else
				currentState = null;
		}

		return output;
	}

	public OutputSequence apply(InputSequence inputs) {
		return apply((GenericInputSequence) inputs).toFixedOutput();
	}

	public GenericOutputSequence apply(GenericInputSequence inputs) {
		GenericInputSequence.Iterator it = inputs.inputIterator();
		while (it.hasNext()) {
			String input = it.next();
			it.setPreviousOutput(apply(input));
		}
		return it.getResponse();
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

	public void addPartiallyKnownTrace(FullyQualifiedState start,
			LmTrace transition, final LmTrace print) {
		assert transition.size() > 0;
		start.addPartiallyKnownTrace(transition, print);
		readyForReapplyHZXWSequence.addAll(start.pollSequencesNeededInW());
	}

	protected void addFullyKnownTrace(FullyKnownTrace v) {
		v.getStart().addFullyKnownTrace(v);
		assert v.getTrace().size() == 1;
		String input = v.getTrace().getInput(0);
		for (LocalizedHZXWSequence localizedSeq : v.getStart()
				.getPendingSequences(input)) {
			localizedSeq.transferPosition++;
			if (v.getEnd().addLocalizedHZXWSequence(localizedSeq)) {
				readyForReapplyHZXWSequence.add(localizedSeq);
			}
		}
	}

	public FullyQualifiedState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(FullyQualifiedState currentState) {
		assert (this.currentState == null || currentState == this.currentState);
		this.currentState = currentState;
		expectedTraces.add(currentState.getExpectedTraces());
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
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses) {
		if (Q.containsKey(WResponses))
			return Q.get(WResponses);
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I,
				conjecture.addState());
		notFullyKnownStates.add(newState);
		StringBuilder s = new StringBuilder();
		for (LmTrace t : WResponses.knownResponses()) {
			s.append(t + ", ");
		}
		LogManager.logInfo("New state discovered : "
				+ newState.toStringWithMatching() + " (" + s + ")");
		Q.put(WResponses, newState);
		return newState;
	}

	public DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> getW() {
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
	public List<? extends GenericInputSequence> getwNotInK(
			FullyQualifiedState s, LmTrace t) {
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
	 * TODO This is a function often used in algorithm. It cost time and memory
	 * (because of multiple creation/deletion).We should change it into a
	 * version which keep data between calls. For example, keep in each state
	 * the length of shortest path and the input used to reach it. Because this
	 * length can only grows, it is easy to refine when it become wrong.
	 * 
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
	public List<State> getDriverStates(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses) {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
			Mealy automata = tDriver.getAutomata();
			List<State> states = new ArrayList<>();
			for (State s : automata.getStates()) {
				boolean stateMatch = true;
				for (LmTrace wTrace : WResponses.knownResponses()) {
					if (!automata.apply(wTrace.getInputsProjection(), s)
							.equals(wTrace.getOutputsProjection())) {
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
		for (Map.Entry<GenericOutputSequence, FullyQualifiedState> e : hResponse2State
				.entrySet()) {
			s.append("\t unknown_" + node_n
					+ " [shape=none,label=\"homing\",fontcolor=blue];\n");
			s.append("\t unknown_" + node_n + " -> " + e.getValue()
					+ " [style=dashed,color=blue,fontcolor=blue,label=\""
					+ h.buildTrace(e.getKey()) + "\"]" + "\n");
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
		GenericOutputSequence output = conjecture.apply(h, s);
		State targetState = conjecture.applyGetState(h, s);
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> partialMapping = hResponse2Wresponses
				.get(output);
		if (partialMapping != null) {
			for (LmTrace wTrace : partialMapping.knownResponses()) {
				GenericOutputSequence targetResponse = conjecture
						.apply(wTrace.getInputsProjection(), targetState);
				GenericOutputSequence traceResponse = wTrace
						.getOutputsProjection();
				if (!targetResponse.equals(traceResponse)) {
					FullyQualifiedState mappedState = hResponse2State
							.get(output);

					throw new InconsistancyHMappingAndConjectureException(s,
							targetState, output, mappedState,
							wTrace.getInputsProjection(),							targetResponse, traceResponse);
				}
			}
			if (partialMapping.isComplete()) {
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

	public List<LocalizedHZXWSequence> getAndResetReadyForReapplyHZXWSequence() {
		List<LocalizedHZXWSequence> r = readyForReapplyHZXWSequence;
		readyForReapplyHZXWSequence = new ArrayList<>();
		return r;
	}
}
