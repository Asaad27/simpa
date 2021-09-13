/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
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
package learner.mealy.hW.dataManager;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;
import drivers.mealy.CompleteMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

import java.util.*;
import java.util.stream.Collectors;

import static drivers.mealy.CompleteMealyDriver.OUTPUT_FOR_UNDEFINED_INPUT;
import static java.util.function.Predicate.not;

public class SimplifiedDataManager {
	public static SimplifiedDataManager instance;// TODO either make a proper
	// singleton either do
	// something else
	private final CompleteMealyDriver driver;
	private int numberOfInputsApplied;
	private final List<LmTrace> globalTraces;
	private LmTrace traceSinceReset;
	private final DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W; //
	// Characterization
	// set
	public final GenericInputSequence h;
	private final List<String> I;// Input Symbols
	private final Map<Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence>,
			FullyQualifiedState> Q;// known

	// states
	private final Set<FullyQualifiedState> notFullyKnownStates;// Fully qualified
	// states with
	// undefined
	// transitions
	private final HWConjecture conjecture;
	private FullyQualifiedState currentState;
	private FullyQualifiedState lastknownState = null;
	private int lastknownStatePos = 0;

	private final Map<GenericOutputSequence, FullyQualifiedState> hResponse2State;
	private final Map<GenericOutputSequence, Characterization<? extends GenericInputSequence, ?
			extends GenericOutputSequence>> hResponse2Wresponses;
	private final GenericHomingSequenceChecker hChecker;

	private Collection<TraceTree> expectedTraces;

	private final Map<GenericOutputSequence, List<HZXWSequence>> hZXWSequences;
	private final List<HZXWSequence> zXWSequences;
	private final Map<GenericOutputSequence, List<LmTrace>> hWSequences;
	private List<LocalizedHZXWSequence> readyForReapplyHZXWSequence = new ArrayList<>();

	protected Collection<FullyQualifiedState> identifiedFakeStates = new ArrayList<>();
	private final Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> initialStateCharacterization;
	private FullyQualifiedState initialState;

	public Collection<FullyQualifiedState> getIdentifiedFakeStates() {
		return identifiedFakeStates;
	}

	public int getHResponsesNb() {
		return hResponse2State.keySet().size();
	}

	public FullyQualifiedState getState(GenericOutputSequence hResponse) {
		assert hResponse.checkCompatibilityWith(h);
		getOrCreateWResponseAfterHresponse(hResponse);// create an empty
		// characterization and
		// possibly fill it from
		// dictionary.
		FullyQualifiedState s = hResponse2State.get(hResponse);
		if (s == null && W.isEmpty()) {
			s = getFullyQualifiedState(W.getEmptyCharacterization());
		}
		return s;
	}

	private Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> getOrCreateWResponseAfterHresponse(
			GenericOutputSequence hR) {
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wR = hResponse2Wresponses
				.get(hR);
		if (wR == null) {
			wR = W.getEmptyCharacterization();
			hResponse2Wresponses.put(hR, wR);
			extendsAsMuchAsPossible(wR, hR);
		}
		return wR;
	}

	public Set<FullyQualifiedState> getNotFullyKnownStates() {
		return notFullyKnownStates;
	}


	/**
	 * use as much sequences as possible in dictionary to improve the
	 * characterization
	 * 
	 * @param wR
	 *            the characterization to improve.
	 * @param hResponse
	 *            the response to homing sequence leading to this
	 *            characterization
	 * @return the number of sequences used
	 */
	private int extendsAsMuchAsPossible(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wR,
			GenericOutputSequence hResponse) {
		List<LmTrace> traces=hWSequences.get(hResponse);
		if (traces == null)
			return 0;
		int usedSequences = 0;
		boolean extended;
		do {
			extended = false;
			for (LmTrace t : traces) {
				if (wR.acceptNextPrint(t)) {
					wR.addPrint(t);
					extended = true;
					usedSequences++;
				}
			}
		} while (extended);
		checkForCompletnessAfterH(hResponse);
		return usedSequences;
	}

