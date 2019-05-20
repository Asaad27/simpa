/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.DTOracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import automata.State;

public class ExtendedState<T extends Test> {
	final private List<ExtendedTransition<T>> children = new ArrayList<>();
	final private Map<String, ExtendedTransition<T>> sortedChildren = new HashMap<>();
	final private List<ExtendedTransition<T>> parents = new ArrayList<>();
	final State stdState;
	ExtendedTransition<T> shortestAlpha = null;
	int alphaLength = 0;
	SortedQueue<T> tests;

	public ExtendedState(State stdState) {
		this.stdState = stdState;
		tests = new SortedQueue<T>();
	}

	/**
	 * null if no test is available
	 */
	Integer cheapestTest;

	void insert(T t) {
		Integer cheapest = tests.cheapestCost();
		tests.insert(t);
		if (cheapest == null || !tests.cheapestCost().equals(cheapest))
			updateAlpha(null, 0, t.getCost());
	}

	boolean isImprovedBy(ExtendedState<T> other) {
		return isImprovedBy(other.alphaLength, other.cheapestTest);
	}

	/**
	 * 
	 * @param other
	 * @return true in case of equality
	 */
	boolean isImprovedBy(int newAlphaLength, Integer newCheapestTest) {
		if (newCheapestTest == null)
			return false;
		if (cheapestTest == null)
			return true;
		if (cheapestTest.equals(newCheapestTest)
				&& alphaLength >= newAlphaLength + 1)
			return true;
		if (cheapestTest.compareTo(newCheapestTest) < 0)
			return true;
		return false;

	}

	private void updateAlpha(ExtendedTransition<T> newAlpha, int newAlphaLength,
			Integer newCheapestTest) {
		assert newAlpha == null || newAlpha.from == this;
		if (isImprovedBy(newAlphaLength, newCheapestTest)) {
			cheapestTest = newCheapestTest;
			shortestAlpha = newAlpha;
			alphaLength = newAlphaLength;
			for (ExtendedTransition<T> transition : parents) {
				ExtendedState<T> parent = transition.from;
				parent.updateAlpha(transition, alphaLength + 1, cheapestTest);
			}
		}
	}

	void searchAlphaNoLoop() {
		alphaLength = 0;
		shortestAlpha = null;
		if (tests.isEmpty()) {
			cheapestTest = null;
		} else {
			cheapestTest = tests.cheapestCost();
		}

		// check if a child state have a highest test.
		for (ExtendedTransition<T> t : children) {
			ExtendedState<T> child = t.to;
			updateAlpha(t, child.alphaLength + 1, child.cheapestTest);
		}

	}

	/**
	 * this method might change alpha in the whole graph ( but the alpha
	 * modified are leading to an existing test ?)
	 * 
	 * @TODO need a check here about modified alpha
	 */
	void searchAlpha() {
		searchAlphaNoLoop();

		// check if alpha makes a loop
		boolean loop = false;
		{
			ExtendedState<T> currentState = this;
			while (currentState.shortestAlpha != null) {
				currentState = currentState.shortestAlpha.to;
				if (currentState == this) {
					loop = true;
					break;
				}
			}
		}
		if (!loop)
			return;

		// now clear the loop
		// 1.clear all alpha leading to this state (put wrong data instead)
		HashSet<ExtendedState<T>> alphaAncestorsStates = new HashSet<>();
		ArrayList<ExtendedState<T>> toProcess = new ArrayList<ExtendedState<T>>();
		toProcess.add(this);
		alphaAncestorsStates.add(this);
		int processed = 0;
		while (processed < toProcess.size()) {
			ExtendedState<T> s = toProcess.get(processed);
			processed++;
			s.cheapestTest = null;
			s.shortestAlpha = null;
			s.alphaLength = 0;
			for (ExtendedTransition<T> t : s.parents) {
				ExtendedState<T> parent = t.from;
				if (parent.shortestAlpha != null && parent.shortestAlpha.to == s
						&& !alphaAncestorsStates.contains(parent)) {
					toProcess.add(parent);
					alphaAncestorsStates.add(parent);
				}
			}
		}
		assert toProcess.size() == alphaAncestorsStates.size();
		// 2.for all modified state, search best test in state and its neigbourg
		// and update it's ancestors
		for (ExtendedState<T> s : toProcess) {
			s.searchAlphaNoLoop();// there cannot be any loop to this state
									// because we removed its alpha parents.
			// TODO we might need to check here if the new alpha exists
			for (ExtendedTransition<T> t : s.parents) {
				t.from.updateAlpha(t, alphaLength + 1, cheapestTest);
			}
		}

	}

