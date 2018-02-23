package automata.mealy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.Automata;
import automata.State;
import learner.mealy.LmTrace;
import main.simpa.Options;
import tools.DotParser;
import tools.GraphViz;
import tools.loggers.LogManager;

public class Mealy extends Automata implements Serializable {
	private static final long serialVersionUID = 3590635279837551088L;

	protected Map<Integer, MealyTransition> transitions;

	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";

	public Mealy(String name) {
		super(name);
		transitions = new HashMap<Integer, MealyTransition>();
	}

	public Boolean addTransition(MealyTransition t) {
		if (!transitions.containsKey(t.hashCode())) {
			super.transitions.add(t);
			transitions.put(t.hashCode(), t);
			return true;
		}
		assert transitions.get(t.hashCode()).equals(t) : "stored " + transitions.get(t.hashCode()) + " and new " + t
				+ " have same hash " + t.hashCode();
		return false;
	}
	public void removeTransition(MealyTransition t){
		MealyTransition existant=transitions.get(t.hashCode());
		assert t.equals(existant);
		transitions.remove(t.hashCode());
		super.transitions.remove(existant);
	}

	public Collection<MealyTransition> getTransitions() {
		return transitions.values();
	}

	public int getTransitionCount() {
		return transitions.size();
	}

	public List<MealyTransition> getTransitionFrom(State s) {
		return getTransitionFrom(s, true);
	}

	public List<MealyTransition> getTransitionFrom(State s, boolean loop) {
		List<MealyTransition> res = new ArrayList<MealyTransition>();
		for (MealyTransition t : transitions.values()) {
			if (t.getFrom().equals(s)) {
				if (t.getTo().equals(s)) {
					if (loop)
						res.add(t);
				} else
					res.add(t);
			}
		}
		return res;
	}

	public MealyTransition getTransitionFromWithInput(State s, String input) {
		assert states.contains(s);
		return transitions.get((s + input).hashCode());
	}

	public MealyTransition getTransitionFromWithInput(State s, String input, boolean loop) {
		MealyTransition t = getTransitionFromWithInput(s, input);
		if ((t.isLoop() && loop) || !t.isLoop())
			return t;
		return null;
	}

	/**
	 * Find a shortest path between two states of the automaton.
	 * 
	 * This function find one of the shortest path and the others (if they
	 * exist) are ignored.
	 * 
	 * @param initial
	 *            the starting state for the path.
	 * @param target
	 *            the ending state for the path.
	 * @return the trace from the initial State to the target state or null if
	 *         no path were found (e.g. if the automaton is not complete/not
	 *         strongly connected).
	 */
	public LmTrace getShortestPath(State initial, State target) {
		Set<State> addedStates = new HashSet<>();
		class Path {
			public State lastState;
			public MealyTransition lastTransition = null;
			public Path previousPath = null;
		}
		LinkedList<Path> toCompute = new LinkedList<>();
		Path initialPath = new Path();
		initialPath.lastState = initial;
		toCompute.add(initialPath);
		addedStates.add(initial);
		while (!toCompute.isEmpty()) {
			Path currentPath = toCompute.pollFirst();
			if (currentPath.lastState == target) {
				// we found the path, now we have to construct the trace
				LinkedList<Path> stack = new LinkedList<>();
				while (currentPath.lastTransition != null) {
					stack.add(currentPath);
					currentPath = currentPath.previousPath;
					assert currentPath != null;
				}
				LmTrace trace = new LmTrace();
				while (!stack.isEmpty()) {
					currentPath = stack.pollLast();
					trace.append(currentPath.lastTransition.getInput(),
							currentPath.lastTransition.getOutput());
				}
				return trace;
			}

			for (MealyTransition t : getTransitionFrom(currentPath.lastState)) {
				if (addedStates.contains(t.getTo()))
					continue;
				Path newPath = new Path();
				newPath.lastState = t.getTo();
				newPath.lastTransition = t;
				newPath.previousPath = currentPath;
				toCompute.add(newPath);
				addedStates.add(t.getTo());
			}
		}
		return null;
	}

	public void exportToDot() {
		exportToDot("");
	}
	public void exportToDot(String comments) {
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName() + " directory");

