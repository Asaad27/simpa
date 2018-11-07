package automata.mealy.splittingTree.smetsersSplittingTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import automata.State;
import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;

/**
 * This class is an implementation of the algorithm described in 'Minimal
 * Separating Sequences for All Pairs of States - Rick Smetsers, Joshua Moerman
 * and David N. Jansen. LATA 2016'.
 * 
 * @TODO The current implementation do not use the optimization given at the end
 *       of article, it needs to be added.
 * 
 * @author Nicolas BREMOND
 *
 */
public class SplittingTree {
	final SplittingTreeNode root;
	LinkedList<SplittingTreeNode> leaves = new LinkedList<>();
	/**
	 * An array of states, ordered by the node containing them.
	 */
	ArrayList<State> states;

	Map<State, Integer> statesPos = new HashMap<>();
	final Collection<String> inputSymbols;
	final Mealy automaton;

	/**
	 * compute a splitting tree for the given conjecture.
	 * 
	 * @param conjecture
	 *            the conjecture on which a splitting tree must be computed.
	 */
	public SplittingTree(LmConjecture conjecture) {
		this(conjecture, conjecture.getInputSymbols());
	}

	/**
	 * Compute a splitting tree for the given automaton which have the given
	 * input symbols.
	 * 
	 * @param automaton
	 *            the automaton on which splitting tree must be computed.
	 * @param inputSymbols
	 *            the inputs symbols of the automaton.
	 */
	public SplittingTree(Mealy automaton, Collection<String> inputSymbols) {
		this.automaton = automaton;
		states = new ArrayList<>(automaton.getStates());
		updateStatesPos();
		root = new SplittingTreeNode(this);
		this.inputSymbols = Collections.unmodifiableCollection(inputSymbols);

		computeTree();
	}

	/**
	 * compute the splitting tree from the automaton stored.
	 */
	private void computeTree() {
		makeAcceptable();
		for (int k = 1; k < states.size(); k++) {
			makeKPlusOneStable(k);
		}
	}

	/**
	 * make the tree «acceptable» according to article definition : for each
	 * input, each pair of state in each leaf produce the same output.
	 */
	private void makeAcceptable() {
		LinkedList<SplittingTreeNode> nodesToTest = new LinkedList<>();
		nodesToTest.add(root);
		while (!nodesToTest.isEmpty()) {
			SplittingTreeNode node = nodesToTest.poll();
			for (String input : inputSymbols) {
				Map<String, Set<State>> statesByOutput = new HashMap<>();
				for (State s : node.getStates()) {
					MealyTransition t = automaton.getTransitionFromWithInput(s,
							input);
					assert t != null : "automaton is not complete";
					String output = t.getOutput();
					Set<State> thisOutputStates = statesByOutput.get(output);
					if (thisOutputStates == null) {
						thisOutputStates = new HashSet<>();
						statesByOutput.put(output, thisOutputStates);
					}
					thisOutputStates.add(s);
				}
				if (statesByOutput.size() > 1) {
					nodesToTest
							.addAll(node.split(input, statesByOutput.values()));
					leaves.remove(node);
					break;
				}
			}
		}
	}

	/**
	 * get the set of states reached from a given collection of states after a
	 * given input sequence.
	 * 
	 * @param initials
	 *            the states on which the input sequence is applied.
	 * @param seq
	 *            the sequence applied on initials states
	 * @return the states reached after the sequence.
	 */
	Set<State> multipleApply(Collection<State> initials, InputSequence seq) {
		Set<State> r = new HashSet<>();
		for (State s : initials) {
			r.add(automaton.applyGetState(seq, s));
		}
		return r;
	}

	private void makeKPlusOneStable(int k) {
		// TODO the article describe a faster way to compute stability, it needs
		// to be implemented.
		LinkedList<SplittingTreeNode> toProcess = new LinkedList<>(leaves);
		while (!toProcess.isEmpty()) {
			SplittingTreeNode leaf = toProcess.poll();
			assert leaf.isLeaf();
			for (String input : inputSymbols) {
				Map<State, State> reachedStates = new HashMap<>();
				for (State s : leaf.getStates()) {
					MealyTransition t = automaton.getTransitionFromWithInput(s,
							input);
					assert t != null : "automaton is not complete";
					reachedStates.put(s, t.getTo());
				}
				SplittingTreeNode v = lca(multipleApply(leaf.getStates(),
						new InputSequence(input)));
				if (!v.isLeaf() && v.distinguishingInputSeq.getLength() == k) {
					InputSequence seq = new InputSequence(input);
					seq.addInputSequence(v.distinguishingInputSeq);
					toProcess.addAll(leaf.split(seq, automaton));
					leaves.remove(leaf);
					break;

				}
				if (!leaf.isLeaf())
					break;
			}

		}

	}

	public int getPositionOfState(State s) {
		assert states.get(statesPos.get(s)) == s;
		return statesPos.get(s);
	}

	/**
	 * compute the Lowest Common Ancestor of several states
	 * 
	 * @param values
	 *            a collection of states
	 * @return the lowest node in tree containing all the given states.
	 */
	private SplittingTreeNode lca(Collection<State> values) {
		assert values.size() > 0;
		int minPos = states.size();
		int maxPos = 0;
		for (State s : values) {
			int pos = getPositionOfState(s);
			if (pos < minPos) {
				minPos = pos;
			}
			if (pos > maxPos)
				maxPos = pos;
		}
		// search a leaf containing at least one state
		SplittingTreeNode n = null;
		for (SplittingTreeNode leaf : leaves) {
			if (leaf.startState <= minPos && leaf.endState > minPos) {
				n = leaf;
				break;
			}
		}
		assert n != null;
		while (minPos < n.startState || maxPos >= n.endState) {
			n = n.parent;
			assert n != null;
		}
		return n;
	}

