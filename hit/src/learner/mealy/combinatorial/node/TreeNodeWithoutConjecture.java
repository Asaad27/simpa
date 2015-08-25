package learner.mealy.combinatorial.node;

import java.util.ArrayList;
import java.util.List;

import learner.mealy.combinatorial.Conjecture;
import automata.State;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;

public abstract class TreeNodeWithoutConjecture extends TreeNode {
	private MealyTransition transition;
	private List<State> states;
	private MealyDriver driver;

	public TreeNodeWithoutConjecture(MealyDriver d){
		super(d);
		driver = d;
	}
	
	protected void initStates(MealyDriver d){
		states = new ArrayList<State>();
	}

	protected TreeNodeWithoutConjecture(TreeNodeWithoutConjecture parent, State s) {
		super(parent,s);
		states = parent.states;
		driver = parent.driver;
	}

	public State addState(){
		State s = new State("S"+states.size(), false);
		states.add(s);
		return s;
	}

	protected void addTransition(State from, State to, String i, String o) {
		assert transition == null;
		transition = new MealyTransition(null, from, to, i, o);
	}

	public Conjecture getConjecture(){
		Conjecture c = new Conjecture(driver);
		for (State s : states)
			c.addState(s);
		TreeNodeWithoutConjecture n = this;
		while (n != null){
			MealyTransition t = n.transition;
			if (t != null)
				c.addTransition(new MealyTransition(c, t.getFrom(), t.getTo(), t.getInput(), t.getOutput()));
			n = (TreeNodeWithoutConjecture) n.father;
		}
		return c;
	}
	
	public List<State> getStates(){
		return states;
	}
	
	public MealyTransition getTransitionFromWithInput(State s, String i){
		TreeNodeWithoutConjecture n = this;
		while (n != null){
			MealyTransition t = n.transition;
			if (t != null && t.getFrom() == s && t.getInput().equals(i))
				return t;
			n = (TreeNodeWithoutConjecture) n.father;
		}
		return null;
	}
}
