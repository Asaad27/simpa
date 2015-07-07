package learner.mealy.combinatorial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tools.loggers.LogManager;
import automata.Automata;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;


public class CombinatorialLearner extends Learner {
	private MealyDriver driver;
	private LmTrace trace;
	private TreeNode root;
	private Conjecture conjecture;

	public CombinatorialLearner(MealyDriver driver) {
		this.driver = driver;
	}

	@Override
	public Automata createConjecture() {
		return conjecture;
	}

	@Override
	public void learn() {
		LogManager.logStep(LogManager.STEPOTHER,"Inferring the system");
		LogManager.logConsole("Inferring the system");
		driver.reset();
		trace = new LmTrace();
		root = new TreeNode(driver);
		TreeNode result;
		while ((result = compute(root)) == null){
			root.addState();
			LogManager.logLine();
			LogManager.logInfo("added a state. States are now " + root.conjecture.getStates());
			LogManager.logConsole("added a state. States are now " + root.conjecture.getStates());
		}
		conjecture = result.conjecture;
	}

	private TreeNode compute(TreeNode n){
		LogManager.logInfo("currently in " + n.getStatesTrace(trace));
		//n.conjecture.exportToDot();
		if (n.isCutted)
			return null;
		if (n.haveForcedChild)
			return compute(n.getOnlyChild());
		if (trace.size() <= n.depth){
			InputSequence i = getShortestUnknowntransition(n.state, n.conjecture);
			if (i == null){
				LogManager.logInfo("no reachable unknown transition found");
				//all transitions are known in conjecture.
				LogManager.logInfo("check connexity on ");
				n.conjecture.exportToDot();
				if (!n.conjecture.isConnex()){
					LogManager.logInfo("conjecture is not connex, cutting");
					n.cut();
					return null;
				}
				if (!applyCounterExample(n.conjecture, n.state))
					return n;
			}else{
				LogManager.logInfo("going to an unknown transition by applying " + i);
				apply(i);
			}
		}
		String i = trace.getInput(n.depth);
		String o = trace.getOutput(n.depth);
		MealyTransition t = n.conjecture.getTransitionFromWithInput(n.state, i);
		if (t != null){
			if (!t.getOutput().equals(o)){
				LogManager.logInfo("we supposed to have transition '" + t + "' but applying '" + i +"' produced '" + o + "'. cutting.");
				n.cut();
				return null;
			}
			TreeNode child  = n.addForcedChild(t.getTo());
			return compute(child);
		}
		for (State q : n.conjecture.getStates()){
			TreeNode child = n.childs.get(q);
			if (child == null)
				child = n.addChild(i,o,q);
			TreeNode returnedNode = compute(child);
			if (returnedNode != null)
				return returnedNode;
		}
		return null;
	}

	/**
	 * check if a conjecture have all transitions
	 * @param c the conjecture to check
	 * @param inputSymbols the inputSymbols
	 * @return true if there are a transition from any state with any input symbol
	 */
	private boolean isFullyKnown(LmConjecture c, List<String> inputSymbols){
		for (State s : c.getStates())
			for (String i : inputSymbols)
				if (c.getTransitionFromWithInput(s, i) == null)
					return false;
		return true;
	}

	private InputSequence getShortestUnknowntransition(State start, Conjecture c){
		LogManager.logInfo("searching an unknown transition from " + start);
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
		//TODO check if the automata is connex
		return null;
	}

	private boolean applyCounterExample(Conjecture c, State currentState){
		LogManager.logInfo("searching counter Example on ");
		c.exportToDot();
		if (driver instanceof TransparentMealyDriver){
			Mealy original = ((TransparentMealyDriver) driver).getAutomata();
			State originalState = ((TransparentMealyDriver) driver).getCurrentState();
			InputSequence r = getShortestCounterExemple(original, originalState, c, currentState);
			if (r == null)
				return false;
			apply(r);
			return true;
		}
		throw new RuntimeException("not implemented");
		//TODO random walk (already exist in MealyDriver but not withoutReset)
	}

	private InputSequence getShortestCounterExemple(Mealy original,
			State originalState, Conjecture c, State currentState) {
		//		Map<State,State> assignedStates;//<origninalState,ConjectureState>
		//		assignedStates = new HashMap<State,State>();
		//		assignedStates.put(originalState,currentState);
		class Node{public InputSequence i; public State originalEnd; public State conjectureEnd;}
		LinkedList<Node> toCompute = new LinkedList<Node>();
		Node n = new Node();
		n.i = new InputSequence();
		n.originalEnd = originalState;
		n.conjectureEnd = currentState;
		toCompute.add(n);
		while (!toCompute.isEmpty()){
			Node current = toCompute.pollFirst();
			if (current.i.getLength() > original.getStateCount())
				continue;
			for (String i : c.getInputSymbols()){
				MealyTransition originalT = original.getTransitionFromWithInput(current.originalEnd, i);
				MealyTransition conjectureT = c.getTransitionFromWithInput(current.conjectureEnd, i);
				if (!originalT.getOutput().equals(conjectureT.getOutput())){
					current.i.addInput(i);
					return current.i;
				}
				Node newNode = new Node();
				newNode.i = new InputSequence();
				newNode.i.addInputSequence(current.i);
				newNode.i.addInput(i);
				newNode.originalEnd = originalT.getTo();
				newNode.conjectureEnd = conjectureT.getTo();
				toCompute.add(newNode);
			}
		}
		return null;
	}

	private OutputSequence apply(InputSequence is){
		OutputSequence os = new OutputSequence();
		for (String i : is.sequence){
			String o = apply(i);
			os.addOutput(o);
		}
		return os;
	}

	private String apply(String i) {
		String o = driver.execute(i);
		trace.append(i, o);
		LogManager.logInfo("now, trace is " + trace);
		return o;
	}

}
