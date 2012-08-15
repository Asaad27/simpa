package learner.mealy.tree;

import java.util.ArrayList;
import java.util.List;

import learner.Learner;
import learner.efsm.LiConjecture;
import learner.mealy.LmConjecture;
import learner.mealy.Node;
import main.Options;
import tools.loggers.LogManager;
import weka.filters.unsupervised.attribute.Add;
import automata.State;
import automata.efsm.ParameterizedInput;
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
		this.i = new ArrayList<String>();
		this.z = new ArrayList<InputSequence>();
		this.root = new ObservationNode();
		this.root.makeInitial();
	}
	
	private boolean noLabelledPred(ObservationNode node) {
		boolean noLabelledPred = true;
		while (node.parent != null){
			ObservationNode parent = (ObservationNode)node.parent;
			if (parent.isState()) return false;
			node = parent;
		}
		return noLabelledPred;
	}
	
	private InputSequence compareNodesUsingZ(Node node1, Node node2) {
		for(InputSequence seq : z){
			Node currentNode1 = node1;
			Node currentNode2 = node2;
			InputSequence dfs = new InputSequence();
			for(String input : seq.sequence){
				dfs.addInput(input);
				if (!currentNode1.childBy(input).output.equals(currentNode2.childBy(input).output)) return dfs;
				currentNode1 = currentNode1.childBy(input);
				currentNode2 = currentNode2.childBy(input);
			}
		}
		return null;
	}
	
	private ObservationNode findFirstEquivalent(ObservationNode node) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			if (currentNode.isState()){
				InputSequence tmpdfs = compareNodesUsingZ(node,  currentNode);
				if (tmpdfs == null) return currentNode;
				queue.addAll(currentNode.children);
			}
			queue.remove(0);		
		}
		return null;
	}
	
	private void addState(ObservationNode node) {
		LogManager.logInfo("New state in node : " + node.id);
		node.state = states.size();
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

	private LmConjecture buildQuotient() {
		//1. Q = Q0		
		this.states = new ArrayList<ObservationNode>();
		
		//2. for (each state u of U being traversed during Breadth First Search)
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode)queue.get(0);

			//4. if (u has no labelled predecessor)
			if (currentNode.isState() && noLabelledPred(currentNode)){
				//6. Extend_Node (u, Z),
				extendNodeWithInputSeqs(currentNode, z);
				
				//7. if (u is Z-equivalent to a traversed state w of U)
				ObservationNode w = findFirstEquivalent(currentNode);
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
			
			queue.remove(0);
			queue.addAll(currentNode.children);
		}
		
		return createConjecture();
	}

	private List<InputSequence> includeInto(List<InputSequence> into, InputSequence seq) {
		List<InputSequence> ret = new ArrayList<InputSequence>();
		boolean exists = false;
		for (InputSequence s : into){
			ret.add(s);
			if (s.startsWith(seq)) exists = true;
		}
		if (!exists) ret.add(seq);
		return ret;
	}	

	private void labelNodes(ObservationNode node, LmConjecture quotient) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(root);
		ObservationNode currentNode = null;
		while(!queue.isEmpty()){
			currentNode = (ObservationNode) queue.get(0);
			ObservationNode currentState = root;
			InputSequence ce = getPreviousInputSequenceFromNode(node);
			node = currentState;
			while (ce.getLength()>0){
				if (node.label == -1){
					node.label = currentState.state;
				}			
				String input = ce.getFirstSymbol();
				node = (ObservationNode) node.childBy(input);
				currentState = (ObservationNode) currentState.childBy(input);
				if (currentState.state==-1) currentState = states.get(currentState.label);
				ce.removeFirstInput();
			}			
			queue.remove(0);
			queue.addAll(currentNode.children);
		}		
	}
	
	public void learn() {
		LogManager.logConsole("Inferring the system");
		InputSequence ce, ceState = null;
		
		LmConjecture Z_Q = buildQuotient();
		
		// 3. while there exists an unprocessed counterexample CE
		do{
			ce = driver.getCounterExample(Z_Q);
			if (ce != null){
				LogManager.logInfo("Adding the counter example to tree");
				
				//3.2.	Build U(Z, CE)
				askInputSequenceToNode(root, ce);
				
				//3.3.	Extend I with inputs from X that appear in CE, yielding I’
				List<String> ip = extendInputSymbolsWith(ce);
				
				LogManager.logObservationTree(root);
				
				//3.4.  while there exists a counterexample for any state of the Z_Q that is not in Z 
				do{
					ceState = driver.getCounterExample(Z_Q);
					if (ceState != null){
						// 3.4.2.	Include it into the set Z
						List<InputSequence> zp = includeInto(z, ceState);
						
						// 3.4.3.	Build-quotient (A, I, Z’, U(Z,CE) 
						LmConjecture Zp_Q = buildQuotient();
						
						// 3.4.4.	Label the nodes of the observation tree by states of the K-quotient
						labelNodes(root, Zp_Q);
						
						// 3.4.5.	Let Z=Z’, I=I’
						z = zp; i = ip;
						
					}					
				}while(ceState != null);				
			}
		}while(ce != null);
		
		LmConjecture conjecture = createConjecture();
		conjecture.exportToDot();
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
		LogManager.logObservationTree(root);
		
		LmConjecture c = new LmConjecture(driver);

		for(int i=0; i<states.size(); i++){
			c.addState(new State("S" + i, i == 0));
		}

		try{
			for(ObservationNode s : states){
				for(String input : i){
					ObservationNode child = (ObservationNode) s.childBy(input);
					c.addTransition(new MealyTransition(c, c.getState(s.state), c.getState(child.label), input, child.output));
				}
			}
		}catch(Exception e){
			LogManager.logObservationTree(root);
			e.printStackTrace();
			System.exit(1);
		}

		LogManager.logInfo(Options.SYMBOL_SIGMA + " : " + z);
		LogManager.logInfo("I : " + i);

		LogManager.logInfo("Conjecture have " + c.getStateCount() + " states and " + c.getTransitionCount() + " transitions : ");		
		for (MealyTransition t : c.getTransitions()){
			LogManager.logTransition(t.toString());
		}
		LogManager.logLine();

		c.exportToDot();

		return c;
	}

