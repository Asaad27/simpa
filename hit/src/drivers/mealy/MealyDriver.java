package drivers.mealy;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.GenericInputSequence.Iterator;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.MultiTrace;
import automata.mealy.multiTrace.NoRecordMultiTrace;
import drivers.Driver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import options.learnerOptions.OracleOption;
import stats.StatsEntry_OraclePart;
import tools.RandomGenerator;
import tools.StandaloneRandom;
import tools.loggers.LogManager;

public abstract class MealyDriver extends Driver<String, String> {
	public class UnableToComputeException extends Exception {
		private static final long serialVersionUID = -6169240870495799817L;

		public UnableToComputeException() {
			super();
		}

		public UnableToComputeException(String message) {
			super(message);
		}
	}

	private String name = null;

	public MealyDriver(String name) {
		this.name = name;
	}


	@Override
	protected void logRequest(String input, String output) {
		LogManager.logRequest(input, output, getNumberOfAtomicRequest());
	}

	public final GenericOutputSequence execute(GenericInputSequence in) {
		Iterator it = in.inputIterator();
		while (it.hasNext()) {
			String outSymbol = execute(it.next());
			it.setPreviousOutput(outSymbol);
		}
		return it.getResponse();
	}

	public final OutputSequence execute(InputSequence in) {
		OutputSequence out=new OutputSequence();
		for (String i:in.sequence){
			out.addOutput(execute(i));
		}
		return out;
	}



	@Override
	public abstract List<String> getInputSymbols();

	@Override
	public String getSystemName() {
		return name;
	}

