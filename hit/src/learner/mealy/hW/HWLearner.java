package learner.mealy.hW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.State;
import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyFixedW;
import automata.mealy.multiTrace.SimpleMultiTrace;
import automata.mealy.splittingTree.smetsersSplittingTree.SplittingTree;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.Learner;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.hW.dataManager.AdaptiveHomingSequenceChecker;
import learner.mealy.hW.dataManager.ConjectureNotConnexException;
import learner.mealy.hW.dataManager.FullyKnownTrace;
import learner.mealy.hW.dataManager.FullyQualifiedState;
import learner.mealy.hW.dataManager.GenericHomingSequenceChecker;
import learner.mealy.hW.dataManager.HZXWSequence;
import learner.mealy.hW.dataManager.InconsistancyHMappingAndConjectureException;
import learner.mealy.hW.dataManager.InconsistancyWhileMergingExpectedTracesException;
import learner.mealy.hW.dataManager.InconsistancyWithConjectureAtEndOfTraceException;
import learner.mealy.hW.dataManager.GenericHNDException;
import learner.mealy.hW.dataManager.LocalizedHZXWSequence;
import learner.mealy.hW.dataManager.SimplifiedDataManager;
import learner.mealy.hW.dataManager.TraceTree;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.StandaloneRandom;

import tools.loggers.LogManager;

public class HWLearner extends Learner {
	private static class CanNotExtendWException extends RuntimeException {
		private static final long serialVersionUID = -7523569929856877603L;

		public CanNotExtendWException() {
			super("W-set cannot be extended");
		}
	}
	private MealyDriver driver;
	private SimplifiedDataManager dataManager;
	private HWStatsEntry stats;
	protected DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W;
	private List<LmTrace> fullTraces;
	private GenericOutputSequence lastDeliberatelyAppliedH = null;
	private GenericHomingSequenceChecker hChecker = null;
	private Map<GenericOutputSequence, List<HZXWSequence>> hZXWSequences = new HashMap<>();
	private List<HZXWSequence> zXWSequences = new ArrayList<>();
	private Map<GenericOutputSequence, List<LmTrace>> hWSequences = new HashMap<>();
	int wRefinenmentNb = 0;
	int nbOfTriedWSuffixes = 0;

	HWOptions options;

	/**
	 * Verify that recorded trace can be produced by the driver (need a
	 * Transparent driver). This method is for assert and debug.
	 * 
	 * @return false if one trace produce a different output when executed on
	 *         driver.
	 */
	public boolean checkTracesAreCompatible() {
		if (driver instanceof TransparentMealyDriver) {
			Mealy d = ((TransparentMealyDriver) driver).getAutomata();
			State initialState = d.getInitialState();
			for (LmTrace trace : fullTraces) {
				if (!d.apply(trace.getInputsProjection(), initialState)
						.equals(trace.getOutputsProjection()))
					return false;
			}
		}
		return true;
	}

	/**
	 * try to check that all executions on driver are recorded in
	 * {@link #fullTraces}. This method is for assertions and debug.
	 * 
	 * @return false if there is an inconsistency between driver and traces
	 *         recorded.
	 */
	public boolean checkTraces() {
		if (!checkTracesAreCompatible())
			return false;
		int totalLength = 0;
		for (LmTrace trace : fullTraces) {
			totalLength += trace.size();
		}
		if (totalLength != driver.numberOfAtomicRequest)
			return false;
		if (fullTraces.size() - 1 != driver.numberOfRequest)
			return false;
		return true;

	}

	public HWLearner(MealyDriver d, HWOptions options) {
		driver = d;
		this.options = options;
		options.getOracleOption().updateWithDriver(driver);
	}