	public GenericInputSequence getMissingInputSequence(
			GenericOutputSequence hResponse) {
		assert (getState(hResponse) == null);
		Iterator<? extends GenericInputSequence> it = getOrCreateWResponseAfterHresponse(
				hResponse).unknownPrints().iterator();
		return it.next();
	}

	public FullyQualifiedState addWresponseAfterH(
			GenericOutputSequence hResponse, GenericInputSequence w,
			GenericOutputSequence wResponse) {
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wRs = getOrCreateWResponseAfterHresponse(
				hResponse);
		assert !wRs.isComplete() : "all responses already known";
		assert (wResponse.checkCompatibilityWith(w));
		assert wRs.getUnknownPrints().contains(w);
		wRs.addPrint(w, wResponse);
		extendsAsMuchAsPossible(wRs, hResponse);
		FullyQualifiedState q=getState(hResponse);
		if (q!=null)return q;
		return checkForCompletnessAfterH(hResponse);
	}

	/**
	 * Check if the state reached after one answer to homing sequence is
	 * completely characterized and if so, record this state in H mapping.
	 * 
	 * @param hResponse
	 *            the answer to h which might be characterized
	 * @return the state characterized if characterization is complete
	 **/
	FullyQualifiedState checkForCompletnessAfterH(
			GenericOutputSequence hResponse) {
		assert hResponse2State.get(hResponse) == null;
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> wRs = hResponse2Wresponses
				.get(hResponse);
		if (wRs.isComplete()) {
			FullyQualifiedState q = getFullyQualifiedState(wRs);
			hResponse2State.put(hResponse, q);
			LogManager.logInfo("We found that the response " + hResponse
					+ " to homing sequence lead in state " + q);
			List<HZXWSequence> sequences = hZXWSequences.get(hResponse);
			if (sequences != null) {
				for (HZXWSequence seq : sequences) {
					LocalizedHZXWSequence localizedSeq = new LocalizedHZXWSequence(
							seq);
					if (q.addLocalizedHZXWSequence(localizedSeq)) {
						readyForReapplyHZXWSequence.add(localizedSeq);
					}
				}
			}
			return q;
		}
		return null;
	}

	public SimplifiedDataManager(CompleteMealyDriver driver,
								 DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W,
								 GenericInputSequence h, List<LmTrace> globalTraces,
								 Map<GenericOutputSequence, List<HZXWSequence>> hZXWSequences,
								 List<HZXWSequence> zXWSequences,
								 Map<GenericOutputSequence, List<LmTrace>> hWSequences,
								 GenericHomingSequenceChecker hChecker, List<String> inputAlphabet) {
		numberOfInputsApplied = 0;
		this.globalTraces = globalTraces;
		traceSinceReset = globalTraces.get(globalTraces.size() - 1);
		this.W = W;
		this.h = h;
		this.I = inputAlphabet;
		this.driver = driver;
		this.hZXWSequences = hZXWSequences;
		this.zXWSequences = zXWSequences;
		this.hWSequences = hWSequences;
		Q = new HashMap<>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new HWConjecture(driver, this);
		instance = this;
		hResponse2State = new HashMap<>();
		hResponse2Wresponses = new HashMap<>();
		currentState = null;
		expectedTraces = new ArrayList<>();
		assert hChecker.getH().equals(h);
		this.hChecker = hChecker;
		initialStateCharacterization = W.getEmptyCharacterization();
		getInitialCharacterization();
		if (initialStateCharacterization.isComplete()) {
			getFullyQualifiedState(initialStateCharacterization);
			getInitialCharacterization();
		}
	}

	/**
	 * Extends the input alphabet I with the symbols in delta. No input symbol in delta must be contained in the old
	 * input alphabet I. The symobls in delta are assumed to be undefined for all states learned so far. This method
	 * adds loops to all learned states for the symbols in delta.
	 *
	 * @param delta
	 */
	public void extendInputAlphabet(List<String> delta) {
		for (var state : getStates()) {
			state.extendInputAlphabet(delta);
			for (var sym : delta) {

				FullyKnownTrace loop = new FullyKnownTrace(state, new LmTrace(sym,
						OUTPUT_FOR_UNDEFINED_INPUT), state);
				addFullyKnownTrace(loop);
			}
		}
		I.addAll(delta);

	}

