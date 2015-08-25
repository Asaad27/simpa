package learner.mealy.combinatorial.node;

import java.util.List;

import learner.mealy.combinatorial.Conjecture;
import automata.State;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;

public abstract class TreeNodeWithConjecture extends TreeNode{
	private Conjecture conjecture;//by defaults the conjecture is shared with the father's conjecture. Note that the father is not suppose to update the conjecture.
	private boolean copyConjectureOnWrite;//indicate if the conjecture is shared with the father's.

	public TreeNodeWithConjecture(MealyDriver d){
		super(d);
		copyConjectureOnWrite = false;
	}

	protected void initStates(MealyDriver d){
		conjecture = new Conjecture(d);
	}
	
	protected TreeNodeWithConjecture(TreeNodeWithConjecture parent, State s) {
		super(parent,s);
		this.conjecture = parent.conjecture;
		copyConjectureOnWrite = true;
	}

	public State addState(){
		State s = conjecture.addState();
		return s;
	}

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

	public Conjecture getConjecture(){
		return conjecture;
	}

	public List<State> getStates(){
		return conjecture.getStates();
	}

	public MealyTransition getTransitionFromWithInput(State s, String i){
		return conjecture.getTransitionFromWithInput(s, i);
	}
}