	/**
	 * Search a counter-example.
	 * 
	 * @param options
	 *            the options for oracle selection and settings
	 * @param conjecture
	 *            the conjecture to test
	 * @param conjectureStartingState
	 *            the current state in conjecture (can be {@code null} if the
	 *            oracle is allowed to reset the driver)
	 * @param appliedSequences
	 *            an object to record execution on driver
	 * @param oracleStats
	 *            the object which will be used to record statistics about
	 *            oracle.
	 * @param noResetAttempt
	 *            simple attempt to find a counter example without doing a
	 *            reset. To make a best search without reset,
	 *            {@code options.}{@link OracleOption#isResetAllowed()
	 *            isResetAllowed()} must be false.
	 * @return true if a counter example is found, false otherwise.
	 * @throws CeExposedUnknownStateException
	 *             if a new state is found while searching the initial state in
	 *             conjecture (during the potential call to
	 *             {@link LmConjecture#searchInitialState(List)})
	 */
	public boolean getCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureStartingState,
			MultiTrace appliedSequences, Boolean noResetAttempt,
			StatsEntry_OraclePart oracleStats)
			throws CeExposedUnknownStateException {
		int startSize = getNumberOfAtomicRequest();
		int startReset = getNumberOfRequest();
		long startTime = System.nanoTime();
		boolean result;
		try {
			result = getCounterExampleNoStats(options, conjecture,
					conjectureStartingState, appliedSequences, noResetAttempt);
		} finally {
			float duration = (float) (System.nanoTime() - startTime)
					/ 1000000000;
			oracleStats.addOracleCall(getNumberOfAtomicRequest() - startSize,
					duration);
			assert startReset
					+ appliedSequences.getResetNumber() == getNumberOfRequest();
			assert appliedSequences.getResetNumber() == 0
					|| (!noResetAttempt && options.isResetAllowed());
		}
		return result;
	}

	/**
	 * same as
	 * {@link #getCounterExample(OracleOption, LmConjecture, State, MultiTrace, Boolean, StatsEntry_OraclePart)}
	 * but for algorithms where the conjecture will not search initial state.
	 */
	public boolean getCounterExample_noThrow(OracleOption options,
			LmConjecture conjecture, State conjectureStartingState,
			MultiTrace appliedSequences, Boolean noResetAttempt,
			StatsEntry_OraclePart oracleStats) {
		assert conjecture.getInitialState() != null || noResetAttempt
				|| !options.isResetAllowed();
		try {
			return getCounterExample(options, conjecture,
					conjectureStartingState, appliedSequences, noResetAttempt,
					oracleStats);
		} catch (CeExposedUnknownStateException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Search a counter example by using a distinction tree
	 * 
	 * @param c
	 *            the conjecture to test
	 * @param curentState
	 *            the current state in conjecture
	 * @param traces
	 *            an object to record traces applied on driver.
	 * @param resetAllowed
	 *            indicate whether the oracle is allowed to do a reset or not
	 * @return {@code true} if a counter example is found, {@code null} if the
	 *         oracle was not able to test all the conjecture (missing
	 *         transitions, unreachable states,…) or {@code false} all
	 *         transitions were tested but without finding a discrepancy.
	 */
	public Boolean getDistinctionTreeBasedCE(LmConjecture c, State curentState,
			MultiTrace traces, boolean resetAllowed) {
		assert curentState != null;
		if (!c.isFullyKnown())
			return null;
		// TODO extend oracle to incomplete automata
		LY_basedOracle oracle = new LY_basedOracle(this, c, curentState,
				traces);
		oracle.resetAllowed = resetAllowed;
		stopLog();
		Boolean found = oracle.searchCE(curentState);
		startLog();
		return found;
	}

	/**
	 * Same as
	 * {@link #getCounterExample(OracleOption, LmConjecture, State, MultiTrace, Boolean, StatsEntry_OraclePart)}
	 * but this method do not record statistics.
	 * 
	 * @throws CeExposedUnknownStateException
	 *             if a new state is found while searching the initial state in
	 *             conjecture (during the potential call to
	 *             {@link LmConjecture#searchInitialState(List)})
	 */
	protected boolean getCounterExampleNoStats(OracleOption options,
			LmConjecture conjecture, State conjectureState,
			MultiTrace appliedSequences,
			Boolean noResetAttempt) throws CeExposedUnknownStateException {
		assert options.isResetAllowed()
				|| noResetAttempt == null : "noReset attempt should only be done when reset is allowed in options";
		if (noResetAttempt == null)
			noResetAttempt = false;
		boolean resetIsAllowed = options.isResetAllowed() && !noResetAttempt;
		if (resetIsAllowed) {
			if (conjectureState != null) {
				// first, try to find a CE without using reset
				boolean result = getCounterExampleNoStats(options, conjecture,
						conjectureState, appliedSequences, true);
				if (result)
					return true;
			}
			// we need to reset to let some oracle start from initial state
			// (e.g. the search of shortest counter-example needs to be in a
			// state were all others states are reachable).
			conjectureState = conjecture.searchInitialState(appliedSequences);
			assert conjectureState != null;
			reset();
			appliedSequences.recordReset();
		}

		assert conjectureState != null;
		if (options.getSelectedItem() == options.shortest) {
			assert this instanceof TransparentMealyDriver;
			InputSequence ce = ((TransparentMealyDriver) this).getShortestCE(
					conjecture, conjectureState, appliedSequences);
			if (ce == null) {
				return false;
			} else {
				OutputSequence ceOut = execute(ce);
				appliedSequences.recordTrace(new LmTrace(ce, ceOut));
				return true;
			}
		} else if (options.getSelectedItem() == options.mrBean) {
			if (options.mrBean.onlyIfCEExists()) {
				assert this instanceof TransparentMealyDriver;
				InputSequence ce = ((TransparentMealyDriver) this)
						.getShortestCE(conjecture, conjectureState,
								appliedSequences);
				if (ce == null) {
					return false;
				} else {
					LogManager.logInfo("a counter example exist (e.g. "
							+ ce
							+ "). Doing random walk until a CE is found");
					if (resetIsAllowed) {
						int maxLength = options.mrBean.getMaxTraceLength();
						int conjectureBound = conjecture.getStateCount()
								* getInputSymbols().size() * 100 + 500;
						if (conjectureBound < maxLength)
							maxLength = conjectureBound;
						boolean counterExampleIsFound;
						do {
							if (conjectureState == null) {
								conjectureState = conjecture
										.searchInitialState(appliedSequences);
								reset();
								appliedSequences.recordReset();
							}
							counterExampleIsFound = doRandomWalk(conjecture,
									conjectureState, appliedSequences,
									maxLength,
									options.mrBean.random.getRand());
							conjectureState = null;
							// here is a difficult point : a short length is
							// good if the automaton is not connex and the
							// counter-example is near the start but a long
							// sequence is good for automaton which have CE in a
							// point «far» from initial state.
							maxLength = maxLength + maxLength / 10 + 1;
						} while (!counterExampleIsFound);
						return true;
					} else {
						if (options.isResetAllowed()) {
							// we are doing an ATTEMPT of no reset CE.
							assert noResetAttempt;
							// we know that a CE exists from the current state
							// but random walk may lead to a sink were the CE is
							// not reachable anymore.
							// Thus, we must set a maximal length to the random
							// walk and abort if the CE is not found.

							// (another solution should be to check existence of
							// CE after each input to know if we should continue
							// the random walk or not)

							return doRandomWalk(conjecture, conjectureState,
									appliedSequences,
									conjecture.getTransitionCount() * 10,
									options.mrBean.random.getRand());
						} else {
							doRandomWalk(conjecture, conjectureState,
									appliedSequences, -1,
									options.mrBean.random.getRand());
							return true;
						}
					}
				}
			} else {
				int maxTraceNumber = options.mrBean.getMaxTraceNumber();
				if (!resetIsAllowed)
					maxTraceNumber = 1;
				for (int i = 0; i < maxTraceNumber; i++) {
					if (i != 0) {
						reset();
						appliedSequences.recordReset();
					}
					boolean counterExampleIsFound = doRandomWalk(conjecture,
							conjectureState, appliedSequences,
							options.mrBean.getMaxTraceLength(),
							options.mrBean.random.getRand());
					if (counterExampleIsFound)
						return true;
				}
				LogManager.logInfo(
						"no counter example found with random walk. the conjecture might be equivalent to the driver.");
				return false;
			}
		} else if (options.getSelectedItem() == options.interactive) {
			return getInteractiveCounterExample(options, conjecture,
					conjectureState, appliedSequences);
		} else if (options.getSelectedItem() == options.distinctionTreeBased) {
			Boolean r = getDistinctionTreeBasedCE(conjecture, conjectureState,
					appliedSequences, resetIsAllowed);
			return r != null && r;
		} else {
			throw new RuntimeException("option not implemented");
		}
	}

	private boolean getInteractiveCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureState,
			MultiTrace appliedSequences) {

		List<InputSequence> counterExamples = null;
		// TODO
//		if (this.automata != null && currentState != null) {
//			counterExamples = conjecture.getCounterExamples(conjectureState,
//					this.automata, currentState, false);
//			if (counterExamples.size() == 0)
//				return false;
//			LogManager.logInfo(
//					"there is no more counter example. user were not asked about one");
//		}

		InputSequence counterExample = new InputSequence();
		LogManager.logInfo("asking for counter example");
		Scanner input = new Scanner(System.in);
		if (counterExamples != null) {
			StringBuilder s = new StringBuilder();
			for (InputSequence iS : counterExamples) {
				s.append(iS + ", ");
			}
			System.out.println("Some counter example are " + s.toString());
			counterExample = counterExamples.get(0);
		}
		System.out.println(
				"What do you want to apply ? \n\tEnter «auto» to use default sequence '"
						+ counterExample
						+ "'\n\t'a,b,c' for the sequence a, b, c\n");

		String answer = input.nextLine();
		input.close();
		if (answer.equals(""))
			answer = "auto";
		System.out.println("understood «" + answer + "»");
		if (!answer.equals("auto")) {
			counterExample = new InputSequence();
			for (String i : answer.split(",")) {
				if (!getInputSymbols().contains(i))
					throw new RuntimeException(
							"user provided an input which is not available for the driver.");
				counterExample.addInput(i);
			}
		}
		System.out.println("using «" + counterExample + "»\n");
		LogManager.logInfo(
				"user choose «" + counterExample + "» as counterExemple");

		OutputSequence driverOut = execute(counterExample);
		appliedSequences.recordTrace(new LmTrace(counterExample, driverOut));
		if (driverOut.equals(
				conjecture.simulateOutput(conjectureState, counterExample))) {
			LogManager.logInfo(
					"CounterExample provided by user is not a counter example."
							+ "Assuming that conjecture is equivalent to the driver.");
			return false;
		}

		return true;

	}

	private boolean doRandomWalk(LmConjecture conjecture, State conjectureState,
			MultiTrace trace, int maxLength, RandomGenerator randomGenerator) {
		assert conjectureState != null;
		assert conjecture.getStates().contains(conjectureState);
		if (Options.getLogLevel().compareTo(LogLevel.ALL) >= 0)
			LogManager.logInfo("Starting a random walk");
		int tried = 0;
		List<String> is = getInputSymbols();
		while (maxLength < 0 || tried < maxLength) {
			String input = randomGenerator.randIn(is);
			String output = execute(input);
			trace.recordIO(input, output);
			MealyTransition transition = conjecture
					.getTransitionFromWithInput(conjectureState, input);
			if (!transition.getOutput().equals(output)) {
				return true;
			}
			conjectureState = transition.getTo();
			tried++;
		}
		return false;
	}

	/**
	 * compute an input sequence s.t. the output sequence entirely define the
	 * final state
	 * 
	 * @return null if a such sequence cannot be computed
	 * @throws UnableToComputeException
	 */
	public InputSequence getHomingSequence() throws UnableToComputeException {
		LogManager.logInfo("Unable to compute homing sequence");
		throw new UnableToComputeException();
	}

	@Override
	public boolean isCounterExample(Object ce, Object c) {
		if (ce == null)
			return false;
		InputSequence realCe = (InputSequence) ce;
		LmConjecture conj = (LmConjecture) c;
		State state = conj.getInitialState();
		reset();
		for (String input : realCe.sequence) {
			MealyTransition t = conj.getTransitionFromWithInput(state, input);
			state = t.getTo();
			if (!execute(input).equals(t.getOutput())) {
				return true;
			}
		}

		return false;
	}

	
	/**
	 * Synchronize the driver and the given automata to a given state (without
	 * using a transparent box). It apply inputs on driver to eliminate states
	 * in automata.
	 * 
	 * If driver is not equivalent to automata, this method MIGHT find a
	 * counterexample.
	 * 
	 * @param automata
	 *            the automata to synchronize with.
	 * @param trace
	 *            the trace of executions applied on driver
	 * @param hintPossibleCurrentStates
	 *            a set of {@link State} of @{code automata} in which the driver
	 *            can be. Use {@code null} if the driver can be in any state.
	 * @return a state of automata representing the current (after this call)
	 *         state of driver if automata is equivalent to driver. If automata
	 *         has equivalent states, only one state is returned.
	 */
	public State searchCurrentState(Mealy automata, LmTrace trace,
			Set<State> hintPossibleCurrentStates) {
		if (hintPossibleCurrentStates != null)
			hintPossibleCurrentStates = Collections
					.unmodifiableSet(hintPossibleCurrentStates);
		LogManager.logInfo(
				"searching the current state in automata by applying inputs on driver");

		Set<State> possibleCurrentState;
		if (hintPossibleCurrentStates != null)
			possibleCurrentState = new HashSet<>(hintPossibleCurrentStates);
		else
			possibleCurrentState = new HashSet<>(automata.getStates());
		InputSequence distinction = null;
		State s1 = null;
		State s2 = null;
		while (possibleCurrentState.size() > 1) {
			if (Options.getLogLevel() == LogLevel.ALL)
				LogManager.logInfo(
						"The automata can currently be in one state in ",
						possibleCurrentState);
			if (distinction == null || distinction.isEmpty()) {
				java.util.Iterator<State> it = possibleCurrentState.iterator();
				s1 = it.next();
				s2 = it.next();
				distinction = automata.getDistinctionSequence(s1, s2);
				if (distinction == null) {
					possibleCurrentState.remove(s1);
					if (Options.getLogLevel() != LogLevel.LOW)
						LogManager.logInfo("States ", s1, " and ", s2,
								" are equivalents. Ignoring state ", s1);
					continue;

				}
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo("Using sequence ", distinction,
							" to distinguish states ", s1, " and ", s2);
			}
			Set<State> previousPossibleState = possibleCurrentState;
			assert !distinction.isEmpty();
			String in = distinction.getFirstSymbol();
			distinction.removeFirstInput();
			String out = execute(in);
			trace.append(in, out);
			possibleCurrentState = new HashSet<>();
			for (State s : previousPossibleState) {
				MealyTransition t = automata.getTransitionFromWithInput(s, in);
				if (t.getOutput().equals(out))
					possibleCurrentState.add(t.getTo());
			}
			if (distinction.isEmpty()) {
				s1 = null;
				s2 = null;
			} else {
				MealyTransition t1 = automata.getTransitionFromWithInput(s1,
						in);
				MealyTransition t2 = automata.getTransitionFromWithInput(s2,
						in);
				if (t1.getOutput().equals(out)) {
					s1 = t1.getTo();
					s2 = t2.getTo();
				} else {
					assert !t2.getOutput().equals(
							out) : "distinction should differ only for the last symbol";
					distinction = null;
					s1 = null;
					s2 = null;
					if (Options.getLogLevel() != LogLevel.LOW)
						LogManager.logInfo(
								"the previously computed distinction sequence is thrown away because the two states are incompatible with last execution.");
				}
			}
		}
		if (possibleCurrentState.size() == 0) {
			LogManager.logInfo(
					"One counter example found (by inadvertance) : no state of given automata can produce the trace ",
					trace);
			return null;
		}
		assert possibleCurrentState.size() == 1;
		State current = possibleCurrentState.iterator().next();
		LogManager.logInfo("only state ", current,
				" can be at the end of trace ", trace);
		return current;
	}

	/**
	 * check if conjecture is compatible with this driver.
	 * 
	 * @param conj
	 *            the conjecture to test.
	 * @return {@code false} if a discrepancy is found between conjecture and
	 *         this automata, {@code true } if no discrepancy is found.
	 */
	public boolean searchConjectureError(LmConjecture conj) {
		LogManager.logLine();
		LogManager.logInfo("Checking conjecture");

		if (this instanceof TransparentMealyDriver) {
			LogManager.logInfo("Checking with transparent driver");
			if (!((TransparentMealyDriver) this).getAutomata()
					.searchConjectureError(conj)) {
				LogManager.logError(
						"Conjecture is inconsistent with transparent driver");
				return false;
			}
		}

		LogManager.logInfo("Checking with distinction tree");
		State conjState = searchCurrentState(conj, new LmTrace(), null);
		if (conjState == null) {
			LogManager.logError("Conjecture is inconsitent with driver");
			return false;
		}
		Boolean r = getDistinctionTreeBasedCE(conj, conjState,
				new NoRecordMultiTrace(), conj.getInitialState() != null);
		assert r != null : "conjecture should be complete";
		if (r == null || r) {
			LogManager.logError("Conjecture is inconsitent with driver");
			return false;
		}

		LogManager.logInfo("Checking with random walk");
		int nbtests = getInputSymbols().size() * 10;
		int testLength = conj.getStateCount() * 2 + 10;
		StandaloneRandom rand = new StandaloneRandom();
		for (int i = 0; i < nbtests; i++) {
			conjState = conj.getInitialState();
			if (conjState != null)
				reset();
			else {
				conjState = searchCurrentState(conj, new LmTrace(), null);
				if (conjState == null) {
					LogManager
							.logError("Conjecture is inconsitent with driver");
					return false;
				}
			}
			if (doRandomWalk(conj, conjState, new NoRecordMultiTrace(),
					testLength, rand)) {
				LogManager.logError("Conjecture is inconsitent with driver");
				return false;
			}
		}

		LogManager.logInfo("Conjecture seems consistent with the driver");
		return true;
	}
	
}