	void leafCreated(SplittingTreeNode n) {
		leaves.add(n);
	}

	/**
	 * move states in the partition.
	 * 
	 * @param childStates
	 *            the states to move.
	 * @param childStart
	 *            the first position to put states
	 * @param parentEnd
	 *            all states are supposed to be between {@code childStart} end
	 *            {@code parentEnd}
	 */
	void move(Set<State> childStates, int childStart, int parentEnd) {
		final int childEnd = childStart + childStates.size();
		int emplacePos = childStart;
		for (int removePos = childEnd; removePos < parentEnd; removePos++) {
			State s = states.get(removePos);
			if (childStates.contains(s)) {
				while (childStates.contains(states.get(emplacePos)))
					emplacePos++;
				assert emplacePos < childEnd;
				swapStates(removePos, emplacePos);
			}
		}
	}

	/**
	 * swap to states in the partition
	 * 
	 * @param pos1
	 *            position of first state
	 * @param pos2
	 *            position of second state
	 */
	private void swapStates(int pos1, int pos2) {
		if (pos1 == pos2)
			return;
		State s = states.get(pos1);
		states.set(pos1, states.get(pos2));
		states.set(pos2, s);
		statesPos.put(states.get(pos1), pos1);
		statesPos.put(s, pos2);
	}

	/**
	 * update the {@link #statesPos} mapping.
	 */
	private void updateStatesPos() {
		for (int i = 0; i < states.size(); i++)
			statesPos.put(states.get(i), i);
	}

	/**
	 * Create an adaptive distinction set from the computed splitting tree
	 * 
	 * @return a distinction set for the given automaton.
	 */
	public TotallyAdaptiveW computeW() {
		TotallyAdaptiveW root = new TotallyAdaptiveW();
		class PartialDistinction {
			public PartialDistinction(TotallyAdaptiveW wPos,
					AdaptiveSymbolSequence pos,
					Map<State, State> reachedStates) {
				super();
				this.wPos = wPos;
				this.seqPos = pos;
				this.reachedStates = reachedStates;
			}

			public PartialDistinction(TotallyAdaptiveW root,
					Collection<State> initialStates) {
				super();
				this.wPos = root;
				this.reachedStates = new HashMap<>();
				for (State s : initialStates) {
					reachedStates.put(s, s);
				}
			}

			public boolean checkReached(Mealy automaton) {
				for (Entry<State, State> entry : reachedStates.entrySet()) {
					if (automaton.applyGetState(getSeqPos().getFullSequence(),
							entry.getKey()) != entry.getValue())
						return false;
					// TODO check wPos
					if (automaton.apply(seqPos.getFullSequence(),
							entry.getKey()) != seqPos)
						return false;
				}
				return true;
			}

			AdaptiveSymbolSequence seqPos = null;

			private AdaptiveSymbolSequence getSeqPos() {
				if (seqPos == null) {
					seqPos = wPos.createNewSymbolSequence();
				}
				return seqPos;
			}

			TotallyAdaptiveW wPos;
			Map<State, State> reachedStates;
		}
		LinkedList<PartialDistinction> toRefine = new LinkedList<>();
		toRefine.add(new PartialDistinction(root, automaton.getStates()));
		while (!toRefine.isEmpty()) {
			PartialDistinction currentBuildingNode = toRefine.poll();
			assert currentBuildingNode.checkReached(automaton);
			if (currentBuildingNode.reachedStates.size() == 1) {
				currentBuildingNode.wPos
						.recordEndOfSequence(currentBuildingNode.getSeqPos());
				continue;
			}
			if (new HashSet<>(currentBuildingNode.reachedStates.values())
					.size() == 1) {
				// we need to apply a new sequence from initial state
				currentBuildingNode = new PartialDistinction(
						currentBuildingNode.wPos.recordEndOfSequence(
								currentBuildingNode.getSeqPos()),
						currentBuildingNode.reachedStates.keySet());

			}
			SplittingTreeNode lowestNode = lca(
					currentBuildingNode.reachedStates.values());
			assert lowestNode.distinguishingInputSeq != null;
			InputSequence inputs = lowestNode.distinguishingInputSeq;

			for (SplittingTreeNode child : lowestNode.getChildren()) {
				Map<State, State> newReachedStates = new HashMap<>();
				for (Entry<State, State> entry : currentBuildingNode.reachedStates
						.entrySet()) {
					if (child.getStates().contains(entry.getValue())) {

						newReachedStates.put(entry.getKey(), automaton
								.applyGetState(inputs, entry.getValue()));
					}
				}
				if (!newReachedStates.isEmpty()) {
					AdaptiveSymbolSequence newPos = currentBuildingNode
							.getSeqPos()
							.extend(new LmTrace(inputs, automaton.apply(inputs,
									child.getStates().iterator().next())));
					PartialDistinction newDistinction = new PartialDistinction(
							currentBuildingNode.wPos, newPos, newReachedStates);
					toRefine.add(newDistinction);
					assert newDistinction.checkReached(automaton);
				}
			}
		}
		assert automaton.acceptCharacterizationSet(root);
		return root;
	}
}
