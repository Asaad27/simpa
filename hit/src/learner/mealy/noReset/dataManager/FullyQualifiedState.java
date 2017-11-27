package learner.mealy.noReset.dataManager;

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
import learner.mealy.noReset.dataManager.vTree.StateNode;
import main.simpa.Options;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;

public class FullyQualifiedState{
	private final List<OutputSequence> WResponses;//used to identify the State
	private Map<LmTrace, FullyKnownTrace> V;//FullyKnownTrace starting from this node
	private Map<String, PartiallyKnownTrace> K;//PartialyllyKnownTrace starting from this node
	private Map<String, FullyKnownTrace> T;//Fully known transitions starting from this node
	private Set<String> R_;//Complementary set of R : unknown transition
	private final State state;
	private final StateNode vNode;
	
	private List<State> driverStates;
	
	protected FullyQualifiedState(List<OutputSequence> WResponses, Collection<String> inputSymbols, State state){
		this.WResponses = WResponses;
		this.state = state;
		R_ = new HashSet<String>(inputSymbols);
		K = new HashMap<String, PartiallyKnownTrace>();
		V = new HashMap<LmTrace, FullyKnownTrace>();
		T = new HashMap<String, FullyKnownTrace>();
		if (Options.ICTSS2015_WITHOUT_SPEEDUP)
			vNode = null;
		else
			vNode = new StateNode(this);
		driverStates = SimplifiedDataManager.instance.getDriverStates(WResponses);
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
			LogManager.logInfo("New transition found : " + v);
		if (!Options.ICTSS2015_WITHOUT_SPEEDUP){
			vNode.addFullyKnownTrace(v);
			//DataManager.instance.exportVTreeToDot();
		}
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
		
		K.remove(v.getTrace());
		V.put(v.getTrace(), v);
		if (Options.LOG_LEVEL == Options.LogLevel.ALL)
			LogManager.logInfo("V is now : " + SimplifiedDataManager.instance.getV());
		if (v.getTrace().size() == 1){
			LmConjecture conjecture = SimplifiedDataManager.instance.getConjecture();
			conjecture.addTransition(new MealyTransition(conjecture, v.getStart().getState(), v.getEnd().getState(), v.getTrace().getInput(0), v.getTrace().getOutput(0)));
			if (Options.LOG_LEVEL == Options.LogLevel.ALL)
				conjecture.exportToDot();
			T.put(v.getTrace(),v);
			R_.remove(v.getTrace().getInput(0));//the transition with this symbol is known
			if (R_.isEmpty()){
				if (Options.LOG_LEVEL != Options.LogLevel.LOW)
					LogManager.logInfo("All transitions from state " + this + " are known.");
				SimplifiedDataManager.instance.setKnownState(this);
			}
		}
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
		assert transition.size()==1;
		PartiallyKnownTrace k = K.get(transition.getInput(0));
		if (k == null){
			k = new PartiallyKnownTrace(this, transition, SimplifiedDataManager.instance.getW());
			K.put(transition.getInput(0), k);
		}
		assert k.getTransition().equals(transition);
		return k;
	}

	public FullyKnownTrace getKnownTransition(String input){
		return T.get(input);
	}
	
	protected boolean addPartiallyKnownTrace(LmTrace transition, LmTrace print) {
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

	public List<OutputSequence> getWResponses() {
		return WResponses;
	}
	
	public StateNode getVNode(){
		return vNode;
	}
}
