package drivers.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import options.RandomOption;
import options.learnerOptions.OracleOption;
import stats.StatsEntry_OraclePart;
import tools.Utils;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;
import drivers.mealy.transparent.TransparentMealyDriver;

public class MealyDriver extends Driver {
	public class UnableToComputeException extends Exception {
		private static final long serialVersionUID = -6169240870495799817L;

		public UnableToComputeException() {
			super();
		}

		public UnableToComputeException(String message) {
			super(message);
		}
	}

	protected Mealy automata;
	protected State currentState;
	protected List<InputSequence> forcedCE;
	protected Set<InputSequence> triedCE;
	private int nbStates = 0;
	private int transitionCount = 0;
	private String name = null;

	public MealyDriver(Mealy automata) {
		super();
		type = DriverType.MEALY;
		this.automata = automata;
		this.forcedCE = getForcedCE();
		triedCE = new HashSet<>();
		this.nbStates = automata.getStateCount();
		this.name = automata.getName();
		this.currentState = automata.getInitialState();
		if (this.currentState==null)
			throw new RuntimeException("the driver has no initial state");
	}

	public MealyDriver(String name) {
		this.name = name;
		this.automata = null;
		triedCE = new HashSet<>();
	}

	public List<String> getStats() {
		return Utils.createArrayList(String.valueOf(nbStates), String.valueOf(getInputSymbols().size()),
				String.valueOf(getOutputSymbols().size()),
				String.valueOf(((float) numberOfAtomicRequest / numberOfRequest)), String.valueOf(numberOfRequest),
				String.valueOf(((float) duration / 1000000000)), String.valueOf(automata.getTransitionCount()));
	}

	protected List<InputSequence> getForcedCE() {
		return null;
	}

	public OutputSequence execute(InputSequence in){
		OutputSequence out=new OutputSequence();
		for (String i:in.sequence){
			out.addOutput(execute(i));
		}
		return out;
	}
	
