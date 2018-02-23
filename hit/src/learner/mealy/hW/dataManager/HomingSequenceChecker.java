package learner.mealy.hW.dataManager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import tools.CompiledSearchGraph;
import tools.loggers.LogManager;

import learner.mealy.LmTrace;
import learner.mealy.hW.dataManager.HomingSequenceChecker.Node.Child;
import main.simpa.Options;
import main.simpa.Options.LogLevel;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

public class HomingSequenceChecker {
	class Node {
		class Child {
			public Node n;
			public String output;

			protected Child(String output, Node childNode) {
				this.output = output;
				this.n = childNode;
			}

			protected Child(String output, Node father,
					CompiledSearchGraph.Node searchStatus) {
				this(output, new Node(father, searchStatus));
			}
		}

		protected Map<String, Child> children;
		protected OutputSequence afterH;// not null if this state was discovered
										// after homing sequence
		protected Node father;// null if the node was discovered after homing
								// sequence

		protected CompiledSearchGraph.Node searchStatus;// null if we don't need
														// to search from this
														// node

		private Node(CompiledSearchGraph.Node status) {
			this.children = new HashMap<>();
			this.afterH = null;
			this.father = null;
			this.searchStatus = status;
		}

		private Node(CompiledSearchGraph.Node status, OutputSequence afterH) {
			this(status);
			this.afterH = afterH;
		}

		public Node(Node father, CompiledSearchGraph.Node status) {
			this(status);
			this.father = father;
		}

		public String getOutput(String input) {
			Child child = children.get(input);
			if (child == null)
				return null;
			return child.output;
		}

		public Node getChildOrCreate(String input, String output) {
			Child child = children.get(input);
			Node childNode;
			if (child == null) {
				CompiledSearchGraph.Node status = compiledSearchGraph
						.getStatus();
				if (status == null) {
					status = compiledSearchGraph.getStart();
				}
				childNode=new Node(this,status);
				addChild(input, output, childNode);
				
			} else {
				assert child.output.equals(output);
				childNode=child.n;
			}
			return childNode;
		}

		protected void addChild(String input, String output, Node childNode) {
			Child c = new Child(output, childNode);
			children.put(input, c);
			if (searchStatus != null) {
				if (children.keySet().containsAll(
						searchStatus.getNonResetingInput()))
					searchStatus = null;
			}
		}
	}

	protected InputSequence h;
	private Map<OutputSequence, Node> knownResponses;
	private Node currentNode;
	private Node startNode;
	private LmTrace lastApplied;
	private CompiledSearchGraph compiledSearchGraph;

	public HomingSequenceChecker(InputSequence h) {
		this.h = h;
		this.knownResponses = new HashMap<>();
		this.compiledSearchGraph = new CompiledSearchGraph(h);
		
		if (compiledSearchGraph.isAcceptingWord()){
			assert(compiledSearchGraph.getStatus()==compiledSearchGraph.getStart());
			assert h.getLength()==0;
			this.currentNode = getState(new OutputSequence());
		}else{
			this.currentNode = new Node(compiledSearchGraph.getStatus());
		}
		this.startNode = this.currentNode;
		this.lastApplied = new LmTrace();
	}

	public void applyInput(String input, String output) {
		lastApplied.append(input, output);

		CompiledSearchGraph.Node status = currentNode.searchStatus;
		boolean hApplied = false;
		if (status == null) {
			compiledSearchGraph.resetStatus();
		} 
			hApplied = compiledSearchGraph.apply(input);
			if (compiledSearchGraph.neededTraceLength() == 0)
				lastApplied = new LmTrace();
		

		String knownOutput = currentNode.getOutput(input);
		if (knownOutput != null && !knownOutput.equals(output)) {
			Stack<Node> fromH = new Stack<>();
			Node current = currentNode;
			while (current.afterH == null) {
				fromH.push(current);
				current = current.father;
			}
			LmTrace traceA = new LmTrace(h, current.afterH);
			while (!fromH.isEmpty()) {
				Node parent = current;
				current = fromH.pop();
				for (java.util.Map.Entry<String, Child> e : parent.children
						.entrySet()) {
					if (e.getValue().n == current) {
						traceA.append(e.getKey(), e.getValue().output);
						break;
					}
				}
			}
			LmTrace traceB = traceA.clone();
			traceB.append(input, knownOutput);
			traceA.append(input, output);
			currentNode=currentNode.children.get(input).n;
			if (Options.LOG_LEVEL == LogLevel.ALL){
				LogManager.logInfo("Inconsistency found in homing sequence");
				exportToDot();
				}
			throw new InvalidHException(traceA, traceB, h);
		}
		if (hApplied&&knownOutput==null) {
			LmTrace hTrace = (lastApplied.subtrace(
					lastApplied.size() - h.getLength(), lastApplied.size()));
			currentNode.addChild(input, output,
					getState(hTrace.getOutputsProjection()));
		}
		currentNode = currentNode.getChildOrCreate(input, output);

	}

	protected Node getState(OutputSequence seq) {
		Node r = knownResponses.get(seq);
		if (r == null) {
			r = new Node(compiledSearchGraph.getStatus(), seq);
			knownResponses.put(seq, r);
		}
		return r;
	}

	private int nodeDotNb;

	private String getOrCreateNodeName(Node n, Map<Node, String> checkerNames) {
		if (!checkerNames.containsKey(n))
			checkerNames.put(n, "checker_" + nodeDotNb++);
		return checkerNames.get(n);
	}

	public void exportToDot() {
		StringBuilder s = new StringBuilder();
		Map<CompiledSearchGraph.Node, String> searchNames = new HashMap<>();
		compiledSearchGraph.addToDot(s, searchNames);

		Map<Node, String> checkerNames = new HashMap<>();

		Set<Node> seen = new HashSet<>();
		Queue<Node> toWrite = new ArrayDeque<>();
		toWrite.add(startNode);
		seen.add(startNode);
		nodeDotNb = 1;

		while (!toWrite.isEmpty()) {
			Node current = toWrite.poll();
			String name = getOrCreateNodeName(current, checkerNames);
			assert (name != null);
			s.append("\t"
					+ name
					+ " [label=\""
					+ ((current.afterH != null) ? (h + "/" + current.afterH)
							: "") + "\"];\n");

			if (current.searchStatus != null) {
				s.append("\t" + name + " -> "
						+ searchNames.get(current.searchStatus)
						+ "[style=dashed,color=green]\n");
			}
			for (Entry<String, Child> entry : current.children.entrySet()) {
				String input = entry.getKey();
				Child child = entry.getValue();
				Node childNode = child.n;
				String childName = getOrCreateNodeName(childNode, checkerNames);

				if (!seen.contains(childNode)) {
					toWrite.add(childNode);
					seen.add(childNode);
				}

				s.append("\t" + name + " -> " + childName + " [label=\""
						+ input + "/" + child.output + "\"]" + "\n");
			}

		}
		s.append("\t_current_ [shape=none,fontcolor=red,label=\"current\"]\n");
		s.append("\t_current_ -> " + getOrCreateNodeName(currentNode, checkerNames) + " [label=\"\",color=red]" + "\n");
		LogManager.logDot(s.toString(), "homingChecker");
	}
}