package learner.mealy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;
import tools.loggers.LogManager;


public class LmConjecture extends automata.mealy.Mealy {
	private static final long serialVersionUID = -6920082057724492261L;
	private List<String> inputSymbols;

	public LmConjecture(Driver d) {
		super(d.getSystemName());
		this.inputSymbols = d.getInputSymbols();
	}

	public List<String> getInputSymbols() {
		return inputSymbols;
	}
	
	/**
	 * check if a conjecture has all transitions
	 * @return true if there is a transition from any state with any input symbol
	 */
	public boolean isFullyKnown(){
		for (State s : getStates())
			for (String i : inputSymbols)
				if (getTransitionFromWithInput(s, i) == null)
					return false;
		return true;
	}

	/**
	 * This class stores several information on the result of a search of
	 * counter example. It stores counter example paths and indicate if a search
	 * completely checked an automaton.
	 * 
	 * @author Nicolas BREMOND
	 */
	public class CounterExampleResult {
		CounterExampleResult(List<State> realStates,
				List<State> conjectureStates) {
			super();
			this.realStates = realStates;
			this.conjectureStates = conjectureStates;
		}

		/**
		 * list of counter examples found from current state.
		 */
		private List<InputSequence> noResetCE = new ArrayList<>();
		/**
		 * list of counter examples found from initial state.
		 */
		private List<InputSequence> fromResetCE = new ArrayList<>();
		private Set<State> testedRealStates = new HashSet<>();
		private Set<State> reachedConjectureStates = new HashSet<>();
		private List<State> realStates;
		private List<State> conjectureStates;

		void addReachedConjectureState(State s) {
			reachedConjectureStates.add(s);
		}

		void addTestedRealState(State realState) {
			testedRealStates.add(realState);
		}

		void addNoResetCE(InputSequence counterExample) {
			noResetCE.add(counterExample);
		}

		/**
		 * Indicate if the automaton is exactly equivalent to the reference. For
		 * a detailed explanation of the result given, see {@link #what()}.
		 * 
		 * Notice that this method can return {@code false} for automata with
		 * unreachable parts even if they were equivalent.
		 * 
		 * @return {@code true} if all states of conjecture and reference were
		 *         checked without finding a counter example, {@code false}
		 *         otherwise.
		 * @see #what()
		 */
		public boolean isCompletelyEquivalent() {
			return testedRealStates.containsAll(realStates)
					&& conjectureStates.containsAll(reachedConjectureStates)
					&& noResetCE.isEmpty() && fromResetCE.isEmpty();
		}

		/**
		 * indicate why an automaton is not completely equivalent.
		 * 
		 * @return a string describing what forbid to say that automata are
		 *         equivalent.
		 * @see #isCompletelyEquivalent()
		 */
		public String what() {
			if (!noResetCE.isEmpty())
				return "The current state in conjecture is not equivalent to current state in real autoaton."
						+ " They can be distinguished with sequence "
						+ noResetCE.iterator().next();
			if (!fromResetCE.isEmpty())
				return "The initial state in conjecture is not equivalent to initial state in real automaton."
						+ " They can be distinguished with sequence "
						+ fromResetCE.iterator().next();
			if (!testedRealStates.containsAll(realStates)) {
				Set<State> nonTesetd = new HashSet<>(realStates);
				nonTesetd.removeAll(testedRealStates);
				return "some states in real automaton where not tested."
						+ " non tests states are " + nonTesetd + ".";
			}
			if (!reachedConjectureStates.containsAll(conjectureStates)) {
				Set<State> nonReached = new HashSet<>(conjectureStates);
				nonReached.removeAll(reachedConjectureStates);
				return "some states in conjecture were never reached during equivalence check."
						+ " Non reached states are " + nonReached + ".";
			}
			assert (isCompletelyEquivalent());
			return "conjecture is equivalent to real automata";
		}
	}

