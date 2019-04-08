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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import automata.State;
import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import automata.mealy.splittingTree.LY_SplittingTree.InputA;
import automata.mealy.splittingTree.LY_SplittingTree.InputB;
import automata.mealy.splittingTree.LY_SplittingTree.InputType;
import automata.mealy.splittingTree.LY_SplittingTree.InputTypeExtended;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

/**
 * This class is used to compute a {@link LY_SplittingTree} from an automata. It
 * splits the algorithm in smaller parts while keeping global values available.
 * 
 * @see LY_SplittingTree
 *
 * @author Nicolas BREMOND
 */
public class LY_SplittingTreeCalculator {

	/**
	 * A path from a leaf with an input of type c) to an other leaf. It is used
	 * to represent the implication graph.
	 * 
	 * @author Nicolas BREMOND
	 */
	static class Path {
		Path(LY_SplittingTree origin, LmTrace seq, LY_SplittingTree target) {
			this.origin = origin;
			this.target = target;
			this.seq = seq;
		}

		Path(LY_SplittingTree origin, String input, String output,
				LY_SplittingTree target) {
			this(origin, new LmTrace(input, output), target);
		}

		LY_SplittingTree origin;
		LY_SplittingTree target;
		LmTrace seq;
	}

	/**
	 * The root of splitting tree.
	 */
	LY_SplittingTree tree = null;

	/**
	 * The leaves at start of the while loop.
	 */
	List<LY_SplittingTree> leaves = new ArrayList<>();
	/**
	 * The leaves created during one execution of the while loop.
	 */
	List<LY_SplittingTree> newLeaves = new ArrayList<>();
	/**
	 * The leaves of biggest cardinality at start of the while loop.
	 */
	List<LY_SplittingTree> biggestLeaves = new ArrayList<>();
	/**
	 * The nodes refined since start of one execution of while loop (contains
	 * node with an input a) and node with an input b) before searching path in
	 * implication graph).
	 */
	List<LY_SplittingTree> refined = new ArrayList<>();
	/**
	 * A mapping to search quickly which leaf contain a given state.
	 */
	HashMap<State, LY_SplittingTree> statesToLeaf = new HashMap<>();

	/**
	 * The automaton on which the splitting tree should be computed.
	 */
	private final Mealy automaton;
	/**
	 * The inputs symbols of the {@link #automaton}.
	 */
	private final Collection<String> inputs;

	/**
	 * Indicate if the computation of splitting tree should be verbose or not.
	 */
	private boolean verbose;

	/**
	 * Compute a splitting tree. The computed splitting tree can be retrieved
	 * with {@link #getSplittingTree()}
	 * 
	 * @param automaton
	 *            the automaton on which splitting tree must be computed
	 * @param inputs
	 *            the inputs used to compute splitting tree
	 * @param verbose
	 *            Override the default verbosity.
	 * @see #LY_SplittingTreeCalculator(Mealy, Collection)
	 * 
	 */
	public LY_SplittingTreeCalculator(Mealy automaton,
			Collection<String> inputs, boolean verbose) {
		this.automaton = automaton;
		this.inputs = inputs;
		this.verbose = verbose;

		tree = new LY_SplittingTree(new HashSet<>(automaton.getStates()));

		biggestLeaves.add(tree);
		leaves.add(tree);
		for (State s : tree.distinguishedInitialStates)
			statesToLeaf.put(s, tree);
		computeTree();
	}

	/**
	 * Compute a splitting tree with default verbosity.
	 * 
	 * @see #LY_SplittingTreeCalculator(Mealy, Collection, boolean) for more
	 *      informations
	 */
	public LY_SplittingTreeCalculator(Mealy automaton,
			Collection<String> inputs) {
		this(automaton, inputs, Options.getLogLevel() == LogLevel.ALL);
	}

	/**
	 * Return the computed splitting tree.
	 * 
	 * @return the computed splitting tree.
	 */
	public LY_SplittingTree getSplittingTree() {
		return tree;
	}

