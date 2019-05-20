/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.mealy.DTOracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.GenericInputSequence.Iterator;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyAdaptiveW.AdaptiveCharacterization;
import automata.mealy.multiTrace.MultiTrace;
import automata.mealy.splittingTree.smetsersSplittingTree.SplittingTree;
import drivers.mealy.MealyDriver;
import drivers.mealy.DTOracle.DTOption.ModeOption;
import drivers.mealy.DTOracle.DTOption.ModeOption.ExtendedMode;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public class DTOracle {

	public class IncompleteConjectureException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		IncompleteConjectureException() {
			super("Conjecture is incomplete");
		}
	}

	public class InconsistencyFound extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public class UnreachableStatesException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnreachableStatesException() {
			super("some states are not reachable");
		}
	}

	abstract class TestPoint implements Test {
		final ExtendedState<TestPoint> startState;

		public TestPoint(ExtendedState<TestPoint> startState) {
			this.startState = startState;
		}

		protected abstract void execute(DTOracle dtOracle, boolean verbose)
				throws InconsistencyFound;
	}

	class FullCharacterizationTest extends TestPoint {
		final InputSequence testedMove;
		private AdaptiveCharacterization characterization;

		public FullCharacterizationTest(ExtendedState<TestPoint> extendedState,
				InputSequence inputSequence) {
			super(extendedState);
			testedMove = inputSequence;
			characterization = distinctionTree.getEmptyCharacterization();
		}

		@Override
		public int getCost() {
			// TODO
			return depths.get(startState.stdState);
		}

		@Override
		public String toString() {
			ArrayList<LmTrace> responses = new ArrayList<LmTrace>();
			for (LmTrace trace : characterization.knownResponses())
				responses.add(trace);
			return "test from state " + startState.stdState
					+ " followed by sequence " + testedMove
					+ " which is currently characterized by " + responses;
		}

		@Override
		protected void execute(DTOracle dtOracle, boolean verbose)
				throws InconsistencyFound {
			if (verbose && Options.getLogLevel() != LogLevel.LOW)
				LogManager.logInfo("Run ", this);
			dtOracle.execute(testedMove);
			GenericInputSequence in = characterization.unknownPrints()
					.iterator().next();
			GenericOutputSequence out = dtOracle.execute(in);
			characterization.addPrint(in, out);
			if (!characterization.isComplete()) {
				dtOracle.insert(this);
			}

		}
	}

	/**
	 * the number of tests recorded.
	 */
	private int testNb = 0;
	final TotallyAdaptiveW distinctionTree;
	boolean verbose;
	/**
	 * The {@link Automata#computeDepths(State) relative depths} of states in
	 * conjecture. This is used to avoid sinks.
	 */
	final Map<State, Integer> depths;
	private ExtendedState<TestPoint> currentState;
	private LmConjecture conjecture;
	private MealyDriver driver;
	private MultiTrace traces;
	Map<State, ExtendedState<TestPoint>> extendedStates;
	public boolean resetAllowed;

	public DTOracle(MealyDriver driver, LmConjecture c,
			State expectedStartingState, MultiTrace traces, boolean verbose) {
		depths = c.computeDepths(expectedStartingState, verbose);
		this.verbose = verbose;
		this.currentState = null;
		this.conjecture = c;
		this.driver = driver;
		this.traces = traces;
		assert traces != null;
		// inputSymbols = driver.getInputSymbols();
		distinctionTree = new SplittingTree(conjecture).computeW();
		assert conjecture.acceptCharacterizationSet(distinctionTree);
		if (verbose) {
			LogManager.logInfo("using distinction tree ", distinctionTree);
			distinctionTree
					.exportToDot(
							Options.getDotDir().toPath()
									.resolve("Distinction tree.dot").toFile(),
							"distinction_tree");
		}
//build extended states
		extendedStates = new HashMap<State, ExtendedState<TestPoint>>();
		for (State s : conjecture.getStates()) {
			ExtendedState<TestPoint> extendedState = new ExtendedState<>(s);
			extendedStates.put(s, extendedState);
		}
		for (MealyTransition t : conjecture.getTransitions()) {
			ExtendedState<TestPoint> from = extendedStates.get(t.getFrom());
			ExtendedState<TestPoint> to = extendedStates.get(t.getTo());
			ExtendedTransition<TestPoint> exteneded = new ExtendedTransition<>(
					t.getInput(), t.getOutput(), from, to);
			from.addChild(exteneded);
			to.addParent(exteneded);
		}

	}

	public void insert(TestPoint test) {
		test.startState.insert(test);
		testNb++;
	}

	/**
	 * This method create the tests to try on driver. The tests must be
	 * registered with the method {@link #insert(TestPoint)}
	 * 
	 * This method is not responsible of running the tests.
	 * 
	 * @param options
	 *            the options provided by user to configure the tests to build.
	 */
	private void createTests(DTOption options) {
		ModeOption mode = options.DTmode;
		if (mode.getSelectedItem() == mode.simpleMode)
			createTestsSimple();
		else if (mode.getSelectedItem() == mode.extendedMode)
			createTestsExtended(mode.extendedMode);
		else
			throw new RuntimeException("not implemented");
	}

	private void createTestsSimple() {
		for (ExtendedState<TestPoint> s : extendedStates.values()) {
			// We are in a non-deterministic loop. The following code must be
			// independent of other executions of the loop to be reproducible.
			for (String input : conjecture.getInputSymbols()) {
				insert(new FullCharacterizationTest(s,
						new InputSequence(input)));
			}
		}
	}

	private void createTestsExtended(ExtendedMode extendedMode) {
		createTestsSimple();
		for (State s : conjecture.getStates()) {
			ExtendedState<TestPoint> extended = extendedStates.get(s);
			for (String input : conjecture.getInputSymbols()) {
				insert(new FullCharacterizationTest(extended,
						new InputSequence(input).addInputSequence(InputSequence
								.generate(conjecture.getInputSymbols(),
										extendedMode.getLength(),
										extendedMode.rand.getRand()))));
			}
		}

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
		assert currentState != null;
		String driverOut = driver.execute(input);
		traces.recordIO(input, driverOut);
		ExtendedTransition<TestPoint> t = currentState.getTransition(input);
		if (t == null)
			throw new IncompleteConjectureException();
		currentState = t.to;
		if (!driverOut.equals(t.output))
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
		assert conjecture.getInitialState() != null;
		currentState = extendedStates.get(conjecture.getInitialState());
		assert currentState != null;
	}

	/**
	 * Actually walk in conjecture to test all transitions.
	 * 
	 * @throws InconsistencyFound
	 *             if a discrepancy is found
	 */
	void testTransitions() throws InconsistencyFound {
		while (testNb > 0) {
			if (!currentState.checkAlpha()) {
				if (verbose)
					LogManager.logInfo("no test reachable from current state");
				assert currentState.shortestAlpha == null;
				boolean alphaWithReset = resetAllowed;
				if (resetAllowed) {
					ExtendedState<TestPoint> initial = extendedStates
							.get(conjecture.getInitialState());
					if (initial.checkAlpha()) {
						reset();
						assert currentState == initial;
					} else
						alphaWithReset = false;
				}
				if (!alphaWithReset)
					throw new UnreachableStatesException();
			}
			if (verbose && currentState.shortestAlpha != null) {
				LogManager.logInfo("moving to a state with tests to run");
			}
			while (currentState.shortestAlpha != null) {
				execute(currentState.shortestAlpha.input);
			}
			TestPoint test = currentState.pollHighest();
			testNb--;
			if (verbose)
				LogManager.logInfo("now running ", test);
			assert currentState == test.startState;
			test.execute(this, verbose);

		}
		assert !currentState.checkAlpha();
	}

	/**
	 * Walk in the conjecture and search for a counter example.
	 * 
	 * @return true if a CE was found, false otherwise.
	 */
	public Boolean searchCE(State currentState, DTOption options) {
		assert (!resetAllowed || conjecture.getInitialState() != null);
		createTests(options);
		if (currentState == null) {
			this.currentState = null;
			reset();
		} else
			this.currentState = extendedStates.get(currentState);
		try {
			testTransitions();
			return false;
		} catch (InconsistencyFound e) {
			return true;
		} catch (UnreachableStatesException e) {
			LogManager.logWarning(
					"the oracle had to stop because some states were unreachable");
			return null;
		} finally {
			for (ExtendedState<TestPoint> s : extendedStates.values()) {
				s.clearTests();
			}
			testNb = 0;
		}
	}
}
