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
package learner.mealy.combinatorial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import automata.Automata;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.SimpleMultiTrace;
import drivers.mealy.MealyDriver;
import learner.Learner;
import learner.mealy.LmTrace;
import learner.mealy.combinatorial.node.ArrayTreeNodeWithConjecture;
import learner.mealy.combinatorial.node.TreeNode;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import stats.StatsEntry;
import tools.GraphViz;
import tools.loggers.LogManager;


public class CutterCombinatorialLearner extends Learner {
	private MealyDriver driver;
	private LmTrace trace;
	private TreeNode root;
	private Conjecture conjecture;
	private CutterCombinatorialStatsEntry stats;
	private ArrayList<TreeNode> currentLevel;
	private ArrayList<TreeNode> nextLevel;
	private CombinatorialOptions options;

	public CutterCombinatorialLearner(MealyDriver driver,
			CombinatorialOptions options) {
		assert options.withCut();
		this.driver = driver;
		this.options = options;
	}

	@Override
	public Automata createConjecture() {
		return conjecture;
	}

	@Override
	public void learn() {
		LogManager.logStep(LogManager.STEPOTHER,"Inferring the system");
		LogManager.logConsole("Inferring the system");
		stats = new CutterCombinatorialStatsEntry(driver, options);
		long start = System.nanoTime();
		driver.reset();
		int statesNumber = 0;
		TreeNode result;
		do {
			root = new ArrayTreeNodeWithConjecture(driver);
			statesNumber++;
			for (int i = 1; i < statesNumber; i++)
				root.addState();
			LogManager.logLine();
			LogManager.logInfo("added a state. States are now " + root.getStates());
			LogManager.logConsole("added a state. States are now " + root.getStates());
			trace = new LmTrace();
			result = compute(root);
			stats.addTraceLength(trace.size());
		} while (result == null);
		conjecture = result.getConjecture();
		float duration = (float)(System.nanoTime() - start)/ 1000000000;
		LogManager.logStep(LogManager.STEPOTHER,"Found an automata which seems to have no counter example in "+duration+"s");
		LogManager.logConsole("Found an automata which seems to have no counter example in "+duration+"s");
		conjecture.exportToDot();
		stats.setDuration(duration);
		stats.updateWithConjecture(conjecture);
	}



	/**
	 * this method travel along the tree and complete Nodes when they are not complete.
	 * @param n the root of the tree
	 * @return a Node with a correct conjecture (according to the teacher @see #getShortestUnknowntransition(State, Conjecture)) or null
	 */
	private TreeNode compute(TreeNode n){
		currentLevel = new ArrayList<>();
		currentLevel.add(n);
		nextLevel = new ArrayList<>();
		TreeNode result;
		boolean finished;
		do{
			finished = false;
			//				currentLevel.size() > 1 ||
			//				(currentLevel.size()==0 &&
			//				!(c = (result = currentLevel.get(0)).getConjecture()).isConnex()&&
			//				!applyCounterExample(c, currentLevel.get(0).getState()))){
			computeLevel();
			for (TreeNode node : currentLevel)
				node.desc = "";
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
			if (currentLevel.size()==0)
				finished = true;
			if (currentLevel.size() == 1){
				result = currentLevel.get(0);
				if (result.getDepth() == trace.size()){
					InputSequence i = getShortestUnknowntransition(result.getState(), result.getConjecture());
					if (i == null){
						Conjecture c = result.getConjecture();
						if (c.isConnex() &&
								!applyCounterExample(c, result.getState()))
							return result;
					}
				}
			}
			if (currentLevel.size() > 0 && 
					currentLevel.get(0).getDepth() < trace.size()){
				finished = false;
			}
		}while(!finished);
		if (currentLevel.size() == 0)
			return null;
		result = currentLevel.get(0);
		if (!result.getConjecture().isConnex())
			return null;
		if (applyCounterExample(result.getConjecture(), result.getState()))
			return null;
		return result;
	}

