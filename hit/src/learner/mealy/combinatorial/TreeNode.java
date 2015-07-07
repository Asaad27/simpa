package learner.mealy.combinatorial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import learner.mealy.LmTrace;
import drivers.mealy.MealyDriver;
import automata.State;
import automata.mealy.MealyTransition;

class TreeNode {
	protected Conjecture conjecture;
	private boolean copyConjectureOnWrite;
	protected Map<State,TreeNode> childs;
	protected boolean haveForcedChild;
	protected boolean isCutted;
	protected final int depth;
	protected final TreeNode father;
	protected final State state;

	public TreeNode(MealyDriver d){
		conjecture = new Conjecture(d);
		copyConjectureOnWrite = false;
		childs = new HashMap<State, TreeNode>();
		haveForcedChild = false;
		isCutted = false;
		depth = 0;
		father = null;
		state = addState();
	}

	public TreeNode(TreeNode parent, State s) {
		this.conjecture = parent.conjecture;
		copyConjectureOnWrite = true;
		childs = new HashMap<State, TreeNode>();
		haveForcedChild = false;
		isCutted = false;
		father = parent;
		depth = parent.depth +1;
		state = s;
	}

	public State addState(){
		assert depth == 0;//this method is supposed to be uniquely applied on the root
		State s = conjecture.addState();
		return s;
	}

	public TreeNode getOnlyChild() {
		assert haveForcedChild;
		Iterator<TreeNode> it = childs.values().iterator();
		return it.next();
	}

	protected void cut() {
		assert childs.isEmpty();
		isCutted = true;
	}

	protected TreeNode addForcedChild(State to) {
		assert childs.isEmpty();
		TreeNode child = new TreeNode(this, to);
		childs.put(to, child);
		haveForcedChild = true;
		return child;
	}

	protected TreeNode addChild(String i, String o, State q) {
		TreeNode child = new TreeNode(this, q);
		child.addTransition(state, q, i, o);
		childs.put(q, child);
		return child;
	}

	private void addTransition(State from, State to, String i, String o) {
		makeConjectureLocal();
		conjecture.addTransition(new MealyTransition(conjecture, from, to, i, o));
	}

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
		if (isCutted)
			s.append("X");
		s.append(haveForcedChild ? "⇒ " : "→ ");
		return s;
	}
}