	public String execute(String input) {
		assert currentState != null : "is the initial state of driver specified ?";
		String output = null;
		if (input.length() > 0) {
			numberOfAtomicRequest++;
			State before = currentState;
			MealyTransition currentTrans = automata.getTransitionFromWithInput(currentState, input);
			if (currentTrans != null) {
				output = new String(currentTrans.getOutput());
				currentState = currentTrans.getTo();
			} else {
				output = new String();
			}
			if (addtolog)
				LogManager.logRequest(input, output, transitionCount, before,
						currentState);
			transitionCount++;
		}
		return output;
	}

	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		for (MealyTransition t : automata.getTransitions()) {
			if (!is.contains(t.getInput()))
				is.add(t.getInput());
		}
		Collections.sort(is);
		return is;
	}

	/**
	 * get the number of states in driver if the driver is transparent.
	 * 
	 * @return the number of states in driver or {@code null} if the driver is
	 *         not transparent.
	 */
	public Integer getStateCount() {
		if (this instanceof TransparentMealyDriver) {
			return ((TransparentMealyDriver) this).getAutomata()
					.getStateCount();
		}
		return null;
	}

	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		for (MealyTransition t : automata.getTransitions()) {
			if (!os.contains(t.getOutput()))
				os.add(t.getOutput());
		}
		Collections.sort(os);
		return os;
	}

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
	 *            a list in which all sequence applied on driver will be added.
	 *            A reset is applied between each trace of this list.
	 * @param oracleStats
	 *            the object which will be used to record statistics about
	 *            oracle.
	 * @return true if a counter example is found, false otherwise.
	 * @throws CeExposedUnknownStateException
	 *             if a new state is found while searching the initial state in
	 *             conjecture (during the potential call to
	 *             {@link LmConjecture#searchInitialState(List)})
	 */
	public boolean getCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureStartingState,
			List<LmTrace> appliedSequences, Boolean forbidReset,
			StatsEntry_OraclePart oracleStats)
			throws CeExposedUnknownStateException {
		int startSize = numberOfAtomicRequest;
		int startReset = numberOfRequest;
		long startTime = System.nanoTime();
		boolean result;
		try {
			result = getCounterExample(options, conjecture,
					conjectureStartingState, appliedSequences, forbidReset);
		} finally {
			float duration = (float) (System.nanoTime() - startTime)
					/ 1000000000;
			oracleStats.addOracleCall(numberOfAtomicRequest - startSize,
					duration);
			assert startReset + appliedSequences.size() - 1 == numberOfRequest;
		}
		return result;
	}

	/**
	 * same as
	 * {@link #getCounterExample(OracleOption, LmConjecture, State, List, Boolean, StatsEntry_OraclePart)}
	 * but for algorithms where the conjecture will not search initial state.
	 */
	public boolean getCounterExample_noThrow(OracleOption options,
			LmConjecture conjecture, State conjectureStartingState,
			List<LmTrace> appliedSequences, Boolean forbidReset,
			StatsEntry_OraclePart oracleStats) {
		assert conjecture.getInitialState() != null || forbidReset
				|| !options.isResetAllowed();
		try {
			return getCounterExample(options, conjecture,
					conjectureStartingState, appliedSequences, forbidReset,
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
	 *            a list which will be filled with the traces applied on driver.
	 *            See {@link LY_basedOracle#traces}.
	 * @param resetAllowed
	 *            indicate whether the oracle is allowed to do a reset or not
	 * @return {@code true} if a counter example is found, {@code null} if the
	 *         oracle was not able to test all the conjecture (missing
	 *         transitions, unreachable states,…) or {@code false} all
	 *         transitions were tested but without finding a discrepancy.
	 */
	public Boolean getDistinctionTreeBasedCE(LmConjecture c, State curentState,
			List<LmTrace> traces, boolean resetAllowed) {
		assert Options.USE_DT_CE;
		if (!c.isFullyKnown())
			return null;
		// TODO extend oracle to incomplete automata
		LY_basedOracle oracle = new LY_basedOracle(this, c, curentState,
				traces);
		oracle.resetAllowed = resetAllowed;
		stopLog();
		Boolean found = oracle.searchCE();
		startLog();
		return found;
	}

	/**
	 * Same as
	 * {@link #getCounterExample(OracleOption, LmConjecture, State, List, StatsEntry_OraclePart)}
	 * but this method do not record statistics.
	 * 
	 * @throws CeExposedUnknownStateException
	 *             if a new state is found while searching the initial state in
	 *             conjecture (during the potential call to
	 *             {@link LmConjecture#searchInitialState(List)})
	 */
	protected boolean getCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureState,
			List<LmTrace> appliedSequences, Boolean forbidReset)
			throws CeExposedUnknownStateException {
		if (forbidReset == null)
			forbidReset = false;
		boolean resetIsAllowed = options.isResetAllowed() && !forbidReset;
		if (resetIsAllowed) {
			if (conjectureState != null) {
				// first, try to find a CE without using reset
				boolean result = getCounterExample(options, conjecture,
						conjectureState, appliedSequences, true);
				if (result)
					return true;
			}
			// we need to reset to let some oracle start from initial state
			// (e.g. the search of shortest counter-example needs to be in a
			// state were all others states are reachable).
			conjectureState = conjecture.searchInitialState(appliedSequences);
			reset();
			appliedSequences.add(new LmTrace());
		}

		assert conjectureState != null;
		if (options.getSelectedItem() == options.shortest) {
			assert this.automata != null;
			assert this.getCurrentState() != null;
			if (!this.automata.isConnex())
				throw new RuntimeException(
						"automata must be strongly connected");
			List<InputSequence> counterExamples = conjecture.getCounterExamples(
					conjectureState, this.automata, getCurrentState(), true);
			if (counterExamples.isEmpty() && options.isResetAllowed()) {
				reset();
				appliedSequences.add(new LmTrace());
				conjectureState = conjecture.getInitialState();
				assert conjectureState != null;
				counterExamples = conjecture.getCounterExamples(conjectureState,
						this.automata, getCurrentState(), true);
			}
			if (counterExamples.isEmpty()) {
				appliedSequences.add(new LmTrace());
				return false;
			} else {
				InputSequence ceIn = counterExamples.get(0);
				OutputSequence ceOut = execute(ceIn);
				appliedSequences.add(new LmTrace(ceIn, ceOut));
				return true;
			}
		} else if (options.getSelectedItem() == options.mrBean) {
			if (options.mrBean.onlyIfCEExists()) {
				assert this.automata != null;
				assert this.getCurrentState() != null;
				if (!this.automata.isConnex())
					throw new RuntimeException(
							"automata must be strongly connected");
				List<InputSequence> counterExamples = conjecture
						.getCounterExamples(conjectureState, this.automata,
								getCurrentState(), true);
				if (counterExamples.isEmpty() && resetIsAllowed) {
					reset();
					appliedSequences.add(new LmTrace());
					conjectureState = conjecture.getInitialState();
					assert conjectureState != null;
					counterExamples = conjecture.getCounterExamples(
							conjectureState, this.automata, getCurrentState(),
							true);
				}
				if (counterExamples.isEmpty()) {
					appliedSequences.add(new LmTrace());
					return false;
				} else {
					LogManager.logInfo("a counter example exixst (e.g. "
							+ counterExamples.get(0)
							+ "). Doing random walk until a CE is found");
					if (options.isResetAllowed()) {
						int maxLength = options.mrBean.getMaxTraceLength();
						int conjectureBound = conjecture.getStateCount()
								* getInputSymbols().size() * 100 + 500;
						if (conjectureBound < maxLength)
							maxLength = conjectureBound;
						boolean counterExampleIsFound;
						do {
							LmTrace trace = new LmTrace();
							counterExampleIsFound = doRandomWalk(conjecture,
									conjectureState, trace, maxLength,
									options.mrBean.random);
							appliedSequences.add(trace);
							reset();
							// here is a difficult point : a short length is
							// good if the automaton is not connex and the
							// counter-example is near the start but a long
							// sequence is good for automaton which have CE in a
							// point «far» from initial state.
							maxLength = maxLength + maxLength / 10 + 1;
						} while (!counterExampleIsFound);
						return true;
					} else {
						assert this.automata.isConnex();
						LmTrace trace = new LmTrace();
						doRandomWalk(conjecture, conjectureState, trace, -1,
								options.mrBean.random);
						appliedSequences.add(trace);
						return true;
					}
				}
			} else {
				for (int i = 0; i < options.mrBean.getMaxTraceNumber(); i++) {
					LmTrace trace = new LmTrace();
					appliedSequences.add(trace);
					if (i != 0)
						reset();// according to specification, there is a reset
								// BETWEEN the sequences
					boolean counterExampleIsFound = doRandomWalk(conjecture,
							conjectureState, trace,
							options.mrBean.getMaxTraceLength(),
							options.mrBean.random);
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
		} else {
			throw new RuntimeException("option not implemented");
		}
	}

	private boolean getInteractiveCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureState,
			List<LmTrace> appliedSequences) {

		List<InputSequence> counterExamples = null;
		if (this.automata != null && getCurrentState() != null) {
			counterExamples = conjecture.getCounterExamples(conjectureState,
					this.automata, getCurrentState(), false);
			if (counterExamples.size() == 0)
				return false;
			LogManager.logInfo(
					"there is no more counter example. user were not asked about one");
		}
		InputSequence counterExample = new InputSequence();
		LogManager.logInfo("asking for counter exemple");
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
		appliedSequences.add(new LmTrace(counterExample, driverOut));
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
			LmTrace trace, int maxLength, RandomOption rand) {
		if (Options.getLogLevel().compareTo(LogLevel.ALL) >= 0)
			LogManager.logInfo("Starting a random walk");
		int tried = 0;
		List<String> is = getInputSymbols();
		while (maxLength < 0 || tried < maxLength) {
			String input = rand.randIn(is);
			String output = execute(input);
			trace.append(input, output);
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

	@Override
	public void reset() {
		super.reset();
		if (automata != null) {
			automata.reset();
			currentState = automata.getInitialState();
		}
	}

	public boolean isCounterExample(Object ce, Object c) {
		if (ce == null)
			return false;
		InputSequence realCe = (InputSequence) ce;
		LmConjecture conj = (LmConjecture) c;
		MealyDriver conjDriver = new MealyDriver(conj);
		stopLog();
		conjDriver.stopLog();
		reset();
		conjDriver.reset();
		boolean isCe = false;
		for (String input : realCe.sequence) {
			if (!execute(input).equals(conjDriver.execute(input))) {
				isCe = true;
				break;
			}
		}
		startLog();
		conjDriver.startLog();
		return isCe;
	}

	/**
	 * compute an input sequence s.t. the output sequence entirely define the
	 * final state
	 * 
	 * @return null if a such sequence cannot be computed
	 * @throws UnableToComputeException
	 */
	public InputSequence getHomingSequence() throws UnableToComputeException {
		LogManager.logInfo("Computing homing sequence");
		if (automata == null) {
			LogManager.logInfo("Unable to compute homing sequence");
			throw new UnableToComputeException();
		}
		InputSequence r = new InputSequence();
		boolean found = false;
		while (!found) {
			found = true;
			for (int i = 0; i < automata.getStateCount(); i++) {
				State s1 = automata.getState(i);
				for (int j = i + 1; j < automata.getStateCount(); j++) {
					State s2 = automata.getState(j);
					OutputSequence o1 = automata.apply(r, s1);
					State os1 = automata.applyGetState(r, s1);
					OutputSequence o2 = automata.apply(r, s2);
					State os2 = automata.applyGetState(r, s2);
					if (o1.equals(o2) && os1 != os2) {
						found = false;
						LinkedList<InputSequence> l = new LinkedList<>();
						l.add(new InputSequence());
						boolean foundLocalSeq = false;
						while (!foundLocalSeq) {
							InputSequence current = l.poll();
							if (current.getLength() >= nbStates) {
								LogManager.logInfo("Unable to compute homming sequence because " + os1 + " and " + os2
										+ " have same outputs which leads in differents states");
								LogManager.logInfo("Maybe thoose states are equivalent and you can use " + r
										+ " as homming sequence (be careful, some states have not been tested). But in strict definition of homing sequence, if you got the same output, you must be in the same state");
								automata.exportToDot();
								throw new UnableToComputeException(os1 + " and " + os2 + " seems to be equivalents");
							}
							OutputSequence currentO1 = automata.apply(current, os1);
							State currentOs1 = automata.applyGetState(current, os1);
							OutputSequence currentO2 = automata.apply(current, os2);
							State currentOs2 = automata.applyGetState(current, os2);
							if (currentOs1 == currentOs2 || !currentO1.equals(currentO2)) {
								foundLocalSeq = true;
								r.addInputSequence(current);
								if (Options.getLogLevel() != LogLevel.LOW) {
									LogManager.logInfo("appending " + current
											+ " to homing sequence in order to distinguish " + os1 + " and " + os2
											+ " respectively reached from " + s1 + " and " + s2 + " with output " + o1);
									if (currentOs1 == currentOs2)
										LogManager.logInfo("Now, applying homing sequence from " + s1 + " and " + s2
												+ " lead in same state " + currentOs1);
									else {
										o1.addOutputSequence(currentO1);
										o2.addOutputSequence(currentO2);
										LogManager.logInfo("Now, applying homing sequence from " + s1 + " and " + s2
												+ " give outputs " + o1 + " and " + o2);
									}
								}

							} else {
								for (String in : getInputSymbols()) {
									InputSequence toTry = new InputSequence();
									toTry.addInputSequence(current);
									toTry.addInput(in);
									l.add(toTry);
								}
							}
						}
					}
				}
			}
		}
		LogManager.logInfo("Found homing sequence " + r);
		return r;
	}

	/** Get current state **/
	public State getCurrentState() {
		return currentState;
	}
	/** Get init state **/
	public State getInitState() {
		return automata.getInitialState();
	}
	
	
	
}
