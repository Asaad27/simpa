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
	
	public TreeNode removeChild(State q){
		for (ArrayTreeNodeWithoutConjecture n : children){
			if (n.getState().equals(q))
				children.remove(n);
			return n;
		}
		return null;
	}
}
