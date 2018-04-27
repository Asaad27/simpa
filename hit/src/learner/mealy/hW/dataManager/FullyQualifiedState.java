package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
	private Map<String, List<LocalizedHZXWSequence>> pendingHZXWSequences = new HashMap<>();
	
	private List<State> driverStates;
	
	public List<LocalizedHZXWSequence> getPendingSequences(String input) {
		List<LocalizedHZXWSequence> list = pendingHZXWSequences.get(input);
		if (list == null) {
			list = new ArrayList<>();
			pendingHZXWSequences.put(input, list);

		}
		return list;
	}

	/**
	 * Place a localized sequence as far as possible in the graph, according to
	 * the transfer sequence. If we reach the state at the end of transfer
	 * sequence, the sequence is not recorded in the graph, the
	 * {@link LocalizedHZXWSequence#endOfTransferState} of sequence is set and
	 * <code>true</code> is returned.
	 * 
	 * @param sequence
	 *            the localized sequence to place.
	 * @return true if the transfer sequence ends in this state, else otherwise.
	 */
	public boolean addLocalizedHZXWSequence(LocalizedHZXWSequence sequence) {
		assert sequence.transferPosition <= sequence.sequence
				.getTransferSequence().size();
		assert sequence.endOfTransferState == null;
		if (sequence.transferPosition == sequence.sequence.getTransferSequence()
				.size()) {
			sequence.endOfTransferState = this;
			return true;
		} else {
			String input = sequence.sequence.getTransferSequence()
					.getInput(sequence.transferPosition);
			FullyKnownTrace t = getKnownTransition(input);
			if (t == null) {
				getPendingSequences(input).add(sequence);
				return false;
			} else {
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
			expectedTraces.addTrace(trace);
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
				inconsistency.setLastState(this);
				throw inconsistency;
			}
		}
		//clean K ?
		return true;
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

	public FullyKnownTrace getKnownTransition(String input){
		return T.get(input);
	}
	
	protected void addPartiallyKnownTrace(LmTrace transition, LmTrace print) {
		PartiallyKnownTrace k = getKEntry(transition);
		k.addPrint(print);
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
