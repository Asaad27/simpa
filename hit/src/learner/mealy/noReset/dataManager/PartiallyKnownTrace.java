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
	
	public PartiallyKnownTrace(FullyQualifiedState start, LmTrace transition, ArrayList<ArrayList<String>> W, ArrayList<String> inputSymbols){
		this.W = W;
		this.start = start;
		this.transition = transition;
		unknownPrints = (ArrayList<ArrayList<String>>) inputSymbols.clone();
		WResponses = new ArrayList<ArrayList<String>>();
		for (int i =0; i < W.size(); i++)//allocate all the array
			WResponses.add(null);
	}
	
	public ArrayList<ArrayList<String>> getUnknownPrints(){
		return unknownPrints;
	}
	
	/**
	 * 
	 * @param print must be in W to bring information, So supposed to be in W
	 */
	public void addPrint(LmTrace print){
		if (!unknownPrints.remove(print.getInputsProjection())) //the print wasn't in W or has been already removed
		return;
		WResponses.set(W.indexOf(print.getInputsProjection()), print.getOutputsProjection());
		
		if (unknownPrints.isEmpty()){
			//we have totally found a transition
			FullyQualifiedState state = DataManager.instance.getFullyQualifiedState(WResponses);//TODO avoid having a loop in this function
			FullyKnownTrace t = new FullyKnownTrace(start, transition, state);
			DataManager.instance.addFullyKnownTrace(t);//TODO avoid loop in this call
		}
	}
	
	public LmTrace getTransition(){
		return transition;
	}

	public FullyQualifiedState getStart() {
		return start;
	}
}