			file = new File(dir.getPath() + File.separatorChar + name + "_inf.dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			for (MealyTransition t : getTransitions()) {
				writer.write("\t" + t.toDot() + "\n");
			}
			for (State s : states) {
				if (s.isInitial()) {
					writer.write("\t" + s.getName() + " [shape=doubleoctagon]\n");
				}
			}
			writer.write(comments);
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture has been exported to " + file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	public OutputSequence apply(InputSequence I, State s) {
		OutputSequence O = new OutputSequence();
		for (String i : I.sequence) {
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
			O.addOutput(t.getOutput());
		}
		return O;
	}

	public State applyGetState(InputSequence I, State s) {
		for (String i : I.sequence) {
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
		}
		return s;
	}

	/**
	 * compute a distinction sequence for the two states it may be a new
	 * distinction sequence or an append of an existing distinction sequence
	 * 
	 * @param automata
	 * @param inputSymbols
	 * @param s1
	 * @param s2
	 * @param W
	 */
	public void addDistinctionSequence(List<String> inputSymbols, State s1, State s2, List<InputSequence> W) {
		// first we try to add an input symbol to the existing W
		for (InputSequence w : W) {
			for (String i : inputSymbols) {
				InputSequence testw = new InputSequence();
				testw.addInputSequence(w);
				testw.addInput(i);
				if (!apply(testw, s1).equals(apply(testw, s2))) {
					w.addInput(i);
					return;
				}
			}
		}
		// then we try to compute a w from scratch
		LinkedList<InputSequence> testW = new LinkedList<InputSequence>();
		for (String i : inputSymbols)
			testW.add(new InputSequence(i));
		while (true) {
			InputSequence testw = testW.pollFirst();
			if (apply(testw, s1).equals(apply(testw, s2))) {
				if (testw.getLength() > getStateCount()) {
					// TODO find a better way to log and save the automata
					String dir = Options.OUTDIR;
					Options.OUTDIR = "/tmp/";
					LogManager.logInfo("unable to compute distinguish sequence for " + s1 + " and " + s2);
					exportToDot();
					Options.OUTDIR = dir;
					throw new RuntimeException("it looks like if we will not find a w to distinguish " + s1 + " and "
							+ s2 + ".Those state may are equivalents, please look in /tmp");
				}
				for (String i : inputSymbols) {
					InputSequence newTestw = new InputSequence();
					newTestw.addInputSequence(testw);
					newTestw.addInput(i);
					testW.add(newTestw);
				}
			} else {
				for (int i = 0; i < W.size(); i++) {
					InputSequence w = W.get(i);
					if (testw.startsWith(w)) {
						W.remove(w);
					}
				}
				W.add(testw);
				return;
			}

		}
	}

	public static Mealy importFromDot(File file) throws IOException {
		if (!file.exists())
			throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
		if (!file.getName().endsWith(".dot"))
			LogManager.logError("Are you sure that '" + file + "' is a dot file ?");

		DotParser dotParser = new DotParser();
		return dotParser.getAutomaton(file);
	}

	/**
	 * find an input sequence which distinguish two states (i.e. applying this
	 * sequence from the two states do not provide the same output).
	 * 
	 * @param s1
	 *            one state to distinguish
	 * @param s2
	 *            one state to distinguish
	 * @return an {@link InputSequence} for which provide two different outputs
	 *         when applied from s1 and s2. If s1 and s2 are equivalents (i.e.
	 *         cannot be distinguished by any sequence) then returns null.
	 */
	public InputSequence getDistinctionSequence(State s1, State s2) {
		assert (s1 != s2);
		class StatePair {
			private final State s1;
			private final State s2;

			StatePair(State s1, State s2) {
				this.s1 = s1;
				this.s2 = s2;
			}

			public boolean equals(Object other) {
				if (other instanceof StatePair)
					return equals((StatePair) other);
				return false;
			}

			public boolean equals(StatePair other) {
				return (s1 == other.s1 && s2 == other.s2)
						|| (s1 == other.s2 && s2 == other.s1);
			}

			public int hashCode() {
				return s1.hashCode() * s2.hashCode() + s1.hashCode()
						+ s2.hashCode();
			}

			public String toString() {
				if (s1.getId() < s2.getId())
					return "(" + s1 + "," + s2 + ")";
				else
					return "(" + s2 + "," + s1 + ")";

			}
		}
		Set<StatePair> seenPair = new HashSet<>();

		class SameSequence {
			public final InputSequence seq;
			public final State s1;
			public final State s2;

			public SameSequence(InputSequence seq, State s1, State s2) {
				this.seq = seq;
				this.s1 = s1;
				this.s2 = s2;
			}

			public StatePair getPair() {
				return new StatePair(s1, s2);
			}
		}
		LinkedList<SameSequence> toCompute = new LinkedList<>();
		toCompute.add(new SameSequence(new InputSequence(), s1, s2));
		seenPair.add(new StatePair(s1, s2));

		while (!toCompute.isEmpty()) {
			SameSequence current = toCompute.poll();
			if (current.seq.getLength() > getStateCount())
				continue;

			// transitions from s1 and s2 should have the same input set. We
			// assert this by checking same size and then checking if each input
			// in transitions(s1) is also in transitions(s2)
			assert (getTransitionFrom(current.s1).size() == getTransitionFrom(
					current.s2).size());
			for (MealyTransition t1 : getTransitionFrom(current.s1)) {
				MealyTransition t2 = getTransitionFromWithInput(current.s2,
						t1.getInput());
				assert (t2 != null);
				InputSequence newSeq = current.seq.clone();
				newSeq.addInput(t1.getInput());

				if (!t1.getOutput().equals(t2.getOutput()))
					return newSeq;

				SameSequence newSameSeq = new SameSequence(newSeq, t1.getTo(),
						t2.getTo());
				if (!seenPair.contains(newSameSeq.getPair()))
					toCompute.add(newSameSeq);
				seenPair.add(newSameSeq.getPair());
			}
		}
		return null;

	}
	
	public OutputSequence simulateOutput(State start,InputSequence inSeq){
		OutputSequence outSeq=new OutputSequence();
		for (String i:inSeq.sequence){
			MealyTransition t= getTransitionFromWithInput(start, i);
			start=t.getTo();
			outSeq.addOutput(t.getOutput());
		}
		return outSeq;
	}
	public State simulateState(State start,InputSequence inSeq){
		for (String i:inSeq.sequence){
			MealyTransition t= getTransitionFromWithInput(start, i);
			start=t.getTo();
		}
		return start;
	}
	
	/**
	 * try to apply a trace from any state of this automaton.
	 * 
	 * @param fullTrace the trace to test
	 * @return position of input in trace which is incompatible with all states. if the trace is compatible with at least one state, leght of trace is returned.
	 */
	public int simulateOnAllStates(LmTrace fullTrace){
		Set<State> compatibleStates=new HashSet<>(getStates());
		int i=0;
		while (i<fullTrace.size()){
			Set<State> newcompatibles=new HashSet<>(compatibleStates.size());
			for (State s : compatibleStates){
				MealyTransition t= getTransitionFromWithInput(s, fullTrace.getInput(i));
				if (t.getOutput().equals(fullTrace.getOutput(i)))
					newcompatibles.add(t.getTo());
			}
			if (newcompatibles.isEmpty())
				return i;
			compatibleStates=newcompatibles;
			i++;
		}
		return i;
	}
	
	/**
	 * indicate if the sequence provided is a homing sequence for this automata
	 * @param h the sequence to test
	 * @return true if it is a homing sequence
	 */
	public boolean acceptHomingSequence(InputSequence h){
		Map<OutputSequence,State> responses=new HashMap<>();
		for (State s:getStates()){
			OutputSequence response=apply(h, s);
			State end=applyGetState(h, s);
			if (responses.containsKey(response)&&responses.get(response)!=end)
				return false;
			responses.put(response, end);
		}
		return true;
	}

	/**
	 * indicate if the set of sequence provided is a characterization set for this automata.
	 * @param W the W-set to test
	 * @return true if it is a characterization set for this automata. Note that if the automata is not minimal (i.e. two states are equivalents) this will always return false.
	 */
	public boolean acceptCharacterizationSet(List<InputSequence> W) {
		Set<List<OutputSequence>> responses = new HashSet<>();
		for (State s : getStates()) {
			List<OutputSequence> WResponses = new ArrayList<>();
			for (InputSequence inputSeq : W) {
				OutputSequence response = apply(inputSeq, s);
				WResponses.add(response);
			}
			if (responses.contains(WResponses))
				return false;
			responses.add(WResponses);
		}
		return true;
	}
}
