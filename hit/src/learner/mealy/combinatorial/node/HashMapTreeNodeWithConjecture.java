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
	
	public TreeNode removeChild(State q){
		return children.remove(q);
	}
}
