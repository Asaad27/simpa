package learner.mealy.noReset.dataManager;

import learner.mealy.LmTrace;

import java.util.ArrayList;

/**
 * This class aim to replace the K set.
 */
public class PartiallyKnownTrace {
	private final FullyQualifiedState start;
	private final LmTrace transition; //this is probably in I \cup W. Read algorithm carefully to be sure 
	private ArrayList<ArrayList<String>> WResponses; //a partial footprint of the state  \in WxO
	private ArrayList<ArrayList<String>> unknownPrints;
	private final ArrayList<ArrayList<String>> W; //use at end to keep the order of W elements
	
	public PartiallyKnownTrace(FullyQualifiedState start, LmTrace transition, ArrayList<ArrayList<String>> W){
		this.W = W;
		this.start = start;
		this.transition = transition;
		unknownPrints = new ArrayList<ArrayList<String>>(W);
		WResponses = new ArrayList<ArrayList<String>>();
		for (int i =0; i < W.size(); i++)//allocate all the array
			WResponses.add(null);
	}
	
	protected ArrayList<ArrayList<String>> getUnknownPrints(){
		return unknownPrints;
	}
	
	/**
	 * 
	 * @param print must be in W to bring information, So supposed to be in W
	 * @return false if the print was already known
	 */
	public boolean addPrint(LmTrace print){
		assert W.contains(print.getInputsProjection());
		if (!unknownPrints.remove(print.getInputsProjection())){ //the print wasn't in W or has been already removed
			return false;
		}

		WResponses.set(W.indexOf(print.getInputsProjection()), print.getOutputsProjection());
		
		if (unknownPrints.isEmpty()){
			//we have totally found a transition
			FullyQualifiedState state = DataManager.instance.getFullyQualifiedState(WResponses);//TODO avoid having a loop in this function
			FullyKnownTrace t = new FullyKnownTrace(start, transition, state);
			DataManager.instance.startRecursivity();
			DataManager.instance.addFullyKnownTrace(t);//TODO avoid loop in this call
			DataManager.instance.endRecursivity();
		}
		return true;
	}
	
	public LmTrace getTransition(){
		return transition;
	}

	public FullyQualifiedState getStart() {
		return start;
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for (int i = 0 ; i < W.size(); i++)
			s.append("(" + start + ", " + transition + ", " + W.get(i) + "),");
		return s.toString();
	}
}
