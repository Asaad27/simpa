package learner.mealy.hW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import learner.mealy.hW.dataManager.ConjectureNotConnexException;
import learner.mealy.hW.dataManager.FullyQualifiedState;
import learner.mealy.hW.dataManager.InconsistancyHMappingAndConjectureException;
import learner.mealy.hW.dataManager.InconsistancyWithConjectureAtEndOfTraceException;
import learner.mealy.hW.dataManager.InvalidHException;
import learner.mealy.hW.dataManager.SimplifiedDataManager;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.Utils;

import tools.loggers.LogManager;

public class HWLearner extends Learner {
	private MealyDriver driver;
	private SimplifiedDataManager dataManager;
	private HWStatsEntry stats;
	protected List<InputSequence> W;
	private int n;// the maximum number of states
	private LmTrace fullTrace;

	public HWLearner(MealyDriver d) {
		driver = d;
	}

	/**
	 * If some sequence are applied on driver but not returned, the datamanager
	 * is also updated.
	 * 
	 * @return the trace applied on driver or null if no counter example is
	 *         found
	 */
	public LmTrace getCounterExemple() {
		int startSize = dataManager.traceSize();
		long startTime = System.nanoTime();
		LmTrace returnedCE = null;
		if (Options.USE_SHORTEST_CE) {
			stats.setOracle("shortest");
			returnedCE = getShortestCounterExemple();
		} else {
			stats.setOracle("MrBean");
			LmTrace shortestCe = (driver instanceof TransparentMealyDriver) ? getShortestCounterExemple(false)
					: new LmTrace();
			returnedCE = (shortestCe == null) ? null
					: getRandomCounterExemple();// we do not compute random CE
												// if we know that there is no
												// CE
		}
		float duration = (float) (System.nanoTime() - startTime) / 1000000000;
		stats.increaseOracleCallNb(dataManager.traceSize() - startSize
				+ ((returnedCE == null) ? 0 : returnedCE.size()), duration);
		return returnedCE;
	}