	/**
	 * get counter examples to distinguish conjecture from another automata.
	 * 
	 * suppose that input symbols of conjecture are the same as automata
	 * (otherwise it will check for counter example matching only symbols from
	 * conjecture)
	 * 
	 * This method is implemented by walking in real automata and checking if we
	 * can associate a state of conjecture to each real state. This mean that
	 * the conjecture can have extra states which will not be checked.
	 * 
	 * The conjecture can be not minimal and in this case, a warning might be
	 * logged (but this is not systematically the case, for instance it will not
	 * be logged if real automaton has the same «non-minimality» as conjecture).
	 * 
	 * @param conjectureStartingState
	 *            state to start algorithm in conjecture (this). Must be
	 *            equivalent to realStartingState
	 * @param realAutomata
	 *            the reference automata. All states must be reachable from the
	 *            given starting state, otherwise the checking will be partial.
	 * @param realStartingState
	 *            state to start algorithm in reference automata. Should be
	 *            equivalent to conjectureStartingState
	 * @param stopOnFirst
	 *            if {@code false}, The list including all counter examples
	 *            which are not containing loop will be returned. If
	 *            {@code true}, at most one counter example will be returned
	 *            (the first of the list). Note that the first is not always the
	 *            shortest.
	 * @return a list of {@link InputSequence} which will provide different
	 *         output if applied in conjecture and real automata from the
	 *         provided starting states.
	 */
	public List<InputSequence> getCounterExamples(State conjectureStartingState,
			Mealy realAutomata, State realStartingState, boolean stopOnFirst) {
		assert realAutomata.isConnex();
		CounterExampleResult counterExamples = getAllCounterExamplesReachable(
				conjectureStartingState, realAutomata, realStartingState,
				false);
		assert counterExamples.testedRealStates
				.containsAll(realAutomata.getStates());
		return counterExamples.noResetCE;
	}

	/**
	 * Search counter examples from initial state.
	 * 
	 * @param realAutomata
	 *            the reference automaton. Only states reachable from initial
	 *            state of this automaton will be checked.
	 * @param stopOnFirst
	 *            if <code>false</code>, The list including all counter examples
	 *            which are not containing loop will be returned. If
	 *            <code>true>,at most one counter example will be returned (the
	 *            first of the list). Note that the first is not always the
	 *            shortest.
	 * @return a {@link CounterExampleResult} object
	 */
	public CounterExampleResult getCounterExamplesWithReset(
			Mealy realAutomata,boolean stopOnFirst) {
		assert (getInitialState() != null
				&& realAutomata.getInitialState() != null);

		CounterExampleResult result = getAllCounterExamplesReachable(
				getInitialState(), realAutomata,
				realAutomata.getInitialState(), false);
		result.fromResetCE = result.noResetCE;
		result.noResetCE = new ArrayList<>();
		return result;
	}

	/**
	 * Search counter examples both from given states and from initial state (if
	 * defined in conjecture). It can return a same counter example two times,
	 * one with a path starting after a reset and one with a path from current
	 * state.
	 * 
	 * @param conjectureStartingState
	 *            the starting state in conjecture. should be equivalent to
	 *            {@code realStartingState}
	 * @param realAutomaton
	 *            the automaton to compare. All states should be reachable
	 *            either from starting state or from initial state.
	 * @param realStartingState
	 *            the starting state in real automaton.
	 * @param stopOnFirst
	 *            if <code>false</code>, The list including all counter examples
	 *            which are not containing loop will be returned. If
	 *            <code>true>,at most one counter example will be returned (the
	 *            first of the list). Note that the first is not always the
	 *            shortest.
	 * @return a {@link CounterExampleResult} object
	 */
	public CounterExampleResult getAllCounterExamples(
			State conjectureStartingState, Mealy realAutomaton,
			State realStartingState, boolean stopOnFirst) {
		assert realAutomaton.isConnex() || getInitialState() != null;

		CounterExampleResult result = getAllCounterExamplesReachable(
				conjectureStartingState, realAutomaton, realStartingState,
				stopOnFirst);
		if (getInitialState() != null) {
			CounterExampleResult resultFromReset = getCounterExamplesWithReset(
					realAutomaton,stopOnFirst);
			result.fromResetCE = resultFromReset.fromResetCE;
			result.reachedConjectureStates
					.addAll(resultFromReset.reachedConjectureStates);
			result.testedRealStates.addAll(resultFromReset.testedRealStates);
		}
		return result;

	}

