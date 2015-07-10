package learner.mealy.combinatorial.node;

import learner.mealy.LmTrace;
import learner.mealy.combinatorial.Conjecture;
import drivers.mealy.MealyDriver;
import automata.State;
import automata.mealy.MealyTransition;

public abstract class TreeNodeWithConjecture {
	private Conjecture conjecture;//by defaults the conjecture is shared with the father's conjecture. Note that the father is not suppose to update the conjecture.
	private boolean copyConjectureOnWrite;//indicate if the conjecture is shared with the father's.
	protected boolean haveForcedChild;//indicate that there is only one child which is imposed due to a previous transition
	private boolean isCut;//indicate that this node is incoherent with trace so it must be ignored and do not have children.
	private final int depth;//the depth of the node in the tree.
	private final TreeNodeWithConjecture father;
	private final State state;

	/**
	 * Create the root of the tree.
	 * @param d the driver used to create conjecture
	 */
	public TreeNodeWithConjecture(MealyDriver d){
		conjecture = new Conjecture(d);
		copyConjectureOnWrite = false;
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
	protected TreeNodeWithConjecture(TreeNodeWithConjecture parent, State s) {
		this.conjecture = parent.conjecture;
		copyConjectureOnWrite = true;
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
	abstract public TreeNodeWithConjecture getOnlyChild();

	abstract public TreeNodeWithConjecture getChild(State s);

	/**
	 * mark the Node as cut.
	 */
	public void cut() {
		isCut = true;
	}

	public abstract TreeNodeWithConjecture addForcedChild(State to);

	public abstract TreeNodeWithConjecture addChild(String i, String o, State q);

	protected void addTransition(State from, State to, String i, String o) {
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

	public State getState(){
		return state;
	}
	
	public Conjecture getConjecture(){
		return conjecture;
	}
	
	public boolean isCut(){
		return isCut;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public boolean haveForcedChild(){
		return haveForcedChild;
	}
}
