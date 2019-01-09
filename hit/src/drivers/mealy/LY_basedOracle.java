package drivers.mealy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.Automata;
import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.GenericInputSequence.Iterator;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyAdaptiveW.AdaptiveCharacterization;
import automata.mealy.multiTrace.MultiTrace;
import automata.mealy.splittingTree.LY_SplittingTree;
import automata.mealy.splittingTree.smetsersSplittingTree.SplittingTree;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

/**
 * This class aims to check a conjecture with a pseudo-checking sequence. The
 * pseudo-checking sequence is made by applying a {@link LY_SplittingTree
 * distinction tree} after each transition of the conjecture.
 * 
 * @author Nicolas BREMOND
 *
 */
public class LY_basedOracle {

	public class IncompleteConjectureException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		IncompleteConjectureException() {
			super("Conjecture is incomplete");
		}
	}

	public class UnreachableStatesException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnreachableStatesException(Set<State> set) {
			super("some states are not reachable : " + set);
		}
	}

	public class InconsistencyFound extends Exception {
		private static final long serialVersionUID = 1L;

	}

	/**
	 * The {@link Automata#computeDepths(State) relative depths} of states in
	 * conjecture. This is used to avoid sinks.
	 */
	final Map<State, Integer> depths;
	/**
	 * The current characterization of each transition, sorted by starting state
	 */
	Map<State, Map<MealyTransition, AdaptiveCharacterization>> transitionsToTest = new HashMap<>();
	/**
	 * The current state in conjecture.
	 */
	State currentState;
	final LmConjecture conjecture;
	/**
	 * The traces applied on driver.
	 */
	MultiTrace traces;
	final MealyDriver driver;
	final List<String> inputSymbols;
	public boolean verbose = Options.getLogLevel() == LogLevel.ALL;
	/**
	 * allow the use of reset to go to untested transitions.
	 */
	public boolean resetAllowed = true;

	public LY_basedOracle(MealyDriver driver, LmConjecture c,
			State currentState, MultiTrace traces) {
		depths = c.computeDepths(currentState, verbose);
		assert currentState != null;
		this.currentState = currentState;
		this.conjecture = c;
		this.driver = driver;
		this.traces = traces;
		assert traces != null;
		inputSymbols = driver.getInputSymbols();
		TotallyAdaptiveW distinctionTree = new SplittingTree(conjecture)
				.computeW();
		if (!conjecture.acceptCharacterizationSet(distinctionTree))
			throw new RuntimeException("invalid tree");
		if (verbose) {
			LogManager.logInfo("using distinction tree ", distinctionTree);
			distinctionTree.exportToDot();
		}
		for (State s : conjecture.getStates()) {
			// can be changed to add only reachable states
			Collection<MealyTransition> transitions = conjecture
					.getTransitionFrom(s);
			if (transitions.size() > 0) {
				Map<MealyTransition, AdaptiveCharacterization> characs = new HashMap<>();
				transitionsToTest.put(s, characs);
				for (MealyTransition t : transitions) {
					characs.put(t, distinctionTree.getEmptyCharacterization());
				}
			}
		}
	}

	/**
	 * Search a path from current state to a state with unchecked transitions.
	 * The path returned leads to a state of nearest depth.
	 * 
	 * @return a path to a state with unchecked transitions.
	 */
	InputSequence computeAlpha() {
		// TODO this method can be improved by saving paths to reuse them on
		// next call
		class Path {
			InputSequence seq;
			State reachedState;

			Path(State s) {
				seq = new InputSequence();
				reachedState = s;
			}

			private Path() {
			}

			Path extendsWith(String input) {
				Path newPath = new Path();
				newPath.seq = new InputSequence();
				newPath.seq.addInputSequence(seq);
				newPath.seq.addInput(input);
				MealyTransition t = conjecture
						.getTransitionFromWithInput(reachedState, input);
				if (t == null) {
					// can also be changed to avoid incomplete parts
					throw new IncompleteConjectureException();
				}
				newPath.reachedState = t.getTo();
				return newPath;
			}
		}

		LinkedList<Path> currentPaths = null;
		LinkedList<Path> nextPaths = new LinkedList<>();
		nextPaths.add(new Path(currentState));
		Integer depth = depths.get(currentState);
		Set<State> statesSeen = new HashSet<>();

		while (!nextPaths.isEmpty()) {
			currentPaths = nextPaths;
			nextPaths = new LinkedList<>();
			while (!currentPaths.isEmpty()) {
				Path p = currentPaths.poll();
				assert conjecture.applyGetState(p.seq,
						currentState) == p.reachedState;
				if (statesSeen.contains(p.reachedState))
					continue;
				if (depths.get(p.reachedState) > depth) {
					nextPaths.add(p);
					continue;
				}
				statesSeen.add(p.reachedState);
				if (transitionsToTest.get(p.reachedState) != null) {
					if (verbose)
						LogManager.logInfo(
								"Found alpha sequence leading to not fully tested state : ",
								p.seq);
					return p.seq;
				}
				for (String input : inputSymbols) {
					Path nextP = p.extendsWith(input);
					if (statesSeen.contains(nextP.reachedState))
						continue;
					assert depths.get(nextP.reachedState) >= depth;
					if (depths.get(nextP.reachedState) == depth)
						currentPaths.add(nextP);
					else
						nextPaths.add(nextP);
				}
			}
			depth++;
		}
		return null;
	}

	/**
	 * Execute a string on driver and conjecture and check outputs.
	 * 
	 * @param input
	 *            the input to execute
	 * @return the output produced by driver
	 * @throws InconsistencyFound
	 *             if the output from driver and conjecture differs.
	 */
	private String execute(String input) throws InconsistencyFound {
		String driverOut = driver.execute(input);
		traces.recordIO(input, driverOut);
		MealyTransition t = conjecture.getTransitionFromWithInput(currentState,
				input);
		if (t == null)
			throw new IncompleteConjectureException();
		currentState = t.getTo();
		if (!driverOut.equals(t.getOutput()))
			throw new InconsistencyFound();
		return driverOut;
	}

	/**
	 * Execute a sequence on driver and conjecture and check outputs. Stops
	 * after the first discrepancy observed.
	 * 
	 * @param seq
	 *            the sequence to execute
	 * @return the sequence observed
	 * @throws InconsistencyFound
	 *             if the output from driver and conjecture differs.
	 */
	private GenericOutputSequence execute(GenericInputSequence seq)
			throws InconsistencyFound {
		Iterator it = seq.inputIterator();
		while (it.hasNext()) {
			String out = execute(it.next());
			it.setPreviousOutput(out);
		}
		return it.getResponse();
	}

	/**
	 * Reset the driver and the conjecture to the initial state.
	 */
	private void reset() {
		driver.reset();
		traces.recordReset();
		currentState = conjecture.getInitialState();
		assert currentState != null;
	}

	/**
	 * Actually walk in conjecture to test all transitions.
	 * 
	 * @throws InconsistencyFound
	 *             if a discrepancy is found
	 */
	void testTransitions() throws InconsistencyFound {
		while (transitionsToTest.size() > 0) {
			State startState = currentState;
			Map<MealyTransition, AdaptiveCharacterization> localTransitions = transitionsToTest
					.get(currentState);
			if (localTransitions == null) {
				if (verbose) {
					LogManager.logInfo("All transitions from current state (",
							currentState,
							") are known. Let's move to another state with incomplete transitions");
				}
				InputSequence seq = computeAlpha();
				if (seq == null) {
					if (!resetAllowed || traces.isAfterRecordedReset()) {
						assert !resetAllowed
								|| currentState == conjecture.getInitialState();
						throw new UnreachableStatesException(
								transitionsToTest.keySet());
					}
					reset();
					continue;
				}
				execute(seq);
				continue;
			}
			// TODO optional: search a transition leading to a state of
			// nearest depth
			MealyTransition transition = localTransitions.keySet().iterator()
					.next();
			if (verbose) {
				LogManager.logInfo("Testing transition ", transition, ".");
			}
			execute(transition.getInput());
			AdaptiveCharacterization charac = localTransitions.get(transition);
			GenericInputSequence in = charac.unknownPrints().iterator().next();
			GenericOutputSequence out = execute(in);
			charac.addPrint(in, out);
			if (charac.isComplete()) {
				if (verbose) {
					LogManager.logInfo("Transition ", transition,
							" is checked.");
				}
				localTransitions.remove(transition);
				if (localTransitions.isEmpty())
					transitionsToTest.remove(startState);
			}
		}
	}

	/**
	 * Walk in the conjecture and search for a counter example.
	 * 
	 * @return true if a CE was found, false otherwise.
	 */
	public Boolean searchCE() {
		assert (!resetAllowed || conjecture.getInitialState() != null);
		try {
			testTransitions();
			return false;
		} catch (InconsistencyFound e) {
			return true;
		} catch (UnreachableStatesException e) {
			return null;
		}
	}
}