	/**
	 * Search counter example starting from the given state by testing paths in
	 * the reference automaton and checking if it produce the same output in
	 * conjecture.
	 * 
	 * @param conjectureStartingState
	 *            the starting state in conjecture
	 * @param realAutomaton
	 *            the reference automaton
	 * @param realStartingState
	 *            the starting state in reference automaton.
	 * @param stopOnFirst
	 *            if <code>false</code>, The list including all counter examples
	 *            which are not containing loop will be returned. If
	 *            <code>true>,at most one counter example will be returned (the
	 *            first of the list). Note that the first is not always the
	 *            shortest.
	 * @return a {@link CounterExampleResult} object
	 */
	protected CounterExampleResult getAllCounterExamplesReachable(
			State conjectureStartingState, Mealy realAutomaton,
			State realStartingState, boolean stopOnFirst) {
		CounterExampleResult result = new CounterExampleResult(
				realAutomaton.getStates(), getStates());
		LmConjecture conjecture = this;
		// to each real state, we associate a state in conjecture
		Map<State, State> realToConjecture = new HashMap<>();
		// to each real state, we associate a path to reach them
		Map<State, InputSequence> accesPath = new HashMap<>();

		realToConjecture.put(realStartingState, conjectureStartingState);
		accesPath.put(realStartingState, new InputSequence());
		LinkedList<State> uncheckedStates = new LinkedList<>();

		uncheckedStates.add(realStartingState);

		while (!uncheckedStates.isEmpty()) {
			State realState = uncheckedStates.pollFirst();
			State conjectureState = realToConjecture.get(realState);
			assert conjectureState != null;
			for (String i : inputSymbols) {
				MealyTransition realTransition = realAutomaton
						.getTransitionFromWithInput(realState, i);
				MealyTransition conjectureTransition = conjecture
						.getTransitionFromWithInput(conjectureState, i);
				State realTarget = realTransition.getTo();
				State conjectureTarget = conjectureTransition.getTo();
				String realOutput = realTransition.getOutput();
				String conjectureOutput = conjectureTransition.getOutput();
				result.addReachedConjectureState(conjectureTarget);

				// first check if transition in both automata provide same
				// output
				if (!realOutput.equals(conjectureOutput)) {
					InputSequence counterExample = new InputSequence();
					counterExample.addInputSequence(accesPath.get(realState));
					counterExample.addInput(i);
					result.addNoResetCE(counterExample);
					if (stopOnFirst)
						return result;
				}
				// now map the realTarget to a state in conjecture (if not
				// already done)
				State mappedTarget = realToConjecture.get(realTarget);
				if (mappedTarget == null) {
					realToConjecture.put(realTarget, conjectureTarget);
					mappedTarget = conjectureTarget;
					InputSequence path = accesPath.get(realState).clone();
					path.addInput(i);
					accesPath.put(realTarget, path);
					uncheckedStates.add(realTarget);
				}
				// then check if transition in both automata lead in same state
				if (mappedTarget != conjectureTarget) {
					// if that states in conjecture are not equivalent, we
					// should find a distinction sequence.
					InputSequence distinctionSequence = conjecture
							.getDistinctionSequence(conjectureTarget,
									mappedTarget);
					if (distinctionSequence == null) {
						// the two states are equivalents
						LogManager.logWarning(
								"We found two equivalent states in conjecture while looking for counter example (states "
										+ conjectureTarget + " and "
										+ mappedTarget + ").");
						continue;
					}
					// now, we have realTarget which can be reach by to
					// sequences and this two sequences leads in two different
					// states in conjecture.
					OutputSequence realDistinctionOutput = realAutomaton
							.simulateOutput(realTarget, distinctionSequence);
					InputSequence counterExample = new InputSequence();
					if (!conjecture.simulateOutput(mappedTarget,
							distinctionSequence).equals(realDistinctionOutput)) {
						// We use the path which lead to realTarget and
						// mappedTarget (the first path discovered)
						counterExample.addInputSequence(accesPath
								.get(realTarget));
					} else {
						assert !conjecture.simulateOutput(conjectureTarget,
								distinctionSequence).equals(realDistinctionOutput);
						// We have to use the other path which lead to realTarget
						// and conjectureTarget
						counterExample.addInputSequence(accesPath
								.get(realState));
						counterExample.addInput(i);
					}
					counterExample.addInputSequence(distinctionSequence);
					result.addNoResetCE(counterExample);
					if (stopOnFirst)
						return result;
				}
			}
			result.addTestedRealState(realState);
		}
		return result;
	}

	/**
	 * Use the driver to identify the initial state of the conjecture.
	 * 
	 * This method is designed to be overridden by algorithms with low resets
	 * calls.
	 * 
	 * @param appliedSequences
	 *            the sequences applied on driver to identify the initial state.
	 *            A new sequence must be added in the list after each reset.
	 * @return the initial state of the conjecture or null if the initial state
	 *         cannot be discovered.
	 */
	public State searchInitialState(List<LmTrace> appliedSequences)
			throws CeExposedUnknownStateException {
		return getInitialState();
	}
}
