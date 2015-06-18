package learner.mealy.noReset.dataManager;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.GlobalTrace;
import tools.loggers.LogManager;
import drivers.mealy.MealyDriver;

public class DataManager {
	public static DataManager instance;//TODO either make a proper singleton either do something else
	private GlobalTrace trace;
	private final ArrayList<ArrayList<String>> W; //Characterization set
	private final ArrayList<String> I;//Input Symbols
	private Map<ArrayList<ArrayList<String>>, FullyQualifiedState> Q;//known states
	
	//Maybe V and T should be removed, they are already in FullyQualifiedState
	private ArrayList<FullyKnownTrace> V;
	private ArrayList<FullyKnownTrace> T;//verified transitions

	
	public DataManager(MealyDriver driver, ArrayList<ArrayList<String>> W, ArrayList<String> I, GlobalTrace trace){
		this.trace = trace;
		this.W = W;
		this.I = I;
		T = new ArrayList<FullyKnownTrace>();
		Q = new HashMap<ArrayList<ArrayList<String>>, FullyQualifiedState>();
		V = new ArrayList<FullyKnownTrace>();
		instance = this;
	}
	
	public void addPartialyKnownTrace(FullyQualifiedState start, LmTrace transition, LmTrace print){
		start.addPartiallyKnownTrace(transition,print);
	}

	public void addFullyKnownTrace(FullyKnownTrace t){
		V.add(t);
		t.getStart().addFullyKnownTrace(t);
		if (t.getTrace().size() == 1)
			T.add(t);
		//update C ? //already done in FullyQualifiedState
		//clean K ?
	}
	
	// It may be a good thing to implement others methods based on more precise data.
	public void updateC(){
		for (int i =0 ; i < trace.size(); i++){
			for (FullyKnownTrace t : T){
				if (t.getStart() == trace.getC(i) && trace.hasSuffix(i, t.getTrace())){
					trace.setC(i+t.getTrace().size(), t.getEnd());
					//TODO udpate K
				}
			}
		}
	}

	/**
	 * get an existing or create a new FullyQualifiedState
	 * @param wResponses
	 * @return
	 */
	public FullyQualifiedState getFullyQualifiedState(ArrayList<ArrayList<String>> WResponses) {
		if (Q.containsKey(WResponses))
			return Q.get(WResponses);
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I);
		Q.put(WResponses,newState);
		return newState;
	}
	
	public ArrayList<ArrayList<String>> getW(){
		return W;
	}

	public ArrayList<String> getInputSymbols() {
		return I;
	}

	/**
	 * get elements which are not in K (ie we don't know the result of tr_s(t↓I.w)
	 * @param s a state
	 * @param t a transition(of length 1 or more) typically an input symbol or an element of W
	 * @return a set Z s.t. \forall w \in Z, (s, t, w) \notin W
	 */
	public ArrayList<ArrayList<String>> getwNotInK(FullyQualifiedState s, LmTrace t){
		return s.getwNotInK(t);
	}
	
	/**
	 * get transitions for which we do not know the output (ie we don't know the result of tr_s(x)
	 * @param s
	 * @return a set X s.t. \forall x \in X (s,x) \notin R
	 */
	public ArrayList<String> getxNotInR(FullyQualifiedState s){
		return s.getUnknowTransitions();
	}
}
