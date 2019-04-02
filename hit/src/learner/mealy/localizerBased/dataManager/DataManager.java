/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.localizerBased.dataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.localizerBased.LocalizerBasedOptions;
import learner.mealy.localizerBased.dataManager.vTree.AbstractNode;
import learner.mealy.localizerBased.dataManager.vTree.StateNode;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.GraphViz;
import tools.loggers.LogManager;

public class DataManager {
	class WaitingState {public FullyQualifiedState state; public int pos;}
	public static DataManager instance;//TODO either make a proper singleton either do something else
	private MealyDriver driver;
	private LmTrace trace;
	private ArrayList<FullyQualifiedState> C;//Identified states in trace
	private ArrayList<InputSequence> WInTrace;//Identified w-sequences in trace //TODO check that a W cannot be a prefix of another W
	private ArrayList<Collection<InputSequence>> WInTraceReversed;//Identified w-sequences in trace but indexed by their last position
	private final List<InputSequence> W; //Characterization set
	private final ArrayList<String> I;//Input Symbols
	private Map<List<OutputSequence>, FullyQualifiedState> Q;//known states
	private Set<FullyQualifiedState> notFullyKnownStates;//Fully qualified states with undefined transitions
	private LmConjecture conjecture;
	private int recursivity; //for log
	private LinkedList<WaitingState> waitingStates;//the call to setCwhich must be done by the root setC
	private LinkedList<FullyKnownTrace> waitingFullyKnownTrace;
	private boolean lockAdd;//indicate whether an add is currently running.
	private AbstractNode currentVNode;
	protected LocalizerBasedOptions options;
	
	public int maxStates;

	public DataManager(MealyDriver driver, ArrayList<InputSequence> W,
			int maxStates, LocalizerBasedOptions options) {
		this.maxStates = maxStates;
		this.trace = new LmTrace();
		this.W = W;
		this.I = new ArrayList<String>(driver.getInputSymbols());
		this.driver = driver;
		C = new ArrayList<FullyQualifiedState>();
		C.add(null);
		WInTrace = new ArrayList<InputSequence>();
		WInTrace.add(null);
		WInTraceReversed = new ArrayList<Collection<InputSequence>>();
		WInTraceReversed.add(new ArrayList<InputSequence>());
		Q = new HashMap<List<OutputSequence>, FullyQualifiedState>();
		notFullyKnownStates = new HashSet<FullyQualifiedState>();
		conjecture = new LmConjecture(driver);
		recursivity = 0;
		waitingStates = new LinkedList<WaitingState>();
		waitingFullyKnownTrace = new LinkedList<>();
		lockAdd = false;
		instance = this;
		driver.reset();
		currentVNode = null;
		this.options = options;
	}