	private void computeLevel(){
		assert currentLevel.size() > 0;

		int currentDepth = currentLevel.get(0).getDepth();
		LogManager.logInfo("computing level " + currentDepth);

		if (trace.size() <= currentDepth){
			applyCuttingSequence();
		}else{
			exportTreeToDot();
			System.out.println("computing "+currentLevel.size()+" nodes");
		}

		if (trace.size() <= currentDepth){
			TreeNode n = currentLevel.get(0);
			LogManager.logLine();
			LogManager.logLine();
			LogManager.logInfo("trace is not long enough searching unknown transition/counter example");
			Conjecture c = n.getConjecture();
			InputSequence i = getShortestUnknowntransition(n.getState(), c);
			if (i == null){
				LogManager.logInfo("all transitions known, appliying counter example");
				if(c.isConnex()){
					if(!applyCounterExample(c, n.getState())){
						nextLevel.add(n);
						return;
					}
				} else {
					LogManager.logInfo("conjecture is not connex. applying «random» symbol and hoping node will be cut");
					apply(driver.getInputSymbols().get(0));
					n.cut();
				}
			}else{
				LogManager.logInfo("going to an unknown transition by applying " + i);
				apply(i);
			}
		}

		LogManager.logInfo("computing "+ trace.subtrace(currentDepth, currentDepth+1)+" for each node");
		String i = trace.getInput(currentDepth);
		String o = trace.getOutput(currentDepth);
		for (TreeNode n : currentLevel){
			if (n.isCut())
				continue;
			if (n.haveForcedChild()){
				nextLevel.add(n.getOnlyChild());
				continue;
			}

			MealyTransition t = n.getTransitionFromWithInput(n.getState(), i);
			if (t != null){
				if (!t.getOutput().equals(o)){
					if (Options.getLogLevel()  != LogLevel.LOW)
						LogManager.logInfo("we supposed to have transition '" + t + "' but applying '" + i +"' produced '" + o + "'. cutting.");
					n.cut();
					continue;
				}
				TreeNode child  = n.addForcedChild(t.getTo());
				nextLevel.add(child);
				stats.addNode();
				continue;
			}
			int checkedChildren = 1;
			for (State q : n.getStates()){
				if (checkedChildren > n.getDiscoveredStatesNb()+1){
					LogManager.logInfo("currently in " + n.getStatesTrace(trace) + " : going to a child with state "+q+" is useless (that's relabelling)");
					break;
				}
				TreeNode child = n.getChild(q);
				if (child == null){
					child = n.addChild(i,o,q);
					stats.addNode();
				}
				nextLevel.add(child);
				checkedChildren ++;
				//n.removeChild(q);
			}
		}
	}

