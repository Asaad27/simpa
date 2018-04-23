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
import java.util.Map.Entry;
import java.util.Set;

import automata.Automata;
import automata.State;
import automata.Transition;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import tools.DotParser;
import tools.GraphViz;
import tools.Utils;
import tools.loggers.LogManager;

public class Mealy extends Automata implements Serializable {
	private static final long serialVersionUID = 3590635279837551088L;

	protected Map<Integer, MealyTransition> transitions;

	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";

	/**
	 * create a mutant version of this automaton. The mutation will occurs on
	 * one specific transition, and have one chance over two to change the
	 * output an one chance over two to change the destination.
	 * 
	 * @return a new automaton with a mutated transition.
	 */
	public Mealy mutate() {
		boolean mutateOutput = Utils.randInt(2) > 0;
		return mutate(mutateOutput);
	}

	/**
	 * @see {@link #mutate()
	 * @param mutat
	 *            control if the mutation should be on output or on destination
	 *            of transition.
	 * @return a muted automaton
	 */
	public Mealy mutate(boolean mutateOutput) {
		Mealy r = new Mealy("mutant_" + getName());
		Map<State, State> states = new HashMap<>();
		for (State s : getStates()) {
			states.put(s, r.addState(s.isInitial()));
		}
		State mutantS = Utils.randIn(getStates());
		List<String> inputs = new ArrayList<>();
		for (Transition t : getTransitionFrom(mutantS)) {
			inputs.add(t.getInput());
		}
		String mutantInput = Utils.randIn(inputs);
		MealyTransition mutantT = null;
		Set<String> outputs = new HashSet<String>();
		for (MealyTransition t : getTransitions()) {
			if (t.getFrom() == mutantS && t.getInput() == mutantInput) {
				mutantT = t;
				continue;
			}
			outputs.add(t.getOutput());
			r.addTransition(new MealyTransition(r, states.get(t.getFrom()),
					states.get(t.getTo()), t.getInput(), t.getOutput()));
		}
		String mutantOutput = mutantT.getOutput();
		State mutantTo = mutantT.getTo();
		if (mutateOutput) {
			outputs.remove(mutantT.getOutput());
			mutantOutput = Utils.randIn(outputs);
		} else {
			List<State> possibleStates = new ArrayList<>();
			possibleStates.addAll(getStates());
			possibleStates.remove(mutantT.getTo());
			mutantTo = Utils.randIn(possibleStates);
		}
		r.addTransition(new MealyTransition(r, states.get(mutantS),
				states.get(mutantTo), mutantInput, mutantOutput));
		return r;
	}

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
			boolean groupTransitions = false; // TODO make an option for this
			if (groupTransitions) {
				Map<State, Map<State, List<MealyTransition>>> grouped = new HashMap<>();
				for (MealyTransition t : getTransitions()) {
					Map<State, List<MealyTransition>> from = grouped
							.get(t.getFrom());
					if (from == null) {
						from = new HashMap<>();
						grouped.put(t.getFrom(), from);
					}
					List<MealyTransition> to = from.get(t.getTo());
					if (to == null) {
						to = new ArrayList<>();
						from.put(t.getTo(), to);
					}
					to.add(t);
				}
				for (Map.Entry<State, Map<State, List<MealyTransition>>> from : grouped
						.entrySet()) {
					for (Entry<State, List<MealyTransition>> to : from
							.getValue().entrySet()) {
						StringBuilder txt = new StringBuilder();
						for (MealyTransition t : to.getValue()) {
							txt.append(
									t.getInput() + "/" + t.getOutput() + "\\n");
						}
						txt.delete(txt.length() - 2, txt.length());
						writer.write(
								"\t" + from.getKey() + " -> " + to.getKey()
										+ " [label=" + tools.GraphViz
												.id2DotAuto(txt.toString())
										+ "];\n");
					}
				}
			} else {
				for (MealyTransition t : getTransitions()) {
					writer.write("\t" + t.toDot() + "\n");
				}
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
		return apply((GenericInputSequence) I, s).toFixedOutput();
	}