	/**
	 * The main while loop of the algorithm.
	 */
	private void computeTree() {
		while (leaves.size() != automaton.getStateCount()) {
			assert leavesArePartition();
			biggestLeaves = getBiggestLeaves();
			computeInputs();
			for (LY_SplittingTree leaf : biggestLeaves) {
				if (leaf.bestInput.isValid()
						&& leaf.bestInput.type == InputType.B)
					processInputB(leaf);
			}
			computeCLeaves();
			leaves.removeAll(refined);
			leaves.addAll(newLeaves);
			refined.clear();
			newLeaves.clear();
			assert leavesArePartition();
			if (verbose) {
				HashSet<LY_SplittingTree> otherLeaves = new HashSet<>(leaves);
				otherLeaves.removeAll(biggestLeaves);
				LogManager.logInfo("Next leaves to refine are ", biggestLeaves,
						". Other leaves are " + otherLeaves);
			}
		}
		assert leavesArePartition();
	}

	private List<LY_SplittingTree> getBiggestLeaves() {
		List<LY_SplittingTree> biggestLeaves = new ArrayList<>();
		int biggestLeavesSize = 0;
		for (LY_SplittingTree leaf : leaves) {
			int leafSize = leaf.distinguishedInitialStates.size();
			if (leafSize > biggestLeavesSize) {
				biggestLeaves.clear();
				biggestLeavesSize = leafSize;
			}
			if (leafSize == biggestLeavesSize) {
				biggestLeaves.add(leaf);
			}
		}
		return biggestLeaves;
	}

	/**
	 * Check if leaves are actually a partition of the states of automata. This
	 * method is made for assertions.
	 * 
	 * @return false if one state is not present in any leaf of the tree or if a
	 *         state exists in two leaves.
	 */
	public boolean leavesArePartition() {
		for (State s : automaton.getStates()) {
			boolean found = false;
			for (LY_SplittingTree leaf : leaves) {
				boolean contain = leaf.distinguishedInitialStates.contains(s);
				if (found && contain)
					return false;// state exists in two leaves
				if (contain) {
					found = true;
					assert statesToLeaf.get(s) == leaf;
				}
			}
			if (!found)
				return false;// no leave contain state
		}
		return true;
	}

	/**
	 * Pre-compute input type for each input and each biggest leaf and process
	 * the one of type a).
	 */
	private void computeInputs() {
		for (LY_SplittingTree leaf : biggestLeaves) {
			for (String input : inputs) {
				InputTypeExtended r = leaf.inputIsValid(input, automaton,
						leaves);
				r.setInput(input);
				if (!r.isBetterThan(leaf.bestInput))
					continue;
				leaf.bestInput = r;
				if (r.isValid() && r.type == InputType.A) {
					processInputA(leaf);
					break;
				}
			}
			if (!leaf.bestInput.isValid()) {
				// TODO new Sequence is needed…
				throw new RuntimeException("cannot compute splitting tree");
			}
		}
	}

	/**
	 * Search path in the graph to process the input of type c).
	 */
	private void computeCLeaves() {
		LinkedList<Path> paths = new LinkedList<>();
		for (LY_SplittingTree leaf : refined) {
			paths.addAll(leaf.reachedByLeaves);
			assert leaf.seenInImplacationGraph == false;
			leaf.seenInImplacationGraph = true;
		}

		while (!paths.isEmpty()) {
			Path path = paths.poll();
			if (path.origin.seenInImplacationGraph)
				continue;
			path.origin.seenInImplacationGraph = true;
			if (path.origin.bestInput.isValid()) {
				assert path.origin.bestInput.type == InputType.C;
				processInputC(path);
			}
			for (Path transition : path.origin.reachedByLeaves) {
				LmTrace seq = new LmTrace();
				seq.append(transition.seq);
				seq.append(path.seq);
				Path newPath = new Path(transition.origin, seq, path.target);
				paths.add(newPath);
			}
		}

		if (refined.size() != biggestLeaves.size()) {
			// TODO new seq …
			throw new RuntimeException("cannot compute splitting tree");
		}
	}

