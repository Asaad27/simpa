package learner.mealy.noReset.dataManager;

import learner.mealy.LmTrace;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

/**
 * This class aim to replace the K set.
 */
public class PartiallyKnownTrace {
	private final FullyQualifiedState start;
	private final LmTrace transition; //this is probably in I \cup W. Read algorithm carefully to be sure 
	private List<OutputSequence> WResponses; //a partial footprint of the state  \in WxO
	private List<InputSequence> unknownPrints;
	
	public PartiallyKnownTrace(FullyQualifiedState start, LmTrace transition, List<InputSequence> W){
		this.start = start;
		this.transition = transition;
		unknownPrints = new ArrayList<InputSequence>(W);
		WResponses = new ArrayList<OutputSequence>();
		for (int i =0; i < W.size(); i++)//allocate all the array
			WResponses.add(null);
	}
	
	protected List<InputSequence> getUnknownPrints(){
		return unknownPrints;
	}
	
	/**
	 * 
	 * @param print must be in W to bring information, So supposed to be in W
	 * @return false if the print was already known
	 */
	protected boolean addPrint(LmTrace print){
		assert DataManager.instance.getW().contains(print.getInputsProjection());
		if (!unknownPrints.remove(print.getInputsProjection())){ //the print wasn't in W or has been already removed
			return false;
		}
		WResponses.set(DataManager.instance.getW().indexOf(print.getInputsProjection()), print.getOutputsProjection());
		DataManager.instance.logRecursivity("New print(=a response to W input) found : " + start + " followed by " + transition + " → " + print);
		DataManager.instance.startRecursivity();
		DataManager.instance.logRecursivity("K is now : " + DataManager.instance.getK());
		if (unknownPrints.isEmpty()){// rule 4 in algorithm
			//we have totally found a transition
			FullyQualifiedState state = DataManager.instance.getFullyQualifiedState(WResponses);
			FullyKnownTrace t = new FullyKnownTrace(start, transition, state);
			DataManager.instance.addFullyKnownTrace(t);//TODO avoid loop in this call
		}
		DataManager.instance.endRecursivity();
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
		for (int i = 0 ; i < DataManager.instance.getW().size(); i++){
			if (WResponses.get(i) != null){
				LmTrace t = new LmTrace(DataManager.instance.getW().get(i),WResponses.get(i));
				s.append("(" + start + ", " + transition + ", " + t + "),");
			}
		}
		return s.toString();
	}
}