	public LmTrace getRandomCounterExemple() {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			Options.MAX_CE_LENGTH = d.getAutomata().getStateCount() *d.getAutomata().getStateCount() * 100;
		}
		LmConjecture conjecture = dataManager.getConjecture();
		State conjectureStartingState = dataManager.getCurrentState()
				.getState();
		LmTrace ce = new LmTrace();
		boolean found = driver.getRandomCounterExample_noReset(conjecture,
				conjectureStartingState, ce);
		if (!found)
			dataManager.walkWithoutCheck(ce);
		return found ? ce : null;
	}

	public LmTrace getShortestCounterExemple() {
		return getShortestCounterExemple(true);
	}

	/**
	 * Add a new sequence in a W-set. If an element of W is a prefix of new
	 * sequence, it is replaced by the new sequence.
	 * 
	 * @param newW
	 *            The sequence to add in W. This is not allowed to be a prefix
	 *            of an existing sequence in W
	 * @param W
	 *            The set to extend
	 */
	private void addOrExtendInW(InputSequence newW, List<InputSequence> W) {
		for (InputSequence w : W) {
			assert (!w.startsWith(newW));
			if (newW.startsWith(w) || w.getLength() == 0) {
				W.remove(w);
				LogManager
						.logInfo("removing "
								+ w
								+ " from W-set because it's a prefix of new inputSequence");
				break;// because W is only extended with this method, there is
						// at most one prefix of newW in W (otherwise, one
						// prefix of newW in W is also a prefix of the other
						// prefix of newW in W, which is not possible by
						// construction of W)
			}
		}
		LogManager.logInfo("W-set extended with " + newW);
		W.add(newW);
	}
	public LmTrace getShortestCounterExemple(boolean applyOndriver) {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			Mealy realAutomata = d.getAutomata();
			LmConjecture conjecture = dataManager.getConjecture();
			State conjectureStartingState = dataManager.getCurrentState()
					.getState();
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
				if (applyOndriver)
					return new LmTrace(counterExample,
							driver.execute(counterExample));
				else
					return new LmTrace(counterExample,
							realAutomata.simulateOutput(realStartingState,
									counterExample));
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
		fullTrace = new LmTrace();
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long start = System.nanoTime();
		
		List<InputSequence> W = new ArrayList<InputSequence>();
		W.add(new InputSequence());
		InputSequence h = new InputSequence();
		stats = new HWStatsEntry(driver);

		LmTrace counterExampleTrace;
		boolean inconsistencyFound;

		do {
			stats.updateMemory((int) (runtime.totalMemory() - runtime
					.freeMemory()));

			counterExampleTrace = null;
			inconsistencyFound = false;
			LogManager.logLine();
			LogManager.logStep(LogManager.STEPOTHER, "Starting new learning");
			try {
				try {
					learn(W, h);
				} catch (ConjectureNotConnexException e) {
					checkInconsistencyHMapping();
					throw e;
				}
				checkInconsistencyHMapping();
			} catch (InvalidHException e) {
				stats.increaseHInconsitencies();
				if (Options.LOG_LEVEL != LogLevel.LOW) {
					LogManager
							.logInfo("Non-determinism found (due to homming sequence) : "
									+ e);
					LogManager
							.logConsole("Non-determinism found (due to homming sequence) : "
									+ e);
				}
				h = e.getNewH();
				LogManager.logInfo("h is now " + h);
				if (Options.ADD_H_IN_W) {
					boolean hIsInW = false;
					for (InputSequence w : W) {
						if (w.startsWith(h)) {
							hIsInW = true;
							break;
						}
					}
					if (!hIsInW)
						addOrExtendInW(h, W);
				}
				inconsistencyFound = true;
			} catch (ConjectureNotConnexException e) {
				if (Options.LOG_LEVEL != LogLevel.LOW) {
					LogManager
							.logInfo("The conjecture is not connex. We stop here and look for a counter example");

					LogManager
							.logConsole("The conjecture is not connex. We stop here and look for a counter example");
				}
			} catch (InconsistancyWithConjectureAtEndOfTraceException e) {
				stats.increaseWInconsistencies();
				if (Options.LOG_LEVEL != LogLevel.LOW) {
					LogManager.logInfo("Non-determinism found : " + e);
					LogManager.logConsole("Non-determinism found : " + e);
				}
				counterExampleTrace = new LmTrace();
				inconsistencyFound = true;
			}

			if (!inconsistencyFound && Options.TRY_TRACE_AS_CE) {
				// try to apply fullTrace on any state of conjecture to detect
				// inconsistency.
				LmConjecture conjecture = dataManager.getConjecture();
				if (conjecture.isConnex()) {
					int firstDiff = conjecture.simulateOnAllStates(fullTrace);
					if (firstDiff != fullTrace.size()) {
						counterExampleTrace = fullTrace.subtrace(0,
								firstDiff + 1);
						LogManager
								.logInfo("The trace from start of learning cannot be applied on any state of conjecture."
										+ "We will try to use it as a counter example.");
						OutputSequence ConjectureCEOut = conjecture
								.simulateOutput(dataManager.getCurrentState()
										.getState(), counterExampleTrace
										.getInputsProjection());
						OutputSequence DriverCEOut = driver.execute(counterExampleTrace
								.getInputsProjection());
						if (ConjectureCEOut.equals(DriverCEOut)){
							LogManager.logInfo("When trying to apply the expected counter example, the driver produced the same output than the conjecture."+"This cannot be used as counter example");
							dataManager.walkWithoutCheck(counterExampleTrace);
							counterExampleTrace=null;
						}else{
							LogManager.logInfo("The trace was a counter example");
							inconsistencyFound = true;
						}
					} else {
						LogManager
								.logInfo("The trace from start of learning can be applied on at leat one state");
					}
				}
			}

			if (!inconsistencyFound) {
				LogManager.logInfo("asking for a counter example");
				counterExampleTrace = getCounterExemple();
				if (counterExampleTrace == null)
					LogManager.logInfo("No counter example found");
				else {
					LogManager.logInfo("one counter example found : "
							+ counterExampleTrace);
					if (Options.LOG_LEVEL != LogLevel.LOW)
						LogManager.logConsole("one counter example found : "
								+ counterExampleTrace);
				}

			}

			if (counterExampleTrace != null) {


				LogManager
						.logInfo("geting smallest suffix in counter example which is not in W and not a prefix of a W element");
				int traceSize = dataManager.traceSize();
				int l = 0;
				InputSequence newW;
				boolean newWIsPrefixInW;
				do {
					InputSequence counterExample = counterExampleTrace
							.getInputsProjection();
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
				addOrExtendInW(newW, W);
				dataManager.walkWithoutCheck(counterExampleTrace);
			}
			stats.increaseWithDataManager(dataManager);
		} while (counterExampleTrace != null || inconsistencyFound);

		float duration = (float) (System.nanoTime() - start) / 1000000000;
		stats.setDuration(duration);
		stats.setSearchCEInTrace(Options.TRY_TRACE_AS_CE?"naive":"none");
		stats.setAddHInW(Options.ADD_H_IN_W);
		stats.setCheck3rdInconsistency(
				Options.CHECK_INCONSISTENCY_H_NOT_HOMING);
	
		stats.updateMemory((int) (runtime.totalMemory() - runtime.freeMemory()));
		stats.finalUpdate(dataManager);

		// The transition count should be stopped
		driver.stopLog();

		if (driver instanceof TransparentMealyDriver) {
			if ((counterExampleTrace = getShortestCounterExemple()) != null) {
				dataManager.walkWithoutCheck(counterExampleTrace);
				LogManager.logError("another counter example can be found");
				throw new RuntimeException("wrong conjecture");
			} else {
				LogManager
						.logInfo("no counter example can be found, this almost mean that the conjecture is correct"
								+ " (more precisely, this mean we are in a sub part of the automata which is equivalent to the driver)");
			}
		} else {
			LogManager
					.logInfo("black box is not transparent. cannot do a real verification of conjecture");
		}
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

	private void checkInconsistencyHMapping() {
		if (!Options.CHECK_INCONSISTENCY_H_NOT_HOMING)
			return;
		LogManager.logInfo(
				"Checking for inconsistencies between conjecture and h");
		LinkedList<State> reachableStates = new LinkedList<>();
		reachableStates.add(dataManager.getCurrentState().getState());
		Set<State> seenStates = new HashSet<>();
		// knownResponses is a mapping with the structure :
		// [observed h answer] -> (mapping [final state] -> [start state])
		// this record for each answer what are the possible final state and how
		// to reach them.
		Map<OutputSequence, Map<State, List<State>>> knownResponses = new HashMap<>();
		while (!reachableStates.isEmpty()) {
			State triedState = reachableStates.poll();
			if (seenStates.contains(triedState))
				continue;
			seenStates.add(triedState);
			try {
				if (dataManager.isCompatibleWithHMapping(triedState) == null) {
					LogManager.logInfo("from state " + triedState
							+ ", homing sequence produce a response unknown from mapping");
				}
			} catch (InconsistancyHMappingAndConjectureException e) {
				String INC_NAME = "3rd inconsistency";
				if (Options.LOG_LEVEL != LogLevel.LOW)
					LogManager.logInfo("Inconsistency found :" + e);
				InputSequence distinctionW = e.getDistinctionSequence();
				if (distinctionW == null) {
					LogManager.logWarning(
							"inconsistency can not be used because the state reached after homing sequence ("
									+ e.getStateAfterH()
									+ ")  has the same transitions than caracterization of mapped State ("
									+ e.getMappedTarget() + ").");
				} else {
					assert W.contains(distinctionW);
					LogManager.logInfo(
							INC_NAME + " : first placing conjecture in state "
									+ e.getStateBeforeH());
					assert e.getStateBeforeH() == triedState;
					State currentState = dataManager.getCurrentState()
							.getState();
					dataManager.apply(dataManager.getConjecture()
							.getShortestPath(currentState, e.getStateBeforeH())
							.getInputsProjection());// path exists because
													// triedState is reachable.
					currentState = dataManager.getCurrentState().getState();
					assert currentState == e.getStateBeforeH();
					LogManager.logInfo(
							INC_NAME + " : then apply homing sequence");
					dataManager.apply(dataManager.h);
					currentState = dataManager.getCurrentState().getState();
					assert currentState == e.getStateAfterH();
					LogManager.logInfo(INC_NAME
							+ " : finally, apply distinction sequence '"
							+ distinctionW
							+ "' in order to raise an inconsistency of type one or two");
					;
					dataManager.apply(distinctionW);
					throw new RuntimeException(
							"an inconsistency of type one or two was expected");
				}
			}
			LmConjecture conjecture = dataManager.getConjecture();
			OutputSequence hResponse = conjecture.apply(dataManager.h,
					triedState);
			State endState = conjecture.applyGetState(dataManager.h,
					triedState);
			Map<State, List<State>> sameAnswerMap = knownResponses
					.get(hResponse);
			if (sameAnswerMap == null) {
				sameAnswerMap = new HashMap<>();
				knownResponses.put(hResponse, sameAnswerMap);
				List<State> list = new ArrayList<>();
				list.add(triedState);
				sameAnswerMap.put(endState, list);
			} else {
				List<State> list = sameAnswerMap.get(endState);
				if (list == null) {
					list = new ArrayList<>();
					sameAnswerMap.put(endState, list);
				}
				list.add(triedState);

				for (State otherEnd : sameAnswerMap.keySet()) {
					if (otherEnd == endState)
						continue;
					List<State> otherStarts = sameAnswerMap.get(otherEnd);
					assert otherStarts != null && otherStarts.size() >= 1;
					for (State otherStart : otherStarts) {
						proceedHNotHomingforConjecture(otherStart, triedState);
					}
				}
			}
			for (MealyTransition t : dataManager.getConjecture()
					.getTransitionFrom(triedState)) {
				if (!seenStates.contains(t.getTo()))
					reachableStates.add(t.getTo());
			}
		}
		LogManager.logInfo("no inconsistencies discovered");
	}

	/**
	 * Use a detected inconsistency showing that h is not a homing sequence for
	 * conjecture and try to transform this into an inconsistency of type one or
	 * two.
	 * 
	 * This function suppose that the reachable part of conjecture is complete.
	 * 
	 * @param aStart
	 *            a reachable State giving the same answer than bStart to h but
	 *            leading in a different state.
	 * @param bStart
	 *            a reachable State giving the same answer than aStart to h but
	 *            leading in a different state.
	 */
	private void proceedHNotHomingforConjecture(State aStart, State bStart) {
		assert aStart != bStart;
		LmConjecture conjecture = dataManager.getConjecture();
		State currentState = dataManager.getCurrentState().getState();
		assert conjecture.apply(dataManager.h, aStart)
				.equals(conjecture.apply(dataManager.h, bStart));
		assert conjecture.getShortestPath(currentState, aStart) != null
				&& conjecture.getShortestPath(currentState,
						bStart) != null : "states are supposed to be reachable";
		State aEnd = conjecture.applyGetState(dataManager.h, aStart);
		State bEnd = conjecture.applyGetState(dataManager.h, bStart);
		assert aEnd != bEnd;

		if (Options.LOG_LEVEL != LogLevel.LOW) {
			LogManager.logInfo(
					"New inconsistency found : h is not homming for this conjecture. In conjecture, from states "
							+ aStart + " and " + bStart
							+ " we can observe the same answer to homing sequence '"
							+ conjecture.apply(dataManager.h, aStart)
							+ "' but this lead in two differents states " + aEnd
							+ " and " + bEnd
							+ ". Now we try to use this inconsistency");
		}

		List<InputSequence> distinctionSequences = new ArrayList<>();
		distinctionSequences.addAll(W);
		// for the case were aEnd and bEnd have the same characterizing
		// transitions, we try to find a custom distinction sequence. This can
		// be improved in order to try a sequence which distinguish the two
		// state and allow a path from one state to the other.
		InputSequence distinctionSequence = conjecture
				.getDistinctionSequence(aEnd, bEnd);
		if (distinctionSequence != null) {
			distinctionSequences.add(distinctionSequence);
		}
		boolean statesAreEquivalents = true;
		for (InputSequence w : distinctionSequences) {
			if (!conjecture.apply(w, aEnd).equals(conjecture.apply(w, bEnd))) {
				statesAreEquivalents = false;
				if (Options.LOG_LEVEL != LogLevel.LOW) {
					LogManager.logInfo("States " + aEnd + " and " + bEnd
							+ " can be distinguished by ");
				}
				State start = aStart;
				LmTrace path = conjecture.getShortestPath(
						conjecture.applyGetState(w, aEnd), bStart);
				if (path == null) {
					start = bStart;
					path = conjecture.getShortestPath(
							conjecture.applyGetState(w, bEnd), aStart);
				}
				if (path != null) {
					LogManager.logInfo(
							"there is a path in conjecture which will raise an inconsistency of type one or two");
					if (Options.LOG_LEVEL != LogLevel.LOW) {
						LogManager.logConsole(
								"inconsistency found : h is not homming for conjecture. Moreover there is a path in conjecture to raise an inconsistency of type one or two");
					}
					dataManager.apply(
							conjecture.getShortestPath(currentState, start)
									.getInputsProjection());
					dataManager.apply(dataManager.h);
					dataManager.apply(w);
					dataManager.apply(path.getInputsProjection());
					dataManager.apply(dataManager.h);
					dataManager.apply(w);
					throw new RuntimeException(
							"an inconsistency of type one or two should have been raised before");
				}
			}
		}
		if (Options.LOG_LEVEL != LogLevel.LOW) {
			if (statesAreEquivalents)
				LogManager.logInfo("States " + aEnd + " and " + bEnd
						+ " have the same characterization in conjecture. We cannot say that h is not homming because conjecture is not minimal.");
			else
				LogManager.logInfo(
						"conjecture is not connex and we don't have a path to try the other state if we apply h and a distinction sequence.");
		}
	}

	public void learn(List<InputSequence> W, InputSequence h) {
		LogManager.logStep(LogManager.STEPOTHER, "Inferring the system");
		if (Options.LOG_LEVEL!=LogLevel.LOW)
		LogManager.logConsole("Inferring the system with W=" + W + "and h=" + h);


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


		dataManager = new SimplifiedDataManager(driver, this.W, h,fullTrace);

		// start of the algorithm
		do {
			Runtime runtime = Runtime.getRuntime();
			// runtime.gc(); //The garbage collection takes time and induce
			// wrong measurement of duration
			stats.updateMemory((int) (runtime.totalMemory() - runtime
					.freeMemory()));

			LogManager.logLine();
			LmTrace sigma;
			FullyQualifiedState lastKnownQ = null;
			FullyQualifiedState q = localize(dataManager);
			InputSequence alpha = dataManager.getShortestAlpha(q);
			dataManager.apply(alpha);
			assert dataManager.getCurrentState() != null;
			lastKnownQ = dataManager.getCurrentState();

			Set<String> X = dataManager.getxNotInR(lastKnownQ);
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
			if (dataManager.getCurrentState() != null) {
				// this is a corner case when h and W are empty.
				LogManager
						.logInfo("We are in corner case of implementation because W is empty.");
				assert h.getLength() == 0;
				continue;
			}
			assert dataManager.getCurrentState() == null : "we are trying to qualify this state, that should not be already done.";

			List<InputSequence> allowed_W = dataManager.getwNotInK(lastKnownQ,
					sigma);
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
						+ lastKnownQ
						+ " followed by "
						+ sigma
						+ "gives "
						+ dataManager.getSubtrace(newStatePos,
								dataManager.traceSize()));
			dataManager.addPartiallyKnownTrace(
					lastKnownQ,
					sigma,
					dataManager.getSubtrace(newStatePos,
							dataManager.traceSize()));
			if (dataManager.getCurrentState() == null) {
				 localize(dataManager);
			}
		} while (!dataManager.isFullyKnown());

		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			dataManager.getConjecture().exportToDot();
		LogManager.logInfo("end of sub-learning.");
	}

	public LmConjecture createConjecture() {
		LmConjecture c = dataManager.getConjecture();
		LogManager.logInfo("Conjecture has " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		return c;
	}

	public HWStatsEntry getStats() {
		return stats;
	}

	
	private FullyQualifiedState localize(SimplifiedDataManager dataManager) {
		LogManager.logInfo("Localizing...");
		FullyQualifiedState s = dataManager.getCurrentState();
		if (s != null) {
			LogManager.logInfo("We already know the current state (which is "
					+ s + ")");
			return s;
		}
		OutputSequence hResponse;
		do {
			LogManager.logInfo("Applying h to localize (h=" + dataManager.h
					+ ")");
			hResponse = dataManager.apply(dataManager.h);
			s = dataManager.getState(hResponse);
			if (s == null) {
				InputSequence missingW = dataManager
						.getMissingInputSequence(hResponse);
				LogManager
						.logInfo("We don't know were we are. We apply sequence "
								+ missingW + " from W-set");
				OutputSequence wResponse = dataManager.apply(missingW);
				dataManager.addWresponseAfterH(hResponse, missingW, wResponse);
			}

		} while (s == null);

		LogManager.logInfo("We know that after h, the answer " + hResponse
				+ " means we arrived in state " + s);
		dataManager.setCurrentState(s);

		stats.increaseLocalizeCallNb();
		return s;
	}

	private boolean checkRandomWalk() {
		LogManager.logStep(LogManager.STEPOTHER,
				"checking the computed conjecture with Random Walk");
		HWtMealyDriver generatedDriver = new HWtMealyDriver(
				dataManager.getConjecture());
		generatedDriver.stopLog();
		generatedDriver.setCurrentState(dataManager.getCurrentState().getState());

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
		State currentConjectureState = dataManager.getCurrentState().getState();
		FoundState currentFoundState = new FoundState(currentConjectureState,
				driver.getInputSymbols());
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
				String o = driver.execute(i);
				dataManager.walkWithoutCheck(i, o);
				if (!o.equals(t.getOutput())) {
					LogManager.logInfo("expected output was " + t.getOutput());
					return false;
				}
				currentFoundState = assigned.get(currentState);
				if (currentFoundState == null) {
					currentFoundState = new FoundState(dataManager.getCurrentState().getState(),
							driver.getInputSymbols());
					assigned.put(currentState, currentFoundState);
				} else if (currentFoundState.computedState != dataManager.getCurrentState().getState()) {
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

}