	public GenericOutputSequence apply(GenericInputSequence seq, State s) {
		GenericInputSequence.Iterator it = seq.inputIterator();
		while (it.hasNext()) {
			String input = it.next();
			MealyTransition t = getTransitionFromWithInput(s, input);
			assert t != null;
			s = t.getTo();
			it.setPreviousOutput(t.getOutput());
		}
		return it.getResponse();
	}

	/**
	 * apply an InputSequence on an incomplete automaton
	 * 
	 * @param inSeq
	 *            the sequence to apply
	 * @param s
	 *            the state from which the sequence must be applied
	 * @param outSeq
	 *            a sequence to fill with outputs. can be null.
	 * @return true if the full sequence was applied, false otherwise.
	 */
	public boolean applyIfTransitionExists(InputSequence inSeq, State s,
			OutputSequence outSeq) {
		for (String input : inSeq.sequence) {
			MealyTransition t = getTransitionFromWithInput(s, input);
			if (t == null)
				return false;
			s = t.getTo();
			if (outSeq != null)
				outSeq.addOutput(t.getOutput());
		}
		return true;
	}

	public State applyGetState(GenericInputSequence seq, State s) {
		GenericInputSequence.Iterator it = seq.inputIterator();
		while (it.hasNext()) {
			String input = it.next();
			MealyTransition t = getTransitionFromWithInput(s, input);
			assert t != null;
			s = t.getTo();
			it.setPreviousOutput(t.getOutput());
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
	 * The automata is supposed to be complete.
	 * 
	 * @param fullTrace
	 *            the trace to test
	 * @return position of input in trace which is incompatible with all states.
	 *         if the trace is compatible with at least one state, length of
	 *         trace is returned.
	 */
	public int checkOnAllStates(LmTrace fullTrace) {
		assert !(this instanceof LmConjecture)
				|| ((LmConjecture) this).isFullyKnown();
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
	 * @see {@link #checkOnOneState(LmTrace, State)}
	 */
	public int checkOnOneState(LmTrace fullTrace) {
		State initialState = getInitialState();
		if (initialState == null)
			throw new RuntimeException(
					"cannot process, initial state of automata is not defined");
		return checkOnOneState(fullTrace, initialState);
	}

	/**
	 * try to apply a trace from a state of this automaton and check if the
	 * output sequence matches the trace.
	 * 
	 * @param fullTrace
	 *            the trace to test
	 * @param startingState
	 *            optional, indicate the starting state to apply sequence. If
	 *            omitted, initial state is used.
	 * @return position of input in trace which is incompatible with the initial
	 *         state. if the trace is compatible with execution on this
	 *         automaton, length of trace is returned.
	 * 
	 * @see {@link Mealy#checkOnAllStates(LmTrace)}
	 */
	public int checkOnOneState(LmTrace fullTrace, State startingState) {
		State currentState = startingState;
		int i = 0;
		while (i < fullTrace.size()) {
			MealyTransition t = getTransitionFromWithInput(currentState,
					fullTrace.getInput(i));
			if (!t.getOutput().equals(fullTrace.getOutput(i)))
				return i;
			currentState = t.getTo();
			i++;
		}
		return i;
	}

	/**
	 * indicate if the sequence provided is a homing sequence for this automata
	 * 
	 * @param h
	 *            the sequence to test
	 * @return true if it is a homing sequence
	 */
	public boolean acceptHomingSequence(GenericInputSequence h) {
		Map<GenericOutputSequence, State> responses = new HashMap<>();
		for (State s : getStates()) {
			GenericOutputSequence response = apply(h, s);
			State end = applyGetState(h, s);
			if (responses.containsKey(response)
					&& responses.get(response) != end)
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
	public boolean acceptCharacterizationSet(
			List<? extends GenericInputSequence> W) {
		Set<List<GenericOutputSequence>> responses = new HashSet<>();
		for (State s : getStates()) {
			List<GenericOutputSequence> WResponses = new ArrayList<>();
			for (GenericInputSequence inputSeq : W) {
				GenericOutputSequence response = apply(inputSeq, s);
				WResponses.add(response);
			}
			if (responses.contains(WResponses))
				return false;
			responses.add(WResponses);
		}
		return true;
	}
}