	/**
	 * If some sequence are applied on driver but not returned, the datamanager
	 * is also updated.
	 *
	 * @return the trace applied on driver and not on dataManager or null if no
	 *         counter example is found
	 * @throws CeExposedUnknownStateException
	 *             if a new state is discovered while searching the initial
	 *             state in conjecture.
	 */
	public LmTrace getCounterExemple(List<GenericHNDException> hExceptions,
			boolean forbidReset) throws CeExposedUnknownStateException {
		SimpleMultiTrace appliedSequences = new SimpleMultiTrace();
		LmTrace returnedCE = null;
		LmConjecture conjecture = dataManager.getConjecture();
		State conjectureStartingState = dataManager.getCurrentState()
				.getState();
		boolean found = false;
		try {
			found = driver.getCounterExample(options.getOracleOption(),
					conjecture,
					conjectureStartingState, appliedSequences, forbidReset,
					stats.oracle);
		} finally {
			Iterator<LmTrace> it = appliedSequences.iterator();
			while (it.hasNext()) {
				LmTrace trace = it.next();
				if (found && !it.hasNext()) {
					returnedCE = trace;
				} else
					dataManager.walkWithoutCheck(trace, hExceptions);
				if (it.hasNext())
					dataManager.walkWithoutCheckReset();
			}
			assert checkTracesAreCompatible();
		}
		return returnedCE;
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

	private GenericInputSequence proceedHException(GenericHNDException e) {
		stats.increaseHInconsitencies();
		if (Options.getLogLevel() != LogLevel.LOW) {
			LogManager.logInfo(
					"Non-determinism found (due to homming sequence) : " + e);
			LogManager.logConsole(
					"Non-determinism found (due to homming sequence) : " + e);
		}

		GenericInputSequence h = e.getNewH();
		if (e instanceof AdaptiveHomingSequenceChecker.AdaptiveHNDException) {
			AdaptiveHomingSequenceChecker.AdaptiveHNDException adaptiveE = (AdaptiveHomingSequenceChecker.AdaptiveHNDException) e;
			adaptiveE.updateH();
			if (Options.getLogLevel() != LogLevel.LOW)
				adaptiveE.getNewH().exportToDot();
			List<GenericOutputSequence> responses = new ArrayList<>(
					hZXWSequences.keySet());
			for (GenericOutputSequence hresponse : responses) {
				if (!((AdaptiveSymbolSequence) hresponse).isFinal())
					hZXWSequences.remove(hresponse);
			}
		} else {
			hZXWSequences.clear();
			zXWSequences.clear();
			hWSequences.clear();
			LogManager.logInfo("h is now " + h);
		}
		hChecker = GenericHomingSequenceChecker.getChecker(h);

		for (LmTrace trace : fullTraces) {
			hChecker.reset();
			for (int i = 0; i < trace.size(); i++)
				try {
					hChecker.apply(trace.getInput(i), trace.getOutput(i));
				} catch (GenericHNDException rec) {
					return proceedHException(rec);
				}
		}
		if (options.addHInW.isEnabled()) {
			assert !options
					.useAdaptiveH() : "not implemented : add h in W and adaptive h are incompatible at this time";
			assert !options
					.useAdaptiveW() : "not implemented : add h in W and adaptive w sequences are incompatible at this time";
			InputSequence hFixed = (InputSequence) h;
			TotallyFixedW WFixed = (TotallyFixedW) W;
			boolean hIsInW = false;
			for (InputSequence w : WFixed) {
				if (w.startsWith(hFixed)) {
					hIsInW = true;
					break;
				}
			}
			if (!hIsInW) {
				LmTrace lastTrace = fullTraces.get(fullTraces.size() - 1);
				int traceLength = lastTrace.size();
				LmTrace hTrace = lastTrace.subtrace(
						traceLength - hFixed.getLength(), traceLength);
				assert hTrace.getInputsProjection().equals(hFixed);
				addOrExtendInW(hFixed, WFixed);
			}
		}
		return h;
	}

	public void learn() {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver d = (TransparentMealyDriver) driver;
			if ((!d.getAutomata().isConnex(true))
					&& !options.useReset.isEnabled())
				throw new RuntimeException("driver must be strongly connected");
		}
		fullTraces = new ArrayList<>();
		fullTraces.add(new LmTrace());
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();

		DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W;
		if (options.useAdaptiveW()) {
			W = new TotallyAdaptiveW();
			W.refine(W.getEmptyCharacterization(), new LmTrace());
			if (options.usePrecomputedW()) {
				if (!(driver instanceof TransparentMealyDriver))
					throw new RuntimeException(
							"driver must be transparent to compute adaptive W");
				TransparentMealyDriver transparent = (TransparentMealyDriver) driver;
				SplittingTree tree = new SplittingTree(
						transparent.getAutomata(),
						transparent.getInputSymbols());
				W = tree.computeW();
			}
			assert !options.addIInW();
		} else {
			TotallyFixedW W_fixed = new TotallyFixedW();
			W = W_fixed;
			if (options.usePrecomputedW()) {
				W = new TotallyFixedW(LocalizerBasedLearner
						.computeCharacterizationSet(driver));
			}
			if (options.addIInW()) {
				for (String input : driver.getInputSymbols())
					W_fixed.add(new InputSequence(input));
			}
		}
		if (W.isEmpty())
			W.refine(W.getEmptyCharacterization(), new LmTrace());

		LogManager.logConsole("hw : start of learning");
		long start = System.nanoTime();

		GenericInputSequence h = null;
		if (!options.useAdaptiveH())
			h = new InputSequence();
		else
			h = new AdaptiveSymbolSequence();
		hChecker = GenericHomingSequenceChecker.getChecker(h);
		stats = new HWStatsEntry(driver, options);

		LmTrace counterExampleTrace;
		boolean inconsistencyFound;
		boolean stateDiscoveredInCe = false;

		do {
			stats.updateMemory((int) (runtime.totalMemory() - runtime
					.freeMemory()));

			counterExampleTrace = null;
			inconsistencyFound = false;
			if (!stateDiscoveredInCe) {
				LogManager.logLine();
				LogManager.logStep(LogManager.STEPOTHER,
						"Starting new learning");
			}
			List<GenericHNDException> hExceptions = new ArrayList<>();
			try {
				try {
					learn(W, h, stateDiscoveredInCe);
					assert checkTraces();
				} catch (ConjectureNotConnexException e) {
					checkInconsistencyHMapping();
					throw e;
				}
				checkInconsistencyHMapping();
			} catch (GenericHNDException e) {
				h = proceedHException(e);
				inconsistencyFound = true;
			} catch (ConjectureNotConnexException e) {
				if (Options.getLogLevel() != LogLevel.LOW) {
					LogManager
							.logInfo("The conjecture is not connex. We stop here and look for a counter example");

					LogManager
							.logConsole("The conjecture is not connex. We stop here and look for a counter example");
				}
			} catch (InconsistancyWithConjectureAtEndOfTraceException e) {
				stats.increaseWInconsistencies();
				if (Options.getLogLevel() != LogLevel.LOW) {
					LogManager.logInfo("Non-determinism found : " + e);
					LogManager.logConsole("Non-determinism found : " + e);
				}
				counterExampleTrace = new LmTrace();
				inconsistencyFound = true;
			} catch (InconsistancyWhileMergingExpectedTracesException e) {
				stats.increaseWInconsistencies();
				if (Options.getLogLevel() != LogLevel.LOW) {
					LogManager.logInfo("Non-determinism found : " + e);
					LogManager.logConsole("Non-determinism found : " + e);
				}
				extendsW(e.getStates(), e.getTrace(), 0);
				inconsistencyFound = true;
			} catch (InconsistencyBeforeSearchingAdvancedAlphaException e) {
				inconsistencyFound = true;
			} catch (OracleGiveCounterExampleException e) {
				assert hExceptions.isEmpty();
				hExceptions = e.gethExceptions();
				LogManager
						.logInfo("one counter example found during inference: "
								+ counterExampleTrace);
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logConsole(
							"one counter example found during inference : "
									+ counterExampleTrace);
				counterExampleTrace = e.getCounterExampletrace();
			}

			// TODO since we apply counterExampleTrace on dataManager after
			// processing it, it would be nicer to add it just after running it
			// on driver.
			if (!inconsistencyFound && options.searchCeInTrace.isEnabled()
					&& counterExampleTrace == null) {
				inconsistencyFound = searchAndProceedCEInTrace();
/*
				counterExampleTrace = searchCEInTrace();
				if (counterExampleTrace != null) {
					virtualCounterExample = true;
					// we have to check if counterExempleTrace has a sufficient
					// length for suffix1by1
					
					throw new RuntimeException("not implemented : a known characterization is needed");
					for (GenericInputSequence w : W) {
						if (w.hasPrefix(counterExampleTrace)) {
							if (Options.LOG_LEVEL != LogLevel.LOW) {
								LogManager.logInfo(
										"Counter example is already in W. Forgetting it");
							}
							virtualCounterExample = false;
							counterExampleTrace = null;
							break;
						}
					}
				}
				 */
			}

			stateDiscoveredInCe = false;
			if (!inconsistencyFound && counterExampleTrace == null) {
				LogManager.logInfo("asking for a counter example");
				if (options.useReset.isEnabled())
					try {
						counterExampleTrace = getCounterExemple(hExceptions,
								false);
					} catch (CeExposedUnknownStateException e) {
						dataManager.getFullyQualifiedState(e.characterization);
						dataManager.getInitialCharacterization();
						stateDiscoveredInCe = true;
						continue;
					}
				else
					try {
						counterExampleTrace = getCounterExemple(hExceptions,
								true);
					} catch (CeExposedUnknownStateException e) {
						// this should not happen because the discovery of a
						// new state needs to reset the driver
						assert false;
						throw new RuntimeException(e);
					}
				if (counterExampleTrace == null)
					LogManager.logInfo("No counter example found");
				else {
					LogManager.logInfo("one counter example found : "
							+ counterExampleTrace);
					if (Options.getLogLevel() != LogLevel.LOW)
						LogManager.logConsole("one counter example found : "
								+ counterExampleTrace);
				}

			}

			if (counterExampleTrace != null) {
				dataManager.walkWithoutCheck(counterExampleTrace, hExceptions);
				LogManager
						.logInfo("geting smallest suffix in counter example which is not in W and not a prefix of a W element");
				if (driver instanceof TransparentMealyDriver
						&& ((TransparentMealyDriver) driver).getAutomata()
								.acceptCharacterizationSet(W)) {
					LogManager
							.logWarning("We are adding new element to W but it is already a W-set for this driver");
				}
				FullyQualifiedState currentState = dataManager
						.getLastKnownState();
				int firstStatePos = dataManager.getLastKnownStatePos();// pos in
																		// trace of
																	// first
																	// input
																	// after h
				List<FullyQualifiedState> states = new ArrayList<>();
				states.add(currentState);
				LmTrace lastTrace = dataManager.getTraceSinceReset();
				for (int i = firstStatePos; i < lastTrace.size() - 1; i++) {
					FullyKnownTrace transition = currentState
							.getKnownTransition(lastTrace.getInput(i));
					if (transition == null)
						break;
					currentState = transition.getEnd();
					states.add(currentState);
				}
				try {
					extendsW(states, lastTrace, firstStatePos);
				} catch (CanNotExtendWException e) {
					if (hExceptions.size() == 0)
						throw e;
				}
			}
			stats.increaseWithDataManager(dataManager);
			if (hExceptions.size() > 0) {
				h = proceedHException(hExceptions.get(0));
				if (hExceptions.size() > 1) {
					LogManager.logConsole(
							"multiple h inconsistencies found, only one proceeded.");
				}

			}
			assert checkTraces();
		} while (counterExampleTrace != null || inconsistencyFound
				|| stateDiscoveredInCe);

		float duration = (float) (System.nanoTime() - start) / 1000000000;
		LogManager.logConsole("hw : end of learning : " + duration + "s");
		stats.setDuration(duration);
		stats.setAvgTriedWSuffixes((float)nbOfTriedWSuffixes/wRefinenmentNb);
	
		stats.updateMemory((int) (runtime.totalMemory() - runtime.freeMemory()));
		stats.finalUpdate(dataManager);

		if (!dataManager.getConjecture().checkOnAllStatesWithReset(fullTraces)
				.isCompatible()) {
			LogManager.logWarning(
					"conjecture is false or driver is not strongly connected");
			throw new RuntimeException("wrong conjecture");
		}

		// State initialState=searchInitialState();
		// if (initialState!=null)
		// dataManager.getConjecture().exportToDot("\t"
		// + initialState.getName() + " [shape=doubleoctagon]\n");

		// the next call is not mandatory for algorithm, see checkEquivalence
		// description.
		//checkEquivalence(new File("reference.dot"));

	}