	public String apply(String input){
		startRecursivity();
		String output = driver.execute(input);
		trace.append(input,output);
		C.add(null);
		WInTrace.add(null);
		WInTraceReversed.add(new ArrayList<InputSequence>());
		for (InputSequence w : W){//TODO we can make an optimized version of that
			if (trace.subtrace(traceSize()-w.getLength(), traceSize()).getInputsProjection().equals(w)){
				WInTrace.set(traceSize()-w.getLength(),w);
				WInTraceReversed.get(traceSize()).add(w);
				if (traceSize()-w.getLength()-1 >=0 && getC(traceSize()-w.getLength()-1) != null)
					updateK(traceSize()-w.getLength()-1);
				for (InputSequence w2 : WInTraceReversed.get(traceSize()-w.getLength()))
					if (traceSize() - w.getLength() - w2 .getLength() >= 0 && getC(traceSize()-w.getLength()-w2.getLength()) != null)
						updateK(traceSize()-w.getLength()-w2.getLength());
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
		if (currentVNode != null)
			currentVNode = currentVNode.getChildOrCreate(input, output);
		//exportVTreeToDot();
		endRecursivity();
		return output;
	}

	public OutputSequence apply(InputSequence inputs){
		OutputSequence outputs = new OutputSequence();
		for (String input : inputs.sequence)
			outputs.addOutput(apply(input));
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
		waitingFullyKnownTrace.add(v);
		doWaitingAdd();
	}

	public FullyQualifiedState getC(int pos){
		return C.get(pos);
	}

	public void setC(int pos,FullyQualifiedState s){
		if (options.useSpeedUp()) {
			if (currentVNode == null){
				AbstractNode n = s.getVNode();
				for (int i = pos; i < traceSize(); i++){
					n = n.getChildOrCreate(trace.getInput(i), trace.getOutput(i));
				}
				currentVNode = n;
				exportVTreeToDot();
			}
		}
		WaitingState ws = new WaitingState();
		ws.pos = pos;
		ws.state = s;
		waitingStates.add(ws);
		doWaitingAdd();
	}

	/**
	 * must be call only if there is no other call.
	 * This method is used in order to avoid highly recursive call which make concurrence exceptions in loop.
	 * @see #doWaitingAdd()
	 * @see #setC(int, FullyQualifiedState)
	 * @param pos
	 * @param s
	 */
	private void setCchecked(int pos,FullyQualifiedState s){
		assert s != null;
		assert C.get(pos) == null || C.get(pos) == s : "Relabelling " + C.get(pos) + " with " + s + " at pos " + pos;
		if (C.get(pos) != null){
			return;
		}
		C.set(pos, s);
		if (Options.getLogLevel() != Options.LogLevel.LOW)
			logRecursivity("Labelling trace : position " + pos + " is now " + s);
		startRecursivity();
		for (FullyQualifiedState q : Q.values()){
			for (FullyKnownTrace v : q.getVerifiedTrace())
				if (s == v.getStart() && getSubtrace(pos, pos+v.getTrace().size()).equals(v.getTrace())){
					setC(pos + v.getTrace().size(), v.getEnd());
				}
		}
		updateK(pos);
		updateV(pos);
		endRecursivity();
	}

	private void doWaitingAdd(){
		if (lockAdd)
			return;
		lockAdd = true;
		boolean upToDate = false;
		while (!upToDate){
			deduceFromVTree();
			upToDate = true;
			FullyKnownTrace v = waitingFullyKnownTrace.poll();
			if (v != null){
				upToDate = false;
				v.getStart().addFullyKnownTrace(v);
			}
			WaitingState ws = waitingStates.poll();
			if (ws  != null){
				upToDate = false;
				setCchecked(ws.pos,ws.state);
			}
		}
		lockAdd = false;
	}

	public int traceSize(){
		return trace.size();
	}

	public LmTrace getSubtrace(int start, int end){
		return trace.subtrace(start, end);
	}

	/**
	 * update C,K,V and T.
	 * @return true if C,K,V,T were already up to date.
	 */
	public boolean updateCKVT(){
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


		boolean wasUpToDate = true;
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
					for(InputSequence w : W){
						//first check for input symbols
						if (getSubtrace(i+1, i+1+w.getLength()).getInputsProjection().equals(w) &&
								getC(i+1) ==null &&
								addPartiallyKnownTrace(getC(i), getSubtrace(i, i+1), getSubtrace(i+1, i+1+w.getLength()))){
							isUpToDate = false;
							LogManager.logError("rule 3 : K not up to date near transition n°" +i);
						}
						//then check for W elements
						for(InputSequence w1 : W)
							if (getSubtrace(i, i+w1.getLength()).getInputsProjection().equals(w1) && 
									getC(i+w1.getLength()) == null &&
									getSubtrace(i+w1.getLength(), i+w1.getLength()+w.getLength()).getInputsProjection().equals(w) &&
									addPartiallyKnownTrace(getC(i), getSubtrace(i, i+w1.getLength()), getSubtrace(i+w1.getLength(), i+w1.getLength()+w.getLength()))){
								isUpToDate = false;
								LogManager.logError("rule 3 : K not up to date near transition n°" +i);
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

			if (!isUpToDate){
				throw new RuntimeException("C,K,V,T were not up to date");
			}

			if (!isUpToDate)
				wasUpToDate = false;
		}while(!isUpToDate);
		endRecursivity();
		return wasUpToDate;
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
		FullyQualifiedState other = (otherPos > 0) ? C.get(otherPos) : null;
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
			addPartiallyKnownTrace(s, getSubtrace(pos, pos+1), getSubtrace(pos+1, pos+1+WInTrace.get(pos+1).getLength()));
		}
		//try to find an element of W //TODO check if this do not increase complexity
		InputSequence transition = WInTrace.get(pos);
		if (transition != null && C.get(pos+transition.getLength()) == null && WInTrace.get(pos+transition.getLength()) != null){
			addPartiallyKnownTrace(s, getSubtrace(pos, pos+transition.getLength()), getSubtrace(pos+transition.getLength(), pos+transition.getLength()+WInTrace.get(pos+transition.getLength()).getLength()));
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

	/**
	 * get an existing or create a new FullyQualifiedState
	 * @param wResponses
	 * @return
	 */
	public FullyQualifiedState getFullyQualifiedState(List<OutputSequence> WResponses) {
		if (Q.containsKey(WResponses))
			return Q.get(WResponses);
		FullyQualifiedState newState = new FullyQualifiedState(WResponses, I, conjecture.addState());
		notFullyKnownStates.add(newState);
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < W.size(); i++){
			s.append(new LmTrace(W.get(i),WResponses.get(i)) + ", ");
		}
		logRecursivity("New state discovered : " + newState + " (" + s + ")");
		if (Options.getLogLevel() != LogLevel.LOW)
			LogManager.logConsole(
					"New state discovered : " + newState + " (" + s + ")\r");
		Q.put(WResponses,newState);
		return newState;
	}

	protected List<InputSequence> getW(){
		return W;
	}

	/**
	 * get elements which are not in K (ie we don't know the result of tr_s(t↓I.w)
	 * @param s a state
	 * @param t a transition(of length 1 or more) typically an input symbol or an element of W
	 * @return a set Z s.t. \forall w \in Z, (s, t, w) \notin W
	 */
	public List<InputSequence> getwNotInK(FullyQualifiedState s, LmTrace t){
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
	public InputSequence getShortestAlpha(FullyQualifiedState s) {
		assert s != null;
		class Node{
			public InputSequence path;
			public FullyQualifiedState end;
			public boolean equals(Object o){ if (o instanceof Node) return equals((Node) o); return false;}
			public boolean equals(Node o){return path.equals(o.path) && end.equals(o.end);}
			public String toString(){return path.toString() + "→" + end.toString();}

			@Override
			public int hashCode() {
				throw new UnsupportedOperationException();
			}
		}
		class PathComparator implements Comparator<Node>{
			@Override
			public int compare(Node o1, Node o2) {
				int diff = o1.path.getLength() - o2.path.getLength();
				return diff;
			}
		}
		PriorityQueue<Node> paths=new PriorityQueue<Node>(10, new PathComparator());
		Node firstNode = new Node();
		firstNode.end = s;
		firstNode.path = new InputSequence();
		paths.add(firstNode);
		List<FullyQualifiedState> reachedStates = new ArrayList<FullyQualifiedState>();
		while(!paths.isEmpty()){
			firstNode = paths.poll();
			if (reachedStates.contains(firstNode.end))
				continue;
			reachedStates.add(firstNode.end);
			if (!firstNode.end.getUnknowTransitions().isEmpty()){
				LogManager.logInfo("chosen alpha is " + firstNode.path + " that lead in " + firstNode.end);
				return firstNode.path;
			}
			for (FullyKnownTrace t : firstNode.end.getVerifiedTrace()){
				Node childNode = new Node();
				childNode.end = t.getEnd();
				childNode.path = new InputSequence();
				childNode.path.addInputSequence(firstNode.path);
				childNode.path.addInputSequence(t.getTrace().getInputsProjection());
				paths.add(childNode);
			}
		}
		throw new RuntimeException("The infered automata seems to be not totaly connex : we reached " + reachedStates + " but not " + notFullyKnownStates);
	}

	public Collection<FullyQualifiedState> getStates() {
		return Q.values();
	}
	
	public AbstractNode getCurrentVNode() {
		return currentVNode;
	}
	
	public void setCurrentVNode(AbstractNode n) {
		currentVNode = n;
	}

	public void deduceFromVTree(){
		if (!options.useSpeedUp())
			return;
		if (DataManager.instance.getStates().size() != DataManager.instance.maxStates){
			LogManager.logInfo("cannot deduce while there is unknown states");
			return;
		}
		for (FullyQualifiedState s : getStates()){
			StateNode sn = s.getVNode();
			for (String i : sn.getKnownInputs()){
				AbstractNode child = sn.getChild(i);
				if (child.isStateNode())
					continue;
				List<StateNode> incompatibleNodes = child.getIncompatibleStateNode();
				if (incompatibleNodes.size() + 1 == DataManager.instance.maxStates){
					LogManager.logInfo("Found a node which is almost incompatible with all others ! "+sn+" followed by "+i+"/"+sn.getOutput(i)+" give "+child+" which is incompatible with "+incompatibleNodes);
					FullyQualifiedState end = null;
					for (FullyQualifiedState fqs : DataManager.instance.getStates())
						if (!incompatibleNodes.contains(fqs.getVNode()))
							end = fqs;
					FullyKnownTrace v = new FullyKnownTrace(s, new LmTrace(i, sn.getOutput(i)), end);
					LogManager.logInfo("deducing from VTree ! found " + v);
					addFullyKnownTrace(v);
				}
			}

		}
	}
	
	public LmConjecture getConjecture() {
		return conjecture;
	}
	
	public LmTrace getTrace() {
		return trace;
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
		CString.append((C.get(0) == null) ? "?" : C.get(0).toString());
		for (int j = 0; j < CString.length(); j++){
			IOString.append(" ");
		}
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

	private static int n_export = 0;
	public void exportVTreeToDot(){
		if (!options.useSpeedUp())
			return;
		if (Options.getLogLevel() != Options.LogLevel.ALL)
			return;
		if (currentVNode == null){
			LogManager.logInfo("VTree is unknown");
			return;
		}
		n_export++;
		Writer writer = null;
		File file = null;
		File dir = Options.getDotDir();
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
				+ " directory");

			file = new File(dir.getPath() + File.separatorChar
					+ "Vtree_"+n_export+".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");

			currentVNode.exportToDot(writer, new HashSet<AbstractNode>());
			
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("VTree has been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	/**
	 * 
	 * @param WResponses
	 * @return states in driver matching this WResponses (or null if driver is not available)
	 */
	public List<State> getDriverStates(List<OutputSequence>WResponses) {
		if (driver instanceof TransparentMealyDriver) {
			TransparentMealyDriver tDriver = (TransparentMealyDriver) driver;
			Mealy automata = tDriver.getAutomata();
			List<State>states =new ArrayList<>();
			for (State s : automata.getStates()) {
				boolean stateMatch=true;
				for (int i=0;i<W.size();i++){
					if(!automata.apply(W.get(i),s).equals(WResponses.get(i))){
						stateMatch=false;
						break;
					}
					
				}
				if (stateMatch)
					states.add(s);
			}
			return states;
		}
		return null;
	}

}
