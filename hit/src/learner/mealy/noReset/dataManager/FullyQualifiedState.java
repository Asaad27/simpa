package learner.mealy.noReset.dataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;

import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;

public class FullyQualifiedState{
	private final List<OutputSequence> WResponses;//used to identify the State
	private Map<LmTrace, FullyKnownTrace> V;//FullyKnownTrace starting from this node
	private Map<LmTrace, PartiallyKnownTrace> K;//PartialyllyKnownTrace starting from this node
	private Map<LmTrace, FullyKnownTrace> T;//Fully known transitions starting from this node
	private Set<String> R_;//Complementary set of R : unknown transition
	private final State state;
	
	protected FullyQualifiedState(List<OutputSequence> WResponses, Collection<String> inputSymbols, State state){
		this.WResponses = WResponses;
		this.state = state;
		R_ = new HashSet<String>(inputSymbols);
		K = new HashMap<LmTrace, PartiallyKnownTrace>();
		V = new HashMap<LmTrace, FullyKnownTrace>();
		T = new HashMap<LmTrace, FullyKnownTrace>();
	}
	
	public Boolean equals(FullyQualifiedState other){
		return WResponses.equals(other.WResponses);
	}
	
	public Boolean equals(ArrayList<ArrayList<String>> WResponses){
		return this.WResponses.equals(WResponses);
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
			DataManager.instance.logRecursivity("New transition found : " + v);
		DataManager.instance.startRecursivity();

		LinkedList<FullyKnownTrace> toAdd = new LinkedList<FullyKnownTrace>();
		LinkedList<LmTrace> toRemove = new LinkedList<LmTrace>();
		for (FullyKnownTrace knownV : V.values()){
			if (v.getTrace().equals(knownV.getTrace().subtrace(0, v.getTrace().size()))){
				toAdd.add(new FullyKnownTrace(v.getEnd(), knownV.getTrace().subtrace(v.getTrace().size(), knownV.getTrace().size()), knownV.getEnd()));
				toRemove.add(knownV.getTrace());
			}
		}
		while (!toRemove.isEmpty()){
			LmTrace vtoRemove = toRemove.poll();
			V.remove(vtoRemove);
		}
		while (!toAdd.isEmpty()){
			FullyKnownTrace vToAdd = toAdd.poll();
			if (Options.LOG_LEVEL != Options.LogLevel.LOW)
				DataManager.instance.logRecursivity("Split transition : " + v + " + " + vToAdd);
			DataManager.instance.startRecursivity();
			vToAdd.getStart().addFullyKnownTrace(vToAdd);
			DataManager.instance.endRecursivity();
		}
		
		K.remove(v.getTrace());
		V.put(v.getTrace(), v);
		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			DataManager.instance.logRecursivity("V is now : " + DataManager.instance.getV());
		if (v.getTrace().size() == 1){
			LmConjecture conjecture = DataManager.instance.getConjecture();
			conjecture.addTransition(new MealyTransition(conjecture, v.getStart().getState(), v.getEnd().getState(), v.getTrace().getInput(0), v.getTrace().getOutput(0)));
			if (Options.LOG_LEVEL == Options.LogLevel.ALL)
				conjecture.exportToDot();
			T.put(v.getTrace(),v);
			R_.remove(v.getTrace().getInput(0));//the transition with this symbol is known
			if (R_.isEmpty()){
				if (Options.LOG_LEVEL != Options.LogLevel.LOW)
					DataManager.instance.logRecursivity("All transitions from state " + this + " are known.");
				DataManager.instance.startRecursivity();
				DataManager.instance.setKnownState(this);
				DataManager.instance.endRecursivity();
			}
		}
		DataManager.instance.updateC(v);
		DataManager.instance.endRecursivity();
		//clean K ?
		return true;
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
		PartiallyKnownTrace k = K.get(transition);
		if (k == null){
			k = new PartiallyKnownTrace(this, transition, DataManager.instance.getW());
			K.put(transition, k);
		}
		return k;
	}

	protected boolean addPartiallyKnownTrace(LmTrace transition, LmTrace print) {
		if (V.containsKey(transition))
			return false;
		//try to reduce transition
		for (FullyKnownTrace v : V.values()){
			if (v.equals(transition.subtrace(0, v.getTrace().size())) && v.getTrace().size() < transition.size()){
				if (Options.LOG_LEVEL != Options.LogLevel.LOW)
					DataManager.instance.logRecursivity("Trace reduced using " + v + " : " + v.getEnd() +" followed by "+ transition + " → " +print);
				return v.getEnd().addPartiallyKnownTrace(transition.subtrace(v.getTrace().size(), transition.size()), print);
			}
		}
		PartiallyKnownTrace k = getKEntry(transition);
		return k.addPrint(print);
	}
	
	/**
	 * @see learn.mealy.noReset.dataManager.DataManager.getwNotInK
	 */
	protected List<InputSequence> getwNotInK(LmTrace transition){
		assert !V.containsKey(transition);
		PartiallyKnownTrace k = getKEntry(transition);
		return k.getUnknownPrints();
	}
	
	public String toString(){
		return state.toString();
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

	public List<OutputSequence> getWResponses() {
		return WResponses;
	}
}
