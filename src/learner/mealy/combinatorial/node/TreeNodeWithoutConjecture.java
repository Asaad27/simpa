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

import java.util.ArrayList;
import java.util.List;

import drivers.mealy.CompleteMealyDriver;
import learner.mealy.combinatorial.Conjecture;
import automata.State;
import automata.mealy.MealyTransition;

public abstract class TreeNodeWithoutConjecture extends TreeNode {
	private MealyTransition transition;
	private List<State> states;
	private CompleteMealyDriver driver;

	public TreeNodeWithoutConjecture(CompleteMealyDriver d){
		super(d);
		driver = d;
	}
	
	protected void initStates(CompleteMealyDriver d){
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
