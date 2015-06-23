package learner.mealy.noReset.dataManager;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.log4j.lf5.StartLogFactor5;

import learner.mealy.LmConjecture;
import automata.mealy.MealyTransition;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;
import drivers.mealy.MealyDriver;

public class DataManager {
	protected static DataManager instance;//TODO either make a proper singleton either do something else
	private MealyDriver driver;
	private LmTrace trace;
	private ArrayList<FullyQualifiedState> C;//Identified states in trace
	private ArrayList<ArrayList<String>> WInTrace;//Identified w-sequences in trace //TODO check that a W cannot be a prefix of another W
	private ArrayList<ArrayList<ArrayList<String>>> WInTraceReversed;//Identified w-sequences in trace but indexed by their last position
	private final ArrayList<ArrayList<String>> W; //Characterization set
	private final ArrayList<String> I;//Input Symbols
	private Map<ArrayList<ArrayList<String>>, FullyQualifiedState> Q;//known states
	private Set<FullyQualifiedState> notFullyKnownStates;//Fully qualified states with undefined transitions
	private LmConjecture conjecture;
	private int recursivity; //for log

	
	public DataManager(MealyDriver driver, ArrayList<ArrayList<String>> W){
		this.trace = new LmTrace();
		this.W = W;
		this.I = new ArrayList<String>(driver.getInputSymbols());
		this.driver = driver;
		C = new ArrayList<FullyQualifiedState>();
		C.add(null);
		WInTrace = new ArrayList<ArrayList<String>>();
		WInTrace.add(null);
		WInTraceReversed = new ArrayList<ArrayList<ArrayList<String>>>();
		WInTraceReversed.add(new ArrayList<ArrayList<String>>());
		Q = new HashMap<ArrayList<ArrayList<String>>, FullyQualifiedState>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new LmConjecture(driver);
		recursivity = 0;
		instance = this;
		driver.reset();
	}
	
	public String apply(String input){
		LogManager.logInfo("transition n°"+trace.size());
		startRecursivity();
		String output = driver.execute(input);
		trace.append(input,output);
		C.add(null);
		WInTrace.add(null);
		WInTraceReversed.add(new ArrayList<ArrayList<String>>());
		for (ArrayList<String> w : W){//TODO we can make an optimized version of that
			if (trace.subtrace(traceSize()-w.size(), traceSize()).getInputsProjection().equals(w)){
				WInTrace.set(traceSize()-w.size(),w);
				WInTraceReversed.get(traceSize()).add(w);
				if (traceSize()-w.size()-1 >=0 && getC(traceSize()-w.size()-1) != null)
					updateK(traceSize()-w.size()-1);
				for (ArrayList<String> w2 : WInTraceReversed.get(traceSize()-w.size()))
					if (traceSize() - w.size() - w2 .size() >= 0 && getC(traceSize()-w.size()-w2.size()) != null)
						updateK(traceSize()-w.size()-w2.size());
			}
		}
		for (FullyQualifiedState q : Q.values()){
			for (FullyKnownTrace t : q.getVerifiedTrace()){
				assert traceSize() - t.getTrace().size() >= 0 : "verified trace are supposed to be elements of W and the first states are not supposed to be known";
				if (C.get(traceSize() - t.getTrace().size()) == t.getStart() && 
						getSubtrace(traceSize() - t.getTrace().size(), traceSize()).equals(t.getTrace()))
					setC(traceSize(), t.getEnd());
			}
		}
		endRecursivity();
		return output;
	}
	
	public ArrayList<String> apply(ArrayList<String> inputs){
		ArrayList<String> outputs = new ArrayList<String>();
		for (String input : inputs)
			outputs.add(apply(input));
		return outputs;
	}
	
	public String getK(){
		StringBuilder s = new StringBuilder("{");
		for (FullyQualifiedState q : Q.values()){
			for (PartiallyKnownTrace k : q.getK())
				s.append(k);
		}
		s.append("}");
		return s.toString();
	}
	
	public String getV(){
		StringBuilder VString = new StringBuilder();
		for (FullyQualifiedState q : Q.values()){
			for (FullyKnownTrace v : q.getVerifiedTrace()){
				VString.append("(" + v.getStart() + ", " + v.getTrace() + ", " + v.getEnd() + "), ");
			}
		}
		return "{" + VString + "}";
	}
	public boolean addPartiallyKnownTrace(FullyQualifiedState start, LmTrace transition, LmTrace print){
		assert transition.size() > 0;
		assert W.contains(print.getInputsProjection());
		return start.addPartiallyKnownTrace(transition,print);
	}

