package learner.mealy.combinatorial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import learner.mealy.LmTrace;
import drivers.mealy.MealyDriver;
import automata.State;
import automata.mealy.MealyTransition;

class TreeNode {
	protected Conjecture conjecture;//by defaults the conjecture is shared with the father's conjecture. Note that the father is not suppose to update the conjecture.
	private boolean copyConjectureOnWrite;//indicate if the conjecture is shared with the father's.
	private Map<State,TreeNode> children;
	protected boolean haveForcedChild;//indicate that there is only one child which is imposed due to a previous transition
	protected boolean isCut;//indicate that this node is incoherent with trace so it must be ignored and do not have children.
	protected final int depth;//the depth of the node in the tree.
	protected final TreeNode father;
	protected final State state;

	/**
	 * Create the root of the tree.
	 * @param d the driver used to create conjecture
	 */
	public TreeNode(MealyDriver d){
		conjecture = new Conjecture(d);
		copyConjectureOnWrite = false;
		children = new HashMap<State, TreeNode>();
		haveForcedChild = false;
		isCut = false;
		depth = 0;
		father = null;
		state = addState();
	}

	/**
	 * create a child node in the tree.
	 * @param parent the father of the created node
	 * @param s the state of the node
	 */
	private TreeNode(TreeNode parent, State s) {
		this.conjecture = parent.conjecture;
		copyConjectureOnWrite = true;
		children = new HashMap<State, TreeNode>();
		haveForcedChild = false;
		isCut = false;
		father = parent;
		depth = parent.depth +1;
		state = s;
	}

/**
 * add a state to the conjecture.
 * Note that as all Node's conjectures are partially shared, adding a state to one will add a state to all.
 * It may be better to call this method on the root.
 * @see learner.mealy.combinatorial.Conjecture
 * @return the created state.
 */
	public State addState(){
		State s = conjecture.addState();
		return s;
	}

	/**
	 * get the only child of the node.
	 * This method must only be called on Node which have forced child.
	 * @return
	 */
	public TreeNode getOnlyChild() {
		assert haveForcedChild;
		Iterator<TreeNode> it = children.values().iterator();
		return it.next();
	}
	
	public TreeNode getChild(State s){
		return children.get(s);
	}

	/**
	 * mark the Node as cut.
	 */
	protected void cut() {
		assert children.isEmpty();
		isCut = true;
	}

	protected TreeNode addForcedChild(State to) {
		assert children.isEmpty();
		TreeNode child = new TreeNode(this, to);
		children.put(to, child);
		haveForcedChild = true;
		return child;
	}

	protected TreeNode addChild(String i, String o, State q) {
		TreeNode child = new TreeNode(this, q);
		child.addTransition(state, q, i, o);
		children.put(q, child);
		return child;
	}

	private void addTransition(State from, State to, String i, String o) {
		makeConjectureLocal();
		conjecture.addTransition(new MealyTransition(conjecture, from, to, i, o));
	}

	/**
	 * as we share Conjecture between Nodes, we need to clone them when we want to add states.
	 * This method do this for you.
	 */
	private void makeConjectureLocal() {
		if (copyConjectureOnWrite){
			conjecture = new Conjecture(conjecture);
			copyConjectureOnWrite = false;
		}
	}

	
	public String getStatesTrace(){
		if (father == null)
			return state.toString();
		return father.getStatesTrace() + (father.haveForcedChild ? " ⇒ " : " → ") + state.toString();
	}

	public StringBuilder getStatesTrace(LmTrace t){
		//TODO make a non-recursive version of this method ?
		StringBuilder s;
		if (father == null)
			s = new StringBuilder();
		else 
			s = father.getStatesTrace(t);
		s.append(state.toString());
		s.append(" ");
		if (depth < t.size()){
			s.append(haveForcedChild ? "⇒" : "→");
			s.append("("+t.getInput(depth)+"/"+t.getOutput(depth)+")");
		}
		if (isCut)
			s.append("X");
		s.append(haveForcedChild ? "⇒ " : "→ ");
		return s;
	}
}