	/**
	 * Extends W-set according to a list of characterizations which will be
	 * extended by suffixes of trace.
	 * 
	 * There might be different ways of increasing W. Current implementation
	 * take the shortest suffix of {@code trace} which is not already in the
	 * given characterization.
	 * 
	 * @param states
	 *            the states used to know which characterization should be
	 *            extended.
	 * @param trace
	 *            the trace in which suffixes are taken
	 * @param firstPosInTrace
	 *            an offset in trace. the suffix starting at
	 *            {@code firstPosInTrace} will be added in characterization of
	 *            {@code states[0]} (if no shortest suffix is used).
	 */
	private void extendsW(List<FullyQualifiedState> states, LmTrace trace,
			int firstPosInTrace) throws CanNotExtendWException {
		boolean WExtended = false;
		assert states.get(0) != null;
		assert states.size() + firstPosInTrace <= trace.size();
		for (int i = states.size() - 1; i >= 0; i--) {
			FullyQualifiedState currentState = states.get(i);
			if (currentState == null)
				continue;
			LmTrace endOfTrace = trace.subtrace(firstPosInTrace + i,
					trace.size());
			if (!currentState.getWResponses().contains(endOfTrace)) {
				LogManager.logInfo("characterization ",
						currentState.getWResponses(),
						" will be extended with sequence ", endOfTrace);
				W.refine(currentState.getWResponses(), endOfTrace);
				WExtended = true;
				break;
			} else {
				int traceSize = endOfTrace.size();
				LogManager.logInfo("state ", currentState, " characterized by ",
						currentState.getWResponses(), " already contain trace ",
						endOfTrace.subtrace(0, traceSize - 1)," ",
						endOfTrace.getInput(traceSize - 1), "/?");
				nbOfTriedWSuffixes++;
			}
		}
		wRefinenmentNb++;
		if (!WExtended) {
			throw new CanNotExtendWException();
		}
	}