	protected void addFullyKnownTrace(FullyKnownTrace v){
		v.getStart().addFullyKnownTrace(v);
	}
	
	public FullyQualifiedState getC(int pos){
		return C.get(pos);
	}
	
	public void setC(int pos,FullyQualifiedState s){
		assert s != null;
		assert C.get(pos) == null || C.get(pos) == s : "Relabelling " + C.get(pos) + " with " + s + " at pos " + pos;
		if (C.get(pos) != null){
			return;
		}
		C.set(pos, s);
		logRecursivity("Labelling trace : position " + pos + " is now " + s);
		startRecursivity();
		for (FullyQualifiedState q : Q.values()){
			for (FullyKnownTrace v : q.getVerifiedTrace())
				if (s == v.getStart() && getSubtrace(pos, pos+v.getTrace().size()).equals(v.getTrace()))
					setC(pos + v.getTrace().size(), v.getEnd());
			
		}
		updateK(pos);
		updateV(pos);
		endRecursivity();
	}
	
	public int traceSize(){
		return trace.size();
	}
	
	public LmTrace getSubtrace(int start, int end){
		return trace.subtrace(start, end);
	}
	
	public void updateCKVT(){
		logRecursivity("full C,K,V,T update, should do nothing (and then this func call should be removed)");
		startRecursivity();
		for (FullyQualifiedState q : Q.values()){
			for (FullyKnownTrace t : q.getVerifiedTrace())
				updateC(t);
		}
		for (int i = 0; i <= traceSize(); i++){
			if (getC(i) != null){
				updateK(i);
				updateV(i);
			}
		}
		
		
		
		boolean isUpToDate;
		do{
			isUpToDate = true;
			//rule 1
			for (int i = 1; i < traceSize(); i++)
				if (getC(i) != null){
					int j = i+1;
					while(j < traceSize() && getC(j) == null){
						j++;
					}
					if (getC(j) != null){
						boolean alreadyKnown = false;
						for (FullyQualifiedState q : Q.values()){
							for (FullyKnownTrace v : q.getVerifiedTrace()){
								if (v.getStart() == getC(i) &&
										v.getEnd() == getC(j) &&
										v.getTrace().equals(getSubtrace(i, j)))
									alreadyKnown = true;
							}
						}
						if (!alreadyKnown){
							isUpToDate = false;
							LogManager.logError("rule 1 : V not up to date : ");
							addFullyKnownTrace(new FullyKnownTrace(getC(i), getSubtrace(i, j), getC(j)));
						}
					}
				}
			
			//rule 2
			
			//rule 3
			for (int i = 0; i< traceSize(); i++)
				if (getC(i) != null)
					for(ArrayList<String> w : W){
						//first check for input symbols
						if (getSubtrace(i+1, i+1+w.size()).getInputsProjection().equals(w) &&
								addPartiallyKnownTrace(getC(i), getSubtrace(i, i+1), getSubtrace(i+1, i+1+w.size()))){
							isUpToDate = false;
							LogManager.logError("rule 3 : K not up to date");
						}
						//then check for W elements
						for(ArrayList<String> w1 : W)
							if (getSubtrace(i, i+w1.size()).getInputsProjection().equals(w1) && 
									getSubtrace(i+w1.size(), i+w1.size()+w.size()).getInputsProjection().equals(w) &&
									addPartiallyKnownTrace(getC(i), getSubtrace(i, i+w1.size()), getSubtrace(i+w1.size(), i+w1.size()+w.size()))){
								isUpToDate = false;
								LogManager.logError("rule 3 : K not up to date");
							}
					}
				
			//rule 4
			
			//rule 5
			for (FullyQualifiedState q : Q.values())
				for (FullyKnownTrace v : q.getVerifiedTrace())
					for (int i =0; i < traceSize(); i++)
						if (getC(i) == v.getStart() && 
						getSubtrace(i, i+v.getTrace().size()).equals(v.getTrace()) &&
						getC(i+v.getTrace().size()) == null){
							isUpToDate = false;
							LogManager.logError("rule 5 : C not up to date");
							setC(i+v.getTrace().size(),v.getEnd());
						}
					
			
			
		}while(!isUpToDate);
		endRecursivity();
	}
	
