package learner.mealy.combinatorial.node;

import java.util.List;

import learner.mealy.LmTrace;
import learner.mealy.combinatorial.Conjecture;
import automata.State;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;

public abstract class TreeNode {
	private boolean haveForcedChild;//indicate that there is only one child which is imposed due to a previous transition
	private boolean isCut;//indicate that this node is incoherent with trace so it must be ignored and do not have children.
	private final int depth;//the depth of the node in the tree.
	protected final TreeNode father;
	private final State state;

	/**
	 * Create the root of the tree.
	 * @param d the driver used to create conjecture
	 */
	public TreeNode(MealyDriver d){
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
	protected TreeNode(TreeNode parent, State s) {
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
	public abstract State addState();

	/**
	 * get the only child of the node.
	 * This method must only be called on Node which have forced child.
	 * @return
	 */
	abstract public TreeNode getOnlyChild();

	abstract public TreeNode getChild(State s);

	/**
	 * mark the Node as cut.
	 */
	public void cut() {
		isCut = true;
	}

	public abstract TreeNode addForcedChild(State to);
	
	protected void setForcedChild(){
		haveForcedChild = true;
	}

	public abstract TreeNode addChild(String i, String o, State q);

	abstract protected void addTransition(State from, State to, String i, String o); 

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

	public State getState(){
		return state;
	}
	
	public abstract Conjecture getConjecture();
	
	public boolean isCut(){
		return isCut;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public boolean haveForcedChild(){
		return haveForcedChild;
	}
	
	public abstract List<State> getStates();
	public abstract MealyTransition getTransitionFromWithInput(State s, String i);
}