//	private boolean processNextDistinguishingSequence() {		
//		return getDistinguishingSequenceBFS();
//	}

//	private void updateEquivalent(ObservationNode node) {
//		for (ObservationNode state : states){
//			InputSequence tmpdfs = compareNodesUsingZ(node,  state);
//			if (tmpdfs == null){
//				node.label = state.state;
//				return;
//			}			
//		}
//	}	
	
	// 4.4.	while there exists a counterexample for any state of the Z_Q 
//	private boolean getDistinguishingSequenceBFS() {
//		List<Node> queue = new ArrayList<Node>();
//		queue.add(root);
//		ObservationNode currentNode = null;
//		LogManager.logObservationTree(root);
//		while(!queue.isEmpty()){
//			currentNode = (ObservationNode) queue.get(0);
//			System.out.println(currentNode.id);
//			if (currentNode.state == -1){
//				InputSequence tmpdfs = new InputSequence();
//				if (currentNode.equivTo == -1 && currentNode.haveSigma){
//					addState(currentNode);
//					return true;
//				}else if (currentNode.equivTo != -1){
//					compareNodesUsingSubTrees(tmpdfs, states.get(currentNode.equivTo),  currentNode);
//					if (tmpdfs.getLength() > 0){
//						addState(currentNode, tmpdfs);
//						return true;
//					}
//				}
//			}
//			queue.remove(0);
//			queue.addAll(currentNode.children);
//		}
//		return false;
//	}

	
//	private InputSequence compareNodesUsingSubTrees(InputSequence dfs, Node node1, Node node2) {
//		for(Node cn1 : node1.children){
//			String input = cn1.input;
//			if (node2.haveChildBy(input) && !(cn1.output.equals(node2.childBy(input).output))){
//				dfs.addInput(input);
//				return dfs;
//			}
//		}
//		for(Node cn1 : node1.children){
//			dfs.addInput(cn1.input);
//			if (node2.haveChildBy(cn1.input)){
//				InputSequence cdfs = compareNodesUsingSubTrees(dfs, cn1, node2.childBy(cn1.input));
//				if (cdfs != null) return cdfs;
//			}
//			dfs.removeLastInput();
//		}
//		return null;
//	}
}
