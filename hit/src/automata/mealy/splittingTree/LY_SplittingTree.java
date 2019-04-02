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
package automata.mealy.splittingTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.splittingTree.LY_SplittingTreeCalculator.Path;

/**
 * A Splitting Tree as described by Lee & Yannakakis in "Testing Finite-State
 * Machines: State Identification and Verification" IEEE TRANSACTIONS ON
 * COMPUTERS, VOL. 43, NO. 3, pp 306-320 MARCH 1994
 * 
 * @author Nicolas BREMOND
 *
 */
public class LY_SplittingTree {
	/**
	 * The "set-label" in Lee & Yannakakis
	 */
	final Set<State> distinguishedInitialStates;
	/**
	 * The "input string-label" in Lee & Yannakakis
	 */
	InputSequence distinguishingInputSeq;
	/**
	 * The "f_u(L)" mapping in Lee & Yannakakis
	 */
	Map<State, State> reachedStates = null;

	Set<LY_SplittingTree> children = new HashSet<>();

	final LY_SplittingTree parent;
	/**
	 * the output of {@link #parent}'s input sequence leading to this node
	 */
	final OutputSequence parentOutput;

	// Now the data for construction :

	/**
	 * The input computed for this node with the best type between a) b) or c).
	 */
	InputTypeExtended bestInput = null;
	/**
	 * The list of leaves which have an input of type C leading to this node.
	 */
	List<Path> reachedByLeaves = new ArrayList<>();

	/**
	 * a flag to walk in implication graph. It will be set to true only for
	 * biggest leaves and thus do not need to be reseted.
	 */
	boolean seenInImplacationGraph = false;
	/**
	 * The depth of the node in the tree.
	 */
	final int depth;

	enum InputType {
		/**
		 * A,B and C correspond to type a,b and c in Lee & Yannakakis
		 */
		A, B, C,
	}

	/**
	 * A class to represent the type [a), b) or c)] of an input.
	 * 
	 * @author Nicolas BREMOND
	 */
	abstract class InputTypeExtended {
		abstract boolean isValid();

		InputType type;
		String input;

		void setInput(String i) {
			input = i;
		}

		boolean isBetterThan(InputTypeExtended other) {
			final boolean equalResult = false;// value returned in case of
												// equality
			if (other == null)
				return true;
			if (!isValid()) {
				if (other.isValid())
					return false;
				return equalResult;
			}
			if (!other.isValid())
				return true;
			if (type == other.type)
				return equalResult;
			if (type == InputType.C)
				return false;
			if (type == InputType.A)
				return true;
			assert type == InputType.B;
			assert other.type != InputType.B;
			return other.type == InputType.C;
		}
	}

	class InvalidInput extends InputTypeExtended {
		@Override
		public boolean isValid() {
			return false;
		}

	}

	class InputA extends InputTypeExtended {

		public InputA() {
			type = InputType.A;
		}

		@Override
		public boolean isValid() {
			return true;
		}

	}

	class InputB extends InputTypeExtended {
		protected final List<LY_SplittingTree> reachedLeaves;
		public final String output;

		public InputB(List<LY_SplittingTree> reachedLeaves, String output) {
			type = InputType.B;
			this.reachedLeaves = Collections.unmodifiableList(reachedLeaves);
			this.output = output;
		}

		@Override
		public boolean isValid() {
			return true;
		}

	}

	class InputC extends InputTypeExtended {
		public InputC() {
			type = InputType.C;
		}

		@Override
		public boolean isValid() {
			return true;
		}

	}

	/**
	 * compute the type of an input. This method also build the implication
	 * graph if input is of type c).
	 * 
	 * @param input
	 *            the input to test
	 * @param automaton
	 *            the automaton on which the splitting tree is computed
	 * @param leaves
	 *            the other leaves of the tree.
	 * @return an extended input type.
	 */
	public InputTypeExtended inputIsValid(String input, Mealy automaton,
			Collection<LY_SplittingTree> leaves) {
		Map<String, Set<State>> observedTransitions = new HashMap<>();
		for (State s : distinguishedInitialStates) {
			MealyTransition t = automaton.getTransitionFromWithInput(s, input);
			Set<State> reachedStates = observedTransitions.get(t.getOutput());
			if (reachedStates == null) {
				reachedStates = new HashSet<>();
				observedTransitions.put(t.getOutput(), reachedStates);
			}
			if (reachedStates.contains(t.getTo()))
				return new InvalidInput();
			reachedStates.add(t.getTo());
		}
		if (observedTransitions.size() > 1)
			return new InputA();
		String output = observedTransitions.keySet().iterator().next();
		Set<State> reachedStates = observedTransitions.get(output);
		List<LY_SplittingTree> reachedLeaves = new ArrayList<>();
		for (LY_SplittingTree leaf : leaves) {
			for (State s : reachedStates) {
				if (leaf.distinguishedInitialStates.contains(s)) {
					reachedLeaves.add(leaf);
					break;
				}
			}
		}
		if (reachedLeaves.size() > 1)
			return new InputB(reachedLeaves, output);
		assert reachedLeaves.size() == 1;
		assert reachedLeaves.get(0).distinguishedInitialStates
				.size() == distinguishedInitialStates.size();
		LY_SplittingTree reached = reachedLeaves.get(0);
		Path path = new Path(this, input, output, reached);
		reached.reachedByLeaves.add(path);
		return new InputC();
	}

