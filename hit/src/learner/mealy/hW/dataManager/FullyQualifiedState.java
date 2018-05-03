package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;

import tools.loggers.LogManager;

import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.distinctionStruct.Characterization;

public class FullyQualifiedState{
	private final Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses;
	// TODO V should be a mapping from String to FullyKnownTrace (or can be
	// removed because it will duplicate T (which already duplicate the
	// conjecture))
	private Map<LmTrace, FullyKnownTrace> V;//FullyKnownTrace starting from this node
	private Map<String, PartiallyKnownTrace> K;//PartialyllyKnownTrace starting from this node
	private Map<String, FullyKnownTrace> T;//Fully known transitions starting from this node
	private Set<String> R_;//Complementary set of R : unknown transition
	private final State state;
	
	private TraceTree expectedTraces;
	private Map<String, List<LocalizedHZXWSequence>> toLocalizeHZXWSequences = new HashMap<>();
	private  List<LocalizedHZXWSequence>notYetInWSequences=new LinkedList<>();

	private List<State> driverStates;
	
	public List<LocalizedHZXWSequence> getPendingSequences(String input) {
		List<LocalizedHZXWSequence> list = toLocalizeHZXWSequences.get(input);
		if (list == null) {
			list = new ArrayList<>();
			toLocalizeHZXWSequences.put(input, list);
		}
		return list;
	}

	/**
	 * Get dictionary elements which can be reused to characterize states at end
	 * of transition.This method has most interest with adaptive W-set because a
	 * sequence can be useless at one time and will be needed later to complete
	 * characterization of a state. The elements returned are removed from
	 * storage of this state and will not be returned on another call.
	 * 
	 * @return the elements which will bring information to characterize the
	 *         state after one transition or which can create a W-ND.
	 * @see #hZXWSequenceIsInNeededW(LocalizedHZXWSequence)
	 */
	List<LocalizedHZXWSequence> pollSequencesNeededInW() {
		List<LocalizedHZXWSequence> result = new ArrayList<>();
		Iterator<LocalizedHZXWSequence> it = notYetInWSequences.iterator();
		while (it.hasNext()) {
			LocalizedHZXWSequence seq = it.next();
			if (hZXWSequenceIsInNeededW(seq)) {
				result.add(seq);
				it.remove();
			}
		}
		return result;
	}

