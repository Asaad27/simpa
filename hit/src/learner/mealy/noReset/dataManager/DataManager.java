package learner.mealy.noReset.dataManager;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.PriorityQueue;

import learner.mealy.LmConjecture;
import automata.mealy.MealyTransition;
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
	private Set<FullyQualifiedState> notFullyKnownStates;//Fully qualified states with undefined transitions
	private LmConjecture conjecture;
	private int recursivity; //for log
	
	//Maybe V and T should be removed, they are already in FullyQualifiedState
	private ArrayList<FullyKnownTrace> V;
	private ArrayList<FullyKnownTrace> T;//verified transitions

	
	public DataManager(MealyDriver driver, ArrayList<ArrayList<String>> W, GlobalTrace trace){
		this.trace = trace;
		this.W = W;
		this.I = new ArrayList<String>(driver.getInputSymbols());
		T = new ArrayList<FullyKnownTrace>();
		Q = new HashMap<ArrayList<ArrayList<String>>, FullyQualifiedState>();
		V = new ArrayList<FullyKnownTrace>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new LmConjecture(driver);
		recursivity = 0;
		instance = this;
	}
	
	public void addPartiallyKnownTrace(FullyQualifiedState start, LmTrace transition, LmTrace print){
		assert transition.size() > 0;
		assert W.contains(print.getInputsProjection());
		start.addPartiallyKnownTrace(transition,print);

	}

	public void addFullyKnownTrace(FullyKnownTrace t){
		V.add(t);
		t.getStart().addFullyKnownTrace(t);
		if (t.getTrace().size() == 1){
			T.add(t);
			conjecture.addTransition(new MealyTransition(conjecture, t.getStart().getState(), t.getEnd().getState(), t.getTrace().getInput(0), t.getTrace().getOutput(0)));
			logRecursivity("New transition found :" + t);
		}
		//update C ? //already done in FullyQualifiedState
		//clean K ?
	}
	
	// It may be a good thing to implement others methods based on more precise data.
	public void updateC(){
		for (int i =0 ; i < trace.size(); i++){
			for (FullyKnownTrace t : V){
				if (t.getStart() == trace.getC(i) && trace.hasSuffix(i, t.getTrace())){
					assert trace.getC(i+t.getTrace().size()) == null || trace.getC(i+t.getTrace().size()) == t.getEnd() : "labelling error : " + trace.getC(i+t.getTrace().size()) + " is remplaced by " + t.getEnd() + " at pos " + (i+t.getTrace().size());
					if (trace.getC(i+t.getTrace().size()) == null){
						trace.setC(i+t.getTrace().size(), t.getEnd());
					}
					//TODO update K
				}else{
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
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I, conjecture.addState());
		notFullyKnownStates.add(newState);
		logRecursivity("New state discovered : " + newState);
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
		assert s != null;
		assert t != null;
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
	
	/**
	 * check if the automata is fully known.
	 * @return true if all states are fully known.
	 */
	public Boolean isFullyKnown(){
		return notFullyKnownStates.isEmpty();
	}

	protected void setKnownState(FullyQualifiedState s) {
		logRecursivity("All transitions from state " + s + " are known.");
		notFullyKnownStates.remove(s);
	}

	/**
	 * find a shortest path alpha to a FullyQualifiedState with unknown outputs.
	 * @param s the start state
	 * @return an empty list if the state himself has unknown outputs
	 */
	public ArrayList<String> getShortestAlpha(FullyQualifiedState s) {
 		assert s != null;
		class Node{
			public ArrayList<String> path;
			public FullyQualifiedState end;
			public boolean equals(Object o){ if (o instanceof Node) return equals((Node) o); return false;}
			public boolean equals(Node o){return path.equals(o.path) && end.equals(o.end);}
			public String toString(){return path.toString() + "→" + end.toString();}
		}
		class PathComparator implements Comparator<Node>{
			@Override
			public int compare(Node o1, Node o2) {
				int diff = o1.path.size() - o2.path.size();
				return diff;
			}
		}
		PriorityQueue<Node> paths=new PriorityQueue<Node>(10, new PathComparator());
		Node firstNode = new Node();
		firstNode.end = s;
		firstNode.path = new ArrayList<String>();
		paths.add(firstNode);
		while(true){
			firstNode = paths.poll();
			if (!firstNode.end.getUnknowTransitions().isEmpty()){
				LogManager.logInfo("chosen alpha is " + firstNode.path + " that lead in " + firstNode.end);
				return firstNode.path;
			}
			for (FullyKnownTrace t : firstNode.end.getVerifiedTrace()){
				Node childNode = new Node();
				childNode.end = t.getEnd();
				childNode.path = new ArrayList<String>(firstNode.path);
				childNode.path.addAll(t.getTrace().getInputsProjection());
				paths.add(childNode);
			}
			

		}
	}

	public Collection<FullyQualifiedState> getStates() {
		return Q.values();
	}

	public LmConjecture getConjecture() {
		return conjecture;
	}
	
	protected void startRecursivity(){
		logRecursivity("that let us find :");
		recursivity++;
	}
	protected void endRecursivity(){
		recursivity --;
	}
	protected void logRecursivity(String l){
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < recursivity; i++)
			s.append("\t");
		s.append(l);
		LogManager.logInfo(s.toString());
	}
	
	public String readableTrace(){
		int maxLength=80;
		StringBuilder globalResult = new StringBuilder();
		StringBuilder IO = new StringBuilder();
		StringBuilder C = new StringBuilder();
		C.append("?");
		IO.append(" ");
		for (int i =0; i < trace.size(); i++){
			StringBuilder IOtransition = new StringBuilder(" " + trace.getInput(i) + "/" + trace.getOutput(i) + " ");
			StringBuilder Ctransition = new StringBuilder();
			for (int j = 1; j< IOtransition.length(); j++)
				Ctransition.append("-");
			Ctransition.append(">");
			String Cstate = (trace.getC(i+1) == null) ? "?" : trace.getC(i+1).toString();
			StringBuilder IOState = new StringBuilder();
			for (int j = 0; j < Cstate.length(); j++){
				IOState.append(" ");
			}
			if (C.length() + Cstate.length() + Ctransition.length() > maxLength){
				globalResult.append(IO + "\n" + C +"\n\n");
				IO = new StringBuilder();
				C= new StringBuilder();
			}
			IO.append(IOtransition.append(IOState));
			C.append(Ctransition+Cstate);
		}
		globalResult.append(IO + "\n" + C +"\n");
		return globalResult.toString();
	}
}