	/**
	 * create children for a leaf with an input of type a)
	 * 
	 * @param refiningLeaf
	 *            the leaf to process.
	 */
	private void processInputA(LY_SplittingTree refiningLeaf) {
		if (verbose)
			LogManager.logInfo("Computing input '",
					refiningLeaf.bestInput.input, "' of type a) for ",
					refiningLeaf);
		assert refiningLeaf.bestInput.type == InputType.A;
		InputA extendedInput = (InputA) refiningLeaf.bestInput;
		assert refiningLeaf.distinguishingInputSeq == null;
		refiningLeaf.distinguishingInputSeq = new InputSequence(
				extendedInput.input);
		Map<String, Set<State>> statesByOutput = new HashMap<>();
		for (State s : refiningLeaf.distinguishedInitialStates) {
			String output = automaton
					.getTransitionFromWithInput(s, extendedInput.input)
					.getOutput();
			Set<State> byOutput = statesByOutput.get(output);
			if (byOutput == null) {
				byOutput = new HashSet<>();
				statesByOutput.put(output, byOutput);
			}
			byOutput.add(s);
		}
		for (Entry<String, Set<State>> entry : statesByOutput.entrySet()) {
			LY_SplittingTree newLeaf = new LY_SplittingTree(entry.getValue(),
					refiningLeaf, new OutputSequence(entry.getKey()));
			addLeaf(newLeaf);
		}
		refiningLeaf.updateReachedStates(automaton);
		refined.add(refiningLeaf);
		assert refiningLeaf.checkReached(automaton);
	}

	/**
	 * create children for a leaf with an input of type b)
	 * 
	 * @param refiningLeaf
	 *            the leaf to process.
	 */
	private void processInputB(LY_SplittingTree refiningLeaf) {
		assert refiningLeaf.bestInput.type == InputType.B;
		if (verbose)
			LogManager.logInfo("Computing input '",
					refiningLeaf.bestInput.input, "' of type b) for ",
					refiningLeaf);
		InputB extendedInput = (InputB) refiningLeaf.bestInput;
		LY_SplittingTree ancestor = LY_SplittingTree
				.findNearestAncestor(extendedInput.reachedLeaves);
		processInputBC(refiningLeaf,
				new LmTrace(extendedInput.input, extendedInput.output),
				ancestor);
	}

	/**
	 * create children for a leaf with an input of type c)
	 * 
	 * @param refiningLeaf
	 *            the leaf to process.
	 */
	private void processInputC(Path path) {
		LY_SplittingTree refiningLeaf = path.origin;
		assert refiningLeaf.bestInput.type == InputType.C;
		if (verbose)
			LogManager.logInfo("Computing input '",
					refiningLeaf.bestInput.input, "' of type c) for ",
					refiningLeaf);
		processInputBC(refiningLeaf, path.seq, path.target);
	}

	/**
	 * common part of refining node with input of type b) or c).
	 * 
	 * @param refiningLeaf
	 *            the leaf to refine
	 * @param sigma
	 *            the path to another node
	 * @param endNode
	 *            the other node
	 */
	private void processInputBC(LY_SplittingTree refiningLeaf, LmTrace sigma,
			LY_SplittingTree endNode) {
		refiningLeaf.distinguishingInputSeq = new InputSequence();
		refiningLeaf.distinguishingInputSeq
				.addInputSequence(sigma.getInputsProjection());
		refiningLeaf.distinguishingInputSeq
				.addInputSequence(endNode.distinguishingInputSeq);
		refiningLeaf.reachedStates = new HashMap<>();
		for (State s : refiningLeaf.distinguishedInitialStates) {
			refiningLeaf.reachedStates.put(s, endNode.reachedStates.get(
					automaton.applyGetState(sigma.getInputsProjection(), s)));
		}
		for (LY_SplittingTree endNodeChild : endNode.getChildren()) {
			Set<State> init = new HashSet<>();
			for (Entry<State, State> entry : refiningLeaf.reachedStates
					.entrySet()) {
				if (endNodeChild.distinguishedInitialStates.contains(
						automaton.applyGetState(sigma.getInputsProjection(),
								entry.getKey()))) {
					init.add(entry.getKey());
				}
			}
			if (!init.isEmpty()) {
				OutputSequence output = new OutputSequence();
				output.addOutputSequence(sigma.getOutputsProjection());
				output.addOutputSequence(endNodeChild.parentOutput);
				LY_SplittingTree child = new LY_SplittingTree(init,
						refiningLeaf, output);
				addLeaf(child);
			}
		}
		refined.add(refiningLeaf);
		assert refiningLeaf.checkReached(automaton);

	}

