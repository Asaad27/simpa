package learner.mealy.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.Node;
import main.Options;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import drivers.Driver;
import drivers.mealy.MealyDriver;

public class SigmaLearner extends Learner{
	private MealyDriver driver;
	private List<InputSequence> z;
	private List<String> i;
	private ObservationNode root;
	private List<ObservationNode> states; 
	
	public SigmaLearner(Driver driver){
		this.driver = (MealyDriver)driver;		
		this.i = Arrays.asList(Options.INITIAL_INPUT_SYMBOLS.split(","));
		this.z = new ArrayList<InputSequence>();
		for(String inputSeqString : Options.INITIAL_INPUT_SEQUENCES.split(",")){
			InputSequence seq = new InputSequence();
			for(String inputSym : inputSeqString.split("-")){
				seq.addInput(inputSym);
			}
			z.add(seq);
		}
		this.root = new ObservationNode();
		this.root.makeInitial();
		
		LogManager.logConsole("Options : I -> " + i.toString());
		LogManager.logConsole("Options : Z -> " + z.toString());
	}
	
	private boolean noLabelledPred(ObservationNode node) {
		boolean noLabelledPred = true;
		while (node.parent != null){
			ObservationNode parent = (ObservationNode)node.parent;
			if (parent.isLabelled()) return false;
			node = parent;
		}
		return noLabelledPred;
	}
	
	private InputSequence compareNodesUsingSeqs(Node node1, Node node2, List<InputSequence> z) {
		for(InputSequence seq : z){
			Node currentNode1 = node1;
			Node currentNode2 = node2;
			InputSequence dfs = new InputSequence();
			for(String input : seq.sequence){
				dfs.addInput(input);
				if (!currentNode1.haveChildBy(input) || !currentNode2.haveChildBy(input)) return dfs;
				if (!currentNode1.childBy(input).output.equals(currentNode2.childBy(input).output)) return dfs;
				currentNode1 = currentNode1.childBy(input);
				currentNode2 = currentNode2.childBy(input);
			}
		}
		return null;
	}
	
