package tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.mealy.AdaptiveStructure;

/**
 * a class to reduce the time needed to search an adaptive sequence in a long
 * trace.
 * 
 * @author Nicolas BREMOND
 *
 * @param <InputT>
 * @param <OutputT>
 */
public class AdaptiveSearchGraph<InputT, OutputT> {
	AdaptiveStructure<InputT, OutputT> seq;

	protected class Node {
		final Set<AdaptiveStructure<InputT, OutputT>> currentPositions;
		private Collection<AdaptiveStructure<InputT, OutputT>> leaves = null;
		final int hash;
		private Map<InputT, Map<OutputT, Node>> children = new HashMap<>();

		Node(Set<AdaptiveStructure<InputT, OutputT>> currentPositions) {
			assert currentPositions
					.contains(seq) : "root of tree can always be found";
			this.currentPositions = currentPositions;
			for (AdaptiveStructure<InputT, OutputT> pos : currentPositions) {
				if (pos.isFinal()) {
					// leaves can be added at any moment without notification
					if (!seqPositions.contains(pos)) {
						seqPositions.add(pos);
					}
				}
			}
			this.hash = hash(currentPositions);
		}

		public Collection<AdaptiveStructure<InputT, OutputT>> getLeaves() {
			if (leaves == null) {
				leaves = new ArrayList<>();
				for (AdaptiveStructure<InputT, OutputT> pos : currentPositions) {
					if (pos.isFinal())
						leaves.add(pos);
				}
			}
			return leaves;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof AdaptiveSearchGraph.Node))
				return false;
			@SuppressWarnings("unchecked")
			Node o_ = (Node) o;
			return equals(o_);
		}

		public boolean equals(Node o) {
			return o.currentPositions.equals(currentPositions);
		}

		public Node getChild(InputT in, OutputT out) {
			Map<OutputT, Node> byOutput = children.get(in);
			if (byOutput == null) {
				byOutput = new HashMap<>();
				children.put(in, byOutput);
			}
			Node child = byOutput.get(out);
			if (child == null) {
				child = computeChild(in, out);
				byOutput.put(out, child);
			} else {
				assert child.equals(computeChild(in, out));
			}
			assert nodes.get(child) == child;
			return child;
		}

		private Node computeChild(InputT in, OutputT out) {
			Set<AdaptiveStructure<InputT, OutputT>> childPositions = new HashSet<>();
			childPositions.add(seq);
			for (AdaptiveStructure<InputT, OutputT> pos : currentPositions) {
				if (!pos.isFinal() && in.equals(pos.getInput())) {
					boolean isNew = pos.hasChild(out);
					AdaptiveStructure<InputT, OutputT> posChild = pos
							.getChild(out);
					childPositions.add(posChild);
					if (isNew)
						seqPositions.add(posChild);
				}
			}
			return getNodeOrCreate(childPositions);
		}
	}

	// this list is used to compute hash of Nodes. the position of elements must
	// not change.
	private List<AdaptiveStructure<InputT, OutputT>> seqPositions;
	// storage for all nodes. The mapping should be [Set of adaptive] -> [Node]
	// but we use node as key too to override the hash function.
	private Map<Node, Node> nodes = new HashMap<>();

	private Node currentNode;

	/**
	 * compute a hash of a set of possible position in the searched structure.
	 * 
	 * @param positions
	 *            positions in the adaptiveStructure {@code seq}
	 * @return a hash.
	 */
	protected int hash(Set<AdaptiveStructure<InputT, OutputT>> positions) {
		assert seqPositions.containsAll(positions);
		int i = 1;
		int hash = 0;
		for (AdaptiveStructure<InputT, OutputT> position : seqPositions) {
			if (positions.contains(position)) {
				hash += i;
			}
			i *= 2;
		}
		return hash;
	}

	public AdaptiveSearchGraph(AdaptiveStructure<InputT, OutputT> seq) {
		this.seq = seq;
		assert seq.isRoot();
		seqPositions = new ArrayList<>(seq.getAllNodes());
		Set<AdaptiveStructure<InputT, OutputT>> initialSet = new HashSet<>();
		initialSet.add(seq);
		currentNode = getNodeOrCreate(initialSet);
	}

	/**
	 * indicate that the sequence has been modified.
	 */
	public void updateGraph() {
		assert false : "TODO";
		// TODO
	}

	/**
	 * get a previously created node with the same set or create a new one.
	 * 
	 * @param set
	 *            the positions of the node
	 * @return a new or an existing node with the current positions equals to
	 *         {@code set}
	 */
	private Node getNodeOrCreate(Set<AdaptiveStructure<InputT, OutputT>> set) {
		Node newNode = new Node(set);
		Node existing = nodes.get(newNode);
		if (existing == null) {
			nodes.put(newNode, newNode);
			return newNode;
		} else
			return existing;
	}

	/**
	 * check the sequences found after applying some input/output.
	 * 
	 * @param in
	 *            the input applied
	 * @param out
	 *            the output observed
	 * @return the collection of leaves which are found by the last trace.
	 */
	public Collection<? extends AdaptiveStructure<InputT, OutputT>> apply(
			InputT in, OutputT out) {
		currentNode = currentNode.getChild(in, out);
		return currentNode.getLeaves();
	}

}
