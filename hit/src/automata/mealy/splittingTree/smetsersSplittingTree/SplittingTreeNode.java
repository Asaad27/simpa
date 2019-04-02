/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package automata.mealy.splittingTree.smetsersSplittingTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;

public class SplittingTreeNode {
	final SplittingTree tree;
	/**
	 * position of first state in {@link SplittingTree#states} array
	 * (inclusive).
	 */
	final int startState;
	/**
	 * position after last state in {@link SplittingTree#states} array
	 * (exclusive).
	 */
	final int endState;

	/**
	 * Input sequence for which each child have a different answer.
	 */
	InputSequence distinguishingInputSeq = null;

	private List<SplittingTreeNode> children = new ArrayList<>();

	final SplittingTreeNode parent;

	/**
	 * Create the root node of a splitting tree.
	 * 
	 * @param splittingTree
	 *            the tree containing this node.
	 */
	SplittingTreeNode(SplittingTree splittingTree) {
		this.tree = splittingTree;
		parent = null;
		startState = 0;
		endState = tree.states.size();
		tree.leafCreated(this);
	}

	private SplittingTreeNode(SplittingTreeNode parent, int start, int end) {
		this.tree = parent.tree;
		this.parent = parent;
		parent.children.add(this);
		this.endState = end;
		this.startState = start;
		tree.leafCreated(this);
	}

	/**
	 * get the states in the partition corresponding to this node. The tree must
	 * not be modified while iterating over the returned collection.
	 * 
	 * @return a Collection containing the states corresponding to this node.
	 */
	public Collection<State> getStates() {
		return new Collection<State>() {

			@Override
			public int size() {
				return endState - startState;
			}

			@Override
			public boolean isEmpty() {
				assert (endState != startState) : "the node should not be empty";
				return endState == startState;
			}

			@Override
			public boolean contains(Object o) {
				for (State state : this) {
					if (state == o)
						return true;
				}
				return false;
			}

			@Override
			public Iterator<State> iterator() {
				return new Iterator<State>() {
					int pos = startState;

					@Override
					public boolean hasNext() {
						return pos != endState;
					}

					@Override
					public State next() {
						State s = tree.states.get(pos);
						pos++;
						return s;
					}
				};
			}

			@Override
			public Object[] toArray() {
				return new ArrayList<State>(this).toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return new ArrayList<State>(this).toArray(a);
			}

			@Override
			public boolean add(State e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return new HashSet<>(this).containsAll(c);
			}

			@Override
			public boolean addAll(Collection<? extends State> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}
		};
	}

	boolean isLeaf() {
		assert distinguishingInputSeq != null || children.isEmpty();
		return distinguishingInputSeq == null;
	}

	/**
	 * get the immediate child of this node containing a given state.
	 * 
	 * @param s
	 *            a state in the partition of this node.
	 * @return the immediate child of this node containing the given state.
	 */
	SplittingTreeNode getChildContaining(State s) {
		assert getStates().contains(s);
		for (SplittingTreeNode child : children)
			if (child.getStates().contains(s))
				return child;
		assert false : "child are not a partition of this node";
		return null;
	}

	/**
	 * get the list of immediate children of this node.
	 * 
	 * @return the list of immediate children of this node.
	 */
	Collection<SplittingTreeNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * same as {@link #split(InputSequence, Collection)} but with a single
	 * input.
	 * 
	 * @param input
	 *            the input used to split this node into several children
	 * @param blocks
	 *            the block of states for each different answer observed
	 * @return a collection of children created
	 */
	Collection<SplittingTreeNode> split(String input,
			Collection<Set<State>> blocks) {
		return split(new InputSequence(input), blocks);
	}

	/**
	 * create the children of this node. Same as
	 * {@link #split(InputSequence, Collection)} apart that this method do not
	 * need to have the states sorted by their answer.
	 * 
	 * @param seq
	 *            the sequence which distinguish
	 * @param automaton
	 * @return
	 */
	Collection<SplittingTreeNode> split(InputSequence seq, Mealy automaton) {
		HashMap<OutputSequence, Set<State>> blocks = new HashMap<>();
		for (State s : getStates()) {
			OutputSequence out = automaton.apply(seq, s);
			Set<State> block = blocks.get(out);
			if (block == null) {
				block = new HashSet<>();
				blocks.put(out, block);
			}
			block.add(s);
		}
		return split(seq, blocks.values());
	}

	/**
	 * create the children of this node.
	 * 
	 * @param inputSeq
	 *            the input sequence used to differentiate the states of this
	 *            node.
	 * @param blocks
	 *            the states of this node sorted by their answer to inputSeq
	 * @return the list of children created
	 */
	Collection<SplittingTreeNode> split(InputSequence inputSeq,
			Collection<Set<State>> blocks) {
		assert distinguishingInputSeq == null;
		Collection<SplittingTreeNode> newLeaves = new LinkedList<>();
		distinguishingInputSeq = inputSeq;
		int childStart = startState;
		for (Set<State> childStates : blocks) {
			assert getStates().containsAll(childStates);
			int childEnd = childStart + childStates.size();
			tree.move(childStates, childStart, endState);
			SplittingTreeNode child = new SplittingTreeNode(this, childStart,
					childEnd);
			newLeaves.add(child);
			childStart = childEnd;
		}
		return newLeaves;
	}
}