	private ObservationNode findFirstEquivalent(ObservationNode node, List<InputSequence> z) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			if (currentNode.id == node.id) break;
			InputSequence tmpdfs = compareNodesUsingSeqs(node,  currentNode, z);
			if (tmpdfs == null) return currentNode;
			queue.remove(0);
			queue.addAll(currentNode.children);	
		}
		return null;
	}
	
	private void addState(ObservationNode node) {
		node.state = states.size();
		
		// ADDED
		node.label = -1;
		
		states.add(node);
	}

	private void extendNodeWithInputSeqs(Node node, List<InputSequence> Z) {
		for (InputSequence seq : Z) {
			askInputSequenceToNode(node, seq);
		}
	}

	private void extendNodeWithSymbol(Node node, List<String> symbols) {
		for (String symbol : symbols) {
			askInputSequenceToNode(node, new InputSequence(symbol));
		}
	}
	
	private List<String> extendInputSymbolsWith(InputSequence ce) {
		List<String> ret = new ArrayList<String>(i);
		for(String sym : ce.sequence){
			if (!ret.contains(sym)) ret.add(sym);
		}		
		return ret;
	}

	private LmConjecture buildQuotient(List<InputSequence> z) {
		LogManager.logInfo("Build Quotient");
		LogManager.logInfo("Z : " + z.toString());
		LogManager.logInfo("I : " + i.toString());
		
		//1. Q = Q0		
		this.states = new ArrayList<ObservationNode>();
		
		//2. for (each state u of U being traversed during Breadth First Search)
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.remove(0);

			//4. if (u has no labelled predecessor)
			if (noLabelledPred(currentNode)){
				//6. Extend_Node (u, Z),
				extendNodeWithInputSeqs(currentNode, z);
				
				//7. if (u is Z-equivalent to a traversed state w of U)
				ObservationNode w = findFirstEquivalent(currentNode, z);
				if (w != null){
					//8. Label u with w
					currentNode.label = w.state;					
				}else{
					//10. Add u into Q
					addState(currentNode);
					//11. Extend_Node (u,I)
					extendNodeWithSymbol(currentNode, i);
					
				}
			}			
			queue.addAll(currentNode.children);			
		}
		
		LmConjecture ret = createConjecture();
		labelNodes(ret);
		
		return ret;
	}

	private List<InputSequence> includeInto(List<InputSequence> into, InputSequence seq) {
		List<InputSequence> ret = new ArrayList<InputSequence>();
		boolean exists = false;
		for (InputSequence s : into){
			ret.add(s);
			if (s.equals(seq)) exists = true;
		}
		if (!exists) ret.add(seq);
		return ret;
	}	

	private void labelNodes(LmConjecture q) {
		LogManager.logInfo("Labeling nodes");
		labelNodesRec(q, root, q.getInitialState(), false);
		LogManager.logObservationTree(root);
	}
	
	private void labelNodesRec(LmConjecture q, ObservationNode node, State s, boolean label) {
		if (label) node.label = s.getId();
		if (node.isLabelled()) label = true;
		if (!node.children.isEmpty()){
			for(Node n : node.children){
				MealyTransition t = q.getTransitionFromWithInput(s, n.input);
				labelNodesRec(q, (ObservationNode) n, t.getTo(), label);
			}
		}
	}
	
	private InputSequence findInconsistency(LmConjecture c) {
		LogManager.logInfo("Searching inconsistency");
		InputSequence ce = findInconsistencyRec(c, c.getInitialState(), root, new InputSequence());
		if (ce != null) LogManager.logInfo("Inconsistency found : " + ce); else LogManager.logInfo("No inconsistency found");
		return ce;
	}
	
	private InputSequence findInconsistencyRec(LmConjecture c, State s, ObservationNode node, InputSequence ce) {
		if (!node.children.isEmpty()){
			for(Node n : node.children){
				if (!((ObservationNode)n).isState()) ce.addInput(n.input);
				MealyTransition t = c.getTransitionFromWithInput(s, n.input);
				if (t != null && t.getOutput().equals(n.output)){
					InputSequence otherCE = findInconsistencyRec(c, t.getTo(), (ObservationNode)n, ce);
					if (otherCE != null) return otherCE;
				}else
					return ce.removeFirstInput();
				if (!((ObservationNode)n).isState()) ce.removeLastInput();
			}
		}
		return null;
	}
	
	public void learn() {
		LogManager.logConsole("Inferring the system");
		InputSequence ce;;
		addtolog = false;
		
		// 1. Build-quotient(A, I, Z, {€}) returning U and K =  (Q, q0, I, O, hK)		
		LmConjecture Z_Q = buildQuotient(z);
		
		// 2. Fix_Point_Consistency(A, I, Z, U, K)
		Z_Q = fixPointConsistency(Z_Q);
		
		// 4. while there exists an unprocessed counterexample CE
		do{
			ce = driver.getCounterExample(Z_Q);
			if (ce != null){
				LogManager.logInfo("Adding the counter example to tree");
				
				//5. U = U U CE
				askInputSequenceToNode(root, ce);
								
				LogManager.logObservationTree(root);
				
				// 6. Fix_Point_Consistency(A, I, Z, U, K)
				Z_Q = fixPointConsistency(Z_Q);
				
			}
		}while(ce != null);
		
		addtolog = true;
	}

	private LmConjecture fixPointConsistency(LmConjecture K) {
		InputSequence inconsistency;
		
		//1.	while there exists a witness w for state q in U such that its input projection is not in Z
		do{
			inconsistency = findInconsistency(K);
			if (inconsistency != null){
				// 4. Z' = Z U {w|I} and I' = I U inp(w)
				// 6. Z = Z' and I = I'
				z = includeInto(z, inconsistency); i = extendInputSymbolsWith(inconsistency);
						
				// 5. Build_quotient(A, I', Z', U) returning an updated observation tree and quotient 
				K = buildQuotient(z);
			}					
		}while(inconsistency != null);
		
		return K;
	}

	private void askInputSequenceToNode(Node node, InputSequence sequence){
		Node currentNode = node;
		InputSequence seq = sequence.clone();
		InputSequence previousSeq = getPreviousInputSequenceFromNode(currentNode);
		while (seq.getLength()>0 && currentNode.haveChildBy(seq.getFirstSymbol())){
			currentNode = currentNode.childBy(seq.getFirstSymbol());
			previousSeq.addInput(seq.getFirstSymbol());
			seq.removeFirstInput();			
		}
		if (seq.getLength() > 0){
			driver.reset();
			for(String input : previousSeq.sequence){
				driver.execute(input);
			}			
			for (String input : seq.sequence){
				if (currentNode.haveChildBy(input)) currentNode = currentNode.childBy(input);
				else currentNode = currentNode.addChild(new ObservationNode(input, driver.execute(input)));				
			}
		}
	}

	private InputSequence getPreviousInputSequenceFromNode(Node node) {
		Node currentNode = node;
		InputSequence seq = new InputSequence();
		while (currentNode.parent != null){
			seq.prependInput(currentNode.input);
			currentNode = currentNode.parent;
		}
		return seq;
	}
	
	public LmConjecture createConjecture() {
		if (addtolog) LogManager.logConsole("Building the conjecture");
		LogManager.logObservationTree(root);
		
		LmConjecture c = new LmConjecture(driver);

		for(int i=0; i<states.size(); i++) c.addState(new State("S" + i, i == 0));

			for(ObservationNode s : states){
				for(String input : i){
					ObservationNode child = (ObservationNode) s.childBy(input);
					if (child.isState()) c.addTransition(new MealyTransition(c, c.getState(s.state), c.getState(child.state), input, child.output));
					else c.addTransition(new MealyTransition(c, c.getState(s.state), c.getState(child.label), input, child.output));
				}
			}

		LogManager.logInfo("Z : " + z);
		LogManager.logInfo("I : " + i);

		LogManager.logInfo("Conjecture have " + c.getStateCount() + " states and " + c.getTransitionCount() + " transitions : ");		
		for (MealyTransition t : c.getTransitions()) LogManager.logTransition(t.toString());
		LogManager.logLine();

		c.exportToDot();

		return c;
	}
}
