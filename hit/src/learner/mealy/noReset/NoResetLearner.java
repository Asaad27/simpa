package learner.mealy.noReset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.ConjectureNotConnexException;
import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import learner.mealy.noReset.dataManager.InconsistancyWithConjectureException;
import learner.mealy.noReset.dataManager.InvalidHException;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.AdenilsoSimaoTool;
import tools.Utils;

import tools.loggers.LogManager;

public class NoResetLearner extends Learner {
	private MealyDriver driver;
	private DataManager dataManager;
	private NoResetStatsEntry stats;
	protected List<InputSequence> W;
	private int n;// the maximum number of states

	public NoResetLearner(MealyDriver d) {
		driver = d;
	}

	/**
	 * 
	 * @return null if no counter example is found
	 */
	public InputSequence getCounterExemple() {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			Mealy realAutomata = d.getAutomata();
			LmConjecture conjecture = dataManager.getConjecture();
			State conjectureStartingState = dataManager.getC(
					dataManager.traceSize()).getState();
			State realStartingState = d.getCurrentState();

			List<InputSequence> counterExamples = conjecture
					.getAllCounterExamples(conjectureStartingState,
							realAutomata, realStartingState);
			if (counterExamples.size() > 0) {
				InputSequence counterExample = counterExamples.get(0);
				if (Options.INTERACTIVE) {
					LogManager.logInfo("asking for counter exemple");
					Scanner input = new Scanner(System.in);
					StringBuilder s = new StringBuilder();
					for (InputSequence iS : counterExamples) {
						s.append(iS + ", ");
					}
					System.out.println("Some counter example are "
							+ s.toString());
					System.out
							.println("What do you want to apply ? \n\tEnter «auto» to use default sequence '"
									+ counterExample
									+ "'\n\t'a,b,c' for the sequence a, b, c\n");

					String answer = input.nextLine();
					if (answer.equals(""))
						answer = "auto";
					System.out.println("understood «" + answer + "»");
					if (!answer.equals("auto")) {
						counterExample = new InputSequence();
						for (String i : answer.split(","))
							counterExample.addInput(i);
					}
					System.out.println("using «" + counterExample + "»\n");
					LogManager.logInfo("user choose «" + counterExample
							+ "» as counterExemple");
				}
				return counterExample;
			} else {
				return null;
			}

		} else {
			throw new RuntimeException("not implemented");
		}
	}

	public void learn() {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			if ((!d.getAutomata().isConnex()))
				throw new RuntimeException("driver must be strongly connected");
		}
		List<InputSequence> W = new ArrayList<InputSequence>();
		W.add(new InputSequence());
		InputSequence h = new InputSequence();
		learn(W, h);// We suppose that this one do not throw
					// InconsistenceException
		W = new ArrayList<InputSequence>();
		InputSequence counterExample = null;
		boolean inconsistencyFound = false;
		while (inconsistencyFound
				|| (counterExample = getCounterExemple()) != null) {
			if (counterExample != null) {
				if (!inconsistencyFound)
					LogManager
							.logInfo(counterExample
									+ " should be a counter example for this automata.");
				OutputSequence ConjectureCEOut = dataManager.getConjecture()
						.simulateOutput(
								dataManager.getC(dataManager.traceSize())
										.getState(), counterExample);
				OutputSequence DriverCEOut = driver.execute(counterExample);
				assert inconsistencyFound
						|| !ConjectureCEOut.equals(DriverCEOut);

				LogManager
						.logInfo("geting smallest suffix in counter example which is not in W and not a prefix of a W element");
				int traceSize = dataManager.traceSize();
				int l = 0;
				InputSequence newW;
				boolean newWIsPrefixInW;
				do {
					l++;
					if (l <= counterExample.getLength()) {
						newW = counterExample.getIthSuffix(l);
					} else {
						newW = dataManager.getSubtrace(
								traceSize - l + counterExample.getLength(),
								traceSize).getInputsProjection();
						newW.addInputSequence(counterExample);
					}
					assert (newW.getLength() == l);
					newWIsPrefixInW = false;
					for (InputSequence w : W) {
						if (w.startsWith(newW)) {
							newWIsPrefixInW = true;
							break;
						}

					}
				} while (newWIsPrefixInW || W.contains(newW));
				if (driver instanceof TransparentMealyDriver
						&& ((TransparentMealyDriver) driver).getAutomata()
								.acceptCharacterizationSet(W)) {
					LogManager
							.logWarning("We are adding new element to W but it is already a W-set for this driver");
				}
				for (InputSequence w : W) {
					if (newW.startsWith(w)) {
						W.remove(w);
						LogManager
								.logInfo("removing "
										+ w
										+ " from W-set because it's a prefix of new inputSequence");
						break;
					}
				}
				LogManager.logInfo("W-set extended with " + newW);
				W.add(newW);
			}
			counterExample = null;
			inconsistencyFound = false;
			LogManager.logStep(LogManager.STEPOTHER, "Starting new learning");
			try {
				learn(W, h);
			} catch (InvalidHException e) {
				LogManager
						.logInfo("Non-determinism found (due to homming sequence) : "
								+ e);
				h = e.getNewH();
				inconsistencyFound = true;
			} catch (ConjectureNotConnexException e) {
				LogManager
						.logInfo("The conjecture is not connex. We stop here and look for a counter example");
			} catch (InconsistancyWithConjectureException e) {
				LogManager.logInfo("Non-determinism found : " + e);
				counterExample = new InputSequence();
				inconsistencyFound = true;
			}
		}
		if ((counterExample = getCounterExemple()) != null) {
			LogManager.logError("another counter example can be found");
			throw new RuntimeException("wrong conjecture");
		} else {
			LogManager
					.logInfo("no counter example can be found, this almost mean that the conjecture is correct"
							+ " (more precisely, this mean we are in a sub part of the automata which is equivalent to the driver)");
		}
		// The transition count should be stopped
		driver.stopLog();
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			if (checkExact(d.getAutomata(), d.getCurrentState())) {
				LogManager.logConsole("The computed conjecture is exact");
				LogManager.logInfo("The computed conjecture is exact");
			} else {
				LogManager.logConsole("The computed conjecture is not correct");
				LogManager.logInfo("The computed conjecture is not correct");
			}
		} else {
			if (checkRandomWalk()) {
				LogManager
						.logConsole("The computed conjecture seems to be consistent with the driver");
				LogManager
						.logInfo("The computed conjecture seems to be consistent with the driver");
			} else {
				LogManager.logConsole("The computed conjecture is not correct");
				LogManager.logInfo("The computed conjecture is not correct");
			}
		}

	}

	public void learn(List<InputSequence> W, InputSequence h) {
		LogManager.logStep(LogManager.STEPOTHER, "Inferring the system");
		LogManager.logConsole("Inferring the system with W=" + W + ", h=" + h
				+ " and n=" + Options.STATE_NUMBER_BOUND);

		n = Options.STATE_NUMBER_BOUND;
		stats = new NoResetStatsEntry(W, driver, n);

		this.W = W;
		StringBuilder logW = new StringBuilder("Using characterization set : [");
		for (InputSequence w : this.W) {
			logW.append(w + ", ");
		}
		logW.append("]");
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
			if (tDriver.getAutomata().acceptCharacterizationSet(W)) {
				logW.append(" (which is a W-set for the driver)");
			} else {
				logW.append(" (which is not a W-set for the driver)");
			}
		}
		LogManager.logInfo(logW.toString());
		LogManager
				.logInfo("Using homing sequence «"
						+ h
						+ "»"
						+ ((driver instanceof TransparentMealyDriver) ? (((TransparentMealyDriver) driver)
								.getAutomata().acceptHomingSequence(h) ? " (which is a homming sequence for driver)"
								: " (which is not a homing sequence for driver)")
								: ""));

		long start = System.nanoTime();

		// GlobalTrace trace = new GlobalTrace(driver);
		dataManager = new DataManager(driver, this.W, h, n);

		// start of the algorithm
		localize(dataManager);

		while (!dataManager.isFullyKnown()) {
			Runtime runtime = Runtime.getRuntime();
			// runtime.gc(); //The garbage collection takes time and induce
			// wrong measurement of duration
			stats.updateMemory((int) (runtime.totalMemory() - runtime
					.freeMemory()));

			LogManager.logLine();
			int qualifiedStatePos;
			LmTrace sigma;
			if (dataManager.getC(dataManager.traceSize()) != null) {
				FullyQualifiedState q = dataManager.getC(dataManager
						.traceSize());
				LogManager.logInfo("We already know the current state (q = "
						+ q + ")");
				InputSequence alpha = dataManager.getShortestAlpha(q);
				dataManager.apply(alpha);
				assert dataManager.updateCKVT();
				assert dataManager.getC(dataManager.traceSize()) != null;
				qualifiedStatePos = dataManager.traceSize();
				FullyQualifiedState quallifiedState = dataManager
						.getC(qualifiedStatePos);
				Set<String> X = dataManager.getxNotInR(quallifiedState);
				if (X.isEmpty()) {
					LogManager
							.logInfo("We discovered the missing transition when we applied alpha");
					continue;// we already are in a known state (because we
								// applied alpha) so we didn't need to localize
				}
				String x = X.iterator().next(); // here we CHOOSE to take the
												// first
				LogManager.logInfo("We choose x = " + x + " in " + X);
				String o = dataManager.apply(x);
				sigma = new LmTrace(x, o);
				LogManager.logInfo("So sigma = " + sigma);
				if (dataManager.getC(dataManager.traceSize()) != null) {
					// this is a corner case when h and W are empty.
					LogManager
							.logInfo("We are in corner case of implementation because W is empty.");
					assert h.getLength() == 0;
					continue;
				}
				assert dataManager.getC(dataManager.traceSize()) == null : "we are trying to qualify this state, that should not be already done.";
			} else {
				LogManager.logInfo("We don't know the current state");
				qualifiedStatePos = dataManager.traceSize() - 1;
				while (dataManager.getC(qualifiedStatePos) == null)
					qualifiedStatePos--;
				LogManager.logInfo("last qualified state is "
						+ dataManager.getC(qualifiedStatePos));
				sigma = dataManager.getSubtrace(qualifiedStatePos,
						dataManager.traceSize());
				LogManager.logInfo("We got sigma = " + sigma);
			}
			FullyQualifiedState q = dataManager.getC(qualifiedStatePos);
			List<InputSequence> allowed_W = dataManager.getwNotInK(q, sigma);
			InputSequence w = new InputSequence();
			if (W.size() != 0)
				w = allowed_W.get(0); // here we CHOOSE to take the
										// first.
			if (Options.LOG_LEVEL != Options.LogLevel.LOW)
				LogManager.logInfo("We choose w = " + w + " in " + allowed_W);
			int newStatePos = dataManager.traceSize();
			dataManager.apply(w);
			if (Options.LOG_LEVEL != Options.LogLevel.LOW)
				LogManager.logInfo("We found that "
						+ q
						+ " followed by "
						+ sigma
						+ "gives "
						+ dataManager.getSubtrace(newStatePos,
								dataManager.traceSize()));
			dataManager.addPartiallyKnownTrace(
					q,
					sigma,
					dataManager.getSubtrace(newStatePos,
							dataManager.traceSize()));
			if (Options.TEST)
				dataManager.updateCKVT();
			if (dataManager.getC(dataManager.traceSize()) == null) {
				localize(dataManager);
				if (Options.TEST)
					dataManager.updateCKVT();
			}
			assert dataManager.updateCKVT();
		}
		float duration = (float) (System.nanoTime() - start) / 1000000000;
		stats.setDuration(duration);
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		stats.updateMemory((int) (runtime.totalMemory() - runtime.freeMemory()));
		stats.setTraceLength(dataManager.traceSize());
		stats.updateWithConjecture(dataManager.getConjecture());
		if (Options.LOG_LEVEL == Options.LogLevel.ALL || Options.TEST)
			LogManager.logConsole(dataManager.readableTrace());
		dataManager.getConjecture().exportToDot();
		if (driver instanceof TransparentMealyDriver) {
			int minTraceLength = AdenilsoSimaoTool
					.minLengthForExhaustivAutomata(
							((TransparentMealyDriver) driver).getAutomata(),
							dataManager.getTrace().getInputsProjection());
			if (minTraceLength > dataManager.traceSize())
				throw new RuntimeException(
						"error in learning, there is another automata which produce the same trace");
			stats.setMinTraceLength(minTraceLength);
		} else {
			stats.setMinTraceLength(-2);
		}
	}

	public LmConjecture createConjecture() {
		LmConjecture c = dataManager.getConjecture();
		LogManager.logInfo("Conjecture has " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		return c;
	}

	public NoResetStatsEntry getStats() {
		return stats;
	}

	/**
	 * 
	 * @param trace
	 *            omega the global trace of the automata, will be completed \in
	 *            (IO)*
	 * @param inputSequences
	 *            a subset of the characterization state \subset W \subset I*
	 * @return the position of the fully identified state in the GlobalTrace
	 */
	private int localize(DataManager dataManager) {
		int startTracePos = dataManager.traceSize();
		LogManager.logInfo("Localizing...");
		FullyQualifiedState s = null;
		int statePos = dataManager.traceSize();
		while (s == null) {
			LogManager.logInfo("Applying h to localize (h=" + dataManager.h
					+ ")");
			OutputSequence hResponse = dataManager.apply(dataManager.h);
			statePos = dataManager.traceSize();
			s = dataManager.getState(hResponse);
			if (s != null) {
				LogManager.logInfo("We know that after h, the answer "
						+ hResponse + " means we arrived in state " + s);
				break;
			}
			InputSequence missingW = dataManager
					.getMissingInputSequence(hResponse);
			LogManager.logInfo("We don't know were we are. We apply sequence "
					+ missingW + " from W-set");
			OutputSequence wResponse = dataManager.apply(missingW);
			s = dataManager.addWresponseAfterH(hResponse, missingW, wResponse);
		}
		dataManager.setC(statePos, s);

		stats.setLocalizeSequenceLength(dataManager.traceSize() - startTracePos);
		stats.increaseLocalizeCallNb();
		return statePos;

	}

	private List<OutputSequence> localize_intern(DataManager dataManager,
			List<InputSequence> inputSequences) {
		if (inputSequences.size() == 1) {
			List<OutputSequence> WResponses = new ArrayList<OutputSequence>();
			WResponses.add(dataManager.apply(inputSequences.get(0)));
			return WResponses;
		}
		LogManager.logInfo("Localizer : Localize with " + inputSequences);

		ArrayList<InputSequence> Z1 = new ArrayList<InputSequence>(
				inputSequences);
		Z1.remove(Z1.size() - 1);
		ArrayList<List<OutputSequence>> localizerResponses = new ArrayList<List<OutputSequence>>();
		if (Options.LOG_LEVEL != Options.LogLevel.LOW)
			LogManager.logInfo("Localizer : Applying " + (2 * n - 1)
					+ " times localize(" + Z1 + ")");
		for (int i = 0; i < 2 * n - 1; i++) {
			localizerResponses.add(localize_intern(dataManager, Z1));
		}

		int j = n;
		boolean isLoop = false;
		while (!isLoop) {
			j--;
			assert (j >= 0) : "no loop was found";
			isLoop = true;
			for (int m = 0; m < n - 1; m++) {
				if (!localizerResponses.get(j + m).equals(
						localizerResponses.get(n + m))) {
					isLoop = false;
					if (Options.LOG_LEVEL != Options.LogLevel.LOW)
						LogManager.logInfo("Tried size " + (n - j)
								+ " : it's not a loop : [" + (j + m) + "] = ("
								+ Z1 + " → " + localizerResponses.get(j + m)
								+ ") ≠ [" + (n + m) + "] = (" + Z1 + " → "
								+ localizerResponses.get(n + m) + ")");
					break;
				}
			}
		}
		if (Options.LOG_LEVEL != Options.LogLevel.LOW)
			LogManager.logInfo("Localizer : Found a loop of size " + (n - j));
		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			LogManager
					.logInfo("Localizer : We know that applying localize_intern("
							+ Z1
							+ ") will produce "
							+ localizerResponses.get(j + n - 1));

		List<OutputSequence> WResponses = localizerResponses.get(j + n - 1);
		List<InputSequence> Z2 = new ArrayList<InputSequence>(Z1);
		Z2.remove(Z2.size() - 1);
		Z2.add(inputSequences.get(inputSequences.size() - 1));
		List<OutputSequence> Z2Responses = localize_intern(dataManager, Z2);
		WResponses.add(Z2Responses.get(Z2Responses.size() - 1));
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < inputSequences.size(); i++) {
			s.append(new LmTrace(inputSequences.get(i), WResponses.get(i))
					+ ", ");
		}
		if (Options.LOG_LEVEL != Options.LogLevel.LOW)
			LogManager.logInfo("Localizer : Before "
					+ inputSequences.get(inputSequences.size() - 1)
					+ " we were in " + s);
		assert WResponses.size() == inputSequences.size();
		return WResponses;
	}

	private boolean checkRandomWalk() {
		LogManager.logStep(LogManager.STEPOTHER,
				"checking the computed conjecture with Random Walk");
		NoResetMealyDriver generatedDriver = new NoResetMealyDriver(
				dataManager.getConjecture());
		generatedDriver.stopLog();
		generatedDriver.setCurrentState(dataManager.getC(
				dataManager.traceSize()).getState());

		// Now the two automata are in same state.
		// We can do a random walk

		int max_try = driver.getInputSymbols().size() * n * 10;
		dataManager = null;// we use directly the driver for the walk so
							// dataManager is not up to date;
		driver.stopLog();
		for (int j = 0; j < max_try; j++) {
			int rand = Utils.randInt(driver.getInputSymbols().size());
			String input = driver.getInputSymbols().get(rand);
			if (!driver.execute(input).equals(generatedDriver.execute(input)))
				return false;
		}

		return true;
	}

	/**
	 * check if the computed conjecture is equivalent to the given automata the
	 * control is made by walking in the given automata in order to follow each
	 * transitions and comparing the two outputs and checking that only one
	 * state of th conjecture can be associated to a state of the given
	 * automata.
	 * 
	 * @param automata
	 *            a connex automata
	 * @param currentState
	 *            the state in the automata which is supposed to be equivalent
	 *            to the current state of the driver
	 * @return true if the two automata are equivalent
	 */
	public boolean checkExact(Mealy automata, State currentState) {
		LogManager.logStep(LogManager.STEPOTHER,
				"checking the computed conjecture is exactly equivalent");
		class FoundState {
			public State computedState; // a state in the conjecture
			public List<String> uncheckedTransitions;

			public FoundState(State s, List<String> I) {
				computedState = s;
				uncheckedTransitions = new ArrayList<String>(I);
			}

			public String toString() {
				return computedState + " but transitions "
						+ uncheckedTransitions + "have not been checked";
			}
		}
		// currentFoundState is maintained in order that
		// currentFoundState.computedState is the current state in conjecture
		FoundState currentFoundState = new FoundState(dataManager.getC(
				dataManager.traceSize()).getState(), driver.getInputSymbols());
		// assigned is a table to associate a FoundState to each state in the
		// given automata
		Map<State, FoundState> assigned = new HashMap<State, FoundState>();
		assigned.put(currentState, currentFoundState);
		State uncheckedState = currentState; // a state with an unchecked
												// transition
		List<String> path = new ArrayList<String>();// the path from the current
													// state to uncheckeState

		// now we iterate over all unchecked transitions
		while (uncheckedState != null) {
			FoundState uncheckedFoundState = assigned.get(uncheckedState);
			LogManager.logInfo("Applying " + path + "in order to go in state "
					+ uncheckedState + " and then try "
					+ uncheckedFoundState.uncheckedTransitions.get(0));
			path.add(uncheckedFoundState.uncheckedTransitions.get(0));

			// we follow path in driver (the conjecture) and the given automata
			for (String i : path) {
				currentFoundState.uncheckedTransitions.remove(i);
				MealyTransition t = automata.getTransitionFromWithInput(
						currentState, i);
				currentState = t.getTo();
				String o = dataManager.apply(i);
				if (!o.equals(t.getOutput())) {
					LogManager.logInfo("expected output was " + t.getOutput());
					return false;
				}
				currentFoundState = assigned.get(currentState);
				if (currentFoundState == null) {
					currentFoundState = new FoundState(dataManager.getC(
							dataManager.traceSize()).getState(),
							driver.getInputSymbols());
					assigned.put(currentState, currentFoundState);
				} else if (currentFoundState.computedState != dataManager.getC(
						dataManager.traceSize()).getState()) {
					LogManager.logInfo("it was expected to arrive in "
							+ t.getTo());
					return false;
				}
			}
			// now we've applied an unchecked transition (which is now checked)

			// and then we compute a new path to go to another state with
			// unchecked transitions
			class Node {
				public List<String> path;
				public State state;
			}
			LinkedList<Node> nodes = new LinkedList<Node>();
			Node node = new Node();
			node.path = new ArrayList<String>();
			node.state = currentState;
			nodes.add(node);

			Map<State, Boolean> crossed = new HashMap<State, Boolean>();// this
																		// map
																		// is
																		// used
																		// to
																		// store
																		// the
																		// node
																		// crossed
																		// during
																		// the
																		// path
																		// searching
																		// (avoid
																		// going
																		// to
																		// the
																		// same
																		// state
																		// by
																		// two
																		// different
																		// path)
			for (State s : automata.getStates())
				crossed.put(s, false);
			uncheckedState = null;
			path = null;
			while (!nodes.isEmpty()) {
				node = nodes.pollFirst();
				if (!assigned.get(node.state).uncheckedTransitions.isEmpty()) {
					uncheckedState = node.state;
					path = node.path;
					break;
				}
				if (crossed.get(node.state))
					continue;
				for (String i : driver.getInputSymbols()) {
					Node newNode = new Node();
					newNode.path = new ArrayList<String>(node.path);
					newNode.path.add(i);
					newNode.state = automata.getTransitionFromWithInput(
							node.state, i).getTo();
					nodes.add(newNode);
					crossed.put(node.state, true);
				}
			}
		}

		return true;
	}

	private static List<InputSequence> computeCharacterizationSet(
			MealyDriver driver) {
		if (driver instanceof TransparentMealyDriver) {
			return computeCharacterizationSet((TransparentMealyDriver) driver);
		} else {
			throw new RuntimeException("unable to compute W");
		}
	}

	private static List<InputSequence> computeCharacterizationSet(
			TransparentMealyDriver driver) {
		LogManager.logStep(LogManager.STEPOTHER,
				"computing characterization set");
		Mealy automata = driver.getAutomata();
		List<InputSequence> W = new ArrayList<InputSequence>();
		List<State> distinguishedStates = new ArrayList<State>();
		for (State s1 : automata.getStates()) {
			if (Options.LOG_LEVEL != Options.LogLevel.LOW)
				LogManager.logInfo("adding state " + s1);
			for (State s2 : distinguishedStates) {
				boolean haveSameOutputs = true;
				for (InputSequence w : W) {
					if (!apply(w, automata, s1).equals(apply(w, automata, s2))) {
						haveSameOutputs = false;
					}
				}
				if (haveSameOutputs) {
					if (Options.LOG_LEVEL != Options.LogLevel.LOW)
						LogManager.logInfo(s1 + " and " + s2
								+ " have the same outputs for W=" + W);
					addDistinctionSequence(automata, driver.getInputSymbols(),
							s1, s2, W);
					if (Options.LOG_LEVEL == Options.LogLevel.ALL)
						LogManager.logInfo("W is now " + W);
				}
			}
			distinguishedStates.add(s1);
		}
		if (automata.getStateCount() == 1)
			W.add(new InputSequence(driver.getInputSymbols().get(0)));
		return W;
	}

	/**
	 * compute a distinction sequence for the two states it may be a new
	 * distinction sequence or an append of an existing distinction sequence
	 * 
	 * @param automata
	 * @param inputSymbols
	 * @param s1
	 * @param s2
	 * @param w2
	 */
	private static void addDistinctionSequence(Mealy automata,
			List<String> inputSymbols, State s1, State s2, List<InputSequence> W) {
		// first we try to add an input symbol to the existing W
		for (InputSequence w : W) {
			for (String i : inputSymbols) {
				InputSequence testw = new InputSequence();
				testw.addInputSequence(w);
				testw.addInput(i);
				if (!apply(testw, automata, s1).equals(
						apply(testw, automata, s2))) {
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
			if (apply(testw, automata, s1).equals(apply(testw, automata, s2))) {
				if (testw.getLength() > automata.getStateCount()) {
					LogManager.logInfo("unable to get find a w to distinguish "
							+ s1 + " and " + s2
							+ ".Those states may be equivalent");
					throw new RuntimeException(
							"unable to distinguish two states for W set");
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

	// Initial attempt to compute a better W set, but currently disused because
	// inefficient
	private static List<InputSequence> computeCharacterizationSetNaiv(
			TransparentMealyDriver driver) {
		LogManager.logStep(LogManager.STEPOTHER,
				"computing characterization set");
		Mealy automata = driver.getAutomata();
		automata.exportToDot();
		List<InputSequence> W = new ArrayList<InputSequence>();
		for (String i : driver.getInputSymbols()) {
			W.add(new InputSequence(i));
		}
		boolean isCaracterizationSet = false;
		while (!isCaracterizationSet) {
			if (Options.LOG_LEVEL == Options.LogLevel.ALL)
				LogManager.logInfo("computing caracterization set : W is now "
						+ W);
			isCaracterizationSet = true;
			for (State s1 : automata.getStates()) {
				for (State s2 : automata.getStates()) {
					if (s1.equals(s2))
						break;// we do not need to test s1 -> s2 AND s2 -> s1
					List<InputSequence> haveSameOutput = new ArrayList<InputSequence>();// the
																						// W
																						// elements
																						// for
																						// which
																						// s1
																						// and
																						// s2
																						// have
																						// the
																						// same
																						// output
					for (InputSequence w : W) {
						if (apply(w, automata, s1).equals(
								apply(w, automata, s2))) {
							haveSameOutput.add(w);
						}
					}
					if (haveSameOutput.size() == W.size()) {
						isCaracterizationSet = false;
						InputSequence toSplit = haveSameOutput.get(0);// here we
																		// choose
																		// to
																		// take
																		// the
																		// first
																		// element
																		// so it
																		// may
																		// be
																		// interesting
																		// to
																		// randomize
																		// that
						W.remove(toSplit);
						for (String i : driver.getInputSymbols()) {
							InputSequence newW = new InputSequence();
							newW.addInputSequence(toSplit);
							newW.addInput(i);
							if (!apply(newW, automata, s1).equals(
									apply(newW, automata, s2))) {
								W.add(newW);
								break;
							}
						}
						for (String i : driver.getInputSymbols()) {
							InputSequence newW = new InputSequence();
							newW.addInputSequence(toSplit);
							newW.addInput(i);
							W.add(newW);
						}
					}
				}
			}
		}

		return W;
	}

	private static OutputSequence apply(InputSequence I, Mealy m, State s) {
		OutputSequence O = new OutputSequence();
		for (String i : I.sequence) {
			MealyTransition t = m.getTransitionFromWithInput(s, i);
			s = t.getTo();
			O.addOutput(t.getOutput());
		}
		return O;
	}

}