	private Scanner input = new Scanner(System.in);
	private int cut = 0;
	private void applyCuttingSequence(){
		LogManager.logLine();
		LogManager.logInfo("searching cutting sequence");
		LogManager.logInfo("current level has "+currentLevel.size()+" nodes");
		System.out.println("searching cutting sequence (hope to reduce actual "+currentLevel.size()+" nodes)");

		InputSequence cuttingSequence = new InputSequence();

		Map<InputSequence,Integer> maxLength = new HashMap<>();
		for (String i : driver.getInputSymbols()){
			LogManager.logInfo("");
			int knownResponses=0;
			Map<String,Integer> responses = new HashMap<>();
			for (TreeNode n : currentLevel){
				MealyTransition t = n.getConjecture().getTransitionFromWithInput(n.getState(), i);
				if (t == null){
				} else {
					String output = t.getOutput();
					n.desc = n.desc+"\\n"+i+"/" + t.getOutput() +"→" + t.getTo();
					if (!responses.containsKey(output)){
						responses.put(output, Integer.valueOf(0));
					}
					knownResponses++;
					responses.put(output,
							Integer.valueOf(responses.get(output) + 1));
				}
				//n.desc = n.desc+"\\n"+i+" : " + localNewNode;
			}

			int maxNodes = (currentLevel.size()-knownResponses)*root.getStates().size();
			for (String output : responses.keySet()){
				Integer nodes = responses.get(output);
				nodes = (currentLevel.size()-knownResponses)*root.getStates().size() + nodes;
				if (nodes > maxNodes)
					maxNodes = nodes;
				if (responses.get(output) == currentLevel.size())
					maxNodes = -1;
				if (options.interactive.isEnabled())
					System.out.println(i+"/"+output+" : "+ nodes + " nodes");
				LogManager.logInfo("if we apply '" +i+ "' and if we get '" + output +"', next level will have "+nodes+" nodes");
			}
			if (options.interactive.isEnabled())
				System.out.println(i+"/? : "+(currentLevel.size()-knownResponses)*root.getStates().size()+" nodes");
			LogManager.logInfo("if we apply '" +i+ "' and if we get an other output, next level will have "+(currentLevel.size()-knownResponses)*root.getStates().size()+" nodes");
			maxLength.put(new InputSequence(i), Integer.valueOf(maxNodes));
		}
		exportTreeToDot();

		int min = 100000000;//TODO make a proper version of that
		for (InputSequence seq : maxLength.keySet()){
			if (maxLength.get(seq) < min && maxLength.get(seq) != -1){
				min = maxLength.get(seq);
				cuttingSequence = seq;
			}
		}


		String[] defaults = new String[]{};
		//defaults = new String[]{"a","a","a","b","a","a","b","a","b","a","a","b","b","a"};

		if (cut < defaults.length){
			cuttingSequence = new InputSequence(defaults[cut]);
			LogManager.logInfo("select forced choice n° " + cut +" : " + cuttingSequence);
			System.out.println("select forced choice n° " + cut +" : " + cuttingSequence);
		} else {
			LogManager.logInfo("no more forced symbols");
		}	
		cut ++;

		if (options.interactive.isEnabled()) {
			System.out.println("what do you want to apply ? \n\tenter «auto» to use default sequence '"+
							cuttingSequence
							+ "'\n\t'a,b,c' for the sequence a, b, c\n\t«empty» to use old algorithm (Shortest unknown transition or shortest counter example)");

			String answer = input.next();
			System.out.println("understood «"+answer+"»");
			if (answer.equals("empty"))
				cuttingSequence = new InputSequence();
			else if (!answer.equals("auto")){
				cuttingSequence = new InputSequence();
				for (String i : answer.split(","))
					cuttingSequence.addInput(i);
			}
			System.out.println("applying «"+cuttingSequence+"»\n");
		}
		apply(cuttingSequence);

	}

	/**
	 * get an inputSequence which lead after an unknown transition
	 * @param start the state from which the sequence is computed
	 * @param c the conjecture to check (and which contains the inputs symbols)
	 * @return an input sequence s.t. the last transition obtained after applying the sequence to start is unknown or null if a such transition is not REACHABLE.
	 * You can get a null return if the automata has an unknown transition but is not connex.
	 */
	private InputSequence getShortestUnknowntransition(State start, Conjecture c){
		LogManager.logInfo("searching an unknown transition from " + start);
		if (Options.getLogLevel() == LogLevel.ALL)
			c.exportToDot();
		class Node{public InputSequence i; public State end;}
		LinkedList<Node> toCompute = new LinkedList<Node>();
		Node n = new Node();
		n.i = new InputSequence();
		n.end = start;
		toCompute.add(n);
		while (!toCompute.isEmpty()){
			Node current = toCompute.pollFirst();
			if (current.i.getLength() > c.getStateCount())
				continue;
			for (String i : c.getInputSymbols()){
				MealyTransition t = c.getTransitionFromWithInput(current.end, i);
				if (t == null){
					current.i.addInput(i);
					return current.i;
				}
				Node newNode = new Node();
				newNode.i = new InputSequence();
				newNode.i.addInputSequence(current.i);
				newNode.i.addInput(i);
				newNode.end = t.getTo();
				toCompute.add(newNode);
			}
		}
		return null;
	}

	/**
	 * search a sequence which distinguish the conjecture and the blackbox and apply it.
	 * 
	 * @param c the conjecture
	 * @param currentState the current position in conjecture
	 * @return true if a counterExemple was found, false if the conjecture seems to be equivalent to the automata.
	 */
	private boolean applyCounterExample(Conjecture c, State currentState){
		LogManager.logLine();
		LogManager.logInfo("Searching counter Example");
		SimpleMultiTrace appliedSequences = new SimpleMultiTrace();

		boolean found = driver.getCounterExample_noThrow(
				options.getOracleOption(), c, currentState, appliedSequences,
				false, stats.oracle);

		assert appliedSequences.getResetNumber() == 0;
		trace.append(appliedSequences.getLastTrace());

		return found;
	}