	private void extendTrace(String input, String output) {
		numberOfInputsApplied++;
		traceSinceReset.append(input, output);
	}

	public String walkWithoutCheck(String input, String output,
								   List<GenericHNDException> hExceptions) {
		extendTrace(input, output);
		String expectedOutput = null;
		// check for Non-Determinism after homing sequence
		try {
			hChecker.apply(input, output);
		} catch (GenericHNDException e) {
			if (hExceptions != null)
				hExceptions.add(e);
		}
		if (currentState != null) {
			FullyKnownTrace transition = currentState.getKnownTransition(input);
			if (transition != null) {
				expectedOutput=transition.getTrace().getOutput(0);
				currentState = transition.getEnd();
			} else
				currentState = null;
		}
		return expectedOutput;
	}

	public OutputSequence walkWithoutCheck(LmTrace seq,
			List<GenericHNDException> hExceptions) {
		OutputSequence outputs = new OutputSequence();
		for (int i = 0; i < seq.size(); i++)
			outputs.addOutput(walkWithoutCheck(seq.getInput(i),
					seq.getOutput(i), hExceptions));
		return outputs;
	}

	public void walkWithoutCheckReset() {
		recordReset();
	}

	public String apply(String input) {
		if (Options.getLogLevel() == Options.LogLevel.ALL)
			LogManager.logInfo("expected Traces are ", expectedTraces);
		String output = driver.execute(input);
		extendTrace(input, output);
		// check for Non-Determinism after homing sequence
		hChecker.apply(input, output);

		// checking the compatibility with K
		if (currentState != null) {
			String expectedOutput = currentState.getExpectedKOutput(input);
			if (expectedOutput != null)
				if (!expectedOutput.equals(output))
					throw new InconsistancyWithConjectureAtEndOfTraceException(
							input, expectedOutput, null, output);
		}

		// checking the compatibility with expected traces.
		Collection<TraceTree> nextExpectedTraces = new ArrayList<TraceTree>();
		for (TraceTree traceTree : expectedTraces) {
			if (traceTree == null)
				continue;
			String expectedOutput = traceTree.getOutput(input);
			if (expectedOutput != null && !expectedOutput.equals(output))
				throw new InconsistancyWithConjectureAtEndOfTraceException(
						input, expectedOutput, traceTree, output);
			nextExpectedTraces.add(traceTree.getSubTreeRO(input));
		}
		expectedTraces = nextExpectedTraces;

		// updating the currentState in conjecture
		if (currentState != null) {
			FullyKnownTrace transition = currentState.getKnownTransition(input);
			if (transition != null) {
				if (!transition.getTrace().getOutput(0).equals(output)) {
					throw new InconsistancyWithConjectureAtEndOfTraceException(
							input, transition.getTrace().getOutput(0), null,
							output);
				}
				currentState = transition.getEnd();
				if (currentState != null)
					LogManager
							.logInfo("According to conjecture, we are now in state "
									+ currentState);
				expectedTraces.add(currentState.getExpectedTraces());
				if (Options.getLogLevel() == LogLevel.ALL)
					LogManager.logInfo("adding expected traces ",
							currentState.getExpectedTraces());
			} else
				currentState = null;
		}

		if (currentState != null) {
			//check defined outputs for model and driver states are identical
			for (var trace : currentState.getKnownTransitions()) {
				var in = trace.getTrace().getInput(0);
				var out = trace.getTrace().getOutput(0);
				if (out.equals(OUTPUT_FOR_UNDEFINED_INPUT) && driver.isDefined(in)) {
					LogManager.logInfo("Input " + in + " is undefined in model state " + currentState + ", but " +
							"defined" +
							" in current driver state.");
					apply(in);
				} else if (!out.equals(OUTPUT_FOR_UNDEFINED_INPUT) && !driver.isDefined(in)) {
					LogManager.logInfo("Input " + in + " is defined in model state " + currentState +
							", but undefined" +
							" in current driver state.");
					apply(in);
				}
			}
		}
		//check for new inputs
		if (!I.containsAll(driver.getDefinedInputs())) {
			var delta = driver.getDefinedInputs().stream().filter(not(I::contains)).collect(Collectors.toList());
//			LogManager.logInfo("New inputs discovered: " + delta);
//			extendInputAlphabet(delta);
			throw new NewInputsFoundException(delta);
		}

		return output;
	}

