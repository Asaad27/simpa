package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import learner.mealy.LmTrace;
import learner.mealy.hW.dataManager.AdaptiveHomingSequenceChecker.InconsistencyGraph.Inconsistency;
import learner.mealy.hW.dataManager.AdaptiveHomingSequenceChecker.InconsistencyGraph.Node.Child;
import tools.AdaptiveSearchGraph;

public class AdaptiveHomingSequenceChecker
		extends GenericHomingSequenceChecker {
	/**
	 * A graph representing the trace were some point were merged. The merging
	 * is made based on an external information. The aims of this class is to
	 * detect changing behavior (non-Determinism) after merged point.
	 */
	static class InconsistencyGraph {
		static class Inconsistency {
			LmTrace commonTrace = new LmTrace();// trace seen after homing
			String differentiatingInput;
			String firstOutput;
			String secondOutput;
		}

		static class Node {
			static class Child {
				public final Node next;
				public final String output;

				public Child(String output, Node next) {
					this.output = output;
					this.next = next;
				}
			}

			Map<String, Child> children = new HashMap<>();
			final Node father;

			public Node(Node father, String input) {
				this.father = father;
				this.input = input;
			}

			public Node() {
				this.father = null;
				this.input = null;
			}

			final String input; // input LEADING TO this node.

			public boolean hasChild(String input) {
				return children.containsKey(input);
			}

			public Child getChild(String input) {
				return children.get(input);
			}

			public Child addChild(String input, String output, Node next) {
				Child child = new Child(output, next);
				assert !children.containsKey(input);
				children.put(input, child);
				return child;
			}
		}

		Node endOfHNode = new Node();
		Node currentNode = new Node();
		public boolean sequenceApplied;

		/**
		 * 
		 * @param hFound
		 *            indicate if this execution is the last of the homing
		 *            sequence.
		 * @param input
		 * @param output
		 * @return true if an inconsistency was found
		 */
		Inconsistency apply(boolean hFound, String input, String output) {
			if (!currentNode.hasChild(input)) {
				Node next;
				if (hFound)
					next = endOfHNode;
				else
					next = new Node(currentNode, input);
				currentNode.addChild(input, output, next);
				currentNode = next;
				return null;
			} else {
				Node.Child child = currentNode.getChild(input);
				boolean inconsistencyFound = !child.output.equals(output);
				if (inconsistencyFound) {
					Inconsistency inconsistency = new Inconsistency();
					inconsistency.differentiatingInput = input;
					inconsistency.firstOutput = child.output;
					inconsistency.secondOutput = output;
					Node ancestorNode = currentNode;
					Stack<Node> ancestors = new Stack<>();
					while (ancestorNode.input != null) {
						ancestors.push(ancestorNode);
						ancestorNode = ancestorNode.father;
					}
					while (!ancestors.isEmpty()) {
						ancestorNode = ancestors.pop();
						inconsistency.commonTrace.append(ancestorNode.input,
								ancestorNode.father
										.getChild(ancestorNode.input).output);
					}
					currentNode = new Node();
					if (hFound)
						currentNode = endOfHNode;
					return inconsistency;
				} else {
					currentNode = child.next;
					assert !hFound || child.next == endOfHNode;
					return null;
				}
			}
		}

		/**
		 * Create a new {@link InconsistencyGraph} without any previous
		 * knowledge.
		 */
		public InconsistencyGraph() {
		}

		/**
		 * Create a new {@link InconsistencyGraph} based on the (reusable)
		 * knowledge from a graph of a prefix sequence.
		 * 
		 * @param sourceGraph
		 *            the graph from which knowledge will be taken.
		 * @param sourceSeq
		 *            the sequences used to merge states in source graph.
		 * @param thisSeq
		 *            the sequence which will be used to merge states in this
		 *            graph.
		 */
		public InconsistencyGraph(InconsistencyGraph sourceGraph,
				AdaptiveSymbolSequence sourceSeq,
				AdaptiveSymbolSequence thisSeq) {
			assert thisSeq.isAnswerTo(sourceSeq) && thisSeq != sourceSeq;
			Stack<String> outputs = new Stack<>();
			AdaptiveSymbolSequence currentSeq = thisSeq;
			while (currentSeq != sourceSeq) {
				outputs.push(currentSeq.getFromOutput());
				currentSeq = currentSeq.getFather();
			}
			Node endOfSourceH = sourceGraph.endOfHNode;
			Node currentNodeBetweenH = endOfSourceH;
			while (currentSeq != thisSeq) {
				Child currentChild = currentNodeBetweenH
						.getChild(currentSeq.getInput());
				currentNodeBetweenH = null;
				if (currentChild == null) {
					break;
				}
				String output = outputs.pop();
				if (currentChild.output == output)
					currentNodeBetweenH = currentChild.next;
				else
					break;
				currentSeq = currentSeq.getChild(output);
			}
			if (currentNodeBetweenH != null) {
				assert currentSeq == thisSeq;
				endOfHNode = copyNode(currentNodeBetweenH, endOfSourceH);
			}

		}

		/**
		 * Copy a node of a graph. This method is designed to copy a sub tree of
		 * the graph (i.e. there shouldn't be a loop in the part to copy). The
		 * {@code endOfCopyNode} indicate where to stop the copy in order to
		 * avoid loop.
		 * 
		 * @param sourceNode
		 *            the node to copy.
		 * @param endOfCopyNode
		 *            a node which shouldn't be copied.
		 * @return a new {@link Node} with the same descendants as
		 *         {@code source} without {@code endOfCopyNode}.
		 */
		private Node copyNode(Node sourceNode, Node endOfCopyNode) {
			Node result = new Node();
			class CopyJob {
				Node source;
				Node dest;

				public CopyJob(Node source, Node dest) {
					this.source = source;
					this.dest = dest;
				}
			}
			LinkedList<CopyJob> toCopy = new LinkedList<>();
			if (sourceNode != endOfCopyNode)
				toCopy.push(new CopyJob(sourceNode, result));
			while (!toCopy.isEmpty()) {
				CopyJob job = toCopy.pop();
				for (Entry<String, Child> entry : job.source.children
						.entrySet()) {
					if (entry.getValue().next != endOfCopyNode) {
						assert entry
								.getValue().next != sourceNode : "there is a loop in the node to copy";
						Node newDest = new Node(job.dest, entry.getKey());
						toCopy.push(
								new CopyJob(entry.getValue().next, newDest));
						job.dest.addChild(entry.getKey(),
								entry.getValue().output, newDest);
					}
				}
			}
			return result;

		}
	}

	static class HInconsistency {
		public final AdaptiveSymbolSequence hResponse;
		public final LmTrace firstTrace;
		public final LmTrace secondTrace;

		public HInconsistency(AdaptiveSymbolSequence hResponse,
				LmTrace firstTrace, LmTrace secondTrace) {
			assert hResponse.isFinal();
			assert firstTrace.getInputsProjection()
					.equals(secondTrace.getInputsProjection());
			this.hResponse = hResponse;
			this.firstTrace = firstTrace;
			this.secondTrace = secondTrace;
		}

		public HInconsistency(AdaptiveSymbolSequence hResponse,
				InconsistencyGraph.Inconsistency inconsistency) {
			assert hResponse.isFinal();
			this.hResponse = hResponse;
			this.firstTrace = inconsistency.commonTrace.clone();
			this.secondTrace = inconsistency.commonTrace.clone();
			firstTrace.append(inconsistency.differentiatingInput,
					inconsistency.firstOutput);
			secondTrace.append(inconsistency.differentiatingInput,
					inconsistency.secondOutput);
		}

		@Override
		public String toString() {
			return "sequence «"
					+ hResponse.getFullSequence().buildTrace(hResponse)
					+ "» is not homing : traces seen after this homing are «"
					+ firstTrace + "» and «" + secondTrace + "»";
		}

	}

	static public class AdaptiveHNDException extends GenericHNDException {
		private static final long serialVersionUID = -2271250172524713361L;
		AdaptiveSymbolSequence h;
		List<HInconsistency> inconsistencies;

		public AdaptiveHNDException(AdaptiveSymbolSequence h,
				List<HInconsistency> inconsistencies) {
			this.h = h;
			this.inconsistencies = inconsistencies;
			assert !inconsistencies.isEmpty();
		}

		@Override
		public String toString() {
			return "" + inconsistencies.size()
					+ " inconsistencies were found for the adaptive homing sequence."
					+ (inconsistencies.size() <= 5 ? inconsistencies
							: inconsistencies.subList(0, 5) + "...");

		}

		@Override
		public AdaptiveSymbolSequence getNewH() {
			updateH();
			return h;
		}

		public void updateH() {
			for (HInconsistency inc : inconsistencies) {
				inc.hResponse.extend(inc.firstTrace);
				inc.hResponse.extend(inc.secondTrace);
			}
		}

	}

	final AdaptiveSymbolSequence h;
	AdaptiveSearchGraph<String, String> hFinder;
	Map<AdaptiveSymbolSequence, InconsistencyGraph> graphs = new HashMap<>();

	public AdaptiveHomingSequenceChecker(AdaptiveSymbolSequence h) {
		this.h = h;
		this.hFinder = new AdaptiveSearchGraph<>(h);
	}

	public void apply(String input, String output) {
		for (InconsistencyGraph graph : graphs.values()) {
			graph.sequenceApplied = false;
		}
		@SuppressWarnings("unchecked")
		Collection<AdaptiveSymbolSequence> leavesSeen = (Collection<AdaptiveSymbolSequence>) hFinder
				.apply(input, output);
		List<HInconsistency> inconsistencies = new ArrayList<>();
		for (AdaptiveSymbolSequence leaf : leavesSeen) {
			assert leaf.isFinal();
			InconsistencyGraph graph = getGraphOrCreate(leaf);
			Inconsistency inc = graph.apply(true, input, output);
			if (inc != null)
				inconsistencies.add(new HInconsistency(leaf, inc));
		}
		for (Entry<AdaptiveSymbolSequence, InconsistencyGraph> entry : graphs
				.entrySet()) {
			InconsistencyGraph graph = entry.getValue();
			if (graph.sequenceApplied)
				continue;
			Inconsistency inc = graph.apply(false, input, output);
			if (inc != null)
				inconsistencies.add(new HInconsistency(entry.getKey(), inc));
		}
		if (!inconsistencies.isEmpty())
			throw new AdaptiveHNDException(h, inconsistencies);
	}

	private InconsistencyGraph getGraphOrCreate(AdaptiveSymbolSequence leaf) {
		assert leaf.isFinal();
		InconsistencyGraph graph = graphs.get(leaf);
		if (graph == null) {
			graph = createGraph(leaf);
			graphs.put(leaf, graph);
		}
		return graph;
	}

	private InconsistencyGraph createGraph(AdaptiveSymbolSequence leaf) {
		assert leaf.isFinal();
		AdaptiveSymbolSequence ancestor = leaf;
		while (ancestor != null) {
			InconsistencyGraph ancestorGraph = graphs.get(ancestor);
			if (ancestorGraph != null) {
				return new InconsistencyGraph(ancestorGraph, ancestor, leaf);
			}
			ancestor = ancestor.getFather();
		}
		return new InconsistencyGraph();
	}

	@Override
	public GenericInputSequence getH() {
		return h;
	}
}