	/**
	 * indicate whether a hzxw sequence can be reused to characterize a state at
	 * the end of a transition.
	 * 
	 * @param localizedSeq
	 *            the sequence to test
	 * @return true if the sequence can help to characterize a state.
	 */
	public boolean hZXWSequenceIsInNeededW(
			LocalizedHZXWSequence localizedSeq) {
		assert localizedSeq.endOfTransferState == this;
		LmTrace transition = localizedSeq.sequence.getTransition();
		assert transition.size() == 1 : "not implemented";
		if (getKnownTransition(transition.getInput(0)) != null)
			return false;
		PartiallyKnownTrace k = getKEntry(transition);
		for (GenericInputSequence print : k.unknownPrints()) {
			if (print.hasAnswer(localizedSeq.sequence.getwResponse())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * indicate if a response to a w sequence is already known for a state at
	 * the end of a transition from this state.
	 * 
	 * @param transition
	 *            the transition after which we should check for the w.
	 * @param wResponse
	 *            a trace obtained from one w.
	 * @return true if the trace transition.wResponse was observed and recorded
	 *         from this state.
	 */
	public boolean kPrintIsKnown(LmTrace transition, LmTrace wResponse) {
		assert transition.size() == 1 : "not implemented";
		FullyKnownTrace t = getKnownTransition(transition.getInput(0));
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> charac;
		if (t != null)
			charac = getWResponses();
		else
			charac = getKEntry(transition).getCharacterization();
		for (LmTrace trace : charac.knownResponses()) {
			if (wResponse.equals(trace))
				return true;
		}
		return false;
	}

	/**
	 * Place a localized sequence as far as possible in the graph, according to
	 * the transfer sequence. If we reach the state at the end of transfer
	 * sequence, the {@link LocalizedHZXWSequence#endOfTransferState
	 * endOfTransferState} of sequence is set and {@code true} is returned if
	 * the print of the sequence can be use to refine characterization of state
	 * at end of transition or if the sequence creates a W-ND.
	 * 
	 * @param sequence
	 *            the localized sequence to place.
	 * @return true if the transfer sequence ends in this state and the
	 *         wResponse can improve characterization of state at end of
	 *         transition or if the sequence is inconsistent with this state,
	 *         false otherwise.
	 */
	public boolean addLocalizedHZXWSequence(LocalizedHZXWSequence sequence) {
		assert sequence.transferPosition <= sequence.sequence
				.getTransferSequence().size();
		assert sequence.endOfTransferState == null;
		if (sequence.transferPosition == sequence.sequence.getTransferSequence()
				.size()) {
			sequence.endOfTransferState = this;
			LmTrace transition = sequence.sequence.getTransition();
			assert transition.size() == 1 : "not implemented";
			String knownTransitionOutput = getExpectedKOutput(
					transition.getInput(0));
			if (knownTransitionOutput != null
					&& !knownTransitionOutput.equals(transition.getOutput(0)))
				return true;
			String expectedTracesOutput = expectedTraces
					.getOutput(transition.getInput(0));
			if (expectedTracesOutput != null
					&& !transition.getOutput(0).equals(expectedTracesOutput))
				return true;
			if (hZXWSequenceIsInNeededW(sequence)) {
				return true;
			} else {
				assert !notYetInWSequences.contains(sequence);
				notYetInWSequences.add(sequence);
				return false;
			}
		} else {
			String input = sequence.sequence.getTransferSequence()
					.getInput(sequence.transferPosition);
			FullyKnownTrace t = getKnownTransition(input);
			if (t == null) {
				getPendingSequences(input).add(sequence);
				return false;
			} else {
				String output = sequence.sequence.getTransferSequence()
						.getOutput(sequence.transferPosition);
				if (!output.equals(t.getTrace().getOutput(0))) {
					return true;
				}
				sequence.transferPosition++;
				return t.getEnd().addLocalizedHZXWSequence(sequence);
			}
		}
	}

	protected FullyQualifiedState(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses,
			Collection<String> inputSymbols, State state) {
		this.WResponses = WResponses;
		this.state = state;
		R_ = new HashSet<String>(inputSymbols);
		K = new HashMap<String, PartiallyKnownTrace>();
		V = new HashMap<LmTrace, FullyKnownTrace>();
		T = new HashMap<String, FullyKnownTrace>();
		driverStates = SimplifiedDataManager.instance.getDriverStates(WResponses);
		if (driverStates!=null && driverStates.size()==0)
			SimplifiedDataManager.instance.identifiedFakeStates.add(this);
		expectedTraces=new TraceTree();
		for (LmTrace trace : WResponses.knownResponses()) {
			if (!expectedTraces.addTrace(trace)) {
				LogManager.logWarning("incoherent characterization : «" + trace
						+ "» is incoherent with previous traces");
				LogManager.logConsole("incoherent characterization : «" + trace
						+ "» is incoherent with previous traces");
				throw new RuntimeException("invalid characterization");
			}
		}
	}
	
	public Boolean equals(FullyQualifiedState other){
		return WResponses.equals(other.WResponses);
	}

	public TraceTree getExpectedTraces() {
		return expectedTraces;
	}

	/**
	 * this method must be called by DataManager because in order to have T and V coherent
	 * @param t a trace starting from this state
	 */
	protected boolean addFullyKnownTrace(FullyKnownTrace v){
		assert v.getStart() == this;
		if (V.containsKey(v.getTrace())){
			return false;
		}
		if (Options.LOG_LEVEL != Options.LogLevel.LOW)
			LogManager.logInfo("New transition found : " + v);
		LinkedList<LmTrace> toRemove = new LinkedList<LmTrace>();
		for (FullyKnownTrace knownV : V.values()){
			if (v.getTrace().equals(knownV.getTrace().subtrace(0, v.getTrace().size()))){
				FullyKnownTrace vToAdd = new FullyKnownTrace(v.getEnd(), knownV.getTrace().subtrace(v.getTrace().size(), knownV.getTrace().size()), knownV.getEnd());
				if (Options.LOG_LEVEL != Options.LogLevel.LOW)
					LogManager.logInfo("Split transition : " + v + " + " + vToAdd);
				SimplifiedDataManager.instance.addFullyKnownTrace(vToAdd);
				toRemove.add(knownV.getTrace());
			}
		}
		while (!toRemove.isEmpty()){
			LmTrace vtoRemove = toRemove.poll();
			V.remove(vtoRemove);
		}
		assert v.getTrace().size() == 1;// otherwise, we should not remove it
										// from K
		K.remove(v.getTrace().getInput(0));
		
		V.put(v.getTrace(), v);
		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			LogManager.logInfo("V is now : " + SimplifiedDataManager.instance.getV());
		if (v.getTrace().size() == 1){
			LmConjecture conjecture = SimplifiedDataManager.instance.getConjecture();
			conjecture.addTransition(new MealyTransition(conjecture, v.getStart().getState(), v.getEnd().getState(), v.getTrace().getInput(0), v.getTrace().getOutput(0)));
			if (Options.LOG_LEVEL == Options.LogLevel.ALL)
				SimplifiedDataManager.instance.exportConjecture();
			T.put(v.getTrace().getInput(0),v);
			R_.remove(v.getTrace().getInput(0));//the transition with this symbol is known
			if (R_.isEmpty()){
				if (Options.LOG_LEVEL != Options.LogLevel.LOW)
					LogManager.logInfo("All transitions from state " + this + " are known.");
				SimplifiedDataManager.instance.setKnownState(this);
			}
		}
		InputSequence inputs = v.getTrace().getInputsProjection();
		OutputSequence outputs = v.getTrace().getOutputsProjection();
		if (expectedTraces.contains(inputs)) {
			assert expectedTraces.getOutput(inputs).equals(
					outputs) : "this trace should have been checked before";
			TraceTree subTree = expectedTraces.pollTree(v.getTrace()
					.getInputsProjection());
			InconsistancyWhileMergingExpectedTracesException inconsistency = v
					.getEnd().addExpectedTraces(subTree);
			if (inconsistency != null) {
				throw inconsistency;
			}
		}
		//clean K ?
		return true;
	}

	public InconsistancyWhileMergingExpectedTracesException addExpectedTrace(
			LmTrace trace) {
		TraceTree t = new TraceTree();
		t.addTrace(trace);
		return addExpectedTraces(t);
	}

	public InconsistancyWhileMergingExpectedTracesException addExpectedTraces(
			TraceTree newTraces) {
		if (newTraces==null)
			return null;
		for (FullyKnownTrace v:V.values()){
			InputSequence inSeq=v.getTrace().getInputsProjection();
			OutputSequence outSeq = v.getTrace().getOutputsProjection();
			OutputSequence traceOut=newTraces.getOutput(inSeq);
			assert traceOut != null || v.getTrace()
					.size() == 1 : "there might be prefixes of v.getTrace() in expectedTraces which are not checked";
			if (traceOut != null && !outSeq.equals(traceOut)) {
				return new InconsistancyWhileMergingExpectedTracesException(
						this, inSeq, outSeq, traceOut);
			}
			TraceTree subtree=newTraces.pollTree(inSeq);
			InconsistancyWhileMergingExpectedTracesException inconsistency = v
					.getEnd().addExpectedTraces(subtree);
			if (inconsistency != null) {
				inconsistency.addPreviousState(this, inSeq, outSeq);
				return inconsistency;
			}
		}
		InconsistancyWhileMergingExpectedTracesException inconsistency = expectedTraces
				.add(newTraces);
		if (inconsistency != null) {
			inconsistency.setLastState(this);
			return inconsistency;
		}
		return null;
	}
	
	/**
	 * @see learner.mealy.noReset.dataManager.DataManeger.getxNotInR
	 * @return
	 */
	public Set<String> getUnknowTransitions(){
		return R_;
	}
	
	/**
	 * get or create a K entry
	 * @param transition the transition of the K entry
	 * @return a new or an existing K entry
	 */
	private PartiallyKnownTrace getKEntry(LmTrace transition){
		assert transition.size()==1;
		PartiallyKnownTrace k = K.get(transition.getInput(0));
		if (k == null){
			assert getKnownTransition(transition.getInput(
					0)) == null : "do not create a partially known trace if a transition already exists";
			assert getExpectedTraces()
					.getOutput(transition.getInputsProjection()) == null
					|| getExpectedTraces()
							.getOutput(transition.getInputsProjection())
							.equals(transition.getOutputsProjection());
			k = new PartiallyKnownTrace(this, transition, SimplifiedDataManager.instance.getW());
			K.put(transition.getInput(0), k);
		}
		assert k.getTransition().equals(transition);
		return k;
	}

	public String getPartiallTransitionOutput(String input) {
		PartiallyKnownTrace k = K.get(input);
		if (k == null)
			return null;
		assert k.getTransition().size() == 1;
		return k.getTransition().getOutput(0);
	}

	public FullyKnownTrace getKnownTransition(String input) {
		assert T.get(input) == null
				|| V.get(T.get(input).getTrace()).equals(T.get(input));
		return T.get(input);
	}
	
	protected void addPartiallyKnownTrace(LmTrace transition, LmTrace print) {
		PartiallyKnownTrace k = getKEntry(transition);
		k.addPrint(print);
		LmTrace trace = new LmTrace();
		trace.append(transition);
		trace.append(print);
		boolean result = expectedTraces.addTrace(trace);
		assert (result);
	}
	
	/**
	 * @see mealy.noReset.dataManager.SimplifiedDataManager.getwNotInK
	 */
	protected List<? extends GenericInputSequence> getwNotInK(
			LmTrace transition) {
		assert !V.containsKey(transition);
		PartiallyKnownTrace k = K.get(transition.getInput(0));
		if (k == null)
			return SimplifiedDataManager.instance.getW()
					.getEmptyCharacterization().getUnknownPrints();
		return k.getUnknownPrints();
	}

	protected String getExpectedKOutput(String input) {
		PartiallyKnownTrace k = K.get(input);
		if (k == null)
			return null;
		else
			return k.getTransition().getOutput(0);
	}

	public String toString(){
		return state.toString();
	}
	public String toStringWithMatching() {
		if (driverStates == null)
			return state.toString();
		if (driverStates.isEmpty())
			return state.toString() + " (not matching any state in driver)";
		return state.toString() + " (matching states " + driverStates
				+ " in driver)";
	}


	public Collection<FullyKnownTrace> getVerifiedTrace() {
		return V.values();
	}

	public State getState() {
		return state;
	}
	
	protected Collection<PartiallyKnownTrace> getK(){
		return K.values();
	}

	public Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> getWResponses() {
		return WResponses;
	}
}
