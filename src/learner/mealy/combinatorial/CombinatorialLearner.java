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
import java.util.LinkedList;

import automata.Automata;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.SimpleMultiTrace;
import drivers.mealy.MealyDriver;
import learner.Learner;
import learner.mealy.LmTrace;
import learner.mealy.combinatorial.node.ArrayTreeNodeWithoutConjecture;
import learner.mealy.combinatorial.node.TreeNode;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import stats.StatsEntry;
import tools.GraphViz;
import tools.loggers.LogManager;


public class CombinatorialLearner extends Learner {
	private MealyDriver driver;
	private LmTrace trace;
	private TreeNode root;
	private Conjecture conjecture;
	private CombinatorialStatsEntry stats;
	private final CombinatorialOptions options;

	public CombinatorialLearner(MealyDriver driver,
			CombinatorialOptions options) {
		assert !options.withCut();
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
		stats = new CombinatorialStatsEntry(driver, options);
		long start = System.nanoTime();
		driver.reset();
		trace = new LmTrace();
		root = new ArrayTreeNodeWithoutConjecture(driver);
		TreeNode result;
		while ((result = compute(root)) == null){
			root.addState();
			LogManager.logLine();
			LogManager.logInfo("added a state. States are now " + root.getStates());
			LogManager.logConsole("added a state. States are now " + root.getStates());
		}
		conjecture = result.getConjecture();
		float duration = (float)(System.nanoTime() - start)/ 1000000000;
		LogManager.logStep(LogManager.STEPOTHER,"Found an automata which seems to have no counter example in "+duration+"s");
		LogManager.logConsole("Found an automata which seems to have no counter example in "+duration+"s");
		conjecture.exportToDot();
		stats.setTraceLength(trace.size());
		stats.setDuration(duration);
		stats.updateWithConjecture(conjecture);
	}

	/**
	 * this method travel along the tree and complete Nodes when they are not complete.
	 * @param n the root of the tree
	 * @return a Node with a correct conjecture (according to the teacher @see #getShortestUnknowntransition(State, Conjecture)) or null
	 */
	private TreeNode compute(TreeNode n){
		//TODO make a non-recursive version of that ?
		if (Options.getLogLevel() != LogLevel.LOW)
			LogManager.logInfo("currently in " + n.getStatesTrace(trace));
		if (n.isCut())
			return null;
		if (n.haveForcedChild())
			return compute(n.getOnlyChild());
		if (trace.size() <= n.getDepth()){
			Conjecture c = n.getConjecture();
			InputSequence i = getShortestUnknowntransition(n.getState(), c);
			if (i == null){
				LogManager.logInfo("no reachable unknown transition found");
				//all transitions are known in conjecture.
				if (!c.isConnex()){
					LogManager.logInfo("conjecture is not connex, cutting");
					n.cut();
					return null;
				}
				if (!applyCounterExample(c, n.getState()))
					return n;
			}else{
				LogManager.logInfo("going to an unknown transition by applying " + i);
				apply(i);
			}
		}
		exportTreeToDot();
		String i = trace.getInput(n.getDepth());
		String o = trace.getOutput(n.getDepth());
		MealyTransition t = n.getTransitionFromWithInput(n.getState(), i);
		if (t != null){
			if (!t.getOutput().equals(o)){
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo("we supposed to have transition '" + t + "' but applying '" + i +"' produced '" + o + "'. cutting.");
				n.cut();
				return null;
			}
			TreeNode child  = n.addForcedChild(t.getTo());
			stats.addNode();
			return compute(child);
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
			TreeNode returnedNode = compute(child);
			if (returnedNode != null)
				return returnedNode;
			checkedChildren ++;
			if (Options.getLogLevel() != LogLevel.ALL)
				n.removeChild(q);
		}
		return null;
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
		SimpleMultiTrace appliedSequences=new SimpleMultiTrace();
		
		boolean found = driver.getCounterExample_noThrow(
				options.getOracleOption(), c,
				currentState, appliedSequences, false, stats.oracle);

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
		LogManager.logInfo("trace is now " + trace);
		return os;
	}

	/**
	 * execute a symbol on the driver and complete the trace.
	 * @param i the input symbol
	 * @return the returned output symbol.
	 * @see #apply(String, boolean)
	 */
	protected String apply(String i) {
		return apply(i, true);
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
	
	private int n_export = 0;

	public void exportTreeToDot() {
		boolean hideCutBranches = false;
		if (Options.getLogLevel() != Options.LogLevel.ALL)
			return;
		n_export++;
		Writer writer = null;
		File file = null;
		File dir = Options.getDotDir();
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName() + " directory");

			file = new File(dir.getPath() + File.separatorChar + "tree_" + n_export + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");

			ArrayList<TreeNode> currentLevel = new ArrayList<>();
			ArrayList<TreeNode> nextLevel = new ArrayList<>();
			currentLevel.add(root);
			while (currentLevel.size() > 0) {
				int currentDepth = currentLevel.get(0).getDepth();
				LmTrace t = trace.subtrace(currentDepth, currentDepth + 1);
				String label = t.toString();
				for (TreeNode n : currentLevel) {
					boolean haveUncuttedChild = n.haveUncuttedChild();
					writer.write("\t" + n.id + " [label=\"" + n.getState() + "\\n" + n.desc + "\""
							+ (n.isCut() ? ",style=dotted" : "") + "]\n");
					if (n.isCut()) {
						String cutId = "cut_" + n.id;
						writer.write("\t" + cutId + " [label=\"\",shape=none]\n");
						if (t.size() != 0) {
							String cutOutput = n.getConjecture().getTransitionFromWithInput(n.getState(), t.getInput(0))
									.getOutput();
							writer.write("\t" + n.id + " -> " + cutId + "[style=dotted,color=red,label=\"("
									+ new LmTrace(t.getInput(0), cutOutput) + ")\"]\n");
						}
					} else if (hideCutBranches && !haveUncuttedChild) {
						String cutId = "cut_" + n.id;
						writer.write("\t" + cutId + " [label=\"\",shape=none]\n");
						writer.write("\t" + n.id + " -> " + cutId + "[style=dashed,color=blue,label=\"" + t + "\"]\n");
					} else if (n.haveForcedChild()) {
						TreeNode n1 = n.getOnlyChild();
						writer.write(
								"\t" + n.id + " -> " + n1.id + "[style=bold,color=green,label=\"" + label + "\"]\n");
						nextLevel.add(n1);
					} else {
						for (State s : n.getStates()) {
							TreeNode n1 = n.getChild(s);
							if (n1 != null) {
								if (hideCutBranches && !n1.isCut() && !n1.haveUncuttedChild()) {
									writer.write("\t" + n.id + " -> " + n1.id + "[label=\"" + label
											+ "\",color=blue,style=dashed]\n");
									writer.write("\t" + n1.id + " [label=\"" + n1.getState()
											+ "\",style=dotted,color=blue]\n");
								} else {
									writer.write("\t" + n.id + " -> " + n1.id + "[label=\"" + label + "\"]\n");
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
			LogManager.logInfo("Tree has been exported to " + file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}
}
