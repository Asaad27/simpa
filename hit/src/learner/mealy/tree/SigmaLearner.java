package learner.mealy.tree;

import java.util.ArrayList;
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
	private List<InputSequence> zQ;
	private List<String> inputSymbols;
	private ObservationNode root;
	private List<ObservationNode> states; 
	private LmConjecture currentConjecture;
	
	public SigmaLearner(Driver driver){
		this.driver = (MealyDriver)driver;		
		this.inputSymbols = driver.getInputSymbols();
		
		// z = €
		zQ = new ArrayList<InputSequence>();
		
		// Q = Q0
		this.root = new ObservationNode();
		this.states = new ArrayList<ObservationNode>();
		this.states.add(this.root);
		initialize();
	}
	
	private void initialize() {
		root.makeInitial();
		extendNode(root, zQ);
		for(Node node : root.children){
			extendNode(node, zQ);
		}
	}

	// Extend-node 
	private void extendNode(Node node, List<InputSequence> Z) {
		for(InputSequence seq : Z){
			askInputSequenceToNode(node, seq);
		}	
	//node.haveSigma = true;
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

	@Override
	protected void completeDataStructure() {
		
	}
	
	public LmConjecture createConjecture() {
		if (addtolog) LogManager.logConsole("Building the conjecture");
		LmConjecture c = new LmConjecture(driver);

		for(int i=0; i<states.size(); i++){
			c.addState(new State("S" + i, i == 0));
		}

		try{
			for(ObservationNode s : states){
				for(String input : inputSymbols){
					ObservationNode child = (ObservationNode) s.childBy(input);
					c.addTransition(new MealyTransition(c, c.getState(s.state), c.getState(child.equivTo), input, child.output));
				}
			}
		}catch(Exception e){
			LogManager.logObservationTree(root);
			e.printStackTrace();
			System.exit(1);
		}

		LogManager.logInfo(Options.SYMBOL_SIGMA + " : " + zQ);

		LogManager.logInfo("Conjecture have " + c.getStateCount() + " states and " + c.getTransitionCount() + " transitions : ");		
		for (MealyTransition t : c.getTransitions()){
			LogManager.logTransition(t.toString());
		}
		LogManager.logLine();

		c.exportToDot();

		return c;
	}

	private boolean processNextDistinguishingSequence() {		
		return getDistinguishingSequenceBFS();
	}

	private ObservationNode findFirstEquivalent(ObservationNode node) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			if (currentNode.haveSigma){
				InputSequence tmpdfs = compareNodesUsingZ(node,  currentNode);
				if (tmpdfs == null) return currentNode;
				queue.addAll(currentNode.children);
			}
			queue.remove(0);			
		}
		return null;
	}
	
	private void updateEquivalent(ObservationNode node) {
		for (ObservationNode state : states){
			InputSequence tmpdfs = compareNodesUsingZ(node,  state);
			if (tmpdfs == null){
				node.equivTo = state.state;
				return;
			}			
		}
	}	
	
	// 4.4.	while there exists a counterexample for any state of the Z_Q 
	private boolean getDistinguishingSequenceBFS() {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		LogManager.logObservationTree(root);
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			System.out.println(currentNode.id);
			if (currentNode.state == -1){
				InputSequence tmpdfs = new InputSequence();
				if (currentNode.equivTo == -1 && currentNode.haveSigma){
					addState(currentNode);
					return true;
				}else if (currentNode.equivTo != -1){
					compareNodesUsingSubTrees(tmpdfs, states.get(currentNode.equivTo),  currentNode);
					if (tmpdfs.getLength() > 0){
						addState(currentNode, tmpdfs);
						return true;
					}
				}
			}
			queue.remove(0);
			queue.addAll(currentNode.children);
		}
		return false;
	}
	
	// Add u into Q
	private void addState(ObservationNode node){
		addState(node, null);
	}

	private void addState(ObservationNode node, InputSequence dfs) {
		LogManager.logInfo("New state in node : " + node.id);
		
		if (dfs != null){
			boolean needToAdd = true;
			for(int i=zQ.size()-1; i>=0; i--){
				InputSequence sig = zQ.get(i);
				if (sig.startsWith(dfs)) { needToAdd = false; break; }
				if (dfs.startsWith(sig) && !dfs.equals(sig)) zQ.remove(i);
			}
			LogManager.logInfo("DFS : " + dfs + " (" + (needToAdd?"need to be added":"already in "+Options.SYMBOL_SIGMA) + ")");
			if (needToAdd){
				zQ.add(dfs);
				LogManager.logInfo(Options.SYMBOL_SIGMA + " : " + zQ);
			}
		}
		node.haveSigma = true;			
		updateSigmaRec(root);
		
		LogManager.logObservationTree(root);
		LogManager.logLine();
		
		ObservationNode newState = findFirstEquivalent(node);
		if (newState.state == -1){
			newState.state = states.size();
			newState.equivTo = states.size();
			states.add(newState);

			for (Node c : newState.children) ((ObservationNode)c).haveSigma = true;
			updateSigmaRec(newState);
		}
		node.equivTo = newState.equivTo;
		
		updateLabelBFS(root);
	}

	// Utilisé dans "u is Z-equivalent to a traversed state w of U"
	private InputSequence compareNodesUsingZ(Node node1, Node node2) {
/*		//for(InputSequence seq : z){
			Node currentNode1 = node1;
			Node currentNode2 = node2;
			InputSequence dfs = new InputSequence();
			//for(String input : seq.sequence){
				dfs.addInput(input);
				if (!currentNode1.childBy(input).output.equals(currentNode2.childBy(input).output)) return dfs;
				currentNode1 = currentNode1.childBy(input);
				currentNode2 = currentNode2.childBy(input);
			//}
		//}
*/		return null;
	}
	
	private InputSequence compareNodesUsingSubTrees(InputSequence dfs, Node node1, Node node2) {
		for(Node cn1 : node1.children){
			String input = cn1.input;
			if (node2.haveChildBy(input) && !(cn1.output.equals(node2.childBy(input).output))){
				dfs.addInput(input);
				return dfs;
			}
		}
		for(Node cn1 : node1.children){
			dfs.addInput(cn1.input);
			if (node2.haveChildBy(cn1.input)){
				InputSequence cdfs = compareNodesUsingSubTrees(dfs, cn1, node2.childBy(cn1.input));
				if (cdfs != null) return cdfs;
			}
			dfs.removeLastInput();
		}
		return null;
	}

	public void learn() {
		LogManager.logConsole("Inferring the system");
		InputSequence ce = null;
		do{
			updateLabelBFS(root);
			LogManager.logObservationTree(root);
			while (processNextDistinguishingSequence()) LogManager.logObservationTree(root);

			stopLog();
			currentConjecture = createConjecture();
			startLog();
			
			ce = driver.getCounterExample(currentConjecture);
			if (ce != null){
				LogManager.logInfo("Adding the counter example to tree");
				// 4.2.	Build U(Z, CE)
				askInputSequenceToNode(root, ce);
				
				//updateLabelWithConjectureRec(askInputSequenceToNode(root, ce));
			}
		}while(ce != null);
	}
	
	// 4.4.4.	Label the nodes of the observation tree by states of the Z’-quotient
	private void updateLabelWithConjectureBFS(ObservationNode node) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			ObservationNode currentState = root;
			InputSequence ce = getPreviousInputSequenceFromNode(node);
			node = currentState;
			while (ce.getLength()>0){
				if (node.equivTo == -1){
					node.equivTo = currentState.state;
				}			
				String input = ce.getFirstSymbol();
				node = (ObservationNode) node.childBy(input);
				currentState = (ObservationNode) currentState.childBy(input);
				if (currentState.state==-1) currentState = states.get(currentState.equivTo);
				ce.removeFirstInput();
			}			
			queue.remove(0);
			queue.addAll(currentNode.children);
		}
	}
	
	// u is Z-equivalent to a traversed state w of U
	private void updateLabelBFS(ObservationNode node) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			updateEquivalent(currentNode);
			queue.remove(0);
			queue.addAll(currentNode.children);
		}
	}

	private void updateSigmaRec(Node node) {
/*		if (node.haveSigma){
			extendNode(node, zQ);
		}
		for(Node child : node.children){
			updateSigmaRec(child);
		}*/
	}
}