	public Set<LY_SplittingTree> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	LY_SplittingTree findNearestAncestor(LY_SplittingTree other) {
		LY_SplittingTree thisAncestor = this;
		LY_SplittingTree otherAncestor = other;
		while (thisAncestor.depth > otherAncestor.depth)
			thisAncestor = thisAncestor.parent;
		while (otherAncestor.depth > thisAncestor.depth)
			otherAncestor = otherAncestor.parent;
		while (thisAncestor != otherAncestor) {
			assert thisAncestor.depth == otherAncestor.depth;
			thisAncestor = thisAncestor.parent;
			otherAncestor = otherAncestor.parent;
		}
		return thisAncestor;
	}

	/**
	 * find the deepest common ancestor to a set of nodes.
	 * 
	 * @TODO can be optimized as explained in Lee & Yannakakis article.
	 * @param nodes
	 *            the (grand)* children of the node to search.
	 * @return a node parent of all the given nodes.
	 */
	static LY_SplittingTree findNearestAncestor(
			Collection<LY_SplittingTree> nodes) {
		Iterator<LY_SplittingTree> it = nodes.iterator();
		if (!it.hasNext())
			return null;
		LY_SplittingTree result = it.next();
		while (it.hasNext()) {
			result = result.findNearestAncestor(it.next());
		}
		return result;
	}

	/**
	 * create a root tree
	 * 
	 * @param states
	 *            the states of the automaton
	 */
	LY_SplittingTree(Set<State> states) {
		this(states, null, null);
	}

	/**
	 * add a child to a node.
	 * 
	 * @param states
	 *            the states which must be distinguished by the new node
	 * @param parent
	 *            the parent of the node to create
	 * @param parentOutput
	 *            the output of the parent's {@link #distinguishingInputSeq}
	 *            produced by the given states.
	 */
	LY_SplittingTree(Set<State> states, LY_SplittingTree parent,
			OutputSequence parentOutput) {
		this.parent = parent;
		if (parent == null)
			depth = 0;
		else {
			depth = parent.depth + 1;
			parent.children.add(this);
			assert parent.distinguishingInputSeq.getLength() == parentOutput
					.getLength();
		}
		this.parentOutput = parentOutput;
		distinguishedInitialStates = Collections.unmodifiableSet(states);
		distinguishingInputSeq = null;
		reachedStates = null;
	}

	/**
	 * compute the member {@link LY_SplittingTree#reachedStates}
	 * 
	 * @param automaton
	 *            the automaton used to find reached states.
	 */
	public void updateReachedStates(Mealy automaton) {
		assert reachedStates == null;
		reachedStates = new HashMap<>();
		for (State s : distinguishedInitialStates) {
			reachedStates.put(s,
					automaton.applyGetState(distinguishingInputSeq, s));
		}
	}

	/**
	 * check if the mapping {@link #reachedStates} is correct. This method was
	 * created for assertion.
	 * 
	 * @param automaton
	 *            the automaton in which paths should be checked.
	 * @return false if one element of mapping is not correct.
	 */
	public boolean checkReached(Mealy automaton) {
		for (Entry<State, State> entry : reachedStates.entrySet())
			if (automaton.applyGetState(distinguishingInputSeq,
					entry.getKey()) != entry.getValue())
				return false;
		return true;
	}

	/**
	 * check that states of this node have the expected output regarding to
	 * (grand)*parents inputs. (this method is made for assertions and should be
	 * optimized if used elsewhere)
	 * 
	 * @param automaton
	 *            the automaton in which we can observe outputs.
	 * @return false if one state do not give the same output as the edge
	 *         leading to it's containing node.
	 */
	public boolean checkStates(Mealy automaton) {
		if (parent == null)
			return true;
		for (State s : distinguishedInitialStates) {
			if (!automaton.apply(parent.distinguishingInputSeq, s)
					.equals(parentOutput))
				return false;
		}
		return parent.checkStates(automaton);
	}

	public boolean equal(Object other) {
		return this == other;
	}

	public String toString() {
		return "node with states " + distinguishedInitialStates;
	}
}