	// invariant : Si le chemin défini existe et est coherent, alors c'est le
	// plus court. (coherent = longueur de alpha correcte et profondeur finale
	// correcte)
	// alpha ne fait pas de boucle
	// Si highestTest==null alors il n'y a pas de chemin vers un etat avec
	// un test.

	/**
	 * 
	 * @param states
	 * @return true if an alpha is found, leading to a state with incomplete
	 *         test.
	 */
	boolean checkAlpha() {
		List<ExtendedState<T>> states = new ArrayList<ExtendedState<T>>();
		ExtendedState<T> currentState = this;
		boolean continueLoop;
		do {
			assert currentState != this || states.size() == 0;
			assert !states.contains(currentState);
			if (currentState.cheapestTest != null
					&& currentState.shortestAlpha == null
					&& currentState.tests.isEmpty()) {
				currentState.searchAlpha();
			}
			if (cheapestTest == null)
			// notice that we test root element and not current
			{
				return false;
			}

			// we check consistency of current state
			if (currentState.cheapestTest == null
					|| currentState.cheapestTest != cheapestTest
					|| currentState.alphaLength
							+ states.size() != alphaLength) {
				assert currentState != this;
				assert states.size() != 0;
				currentState = states.remove(states.size() - 1);
				currentState.searchAlpha();
				continueLoop = true;
				continue;
			}

			// end the loop and switch to next element.
			states.add(currentState);
			continueLoop = false;
			if (currentState.shortestAlpha != null) {
				currentState = currentState.shortestAlpha.to;
				continueLoop = true;
			}
		} while (continueLoop);
		assert new HashSet<ExtendedState<T>>(states).size() == states
				.size() : "alpha contains duplicated states. This needs investigations (maybe we need to build again alpha)";
		return true;
	}

	/**
	 * to be checkde
	 */
	void assertLocallyConsistent() {
		if (cheapestTest != null) {
			if (cheapestTest == tests.cheapestCost()) {
				assert alphaLength == 0;
				assert shortestAlpha == null;
			} else {
				assert alphaLength != 0;
				assert shortestAlpha != null;
			}
		}

	}

	/**
	 * to be checked
	 * 
	 * @return
	 */
	protected boolean isConsistentWithNeighbourg() {
		assertLocallyConsistent();
		// this should not lead to a test highest than any parent
		if (cheapestTest != null)
			for (ExtendedTransition<T> t : parents) {
				ExtendedState<T> parent = t.from;
				if (parent.cheapestTest == null
						|| (parent.cheapestTest < cheapestTest
								&& parent.alphaExists()))
					return false;
			}

		if (shortestAlpha != null) {
			if (alphaLength != shortestAlpha.to.alphaLength + 1)
				return false;
		}
		return true;
	}

	/**
	 * debug method to test without making any change
	 * 
	 * @return true if alpha* lead to an extended state with a test of depth
	 *         highestTest
	 */
	boolean alphaExists() {
		if (cheapestTest == null)
			return false;
		ExtendedState<T> currentState = this;
		while (currentState.shortestAlpha != null) {
			currentState = currentState.shortestAlpha.to;
		}
		return (!currentState.tests.isEmpty())
				&& currentState.tests.cheapestCost() == cheapestTest;
	}

	public void addChild(ExtendedTransition<T> transition) {
		assert transition.from == this;
		children.add(transition);
		sortedChildren.put(transition.input, transition);
	}

	public void addParent(ExtendedTransition<T> transition) {
		assert transition.to == this;
		parents.add(transition);
	}

	public ExtendedTransition<T> getTransition(String input) {
		return sortedChildren.get(input);
	}

	public void clearTests() {
		tests.clear();
	}

	public T pollHighest() {
		return tests.pollCheapest();
	}
}
