package learner.mealy.combinatorial.node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import automata.State;
import drivers.mealy.MealyDriver;

class HashMapTreeNodeWithConjecture extends TreeNodeWithConjecture{
	private Map<State,HashMapTreeNodeWithConjecture> children;

	public HashMapTreeNodeWithConjecture(MealyDriver d){
		super(d);
		children = new HashMap<State, HashMapTreeNodeWithConjecture>();
	}

	private HashMapTreeNodeWithConjecture(HashMapTreeNodeWithConjecture parent, State s) {
		super(parent,s);
		children = new HashMap<State, HashMapTreeNodeWithConjecture>();
	}

	public HashMapTreeNodeWithConjecture getOnlyChild() {
		assert haveForcedChild();
		Iterator<HashMapTreeNodeWithConjecture> it = children.values().iterator();
		return it.next();
	}
	
	public HashMapTreeNodeWithConjecture getChild(State s){
		return children.get(s);
	}

	public void cut() {
		assert children.isEmpty();
		super.cut();
	}

	public HashMapTreeNodeWithConjecture addForcedChild(State to) {
		assert children.isEmpty();
		HashMapTreeNodeWithConjecture child = new HashMapTreeNodeWithConjecture(this, to);
		children.put(to, child);
		setForcedChild();
		return child;
	}

	public HashMapTreeNodeWithConjecture addChild(String i, String o, State q) {
		HashMapTreeNodeWithConjecture child = new HashMapTreeNodeWithConjecture(this, q);
		child.addTransition(getState(), q, i, o);
		children.put(q, child);
		return child;
	}
}