	/**
	 * Save a leaf for next execution of the while loop. Also update the mapping
	 * {@link #statesToLeaf}
	 * 
	 * @param newLeaf
	 *            the leaf to save.
	 */
	private void addLeaf(LY_SplittingTree newLeaf) {
		assert newLeaf.checkStates(automaton);
		newLeaves.add(newLeaf);
		for (State s : newLeaf.distinguishedInitialStates)
			statesToLeaf.put(s, newLeaf);
	}

	/**
	 * Create a distinction sequence from the computed splitting tree
	 * 
	 * @return a distinction sequence for the given automaton.
	 */
	public AdaptiveSymbolSequence computeDistinctionSequence() {
		AdaptiveSymbolSequence root = new AdaptiveSymbolSequence();
		class PartialDistinction {
			public PartialDistinction(AdaptiveSymbolSequence pos,
					Map<State, State> reachedStates) {
				super();
				this.pos = pos;
				this.reachedStates = reachedStates;
			}

			public PartialDistinction(AdaptiveSymbolSequence root,
					List<State> initialStates) {
				super();
				this.pos = root;
				this.reachedStates = new HashMap<>();
				for (State s : initialStates) {
					reachedStates.put(s, s);
				}
			}

			public boolean checkReached(Mealy automaton) {
				for (Entry<State, State> entry : reachedStates.entrySet()) {
					if (automaton.applyGetState(pos.getFullSequence(),
							entry.getKey()) != entry.getValue())
						return false;
					if (automaton.apply(pos.getFullSequence(),
							entry.getKey()) != pos)
						return false;
				}
				return true;
			}

			AdaptiveSymbolSequence pos;
			Map<State, State> reachedStates;
		}
		LinkedList<PartialDistinction> toRefine = new LinkedList<>();
		toRefine.add(new PartialDistinction(root, automaton.getStates()));
		while (!toRefine.isEmpty()) {
			PartialDistinction currentBuildingNode = toRefine.poll();
			assert currentBuildingNode.checkReached(automaton);
			if (currentBuildingNode.reachedStates.size() == 1)
				continue;
			LY_SplittingTree lowestNode = findLowestNodeContaingSet(
					currentBuildingNode.reachedStates.values());
			assert lowestNode.checkReached(automaton);
			InputSequence inputs = lowestNode.distinguishingInputSeq;

			for (LY_SplittingTree child : lowestNode.getChildren()) {
				Map<State, State> newReachedStates = new HashMap<>();
				for (Entry<State, State> entry : currentBuildingNode.reachedStates
						.entrySet()) {
					if (child.distinguishedInitialStates
							.contains(entry.getValue())) {
						newReachedStates.put(entry.getKey(),
								lowestNode.reachedStates.get(entry.getValue()));
					}
				}
				if (!newReachedStates.isEmpty()) {
					AdaptiveSymbolSequence newPos = currentBuildingNode.pos
							.extend(new LmTrace(inputs, child.parentOutput));
					PartialDistinction newDistinction = new PartialDistinction(
							newPos, newReachedStates);
					toRefine.add(newDistinction);
					assert newDistinction.checkReached(automaton);
				}
			}
		}
		return root;
	}

	/**
	 * Get the lowest node in tree containing a set of states.
	 * 
	 * @param states
	 *            the states to search for.
	 * @return a node containing all given states and differentiating them with
	 *         its input.
	 */
	private LY_SplittingTree findLowestNodeContaingSet(
			Collection<State> states) {
		assert states.size() > 1;
		List<LY_SplittingTree> matchedLeaves = new ArrayList<>();
		for (State s : states)
			matchedLeaves.add(statesToLeaf.get(s));
		LY_SplittingTree ancestor = LY_SplittingTree
				.findNearestAncestor(matchedLeaves);
		assert ancestor.distinguishedInitialStates.containsAll(states);
		return ancestor;
	}

}
