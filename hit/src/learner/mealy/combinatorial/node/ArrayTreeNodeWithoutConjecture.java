package learner.mealy.combinatorial.node;

import java.util.ArrayList;
import java.util.List;

import automata.State;
import drivers.mealy.MealyDriver;

public class ArrayTreeNodeWithoutConjecture extends TreeNodeWithoutConjecture{
	private List<ArrayTreeNodeWithoutConjecture> children;

	public ArrayTreeNodeWithoutConjecture(MealyDriver d){
		super(d);
		children = new ArrayList<ArrayTreeNodeWithoutConjecture>();
	}

	private ArrayTreeNodeWithoutConjecture(ArrayTreeNodeWithoutConjecture parent, State s) {
		super(parent,s);
		children = new ArrayList<ArrayTreeNodeWithoutConjecture>();
	}

	public ArrayTreeNodeWithoutConjecture getOnlyChild() {
		assert haveForcedChild();
		return children.get(0);
	}
	
	public ArrayTreeNodeWithoutConjecture getChild(State s){
		for (ArrayTreeNodeWithoutConjecture c : children)
			if (c.getState().equals(s))
				return c;
		return null;
	}

	public void cut() {
		assert children.isEmpty();
		super.cut();
	}

	public ArrayTreeNodeWithoutConjecture addForcedChild(State to) {
		assert children.isEmpty();
		ArrayTreeNodeWithoutConjecture child = new ArrayTreeNodeWithoutConjecture(this, to);
		children.add(child);
		setForcedChild();
		return child;
	}

	public ArrayTreeNodeWithoutConjecture addChild(String i, String o, State q) {
		ArrayTreeNodeWithoutConjecture child = new ArrayTreeNodeWithoutConjecture(this, q);
		child.addTransition(getState(), q, i, o);
		children.add(child);
		return child;
	}
}
