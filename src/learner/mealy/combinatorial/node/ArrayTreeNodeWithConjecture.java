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
import drivers.mealy.CompleteMealyDriver;

public class ArrayTreeNodeWithConjecture extends TreeNodeWithConjecture{
	private List<ArrayTreeNodeWithConjecture> children;

	public ArrayTreeNodeWithConjecture(CompleteMealyDriver d){
		super(d);
		children = new ArrayList<ArrayTreeNodeWithConjecture>();
	}

	private ArrayTreeNodeWithConjecture(ArrayTreeNodeWithConjecture parent, State s) {
		super(parent,s);
		children = new ArrayList<ArrayTreeNodeWithConjecture>();
	}

	public ArrayTreeNodeWithConjecture getOnlyChild() {
		assert haveForcedChild();
		return children.get(0);
	}
	
	public ArrayTreeNodeWithConjecture getChild(State s){
		for (ArrayTreeNodeWithConjecture c : children)
			if (c.getState().equals(s))
				return c;
		return null;
	}

	public void cut() {
		assert children.isEmpty();
		super.cut();
	}

	public ArrayTreeNodeWithConjecture addForcedChild(State to) {
		assert children.isEmpty();
		ArrayTreeNodeWithConjecture child = new ArrayTreeNodeWithConjecture(this, to);
		children.add(child);
		setForcedChild();
		return child;
	}

	public ArrayTreeNodeWithConjecture addChild(String i, String o, State q) {
		ArrayTreeNodeWithConjecture child = new ArrayTreeNodeWithConjecture(this, q);
		child.addTransition(getState(), q, i, o);
		children.add(child);
		return child;
	}
	
	public TreeNode removeChild(State q){
		for (ArrayTreeNodeWithConjecture n : children){
			if (n.getState().equals(q))
				children.remove(n);
			return n;
		}
		return null;
	}
}
