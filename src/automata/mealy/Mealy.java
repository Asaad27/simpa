/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 *     Roland GROZ
 *     Lingxiao WANG
 ********************************************************************************/
package automata.mealy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import automata.Automata;
import automata.State;
import automata.Transition;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyAdaptiveW.AdaptiveCharacterization;
import automata.mealy.splittingTree.smetsersSplittingTree.SplittingTree;
import learner.mealy.LmConjecture;
import learner.mealy.LmConjecture.CounterExampleResult;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import options.valueHolders.SeedHolder;
import tools.DotParser;
import tools.GraphViz;
import tools.loggers.LogManager;

public class Mealy extends Automata implements Serializable {
	private static final long serialVersionUID = 3590635279837551088L;

	protected Map<State, Map<String, MealyTransition>> transitions;

	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";

	/**
	 * create a mutant version of this automaton. The mutation will occurs on
	 * one specific transition, and have one chance over two to change the
	 * output an one chance over two to change the destination.
	 * 
	 * @return a new automaton with a mutated transition.
	 */
	public Mealy mutate(SeedHolder rand) {
		boolean mutateOutput = rand.randBoolWithPercent(50);
		return mutate(mutateOutput, rand);
	}

	/**
	 * @see {@link #mutate()
	 * @param mutat
	 *            control if the mutation should be on output or on destination
	 *            of transition.
	 * @return a muted automaton
	 */
	public Mealy mutate(boolean mutateOutput, SeedHolder rand) {
		Mealy r = new Mealy("mutant_" + getName());
		Map<State, State> states = new HashMap<>();
		for (State s : getStates()) {
			states.put(s, r.addState(s.isInitial()));
		}
		State mutantS = rand.randIn(getStates());
		List<String> inputs = new ArrayList<>();
		for (Transition t : getTransitionFrom(mutantS)) {
			inputs.add(t.getInput());
		}
		String mutantInput = rand.randIn(inputs);
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
			mutantOutput = rand.randIn(outputs);
		} else {
			List<State> possibleStates = new ArrayList<>();
			possibleStates.addAll(getStates());
			possibleStates.remove(mutantT.getTo());
			mutantTo = rand.randIn(possibleStates);
		}
		r.addTransition(new MealyTransition(r, states.get(mutantS),
				states.get(mutantTo), mutantInput, mutantOutput));
		return r;
	}

	public Mealy(String name) {
		super(name);
		transitions = new HashMap<>();
	}

	/**
	 * try to add a transition in the automata
	 * 
	 * @param t
	 *            the transition to add
	 * @return true if the transition was added, false if there was already a
	 *         transition from the same state with the same input
	 */
	public Boolean addTransition(MealyTransition t) {
		super.transitions.add(t);
		Map<String, MealyTransition> fromState = transitions.get(t.getFrom());
		if (fromState == null) {
			fromState = new HashMap<>();
			transitions.put(t.getFrom(), fromState);
		}
		if (fromState.containsKey(t.getInput()))
			return false;
		fromState.put(t.getInput(), t);
		return true;
	}
	public void removeTransition(MealyTransition t){
		MealyTransition existant = getTransitionFromWithInput(t.getFrom(),
				t.getInput());
		assert t.equals(existant);
		transitions.get(t.getFrom()).remove(t.getInput());
		super.transitions.remove(existant);
	}

	public Collection<MealyTransition> getTransitions() {
		Collection<MealyTransition> res = new ArrayList<MealyTransition>();
		for (Map<String, MealyTransition> map : transitions.values())
			res.addAll(map.values());
		return res;
	}

	public int getTransitionCount() {
		// TODO optimize
		return getTransitions().size();
	}

	public Collection<MealyTransition> getTransitionFrom(State s) {
		Map<String, MealyTransition> map = transitions.get(s);
		if (map != null)
			return map.values();
		return new LinkedList<MealyTransition>();
	}

	public MealyTransition getTransitionFromWithInput(State s, String input) {
		assert states.contains(s);
		Map<String, MealyTransition> map = transitions.get(s);
		if (map != null)
			return map.get(input);
		return null;
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
		File dir = Options.getDotDir();
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
					LogManager.logInfo("unable to compute distinguish sequence for " + s1 + " and " + s2);
					exportToDot();
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
	 * download and parse a dot file. If the file is already cached, then the
	 * local file is reused.
	 * 
	 * @param url
	 *            the URL of the file to download
	 * @return the automata written in the file.
	 * @see {@link #importFromDot(File)}
	 */
	public static Mealy importFromUrl(URL url) throws IOException {
		File file = tools.Utils.downloadWithCache(url);
		assert file.exists();
		Mealy m = importFromDot(file);
		String[] parts = url.toString().split("/");
		m.name = "dot_file(" + parts[parts.length - 2] + "_"
				+ parts[parts.length - 1].replaceAll("\\.dot$", "") + ")";
		return m;
	}

	/**
	 * download and parse a dot file.
	 * 
	 * @param url
	 *            the URL of the file to download
	 * @param fromCache
	 *            when true, firstly check if the file was already downloaded
	 *            and reuse it if possible.
	 * @return the automata written in the file.
	 * @see {@link #importFromDot(File)}
	 */
	public static Mealy importFromUrl(URL url, boolean fromCache)
			throws IOException {
		File file = tools.Utils.downloadFile(url, fromCache);
		assert file.exists();
		return importFromDot(file);
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

			@Override
			public boolean equals(Object other) {
				if (other instanceof StatePair)
					return equals((StatePair) other);
				return false;
			}

			public boolean equals(StatePair other) {
				return (s1 == other.s1 && s2 == other.s2)
						|| (s1 == other.s2 && s2 == other.s1);
			}

			@Override
			public int hashCode() {
				return s1.hashCode() * s2.hashCode() + s1.hashCode()
						+ s2.hashCode();
			}

			@Override
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

	public class compatibilityCheckResult {
		private boolean isCompatible;
		private LmTrace simpleDiffTrace = null;
		private int simpleDiffPos;

		/**
		 * indicate if an inconsistency was found
		 * 
		 * @return true if all trace can be played on this automaton.
		 */
		public boolean isCompatible() {
			return isCompatible;
		}

		/**
		 * if the inconsistency comes from a single trace, this method return
		 * the faulty trace.
		 * 
		 * @return a trace which cannot be played on any state of the automaton.
		 */
		public LmTrace getSimpleDiffTrace() {
			return simpleDiffTrace;
		}

		/**
		 * if the inconsistency comes from a single trace, this method return
		 * the position of first element of {@link #getSimpleDiffTrace()} which
		 * is inconsistent with this automaton.
		 * 
		 * @return a position in the trace.
		 */
		public int getSimpleDiffPos() {
			assert simpleDiffTrace != null;
			return simpleDiffPos;
		}

		compatibilityCheckResult() {
			isCompatible = true;
		}

		void addDiff(LmTrace t, int diff) {
			isCompatible = false;
			simpleDiffTrace = t;
			simpleDiffPos = diff;
		}
	}

	/**
	 * try to find inconsistency between a set of executed traces and this
	 * automaton.
	 * 
	 * @param fullTraces
	 * @return
	 */
	public compatibilityCheckResult checkOnAllStatesWithReset(
			List<LmTrace> fullTraces) {
		compatibilityCheckResult r = new compatibilityCheckResult();
		for (LmTrace t : fullTraces) {
			int diff = checkOnAllStates(t);
			if (diff != t.size()) {
				r.addDiff(t, diff);
			}
		}
		// TODO check compatibility with several traces at the same time :
		// initial state is the same for all trace thus we can remove some
		// candidate initial with one trace and remove other candidate with
		// another trace
		return r;
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
	 * indicate if the set of sequence provided is a characterization set for
	 * this automata.
	 * 
	 * @param W
	 *            the W-set to test
	 * @return true if the given set can distinguish any pair of states of this
	 *         automata. Note that if the automata is not minimal (i.e. two
	 *         states are equivalents) this will always return false.
	 */
	public boolean acceptCharacterizationSet(
			DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W) {
		Set<Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence>> responses = new HashSet<>();
		for (State s : getStates()) {
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization = W
					.getEmptyCharacterization();
			Iterator<? extends GenericInputSequence> it = characterization
					.unknownPrints().iterator();
			while (it.hasNext()) {
				GenericInputSequence inputSeq = it.next();
				GenericOutputSequence response = apply(inputSeq, s);
				characterization.addPrint(inputSeq,response);

			}
			assert characterization
					.isComplete() : "error in iterator implementation";
			if (responses.contains(characterization))
				return false;
			responses.add(characterization);
		}
		return true;
	}

	/**
	 * A utility function to count the number of different characterizations
	 * observed for a given W i.e. how many states can be distinguished.
	 * 
	 * @param W
	 *            a distinction structure, adaptive or preset, to distinguish
	 *            states.
	 * @return the number of equivalence classes for this W, the minimum is 1
	 *         and the maximum is the number of states.
	 */
	public int countCharacterization(
			DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W) {
		Set<Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence>> characterizations = new HashSet<>();
		;
		for (State s : getStates()) {
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization = W
					.getEmptyCharacterization();
			while (!characterization.isComplete()) {
				GenericInputSequence is = characterization.getUnknownPrints()
						.get(0);
				GenericOutputSequence os = apply(is, s);
				characterization.addPrint(is.buildTrace(os));
			}
			characterizations.add(characterization);
		}
		return characterizations.size();
	}

	/**
	 * Make this automaton strongly connected by adding transitions from sinks
	 * to initials states.
	 * 
	 * @param input
	 *            an input symbol for added transitions. If there are more
	 *            initial states than sinks, multiple transitions will be added
	 *            and in this case, input symbol will be numbered.
	 * @param resetOut
	 *            output symbol for transitions from sink to initial
	 * @param loopOut
	 *            output symbol for looping transitions (added to complete the
	 *            automaton)
	 */
	public void connectStrongly(String input, String resetOut, String loopOut) {
		if (isConnex())
			return;
		List<Set<State>> sinks = new ArrayList<>();
		List<Set<State>> initials = new ArrayList<>();
		int sinkNb = 0;

		// Compute a list of all sinks sets
		Set<State> toTest = new HashSet<State>(states);
		while (!toTest.isEmpty()) {
			Set<State> sink = findStronglyConnectedComponent(toTest, false);
			Set<State> reachingSink = new HashSet<>(sink);
			boolean doAgain = true;
			while (doAgain) {
				doAgain = false;
				for (State s : states) {
					if (reachingSink.contains(s))
						continue;
					for (Transition transition : getTransitionFrom(s)) {
						if (reachingSink.contains(transition.getTo()))
							if (reachingSink.add(transition.getFrom()))
								doAgain = true;

					}
				}
			}

			sinks.add(sink);
			sinkNb += sink.size();
			assert !sink.isEmpty();
			if (toTest == sink)
				toTest = new HashSet<>();
			toTest.removeAll(reachingSink);
			assert !sink.isEmpty();
		}

		// then compute a list of all initial states set
		toTest.addAll(states);
		while (!toTest.isEmpty()) {
			Set<State> initial = new HashSet<>(toTest);
			findUnreachableAncestors(initial);
			assert !initial.isEmpty();
			LinkedList<State> toRemove = new LinkedList<>(initial);
			while (!toRemove.isEmpty()) {
				State s = toRemove.poll();
				if (!toTest.contains(s))
					continue;
				toTest.remove(s);
				for (MealyTransition t : getTransitionFrom(s)) {
					if (toTest.contains(t.getTo()))
						toRemove.add(t.getTo());
				}
			}
		}

		// Now compute the transitions to add

		// The two following list are used to store transitions before adding
		// them (we can't add directly transitions because we use method
		// getShortestPath which need a complete automaton)
		List<State> connectSink = new ArrayList<>();
		List<State> connectInitial = new ArrayList<>();

		// We connect each sink to an initial leading to next sink (so each sink
		// will be reachable from any other sink and this connect all isolated
		// components of the automaton)
		Set<State> sourceS = sinks.get(sinks.size() - 1);
		List<Set<State>> unreachedInitial = new LinkedList<>(initials);
		for (int i = 0; i < sinks.size(); i++) {
			Set<State> targetS = sinks.get(i);
			connectSink.add(sourceS.iterator().next());

			State targetI = null;
			for (Iterator<Set<State>> it = unreachedInitial.iterator(); it
					.hasNext();) {
				Set<State> initial = it.next();
				if (getShortestPath(initial.iterator().next(),
						targetS.iterator().next()) != null) {
					targetI = initial.iterator().next();
					it.remove();
					break;
				}
			}
			connectInitial.add(targetI);
			sourceS = targetS;
		}

		State defaultlInitialState = getInitialState();
		if (defaultlInitialState == null)
			defaultlInitialState = initials.iterator().next().iterator().next();

		// Now connect remaining sinks to initials (ensure that all sink has an
		// escaping transition)
		for (int i = 0; i < connectInitial.size(); i++) {
			if (connectInitial.get(i) == null) {
				State target = null;
				if (unreachedInitial.isEmpty())
					target = defaultlInitialState;
				else
					target = unreachedInitial.remove(0).iterator().next();
				connectInitial.set(i, target);
			}
		}

		String input_mod = input;
		int inputNb = 1;
		if (sinkNb < initials.size()) {
			// we will need an other input symbol later to finish to connect all
			// the node.
			input_mod = input + inputNb;
		}

		// Add the transitions in automaton (cannot be done sooner because of
		// use of method getShortestPath
		Set<State> updatedState = new HashSet<>();
		for (int i = 0; i < connectSink.size(); i++) {
			addTransition(new MealyTransition(this, connectSink.get(i),
					connectInitial.get(i), input_mod, resetOut));
			updatedState.add(connectSink.get(i));
		}
		// At this point, all sink are reachable from the others.
		// We must now connect all remaining initials
		do {
			for (Set<State> sink : sinks) {
				for (State s : sink) {
					if (unreachedInitial.isEmpty())
						break;
					if (!updatedState.contains(s)) {
						updatedState.add(s);
						State target = unreachedInitial.remove(0).iterator()
								.next();
						addTransition(new MealyTransition(this, s, target,
								input_mod, resetOut));
					}
				}
			}

			for (State s : states) {
				if (updatedState.contains(s))
					continue;
				addTransition(
						new MealyTransition(this, s, s, input_mod, loopOut));
			}

			// prepare to add transitions with an other input if there is still
			// some unreachable initial states
			updatedState.clear();
			inputNb++;
			input_mod = input + inputNb;
		} while (!unreachedInitial.isEmpty());
		assert isConnex();
	}

	@Override
	public Map<State, Integer> computeDepths(State start) {
		return computeDepths(start, Options.getLogLevel() == LogLevel.ALL);
	}

	/**
	 * Same as {@link Automata#computeDepths(State)} but can export automata as
	 * a dot file after computing depths.
	 * 
	 * @param start
	 *            node to start computation (can be {@code null})
	 * @param logToDot
	 *            force export to a dot file.
	 */
	public Map<State, Integer> computeDepths(State start, boolean logToDot) {
		Map<State, Integer> result = super.computeDepths(start);
		if (logToDot) {
			LogManager.logInfo(
					"A relative depth is associated to all nodes of the automaton : ");
			StringBuilder comments = new StringBuilder();
			for (Entry<State, Integer> entry : result.entrySet()) {
				comments.append(
						entry.getKey() + " [label="
								+ tools.GraphViz.id2DotAuto(entry.getKey()
										+ "\\nat depth " + entry.getValue())
								+ "]\n");
			}
			exportToDot(comments.toString());
		}
		return result;
	}

	/**
	 * Check if conjecture is compatible with this automata. This automata must
	 * have an initial state or be strongly connected.
	 * 
	 * Suppose that input symbols of conjecture are the same this automata
	 * (otherwise it will check for counter example matching only symbols from
	 * conjecture)
	 * 
	 * If conjecture does not have an initial state, the equivalence is checked
	 * with the whole automaton, not only with a strongly connected part.
	 * 
	 * @param conj
	 *            the conjecture to test.
	 * @return {@code false} if a discrepancy is found between conjecture and
	 *         this automata, {@code true } if no discrepancy is found.
	 */
	public boolean searchConjectureError(LmConjecture conj) {
		if (conj.getInitialState() != null && getInitialState() != null) {
			CounterExampleResult CER = conj.getCounterExamplesWithReset(this,
					true);
			if (!CER.isCompletelyEquivalent()) {
				System.out.println(CER.what());
				return false;
			}
		} else {
			State thisState = getInitialState();
			if (thisState == null) {
				if (!isConnex(false)) {
					LogManager.logWarning("automaton " + this
							+ " do not have an initial state and is not strongly connected."
							+ "The equivalence checking is not implemented as it might be incomplete.");
					assert false : "Unimplemented: we need to choose a state which can reach all others";
					return false;
				}
				thisState = getState(0);
			}
			assert allStatesAreReachableFrom(thisState);
			TotallyAdaptiveW W = new SplittingTree(this,
					conj.getInputSymbols()).computeW();
			AdaptiveCharacterization thisStateCharac = W
					.getEmptyCharacterization();
			while (!thisStateCharac.isComplete()) {
				AdaptiveSymbolSequence w = thisStateCharac.getUnknownPrints()
						.get(0);
				thisStateCharac.addPrint(w, apply(w, thisState));
			}
			LogManager.logInfo("Searching a state in conjecture " + conj
					+ " equivalent to state " + thisState + " in automaton "
					+ this + ".");
			State conjState = null;
			for (State conjTestState : conj.getStates()) {
				AdaptiveCharacterization conjStateCharac = W
						.getEmptyCharacterization();
				while (!conjStateCharac.isComplete()) {
					AdaptiveSymbolSequence w = conjStateCharac
							.getUnknownPrints().get(0);
					conjStateCharac.addPrint(w, conj.apply(w, conjTestState));
				}
				if (conjStateCharac.equals(thisStateCharac)) {
					conjState = conjTestState;
					LogManager.logInfo("state " + conjState + " in conjecture "
							+ conj + " has same characterization ("
							+ thisStateCharac + ") than state " + thisState
							+ " in automaton " + this + ".");
					break;
				}
			}
			if (conjState == null) {
				LogManager.logInfo("No state in conjecture " + conj
						+ " is equivalent to state " + thisState
						+ " in automaton " + this + ".");
				return false;
			} else {
				if (conj.getCounterExamples(conjState, this, thisState, true)
						.size() != 0) {
					LogManager.logInfo("state " + conjState + " in conjecture "
							+ conj + " is not equivalent to state " + thisState
							+ " in automaton " + this + ".");
					return false;
				}
			}
		}
		return true;
	}
}