	/**
	 * @see #apply(String)
	 * @param is
	 * @return
	 */
	private OutputSequence apply(InputSequence is){
		OutputSequence os = new OutputSequence();
		for (String i : is.sequence){
			String o = apply(i,false);
			os.addOutput(o);
		}
		//LogManager.logInfo("trace is now " + trace);
		return os;
	}

	/**
	 * execute a symbol on the driver and complete the trace.
	 * @param i the input symbol
	 * @return the returned output symbol.
	 * @see #apply(String, boolean)
	 */
	private String apply(String i) {
		return apply(i, false);
	}

	/**
	 * execute a symbol on the driver and complete the trace.
	 * @param verbose indicate if trace must be displayed or not
	 * @param i the input symbol
	 * @return the returned output symbol.
	 */
	private String apply(String i, boolean verbose) {
		String o = driver.execute(i);
		trace.append(i, o);
		if (verbose)
			LogManager.logInfo("now, trace is " + trace);
		return o;
	}

	@Override
	public StatsEntry getStats() {
		return stats;
	}

	private int n_export=0;
	public void exportTreeToDot() {
		if (Options.getLogLevel() == Options.LogLevel.LOW)
			return;
		n_export++;
		Writer writer = null;
		File file = null;
		File dir = Options.getDotDir();
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
				+ " directory");

			file = new File(dir.getPath() + File.separatorChar
					+ "tree_"+n_export+".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");

			ArrayList<TreeNode> currentLevel = new ArrayList<>();
			ArrayList<TreeNode> nextLevel = new ArrayList<>();
			currentLevel.add(root);
			while (currentLevel.size() > 0){
				int currentDepth = currentLevel.get(0).getDepth();
				LmTrace t = trace.subtrace(currentDepth, currentDepth+1);
				String label = t.toString();
				for (TreeNode n : currentLevel){
					boolean haveUncuttedChild = n.haveUncuttedChild();
					writer.write("\t"+n.id+" [label=\""+n.getState() + "\\n" + n.desc +"\"" +
							(n.isCut() ? ",style=dotted" : "") + "]\n");
					if (n.isCut()){
						String cutId = "cut_"+n.id;
						String cutOutput = n.getConjecture().getTransitionFromWithInput(n.getState(), t.getInput(0)).getOutput();
						writer.write("\t"+cutId+" [label=\"\",shape=none]\n");
						writer.write("\t" + n.id + " -> " + cutId +
								"[style=dotted,color=red,label=\"("+ new LmTrace(t.getInput(0), cutOutput) +")\"]\n");
					}else if (!haveUncuttedChild){
						String cutId = "cut_"+n.id;
						writer.write("\t"+cutId+" [label=\"\",shape=none]\n");
						writer.write("\t" + n.id + " -> " + cutId +
								"[style=dashed,color=blue,label=\""+ t +"\"]\n");
					}else if (n.haveForcedChild()){
						TreeNode n1 = n.getOnlyChild();
						writer.write("\t"+n.id+" -> "+ n1.id + "[style=bold,color=green,label=\""+label+"\"]\n");
						nextLevel.add(n1);
					}else {
						for (State s : n.getStates()){
							TreeNode n1 = n.getChild(s);
							if (n1 != null){
								if (!n1.isCut() && !n1.haveUncuttedChild()){
									writer.write("\t"+n.id+" -> "+ n1.id + "[label=\""+label+"\",color=blue,style=dashed]\n");
									writer.write("\t"+n1.id+" [label=\""+n1.getState() + "\",style=dotted,color=blue]\n");
								}else{
									writer.write("\t"+n.id+" -> "+ n1.id + "[label=\""+label+"\"]\n");
									nextLevel.add(n1);
								}
							}
						}
					}
				}
				currentLevel = nextLevel;
				nextLevel = new ArrayList<>();
			}

			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Tree has been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

}