	private void checkInconsistencyHMapping() {
		if (!options.checkInconsistenciesHMapping.isEnabled())
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
		Map<GenericOutputSequence, Map<State, List<State>>> knownResponses = new HashMap<>();
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
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo("Inconsistency found :" + e);
				GenericInputSequence distinctionW = e.getDistinctionSequence();
				if (distinctionW == null) {
					LogManager.logWarning(
							"inconsistency can not be used because the state reached after homing sequence ("
									+ e.getStateAfterH()
									+ ")  has the same transitions than caracterization of mapped State ("
									+ e.getMappedTarget() + ").");
				} else {
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
			GenericOutputSequence hResponse = conjecture.apply(dataManager.h,
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

		if (Options.getLogLevel() != LogLevel.LOW) {
			LogManager.logInfo(
					"New inconsistency found : h is not homming for this conjecture. In conjecture, from states "
							+ aStart + " and " + bStart
							+ " we can observe the same answer to homing sequence '"
							+ conjecture.apply(dataManager.h, aStart)
							+ "' but this lead in two differents states " + aEnd
							+ " and " + bEnd
							+ ". Now we try to use this inconsistency");
		}

		// for the case were aEnd and bEnd have the same characterizing
		// transitions, we try to find a custom distinction sequence. This can
		// be improved in order to try a sequence which distinguish the two
		// state and allow a path from one state to the other.
		boolean statesAreEquivalents = true;
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> existingWTried = W
				.getEmptyCharacterization();
		// for last loop (when all existing w were tried), we compute a custom
		// distinction sequence and set exitingWTried to null to finish loop
		while (existingWTried != null) {
			GenericInputSequence w;
			if (existingWTried.isComplete()) {
				w = conjecture.getDistinctionSequence(aEnd, bEnd);
				existingWTried = null;
				if (w == null)
					break;
			} else {
				w = existingWTried.getUnknownPrints().get(0);
				existingWTried
						.addPrint(w.buildTrace(conjecture.apply(w, aEnd)));
			}

			if (!conjecture.apply(w, aEnd).equals(conjecture.apply(w, bEnd))) {
				statesAreEquivalents = false;
				if (Options.getLogLevel() != LogLevel.LOW) {
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
					if (Options.getLogLevel() != LogLevel.LOW) {
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
		if (Options.getLogLevel() != LogLevel.LOW) {
			if (statesAreEquivalents)
				LogManager.logInfo("States " + aEnd + " and " + bEnd
						+ " have the same characterization in conjecture. We cannot say that h is not homming because conjecture is not minimal.");
			else
				LogManager.logInfo(
						"conjecture is not connex and we don't have a path to try the other state if we apply h and a distinction sequence.");
		}
	}

	/**
	 * This method calls
	 * {@link #searchAndProceedCEInOneTrace(LmTrace, FullyQualifiedState, int)}
	 * on all traces observed until a counter example is found.
	 * 
	 * @return true if one counter example is found, false otherwise
	 */
	private boolean searchAndProceedCEInTrace() {
		assert checkTraces();
		if (options.useReset.isEnabled()
				&& dataManager.getInitialState() == null) {
			LogManager.logInfo(
					"Cannot search CE in trace because the initial state is not in conjecture.",
					" As the search of counter example in trace is based on states incompatibility,",
					" we need to know all states of conjecture.",
					" (Otherwise, the computation of compatible states can lead to an incorrect state.");
			return false;
		}
		LogManager.logInfo("Searching counter-example in trace.");
		int globalTracePos = 0;
		for (LmTrace trace : fullTraces) {
			if (searchAndProceedCEInOneTrace(trace,
					dataManager.getInitialState(), globalTracePos)) {
				return true;
			}
			globalTracePos += trace.size();
		}
		LogManager.logInfo("no counter-example found in trace");
		return false;
	}

	/**
	 * search an inconsistency between a trace and the conjecture.
	 * 
	 * @param trace
	 *            a trace observed from real automaton
	 * @param currentState
	 *            the state in conjecture from wich the trace was applied (or
	 *            {@code null} if unknown)
	 * @param globalTracePos
	 *            the position of first transition in global trace (without
	 *            taking resets in account).This is needed for a proper logging
	 * @return true if a counter example was found, false otherwise.
	 */
	private boolean searchAndProceedCEInOneTrace(LmTrace trace,
			FullyQualifiedState currentState, int globalTracePos) {
		TraceTree expectedTraces = null;
		List<FullyQualifiedState> statesHistory = new ArrayList<>();
		Set<FullyQualifiedState> compatibleStates;

		int startPos = -1;
		if (currentState == null) {
			compatibleStates = new HashSet<>(dataManager.getStates());
		} else {
			compatibleStates = new HashSet<>();
			compatibleStates.add(currentState);
			statesHistory.add(currentState);
			startPos = 0;
		}
		for (int i = 0; i < trace.size(); i++) {
			String input = trace.getInput(i);
			String traceOutput = trace.getOutput(i);

			String conjectureOutput = null;
			if (currentState != null) {
				FullyKnownTrace t = currentState.getKnownTransition(input);
				if (t == null) {
					expectedTraces = currentState.getExpectedTraces();
					currentState = null;
				} else {
					currentState = t.getEnd();
					conjectureOutput = t.getTrace().getOutput(0);
				}
			}
			if (currentState == null && expectedTraces != null) {
				conjectureOutput = expectedTraces.getOutput(input);
				expectedTraces = expectedTraces.getSubTreeRO(input);
			}
			boolean extendWfailed = false;
			if (conjectureOutput != null
					&& !conjectureOutput.equals(traceOutput)) {
				assert !(driver instanceof TransparentMealyDriver)
						|| !((TransparentMealyDriver) driver).getAutomata()
								.acceptCharacterizationSet(W)
						|| !((TransparentMealyDriver) driver).getAutomata()
								.acceptHomingSequence(dataManager.h);
				LmTrace ceTrace = trace.subtrace(startPos, i + 1);

				LogManager.logInfo("Before transition ",
						startPos + globalTracePos,
						" (" + (startPos == 0 ? "right"
								: (startPos + " transitions"))
								+ " after the reset)",
						", we can identify the state reached (",
						statesHistory.get(0), "). The trace ", ceTrace,
						" observed from transition ", startPos + globalTracePos,
						" to transition ", i + globalTracePos,
						" is not compatible with state ", statesHistory.get(0),
						" in conjecture.");
				try {
					extendsW(statesHistory, ceTrace, 0);
					LogManager.logInfo("W extended using trace as CE");
					return true;
				} catch (CanNotExtendWException e) {
					LogManager.logInfo(
							"this part of trace cannot be used to refine W. Let's search another sequence");
					currentState = null;
					expectedTraces = null;
					extendWfailed = true;
				}
			}

			Set<FullyQualifiedState> newCompatibles = new HashSet<>(
					compatibleStates.size());
			for (FullyQualifiedState s : compatibleStates) {
				FullyKnownTrace t = s.getKnownTransition(input);
				if (t == null) {
					newCompatibles = new HashSet<>(dataManager.getStates());
					break;
				} else {
					if (t.getTrace().getOutput(0).equals(traceOutput))
						newCompatibles.add(t.getEnd());
				}
			}
			if (newCompatibles.size() == 1) {
				FullyQualifiedState onlyCompatibleState = newCompatibles
						.iterator().next();
				assert onlyCompatibleState != null;
				assert currentState == null
						|| currentState == onlyCompatibleState;
				if (currentState == null) {
					if (expectedTraces == null) {
						startPos = i + 1;
						statesHistory.clear();
					}
					currentState = onlyCompatibleState;
				}
			}
			if (newCompatibles.isEmpty()) {
				if (!extendWfailed)
					LogManager.logInfo(
							"There is no state compatible with the trace. This is a W-ND but we don't know were to add sequence in the distinction structure");
				newCompatibles = new HashSet<>(dataManager.getStates());
			}
			compatibleStates = newCompatibles;

			if (currentState != null || expectedTraces != null) {
				statesHistory.add(currentState);
			}
		}
		return false;
	}

	public void learn(
			DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W,
			GenericInputSequence h, boolean continueLastLearning)
			throws ConjectureNotConnexException,
			OracleGiveCounterExampleException,
			InconsistencyBeforeSearchingAdvancedAlphaException {
		assert checkTraces();
		if (!continueLastLearning) {
			LogManager.logStep(LogManager.STEPOTHER, "Inferring the system");
			if (Options.getLogLevel() != LogLevel.LOW)
				LogManager.logConsole(
						"Inferring the system with W=" + W + " and h=" + h);

			this.W = W;
			StringBuilder logW = new StringBuilder(
					"Using characterization struct : ");
			W.toString(logW);
			if (Options.getLogLevel() == LogLevel.ALL
					&& driver instanceof TransparentMealyDriver) {
				TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
				if (tDriver.getAutomata()
						.acceptCharacterizationSet(W.clone())) {
					logW.append(" (which is a W-set for the driver)");
				} else {
					logW.append(" (which is not a W-set for the driver)");
				}
			}
			if (W instanceof TotallyAdaptiveW
					&& Options.getLogLevel() == LogLevel.ALL) {
				((TotallyAdaptiveW) W).exportToDot();
			}
			LogManager.logInfo(logW.toString());
			LogManager.logInfo("Using homing sequence «" + h + "»"
					+ ((Options.getLogLevel() == LogLevel.ALL
							&& driver instanceof TransparentMealyDriver)
									? (((TransparentMealyDriver) driver)
											.getAutomata()
											.acceptHomingSequence(h)
													? " (which is a homming sequence for driver)"
													: " (which is not a homing sequence for driver)")
									: ""));

			dataManager = new SimplifiedDataManager(driver, this.W, h,
					fullTraces, hZXWSequences, zXWSequences, hWSequences,
					hChecker);
		} else {
			LogManager.logLine();
			LogManager.logInfo(
					"Restarting backbone with previous data structure");
		}

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
			if (options.useDictionary.isEnabled()) {
				List<LocalizedHZXWSequence> sequences;
				while (!(sequences = dataManager
						.getAndResetReadyForReapplyHZXWSequence()).isEmpty()) {
					proceedReadyHZXW(sequences);
				}
			}
			if (dataManager.isFullyKnown())
				break;
			InputSequence alpha;
			try {
				alpha = dataManager.getShortestAlpha(q);
			} catch (ConjectureNotConnexException e) {
				if (!options.useReset.isEnabled())
					throw e;
				LogManager.logInfo(
						"Conjecture is not strongly connected and some part are unreachable.");
				LogManager.logInfo("first, try to search an inconsistency in trace");
				if (dataManager.isFullyKnown() && searchAndProceedCEInTrace())
					throw new InconsistencyBeforeSearchingAdvancedAlphaException();
				if (!q.isMarkedAsSink()) {
					LogManager.logInfo(
							"This is the first time we try to go outside of this strongly connected component."
									+ " We ask oracle only one time.");
					List<GenericHNDException> hExceptions = new ArrayList<>();
					LmTrace counterExampleTrace;
					try {
						counterExampleTrace = getCounterExemple(hExceptions,
								true);
					} catch (CeExposedUnknownStateException e1) {
						// this is not supposed to happen
						throw new RuntimeException(e);
					}
					if (counterExampleTrace != null)
						throw new OracleGiveCounterExampleException(
								counterExampleTrace, hExceptions);
				}
				q.markAsSink();
				LogManager.logInfo(
						"Reseting the driver to try to go outside of the strongly connected sub-part");
				dataManager.reset();
				lastDeliberatelyAppliedH = null;
				FullyQualifiedState R = dataManager.getInitialState();
				if (R == null) {
					Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> Rcharacterization = dataManager
							.getInitialCharacterization();
					GenericInputSequence w = Rcharacterization.unknownPrints()
							.iterator().next();
					GenericOutputSequence wResponse = dataManager.apply(w);
					Rcharacterization.addPrint(w, wResponse);
					if (Rcharacterization.isComplete()) {
						dataManager.getFullyQualifiedState(Rcharacterization);
						dataManager.getInitialCharacterization();
					}
					continue;// restart h z x w learning
				}
				assert R != null;
				assert dataManager.getCurrentState() != null;
				try {
					alpha = dataManager.getShortestAlpha(R);// might throw an
															// ConjectureNotConnex
															// exception again
				} catch (ConjectureNotConnexException e2) {
					LogManager.logInfo(
							"Some incomplete states are unreachable from the identified initial state. Stoping here and look for a counter example");
					throw e;
				}
			}
			OutputSequence alphaResponse = dataManager.apply(alpha);
			assert dataManager.getCurrentState() != null;
			lastKnownQ = dataManager.getCurrentState();

			Set<String> X = dataManager.getxNotInR(lastKnownQ);
			assert !X.isEmpty();
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
				assert h.isEmpty();
				continue;
			}
			assert dataManager.getCurrentState() == null : "we are trying to qualify this state, that should not be already done.";

			assert dataManager.getConjecture().getTransitionFromWithInput(
					lastKnownQ.getState(), x) == null;
			List<? extends GenericInputSequence> allowed_W = dataManager
					.getwNotInK(lastKnownQ, sigma);
			GenericInputSequence w = new InputSequence();
			if (!W.isEmpty())
				w = allowed_W.get(0); // here we CHOOSE to take the first.
			if (Options.getLogLevel() != Options.LogLevel.LOW)
				LogManager.logInfo("We choose w = " + w + " in " + allowed_W);
			GenericOutputSequence wResponse = dataManager.apply(w);
			LmTrace wTrace = w.buildTrace(wResponse);
			if (Options.getLogLevel() != Options.LogLevel.LOW)
				LogManager.logInfo("We found that "
						+ lastKnownQ
						+ " followed by "
						+ sigma
						+ "gives "
						+ wTrace);
			dataManager.addPartiallyKnownTrace(
					lastKnownQ,
					sigma,
					wTrace);
			assert dataManager.getConjecture().getTransitionFromWithInput(
					lastKnownQ.getState(), x) != null
					|| !dataManager.getwNotInK(lastKnownQ, sigma).contains(w);
			if (options.useDictionary.isEnabled()) {
				addHZXWSequence(lastDeliberatelyAppliedH,
						new LmTrace(alpha, alphaResponse), sigma, wTrace);
				List<LocalizedHZXWSequence> sequences;
				while (!(sequences = dataManager
						.getAndResetReadyForReapplyHZXWSequence()).isEmpty()) {
					proceedReadyHZXW(sequences);
				}
			}
			if (dataManager.getCurrentState() == null) {
				 localize(dataManager);
			}
		} while (!dataManager.isFullyKnown());

		if (Options.getLogLevel() == Options.LogLevel.ALL)
			dataManager.getConjecture().exportToDot();
		LogManager.logInfo("end of sub-learning.");
	}

	private void proceedReadyHZXW(
			List<LocalizedHZXWSequence> readyForReapplyHZXWSequence) {
		assert options.useDictionary.isEnabled();
		
		for (LocalizedHZXWSequence localizedSeq : readyForReapplyHZXWSequence) {
			LmTrace transition = localizedSeq.sequence.getTransition();
			FullyQualifiedState initialState = localizedSeq.endOfTransferState;
			if (initialState == null) {
				LogManager.logWarning("there is an inconsistency with trace "
						+ localizedSeq.sequence + " whic is not handled yet");
				//TODO handle the ND.
				continue;
			}
			if (initialState
					.getKnownTransition(transition.getInput(0)) != null) {
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo(
							"cannot reuse trace " + localizedSeq.sequence
									+ " because transition from " + initialState
									+ " is already known");
				continue;
			}
			String expectedTransitionOutput = initialState
					.getPartiallTransitionOutput(transition.getInput(0));
			if (expectedTransitionOutput != null && !expectedTransitionOutput
					.equals(transition.getOutput(0))) {
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logWarning("cannot reuse trace "
							+ localizedSeq.sequence
							+ " because it is not consistent with partially known traces");
				//TODO handle this ND
				continue;
			}

			LmTrace wResponse = localizedSeq.sequence.getwResponse();
			LmTrace expectedTrace = new LmTrace();
			expectedTrace.append(transition);
			expectedTrace.append(wResponse);
			InconsistancyWhileMergingExpectedTracesException inc = initialState
					.addExpectedTrace(expectedTrace);
			if (inc != null) {
				FullyQualifiedState startState;
				if (localizedSeq.sequence.gethResponse() == null) {
					startState = dataManager.getInitialState();
					// TODO maybe startState can be null ? If this can happen,
					// what to do ?
				} else
					startState = dataManager
							.getState(localizedSeq.sequence.gethResponse());
				inc.addPreviousState(startState,
						localizedSeq.sequence.getTransferSequence()
								.getInputsProjection(),
						localizedSeq.sequence.getTransferSequence()
								.getOutputsProjection());
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo("trace ", localizedSeq.sequence,
							" from dictionary is inconsistent with others observations");
				throw inc;
			}
			if (initialState.hZXWSequenceIsInNeededW(localizedSeq)) {
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager
							.logInfo("reusing trace " + localizedSeq.sequence);
				dataManager.addPartiallyKnownTrace(initialState, transition,
						wResponse);
			} else {
				assert initialState.kPrintIsKnown(transition, wResponse);
				LogManager.logInfo("Can not reuse trace ",
						localizedSeq.sequence,
						" because the response to this w is already known");
			}
		}
	}

	public void addHZXWSequence(GenericOutputSequence hResponse,
			LmTrace transfer, LmTrace transition, LmTrace wResponse) {
		List<HZXWSequence> list;
		if (hResponse == null)
			list = zXWSequences;
		else {
			list = hZXWSequences.get(hResponse);
			if (list == null) {
				list = new ArrayList<>();
				hZXWSequences.put(hResponse, list);
			}
		}
		HZXWSequence newSeq = new HZXWSequence(hResponse, transfer, transition,
				wResponse);
		assert !list.contains(
				newSeq) : "one sequence from dictionary wasn't reused";
		list.add(newSeq);
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
		GenericOutputSequence hResponse;
		do {
			LogManager.logInfo("Applying h to localize (h=" + dataManager.h
					+ ")");
			hResponse = dataManager.apply(dataManager.h);
			lastDeliberatelyAppliedH = hResponse;
			s = dataManager.getState(hResponse);
			if (s == null) {
				GenericInputSequence missingW = dataManager
						.getMissingInputSequence(hResponse);
				LogManager
						.logInfo("We don't know were we are. We apply sequence "
								+ missingW + " from W-set");
				GenericOutputSequence wResponse = dataManager.apply(missingW);
				dataManager.addWresponseAfterH(hResponse, missingW, wResponse);
				List<LmTrace> wObserved = hWSequences.get(hResponse);
				if (wObserved == null) {
					wObserved = new ArrayList<>();
					hWSequences.put(hResponse, wObserved);
				}
				assert !wObserved.contains(missingW.buildTrace(wResponse));
				wObserved.add(missingW.buildTrace(wResponse));
			}

		} while (s == null);
		dataManager.endOfH(s);

		LogManager.logInfo("We know that after h, the answer " + hResponse
				+ " means we arrived in state " + s);
		stats.increaseLocalizeCallNb();
		return s;
	}

	/**
	 * This function check equivalence between conjecture and a reference dot
	 * file. It needs to have a complete conjecture.
	 * 
	 * This function is not needed by the algorithm itself, this is a tool for
	 * improving usage of this learner. Actually, it was written for article
	 * JSS2018 when we experimented the inference of muted versions of a same
	 * software.
	 * 
	 * The procedure to use this is to infer the normal software, save the
	 * generated dot file, and then add a call to this function at the end of
	 * learner with the reference dot file as argument.
	 * 
	 * @param referenceFile
	 *            the dot file containing reference automata
	 * @return true if conjecture is equivalent to reference automata.
	 */
	public boolean checkEquivalence(File referenceFile) {
		if (options.useReset.isEnabled())
			throw new RuntimeException("not implemented");
		assert dataManager != null;
		assert W != null;
		assert fullTraces.size() == 0;
		LmTrace fullTrace = fullTraces.get(0);
		Mealy reference;
		try {
			reference = Mealy.importFromDot(referenceFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		LmConjecture conjecture = dataManager.getConjecture();
		InputSequence randomEquivalenceI = InputSequence
				.generate(driver.getInputSymbols(), fullTrace.size() * 10,
						new StandaloneRandom());
		MealyDriver d;
		if (driver instanceof TransparentMealyDriver) {
			d = new TransparentMealyDriver(
					((TransparentMealyDriver) driver).getAutomata());
		} else
			try {
				d = driver.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		OutputSequence randomEquivalenceO = d.execute(randomEquivalenceI);
		LmTrace randomEquivalence = new LmTrace(randomEquivalenceI,
				randomEquivalenceO);
		int firstDiscrepancyR = reference.checkOnOneState(randomEquivalence);
		if (firstDiscrepancyR == randomEquivalence.size()) {
			LogManager.logConsole(
					"Random walk of length " + randomEquivalence.size()
							+ " did not expose discrepancy (trace is "
							+ fullTrace.size() + "symbols long).");
		} else {
			LogManager.logConsole("Random walk exposed discrepancy after "
					+ (firstDiscrepancyR + 1) + " symbols (trace is "
					+ fullTrace.size() + " symbols long).");
		}
		FullyQualifiedState conjectureState = dataManager.getCurrentState();
		// reference can have equivalents states and thus we must check all
		// possible initial state
		List<State> matchingInitialState = new ArrayList<>();
		for (State referenceState : reference.getStates()) {
			boolean matching = true;
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> conjectureCharacterization = conjectureState
					.getWResponses();
			for (LmTrace conjectureTrace : conjectureCharacterization
					.knownResponses()) {
				if (!reference
						.apply(conjectureTrace.getInputsProjection(),
								referenceState)
						.equals(conjecture.apply(
								conjectureTrace.getInputsProjection(),
								conjectureState.getState()))) {
					matching = false;
					break;
				}
			}
			if (matching)
				matchingInitialState.add(referenceState);
		}
		boolean isEquivalent = true;
		if (matchingInitialState.size() == 0) {
			isEquivalent = false;
			LogManager.logConsole("no state in " + referenceFile
					+ " match current state in conjecture");
		}
		boolean oneStateIsEquivalent = false;
		for (State referenceState : matchingInitialState) {
			List<InputSequence> counterExamples = conjecture
					.getCounterExamples(conjectureState.getState(), reference,
							referenceState, true);
			if (counterExamples.isEmpty()) {
				oneStateIsEquivalent = true;
			}
		}
		if (!oneStateIsEquivalent)
			isEquivalent = false;
		if (isEquivalent) {
			LogManager.logConsole("Conjecture is equivalent to reference");
		} else {
			LogManager.logConsole("Conjecture is not equivalent to reference");
		}
		int firstDiscrepancy = reference.checkOnAllStates(fullTrace);
		if (firstDiscrepancy == fullTrace.size()) {
			LogManager.logConsole(
					"This trace do not expose discrepancy with reference");
		} else {
			LogManager.logConsole(
					"This trace expose discrepancy with reference after "
							+ (firstDiscrepancy + 1) + " symbols");
		}
		return isEquivalent;
	}

	/**
	 * eliminate states which cannot be initial state of driver until at most
	 * one remains.
	 * 
	 * if conjecture is wrong, the result is unspecified.
	 * 
	 * @warning this function need to reset the driver and/or to instantiate a
	 *          new one. be careful of side effects on driver and possible
	 *          runtime errors.
	 * 
	 * @TODO This function was added quickly for an article, it is probably
	 *       better to put it in another place of SIMPA
	 * 
	 * @return the initial state of conjecture or unspecified if conjecture is
	 *         false;
	 */
	private State searchInitialState() {
		if (driver instanceof TransparentMealyDriver)
			return ((TransparentMealyDriver)driver).getInitState();
		System.out.println("searching initial state");
		MealyDriver d;
		try {
			d=driver.getClass().newInstance();
		}catch(Exception e) {
			System.err.println("error while instanciating");
			return null;
		}
		LmConjecture conjecture = createConjecture();
		List<State> possibleInitialStates = new ArrayList<>();
		possibleInitialStates.addAll(conjecture.getStates());
		while (possibleInitialStates.size() > 1) {
			State s1 = possibleInitialStates.get(0);
			State s2 = possibleInitialStates.get(1);
			InputSequence seq = conjecture.getDistinctionSequence(s1, s2);
			if (seq == null) {
				possibleInitialStates.remove(s1);
				continue;
			}
			List<State> nextList = new ArrayList<>();
			d.reset();
			LmTrace trace = new LmTrace(seq, d.execute(seq));
			for (State s : possibleInitialStates) {
				if (conjecture.checkOnOneState(trace, s) == seq.getLength())
					nextList.add(s);
			}
			possibleInitialStates = nextList;
		}
		if (possibleInitialStates.size() > 0)
			return possibleInitialStates.get(0);
		System.err.println("no state match initial state of driver");
		return null;
	}
}