	public OutputSequence apply(InputSequence inputs) {
		return apply((GenericInputSequence) inputs).toFixedOutput();
	}

	public GenericOutputSequence apply(GenericInputSequence inputs) {
		GenericInputSequence.Iterator it = inputs.inputIterator();
		while (it.hasNext()) {
			String input = it.next();
			it.setPreviousOutput(apply(input));
		}
		return it.getResponse();
	}

	public void reset() {
		driver.reset();
		recordReset();
	}

	private void recordReset() {
		traceSinceReset = new LmTrace();
		globalTraces.add(traceSinceReset);
		lastknownState = null;
		lastknownStatePos = 0;
		currentState = initialState;
		hChecker.reset();
		expectedTraces = new ArrayList<>();
	}

	public String getK() {
		StringBuilder s = new StringBuilder("{");
		for (FullyQualifiedState q : Q.values()) {
			for (PartiallyKnownTrace k : q.getK())
				s.append(k);
		}
		s.append("}");
		return s.toString();
	}

	public String getV() {
		StringBuilder VString = new StringBuilder();
		for (FullyQualifiedState q : Q.values()) {
			for (FullyKnownTrace v : q.getVerifiedTrace()) {
				VString.append("(" + v.getStart() + ", " + v.getTrace() + ", "
						+ v.getEnd() + "), ");
			}
		}
		return "{" + VString + "}";
	}

	public void addPartiallyKnownTrace(FullyQualifiedState start,
			LmTrace transition, final LmTrace print) {
		assert transition.size() > 0;
		start.addPartiallyKnownTrace(transition, print);
		readyForReapplyHZXWSequence.addAll(start.pollSequencesNeededInW());
	}

	public void addFullyKnownTrace(FullyKnownTrace v) {
		v.getStart().addFullyKnownTrace(v);
		assert v.getTrace().size() == 1;
		String input = v.getTrace().getInput(0);
		for (LocalizedHZXWSequence localizedSeq : v.getStart()
				.getPendingSequences(input)) {
			localizedSeq.transferPosition++;
			if (v.getEnd().addLocalizedHZXWSequence(localizedSeq)) {
				readyForReapplyHZXWSequence.add(localizedSeq);
			}
		}
	}

	public FullyQualifiedState getCurrentState() {
		return currentState;
	}

	public void setCurrentState(FullyQualifiedState currentState) {
		assert (this.currentState == null || currentState == this.currentState);
		this.currentState = currentState;
		expectedTraces.add(currentState.getExpectedTraces());
	}

	/**
	 * @return the number of input applied in this dataManager.
	 */
	public int traceSize() {
		return numberOfInputsApplied;
	}

	/**
	 * @return trace since last reset
	 */
	public LmTrace getTraceSinceReset() {
		return traceSinceReset;
	}

	/**
	 * the initial state of conjecture, if it is known;
	 */
	public FullyQualifiedState getInitialState() {
		assert initialState != null || !getInitialCharacterization()
				/*
				 * during debug, notice that if initial characterization is
				 * complete, initial state will be set by side effect inside
				 * this assert.
				 */
				.isComplete() : "characterization is complete so initial state should be already known";
		return initialState;
	}

