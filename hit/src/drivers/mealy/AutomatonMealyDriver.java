package drivers.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.MultiTrace;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import options.learnerOptions.OracleOption;
import stats.StatsEntry_OraclePart;
import tools.RandomGenerator;
import tools.loggers.LogManager;

public class AutomatonMealyDriver extends MealyDriver {

	protected Mealy automata;
	protected State currentState;
	/**
	 * record the state before last execution for logging purpose.
	 */
	private State previousState = null;
	protected Set<InputSequence> triedCE;
	private int nbStates = 0;
	private String name = null;

	public AutomatonMealyDriver(Mealy automata) {
		super(automata.getName());
		type = DriverType.MEALY;
		this.automata = automata;
		triedCE = new HashSet<>();
		this.nbStates = automata.getStateCount();
		this.currentState = automata.getInitialState();
		if (this.currentState == null)
			throw new RuntimeException("the driver has no initial state");
	}

	@Override
	protected void logRequest(String input, String output) {
		LogManager.logRequest(input, output, getNumberOfAtomicRequest(),
				previousState, currentState);
	}

	@Override
	public final String execute_implem(String input) {
		assert currentState != null : "is the initial state of driver specified ?";
		previousState = currentState;
		String output = null;
		if (input.length() > 0) {
			MealyTransition currentTrans = automata
					.getTransitionFromWithInput(currentState, input);
			if (currentTrans != null) {
				output = new String(currentTrans.getOutput());
				currentState = currentTrans.getTo();
			} else {
				output = new String();
			}
		}
		return output;
	}

	@Override
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

	@Override
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
	 *            an object to record execution on driver
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
			MultiTrace appliedSequences, Boolean forbidReset,
			StatsEntry_OraclePart oracleStats)
			throws CeExposedUnknownStateException {
		int startSize = getNumberOfAtomicRequest();
		int startReset = getNumberOfRequest();
		long startTime = System.nanoTime();
		boolean result;
		try {
			result = getCounterExample(options, conjecture,
					conjectureStartingState, appliedSequences, forbidReset);
		} finally {
			float duration = (float) (System.nanoTime() - startTime)
					/ 1000000000;
			oracleStats.addOracleCall(getNumberOfAtomicRequest() - startSize,
					duration);
			assert startReset
					+ appliedSequences.getResetNumber() == getNumberOfRequest();
			assert appliedSequences.getResetNumber() == 0
					|| (!forbidReset && options.isResetAllowed());
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
			MultiTrace appliedSequences, Boolean forbidReset,
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

	@Override
	public void reset_implem() {
		assert (automata != null);
		automata.reset();
		currentState = automata.getInitialState();
	}

	/**
	 * compute an input sequence s.t. the output sequence entirely define the
	 * final state
	 * 
	 * @return null if a such sequence cannot be computed
	 * @throws UnableToComputeException
	 */
	@Override
	public InputSequence getHomingSequence() throws UnableToComputeException {
		LogManager.logInfo("Computing homing sequence");
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
								LogManager.logInfo(
										"Unable to compute homming sequence because "
												+ os1 + " and " + os2
												+ " have same outputs which leads in differents states");
								LogManager.logInfo(
										"Maybe thoose states are equivalent and you can use "
												+ r
												+ " as homming sequence (be careful, some states have not been tested). But in strict definition of homing sequence, if you got the same output, you must be in the same state");
								automata.exportToDot();
								throw new UnableToComputeException(os1 + " and "
										+ os2 + " seems to be equivalents");
							}
							OutputSequence currentO1 = automata.apply(current,
									os1);
							State currentOs1 = automata.applyGetState(current,
									os1);
							OutputSequence currentO2 = automata.apply(current,
									os2);
							State currentOs2 = automata.applyGetState(current,
									os2);
							if (currentOs1 == currentOs2
									|| !currentO1.equals(currentO2)) {
								foundLocalSeq = true;
								r.addInputSequence(current);
								if (Options.getLogLevel() != LogLevel.LOW) {
									LogManager.logInfo("appending " + current
											+ " to homing sequence in order to distinguish "
											+ os1 + " and " + os2
											+ " respectively reached from " + s1
											+ " and " + s2 + " with output "
											+ o1);
									if (currentOs1 == currentOs2)
										LogManager.logInfo(
												"Now, applying homing sequence from "
														+ s1 + " and " + s2
														+ " lead in same state "
														+ currentOs1);
									else {
										o1.addOutputSequence(currentO1);
										o2.addOutputSequence(currentO2);
										LogManager.logInfo(
												"Now, applying homing sequence from "
														+ s1 + " and " + s2
														+ " give outputs " + o1
														+ " and " + o2);
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

	/** Get init state **/
	public State getInitState() {
		return automata.getInitialState();
	}

}
