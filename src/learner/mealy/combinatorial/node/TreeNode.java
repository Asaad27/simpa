/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.combinatorial.node;

import java.util.List;

import learner.mealy.LmTrace;
import learner.mealy.combinatorial.Conjecture;
import automata.State;
import automata.mealy.MealyTransition;
import drivers.mealy.CompleteMealyDriver;

public abstract class TreeNode {
	private boolean haveForcedChild;//indicate that there is only one child which is imposed due to a previous transition
	private boolean isCut;//indicate that this node is incoherent with trace so it must be ignored and do not have children.
	private final int depth;//the depth of the node in the tree.
	protected final TreeNode father;
	private final State state;
	private int discoveredStatesNb;

	public String desc = "";
	public int id=max_id++;
	private static int max_id=0;


	/**
	 * Create the root of the tree.
	 * @param d the driver used to create conjecture
	 */
	public TreeNode(CompleteMealyDriver d){
		haveForcedChild = false;
		isCut = false;
		depth = 0;
		father = null;
		initStates(d);
		state = addState();
		discoveredStatesNb = 1;
	}

	/**
	 * init the node in order to call addState
	 */
	protected abstract void initStates(CompleteMealyDriver d);

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
		discoveredStatesNb = parent.discoveredStatesNb +1;
		TreeNode n = parent;
		while (n != null){
			if (n.state == state){
				discoveredStatesNb = parent.discoveredStatesNb;
				break;
			}
			n=n.father;
		}
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

	public abstract TreeNode removeChild(State q);

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

	/**
	 * get the number of crossed stated from root to this node (included)
	 * @return
	 */
	public int getDiscoveredStatesNb(){
		return discoveredStatesNb;
	}

	public abstract List<State> getStates();
	public abstract MealyTransition getTransitionFromWithInput(State s, String i);

	public boolean haveUncuttedChild(){
		if (isCut())
			return false;
		if (haveForcedChild())
			return getOnlyChild().haveUncuttedChild();
		int childcount = 0;
		for (State s : getStates()){
			TreeNode child = getChild(s);
			if (child == null)
				continue;
			if (child.haveUncuttedChild())
				return true;
			childcount++;
		}
		return childcount == 0;
	}
}