	/**
	 * get an existing or create a new FullyQualifiedState
	 * 
	 * @param WResponses
	 * @return
	 */
	public FullyQualifiedState getFullyQualifiedState(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses) {
		if (Q.containsKey(WResponses))
			return Q.get(WResponses);
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I,
				conjecture.addState());
		notFullyKnownStates.add(newState);
		StringBuilder s = new StringBuilder();
		for (LmTrace t : WResponses.knownResponses()) {
			s.append(t + ", ");
		}
		LogManager.logInfo("New state discovered : "
				+ newState.toStringWithMatching() + " (" + s + ")");
		Q.put(WResponses, newState);
		return newState;
	}

	/**
	 * check if there is a known state corresponding to the given
	 * characterization.
	 * 
	 * @param WResponses
	 *            the characterization of wanted state
	 * @return true if a state has already been created for this
	 *         characterization.
	 */
	public boolean hasState(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses) {
		return Q.containsKey(WResponses);

	}

	public DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> getW() {
		return W;
	}

	/**
	 * get elements which are not in K (ie we don't know the result of
	 * tr_s(t↓I.w)
	 * 
	 * @param s
	 *            a state
	 * @param t
	 *            a transition(of length 1 or more) typically an input symbol or
	 *            an element of W
	 * @return a set Z s.t. \forall w \in Z, (s, t, w) \notin W
	 */
	public List<? extends GenericInputSequence> getwNotInK(
			FullyQualifiedState s, LmTrace t) {
		assert s != null;
		assert t != null;
		return s.getwNotInK(t);
	}

	/**
	 * get transitions for which we do not know the output (ie we don't know the
	 * result of tr_s(x)
	 * 
	 * @param s
	 * @return a set X s.t. \forall x \in X (s,x) \notin R
	 */
	public Set<String> getxNotInR(FullyQualifiedState s) {
		return s.getUnknowTransitions();
	}

	/**
	 * check if the automata is fully known.
	 * 
	 * @return true if all states are fully known.
	 */
	public Boolean isFullyKnown() {
		return notFullyKnownStates.isEmpty();
	}

	protected void setKnownState(FullyQualifiedState s) {
		notFullyKnownStates.remove(s);
	}



	public Collection<FullyQualifiedState> getStates() {
		return Q.values();
	}

	public LmConjecture getConjecture() {
		return conjecture;
	}

	/**
	 * @param WResponses
	 * @return states in driver matching this WResponses (or null if driver is
	 *         not available)
	 */
	public List<State> getDriverStates(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses) {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
			Mealy automata = tDriver.getAutomata();
			List<State> states = new ArrayList<>();
			for (State s : automata.getStates()) {
				boolean stateMatch = true;
				for (LmTrace wTrace : WResponses.knownResponses()) {
					if (!automata.apply(wTrace.getInputsProjection(), s)
							.equals(wTrace.getOutputsProjection())) {
						stateMatch = false;
						break;
					}
				}
				if (stateMatch)
					states.add(s);
			}
			return states;
		}
		return null;
	}

	public void exportConjecture() {
		StringBuilder s = new StringBuilder();
		int node_n = 0;
		for (Map.Entry<GenericOutputSequence, FullyQualifiedState> e : hResponse2State
				.entrySet()) {
			s.append("\t unknown_" + node_n
					+ " [shape=none,label=\"homing\",fontcolor=blue];\n");
			s.append("\t unknown_" + node_n + " -> " + e.getValue()
					+ " [style=dashed,color=blue,fontcolor=blue,label=\""
					+ h.buildTrace(e.getKey()) + "\"]" + "\n");
			node_n++;
		}
		for (FullyQualifiedState q : Q.values()) {
			q.getExpectedTraces().exportToDot(s, q.toString());
		}
		conjecture.exportToDot(s.toString());
	}

	/**
	 * Check for inconsistencies between h and conjecture. The inconsistency
	 * detected here is a difference between a response from a state and a
	 * response recorded in mapping. This function also check partial mapping.
	 * 
	 * Characterization of states might be not consistent with built
	 * transitions, in this case, thrown exception can have an empty distinction
	 * sequence.
	 * 
	 * @param s
	 *            the starting state from which homing sequence will be applied
	 * @return true if the state match a recorded mapping, or null if the
	 *         mapping is not recorded
	 * @throws InconsistancyHMappingAndConjectureException
	 *             if an inconsistency is discovered, either from complete or
	 *             partial mapping.
	 */
	public Boolean isCompatibleWithHMapping(State s)
			throws InconsistancyHMappingAndConjectureException {
		GenericOutputSequence output = conjecture.apply(h, s);
		State targetState = conjecture.applyGetState(h, s);
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> partialMapping = hResponse2Wresponses
				.get(output);
		if (partialMapping != null) {
			for (LmTrace wTrace : partialMapping.knownResponses()) {
				GenericOutputSequence targetResponse = conjecture
						.apply(wTrace.getInputsProjection(), targetState);
				GenericOutputSequence traceResponse = wTrace
						.getOutputsProjection();
				if (!targetResponse.equals(traceResponse)) {
					FullyQualifiedState mappedState = hResponse2State
							.get(output);

					throw new InconsistancyHMappingAndConjectureException(s,
							targetState, output, mappedState,
							wTrace.getInputsProjection(),							targetResponse, traceResponse);
				}
			}
			if (partialMapping.isComplete()) {
				FullyQualifiedState mappedState = hResponse2State.get(output);
				assert mappedState != null;
				if (mappedState.getState() == targetState)
					return true;
				else
					throw new InconsistancyHMappingAndConjectureException(s,
							targetState, output, mappedState, null, null, null);
			}
		} else {
			assert hResponse2State.get(output) == null;
		}
		return null;
	}

	public List<LocalizedHZXWSequence> getAndResetReadyForReapplyHZXWSequence() {
		List<LocalizedHZXWSequence> r = readyForReapplyHZXWSequence;
		readyForReapplyHZXWSequence = new ArrayList<>();
		return r;
	}

	/**
	 * notify the dataManager that we are at the end of an application of homing
	 * sequence which identified the current state.
	 * 
	 * @param s
	 *            the state identified at end of homing sequence.
	 */
	public void endOfH(FullyQualifiedState s) {
		lastknownState = s;
		lastknownStatePos = traceSinceReset.size();
		setCurrentState(s);
	}

	/**
	 * 
	 * @see #getLastKnownState()
	 */
	public int getLastKnownStatePos() {
		assert initialState != null || lastknownState != null;
		return lastknownStatePos;
	}

	/**
	 * get the last state identified without conjecture (i.e. identified only
	 * with homing sequence or reset). This state was seen before input at
	 * position {@link #getLastKnownStatePos()} in trace.
	 * 
	 * @return the last state identified after homing sequence in trace or null
	 *         if homing sequence wasn't applied.
	 */
	public FullyQualifiedState getLastKnownState() {
		if (lastknownState == null) {
			assert initialState != null;
			return initialState;
		}
		return lastknownState;
	}

	/**
	 * try to characterize initial state with already existing traces. If the
	 * characterization is complete and if the state already exists, then the
	 * state is labeled as initial state.
	 * 
	 * @return the characterization of initial state, complete or partial.
	 * @warning The characterization returned is the internal characterization.
	 *          It can be completed with real element, but not with guessing.
	 */
	public Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> getInitialCharacterization() {
		if (initialState != null)
			return initialStateCharacterization;
		for (GenericInputSequence w : initialStateCharacterization
				.unknownPrints()) {
			for (LmTrace trace : globalTraces) {
				GenericOutputSequence wResponse = trace.getOutput(w);
				if (wResponse != null) {
					initialStateCharacterization.addPrint(w, wResponse);
					break;
				}
			}
		}
		if (hasState(initialStateCharacterization)) {
			initialState = getFullyQualifiedState(initialStateCharacterization);
			LogManager.logInfo(
					"Initial state characterized. this is " + initialState);
			conjecture.setInitialState(initialState.getState());
			for (HZXWSequence seq : zXWSequences) {
				LocalizedHZXWSequence localizedSeq = new LocalizedHZXWSequence(
						seq);
				if (initialState.addLocalizedHZXWSequence(localizedSeq)) {
					readyForReapplyHZXWSequence.add(localizedSeq);
				}
			}
		}
		return initialStateCharacterization;
	}

	public int getTotalResetNb() {
		return globalTraces.size() - 1;
	}

	public List<String> getInputAlphabet() {
		return I;
	}
}
