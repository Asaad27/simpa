package learner.mealy.noReset.dataManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import learner.mealy.LmTrace;

public class FullyQualifiedState{
	private ArrayList<ArrayList<String>> WResponses;//used to identify the State
	private ArrayList<FullyKnownTrace> V;//FullyKnownTrace starting from this node
	private Map<LmTrace, PartiallyKnownTrace> K;//PartialyllyKnownTrace starting from this node
	private ArrayList<FullyKnownTrace> T;//Fully known transitions starting from this node
	private ArrayList<String> R_;//Complementary set of R : unknown transition
	
	public FullyQualifiedState(ArrayList<ArrayList<String>> WResponses, ArrayList<String> inputSymbols){
		this.WResponses = WResponses;
		R_ = (ArrayList<String>) inputSymbols.clone();
		K = new HashMap<LmTrace, PartiallyKnownTrace>();
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
	protected void addFullyKnownTrace(FullyKnownTrace t){
		//TODO We can remove the corresponding PartiallyKnownState
		V.add(t);
		if (t.getTrace().size() == 1){
			T.add(t);
			R_.remove(t.getTrace().getInput(0));//the transition with this symbol is known
		}
	}
	
	public ArrayList<String> getUnknowTransitions(){
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
			k = new PartiallyKnownTrace(this, transition, DataManager.instance.getW(), DataManager.instance.getInputSymbols());
			K.put(transition, k);
		}
		return k;
	}

	protected void addPartiallyKnownTrace(LmTrace transition, LmTrace print) {
		//TODO check if the transition is not even known
		PartiallyKnownTrace k = K.get(transition);
		if (k == null){
			k = new PartiallyKnownTrace(this, transition, DataManager.instance.getW(), DataManager.instance.getInputSymbols());
			K.put(transition, k);
		}
		k.addPrint(print);
	}
	
	/**
	 * @see learn.mealy.noReset.dataManager.DataManager.getwNotInK
	 */
	public ArrayList<ArrayList<String>> getwNotInK(LmTrace transition){
		//TODO check if the transition is not even known (assert)
		PartiallyKnownTrace k = getKEntry(transition);
		return k.getUnknownPrints();
	}
}