	/**
	 * rule 1 in the algorithm
	 * @param pos the position of a discovered state.
	 */
	protected void updateV(int pos){
		assert C.get(pos) != null;
		int otherPos = pos-1;
		while (otherPos>0 && C.get(otherPos) == null)
			otherPos--;
		FullyQualifiedState other = C.get(otherPos);
		if (other != null){
			FullyKnownTrace t = new FullyKnownTrace(other, getSubtrace(otherPos, pos), C.get(pos));
			addFullyKnownTrace(t);
		}
		
		if (pos == traceSize())
			return;
		otherPos = pos+1; 
		while (otherPos < traceSize() && C.get(otherPos) == null)
			otherPos++;
		other = C.get(otherPos);
		if (other != null){
			FullyKnownTrace t = new FullyKnownTrace(C.get(pos), getSubtrace(pos, otherPos), other);
			addFullyKnownTrace(t);
		}
	}
	
	/**
	 * rule 3 in algorithm
	 * @param pos the position of a discovered state
	 */
	protected void updateK(int pos){
		assert C.get(pos) != null;
		FullyQualifiedState s = C.get(pos);
		//try to find an element of I
		if (pos+1 <= traceSize() && WInTrace.get(pos+1) != null && C.get(pos+1) == null){
			addPartiallyKnownTrace(s, getSubtrace(pos, pos+1), getSubtrace(pos+1, pos+1+WInTrace.get(pos+1).size()));
		}
		//try to find an element of W //TODO check if this do not increase complexity
		ArrayList<String> transition = WInTrace.get(pos);
		if (transition != null && C.get(pos+transition.size()) == null && WInTrace.get(pos+transition.size()) != null){
			addPartiallyKnownTrace(s, getSubtrace(pos, pos+transition.size()), getSubtrace(pos+transition.size(), pos+transition.size()+WInTrace.get(pos+transition.size()).size()));
		}
	}
	
	/**
	 * rule 5 in algorithm
	 * @param t
	 */
	protected void updateC(FullyKnownTrace v){
		for (int i =0 ; i <= trace.size(); i++){
			if (v.getStart() == C.get(i) && trace.hasSuffix(i, v.getTrace())){
				int newStatePos = i+v.getTrace().size();
				assert C.get(newStatePos) == null || C.get(newStatePos) == v.getEnd() : "trace error : " + C.get(i+v.getTrace().size()) + " must be remplaced by " + v.getEnd() + " at pos " + (i+v.getTrace().size() + " according to " + v);
				if (C.get(newStatePos) == null){
					setC(newStatePos, v.getEnd());
				}
			}
		}
	}

	private Collection<LmTrace> getMatchingWInTrace(int j) {
		Collection<LmTrace> r = new ArrayList<LmTrace>();
		
		for (ArrayList<String> w : W){//TODO we can make an optimized version of that
			if (trace.subtrace(j, j+w.size()).getInputsProjection().equals(w))
				r.add(trace.subtrace(j, j+w.size()));
		}
		return r;
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
	
	protected ArrayList<ArrayList<String>> getW(){
		return W;
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
	public Set<String> getxNotInR(FullyQualifiedState s){
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
		recursivity++;
	}
	protected void endRecursivity(){
		recursivity --;
	}
	protected void logRecursivity(String l){
		StringBuilder s = new StringBuilder();
		for (int i = 1; i < recursivity; i++)
			s.append("  \t|");
		if (recursivity > 0)
			s.append("  \t⊢");
		s.append(l);
		LogManager.logInfo(s.toString());
	}
	
	public String readableTrace(){
		int maxLength=80;
		StringBuilder globalResult = new StringBuilder();
		StringBuilder IOString = new StringBuilder();
		StringBuilder CString = new StringBuilder();
		CString.append("?");
		IOString.append(" ");
		for (int i =0; i < trace.size(); i++){
			StringBuilder IOtransition = new StringBuilder(" " + trace.getInput(i) + "/" + trace.getOutput(i) + " ");
			StringBuilder Ctransition = new StringBuilder();
			for (int j = 1; j< IOtransition.length(); j++)
				Ctransition.append("-");
			Ctransition.append(">");
			String Cstate = (C.get(i+1) == null) ? "?" : C.get(i+1).toString();
			StringBuilder IOState = new StringBuilder();
			for (int j = 0; j < Cstate.length(); j++){
				IOState.append(" ");
			}
			if (CString.length() + Cstate.length() + Ctransition.length() > maxLength){
				globalResult.append(IOString + "\n" + CString +"\n\n");
				IOString = new StringBuilder();
				CString= new StringBuilder();
			}
			IOString.append(IOtransition.append(IOState));
			CString.append(Ctransition+Cstate);
		}
		globalResult.append(IOString + "\n" + CString +"\n");
		return globalResult.toString();
	}
	

	
	
}
