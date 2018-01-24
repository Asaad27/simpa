package learner.mealy.localizerBased.dataManager.vTree;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.localizerBased.dataManager.DataManager;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public abstract class AbstractNode {
	/**
	 * A class to describe a pair of nodes. There is no order for the two nodes
	 */
	class Pair {
		private AbstractNode n1;
		private AbstractNode n2;

		Pair(AbstractNode n1, AbstractNode n2) {
			this.n1 = n1;
			this.n2 = n2;
		}

		public int hashCode() {
			return n1.hashCode() * n2.hashCode();
		}

		public boolean equals(Pair o) {
			return (n1 == o.n1 && n2 == o.n2) || (n1 == o.n2 && n2 == o.n1);
		}

		public boolean equals(Object o) {
			if (o instanceof Pair)
				return equals((Pair) o);
			else
				return false;
		}

		public String toString() {
			return "(" + n1 + "," + n2 + ")";
		}
	}

	private static int nodesNumber = 0;
	private int nodeId = nodesNumber++;
	// children and outputs are supposed to have the same keys
	private HashMap<String, AbstractNode> children;
	private HashMap<String, String> outputs;

	private Set<AbstractNode> incompatibleNodes = new HashSet<>();

	/**
	 * When a node is marked merged, it is not supposed to be modified because
	 * that may cause concurrent modification. You probably should apply changes
	 * to {@code mergedWith} node instead
	 */
	private AbstractNode mergedWith = null;

	/**
	 * A description for dot exports. Must finish with a newline char.
	 */
	public String desc = "";

	/**
	 * Keep reference on all used nodes, even if they not reachable. That is
	 * used for dot exports.
	 */
	public static List<AbstractNode> instances = new ArrayList<>();

	/**
	 * Reference to nodes in the tree sorted by InputSmybol of the known
	 * transitions and then corresponding output.
	 * <p>
	 * A given node will appears several times if it has several known
	 * transitions.
	 */
	protected static Map<String, Map<String, List<AbstractNode>>> nodesByResponses = new HashMap<>();

	protected AbstractNode() {
		children = new HashMap<>();
		outputs = new HashMap<>();
		if (Options.LOG_LEVEL == LogLevel.ALL)
			instances.add(this);
	}

	/**
	 * Get the inputs for which this node know the output and have a child.
	 */
	public Collection<String> getKnownInputs() {
		return outputs.keySet();
	}

	/**
	 * Get the answer from a given input
	 * 
	 * @return the response to the given input or {@code null} if the answer is
	 *         unknown
	 */
	public String getOutput(String input) {
		assert(outputs.containsKey(input) && children.containsKey(input)) || (!outputs.containsKey(input)
				&& !children.containsKey(input)) : "output and children are not coherent";
		return outputs.get(input);
	}

	/**
	 * create the output sequence we are supposed to have from the current node
	 * 
	 * @return the longest known output sequence from this state which is an
	 *         answer to the start of inputs
	 * @see #getOutput(String)
	 */
	public OutputSequence getOutput(InputSequence inputs) {
		OutputSequence outputs = new OutputSequence();
		makeOutput(inputs, outputs);
		return outputs;
	}

	/**
	 * complete {@code outputs} in order to respect
	 * {@link #getOutput(InputSequence)}
	 * 
	 * @param inputs
	 *            the complete InputSequence
	 * @param outputs
	 *            the start of the OutputSequence
	 */
	private void makeOutput(InputSequence inputs, OutputSequence outputs) {
		if (inputs.getLength() == outputs.getLength())
			return;
		String i = inputs.sequence.get(outputs.getLength());
		String o = getOutput(i);
		if (o == null)
			return;
		outputs.addOutput(o);
		getChild(i).makeOutput(inputs, outputs);
	}

	/**
	 * Get a precise child of the node
	 * 
	 * @param input
	 *            the input for the wanted child
	 * @return {@code null} if the node has no child for the given input
	 */
	public AbstractNode getChild(String input) {
		assert(outputs.containsKey(input) && children.containsKey(input)) || (!outputs.containsKey(input)
				&& !children.containsKey(input)) : "output and children are not coherent";
		return children.get(input);
	}

	/**
	 * Like {@link #getChild(String)} but if the child does not already exists,
	 * it will be created and added for transition {@code input}/{@code output}
	 * 
	 * @param input
	 *            the input symbol for the transition of the wanted child
	 * @param output
	 *            the output symbol to create the transition if it does not
	 *            exists
	 * @return never {@code null}
	 */
	public AbstractNode getChildOrCreate(String input, String output) {
		AbstractNode child;
		if ((child = getChild(input)) != null) {
			assert getOutput(input).equals(output) : "The node " + this + " already have output '" + getOutput(input)
					+ "' for input '" + input + "'";
			return child;
		}
		child = new AnonymousNode(this);
		outputs.put(input, output);
		children.put(input, child);

		// now compute incompatibilities and update nodesByResponses
		Map<String, List<AbstractNode>> byOutput = nodesByResponses.get(input);
		if (byOutput == null) {
			byOutput = new HashMap<>();
			nodesByResponses.put(input, byOutput);
		}
		for (String o : byOutput.keySet())
			if (!o.equals(output))
				for (AbstractNode incomptible : byOutput.get(o))
					setIncompatible(incomptible);
		byOutput.putIfAbsent(output, new ArrayList<AbstractNode>());
		byOutput.get(output).add(this);
		return child;
	}

	/**
	 * Get the reachable nodes which have a transition to this node.
	 * <p>
	 * TODO it may be useful to get parents with the inputs symbols which leads
	 * to this node
	 */
	public abstract List<AbstractNode> getParents();

	public abstract boolean isStateNode();

	public boolean isEndOfTrace() {
		return DataManager.instance.getCurrentVNode() == this;
	}

	/**
	 * Declare this node and {@code o} incompatible. That means there is an
	 * input sequence for which the two node can compute their corresponding
	 * output sequence but the two outputs are different.
	 * <p>
	 * This call is reflexive, you don't need to call
	 * {@code o.setIncompatible(this)}. Moreover it will update the parents
	 * nodes in order to avoid global checking on incompatibilities.
	 */
	private void setIncompatible(AbstractNode o) {
		assert o != this;
		if (mergedWith != null) {
			mergedWith.setIncompatible(o);
			return;
		}
		if (o.mergedWith != null) {
			setIncompatible(o.mergedWith);
			return;
		}
		if (!incompatibleNodes.add(o))
			return;
		if (o.isStateNode() && Options.LOG_LEVEL != LogLevel.LOW)
			LogManager.logInfo(this + "is incompatible with " + o);
		for (AbstractNode p : getParents())
			for (AbstractNode oP : o.getParents())
				for (String i : p.getKnownInputs())// we don't know which
													// transition(s) lead in
													// this state so we have to
													// try all transitions
					if (oP.getChild(i) == o && p.getChild(i) == this)
						p.setIncompatible(oP);
		o.setIncompatible(this);
	}

	protected void changeChild(String input, StateNode newNode) {
		if (Options.LOG_LEVEL != LogLevel.LOW)
			LogManager.logInfo(this + " change child for " + input + " (" + getChild(input) + ") to " + newNode);
		AbstractNode oldNode = getChild(input);
		assert oldNode != null;
		assert!oldNode.isStateNode() || oldNode == newNode;
		children.put(input, newNode);
		newNode.changeFather(null, this);
		oldNode.changeFather(this, null);
		newNode.mergeWith(oldNode);
	}

	/**
	 * Clear the node in order to free it.
	 * <p>
	 * Should be called only before losing reference to this node
	 */
	protected void clearNode() {
		assert mergedWith != null;
		assert getParents().isEmpty();
		for (AbstractNode o : incompatibleNodes) {
			o.incompatibleNodes.remove(this);
		}
		incompatibleNodes.clear();
	}

	/**
	 * take the tree of {@code other} and merge it with current node
	 * 
	 * @param other
	 *            is supposed to be unreachable because it will be no longer
	 *            maintained (except if {@code other == this}).
	 *            {@link #clearNode()} is called on {@code other} at the end
	 */
	protected final void mergeWith(AbstractNode other) {
		if (this == other)
			return;
		assert mergedWith == null;
		if (other.mergedWith != null) {
			mergeWith(other.mergedWith);
			return;
		}
		other.mergedWith = this;
		other.desc = other.desc + "merged with " + this + "\n";
		assert(new Object() {
			boolean test(AbstractNode toTest) {// check that the node is
												// unreachable from the others
				Queue<AbstractNode> toCompute = new LinkedList<>();
				Set<AbstractNode> computed = new HashSet<>();
				toCompute.add(DataManager.instance.getStates().iterator().next().getVNode());
				while (!toCompute.isEmpty()) {
					AbstractNode n = toCompute.poll();
					if (computed.contains(n))
						continue;
					computed.add(n);
					if (n.children.containsValue(toTest))
						return false;
					toCompute.addAll(n.children.values());
				}
				return true;
			}
		}).test(other);
		assert!other.isStateNode() || isStateNode() : "merge must keep states";
		for (AbstractNode n : other.incompatibleNodes)
			setIncompatible(n);
		if (other.isEndOfTrace())
			DataManager.instance.setCurrentVNode(this);
		LogManager.logInfo("merging " + this + " with " + other);
		for (String i : other.children.keySet()) {
			assert(getOutput(i) == null || getOutput(i).equals(other.getOutput(i)));
			AbstractNode otherChild = other.getChild(i);
			AbstractNode child = children.get(i);
			if (child == null) {
				outputs.put(i, other.getOutput(i));
				children.put(i, otherChild);
				otherChild.changeFather(other, this);
			} else {
				assert(!child.isStateNode() || !otherChild.isStateNode() || ((StateNode) child)
						.getState() == ((StateNode) otherChild).getState()) : "incompatible trace from " + child
								+ " and " + otherChild;
				otherChild.changeFather(other, null);
				if (otherChild.isStateNode() && !child.isStateNode()) {
					// we must keep the node with a state
					changeChild(i, (StateNode) otherChild);
					otherChild.mergeWith(child);
				} else {
					child.mergeWith(otherChild);
				}
			}
		}
		other.clearNode();
	}

	/**
	 * change the father of a node
	 * 
	 * @param old
	 *            the link to old father which will be removed or {@code null}
	 * @param newFather
	 *            the new node to keep reference on or {@code null} to only
	 *            delete a reference.
	 * @see #getParents()
	 */
	protected void changeFather(AbstractNode old, AbstractNode newFather) {
		changeFatherLink(old, newFather);
		// compute incompatibilities for new father
		if (newFather != null) {
			// copying incompatibilities prevent from concurrent modifications
			Set<AbstractNode> oldIncompatibilities = new HashSet<>(incompatibleNodes);
			for (String i : newFather.getKnownInputs())
				if (newFather.getChild(i) == this)
					for (AbstractNode incomp : oldIncompatibilities)
						for (AbstractNode incompP : incomp.getParents())
							if (incompP.getChild(i) == incomp)
								newFather.setIncompatible(incompP);

		}
	}

	protected abstract void changeFatherLink(AbstractNode old, AbstractNode newFather);

	/**
	 * get the list of {@link StateNode} which are incompatible with this node
	 * using {@link #incompatibleNodes} (complexity linear in number of
	 * {@link AbstractNode})
	 * 
	 * @return a list of stateNodes for which
	 *         {@link #isCompatibleWith_full(AbstractNode)} will answer {@code
	 *         false}
	 * @see #getIncompatibleStateNode_full()
	 */
	public List<StateNode> getIncompatibleStateNode() {
		List<StateNode> r = new ArrayList<>();
		for (AbstractNode n : incompatibleNodes)
			if (n.isStateNode())
				r.add((StateNode) n);
		// assert mergedWith != null
		// || r.containsAll(getIncompatibleStateNode_full()) &&
		// getIncompatibleStateNode_full().containsAll(r);
		return r;
	}

	/**
	 * same as {@link #getIncompatibleStateNode()} but do not use the
	 * {@link #incompatibleNodes} and compute list from scratch.
	 * 
	 * @return
	 * @see #getIncompatibleStateNode()
	 */
	public abstract List<StateNode> getIncompatibleStateNode_full();

	/**
	 * check if two nodes are compatible
	 * <p>
	 * This method do not use {@link #incompatibleNodes}, result is computed
	 * from scratch This method is not supposed to be called recursively, see
	 * {@link #isCompatibleWith_full_intern(AbstractNode, Set)}
	 * 
	 * @see #isCompatibleWith_full_intern(AbstractNode, Set)
	 * 
	 * @param other
	 * @return false if an InputSequence is found for which {@code this}and
	 *         {@code other} have different outputs or goes in different states.
	 */
	public final boolean isCompatibleWith_full(AbstractNode other) {
		Set<Pair> computed = new HashSet<>();
		boolean r = isCompatibleWith_full_intern(other, computed);
		return r;
	}

	/**
	 * check if two nodes are compatible.
	 * 
	 * @param other
	 *            the node to be compared with.
	 * @param computed
	 *            a set of all pairs of node already computed.
	 * @return
	 * @see #isCompatibleWith_full(AbstractNode)
	 */
	protected boolean isCompatibleWith_full_intern(AbstractNode other, Set<Pair> computed) {
		Pair p = new Pair(this, other);
		if (computed.contains(p))
			return true;
		computed.add(p);
		for (String i : outputs.keySet()) {
			String otherOutput = other.getOutput(i);
			if (otherOutput != null && !otherOutput.equals(getOutput(i)))
				return false;
		}
		for (String i : outputs.keySet()) {
			AbstractNode otherChild = other.getChild(i);
			if (otherChild != null && !getChild(i).isCompatibleWith_full_intern(otherChild, computed))
				return false;
		}
		return true;
	}

	protected AbstractNode getMergedWith() {
		return mergedWith;
	}

	/**
	 * export current node and known relation from this node in dot format.
	 * <p>
	 * This method is called recursively for other nodes which have to be
	 * exported.
	 * 
	 * @param w
	 *            the writer to write the result.
	 * @param computed
	 *            a list of all nodes already exported in the current file.
	 * @throws IOException
	 *             if an error occurs with the writer
	 */
	public void exportToDot(Writer w, Set<AbstractNode> computed) throws IOException {
		boolean exportIncompatibilities = false;
		boolean exportParentReference = false;
		boolean exportUnreachableNodes = true;// need to keep all nodes in
												// static attribute 'instance'
		if (computed.contains(this))
			return;
		computed.add(this);

		w.write("\t" + dotName() + "[" + nodeToDotOptions() + (isEndOfTrace() ? ",color=red" : "")
				+ (mergedWith != null ? ",style=filled,fillcolor=green" : "") + ";]\n");
		for (String i : outputs.keySet()) {
			AbstractNode child = children.get(i);
			w.write("\t" + dotName() + "->" + child.dotName() + "[label=\"" + i + "/" + outputs.get(i) + "\"];\n");
			child.exportToDot(w, computed);
		}
		if (mergedWith != null)
			w.write("\t" + dotName() + "->" + mergedWith.dotName()
					+ "[dir=both,color=green,arrowhead=open,arrowtail=invdot,style=bold];\n");

		for (AbstractNode n : getParents())
			n.exportToDot(w, computed);
		if (exportParentReference)
			for (AbstractNode p : getParents())
				w.write("\t" + dotName() + "->" + p.dotName() + "[label=\"keep parent\",color=blue];\n");
		if (exportIncompatibilities)
			for (AbstractNode n : incompatibleNodes)
				if (nodeId > n.nodeId)
					w.write("\t" + dotName() + "->" + n.dotName()
							+ "[dir=both,color=red,arrowhead=diamond,arrowtail=diamond];\n");
		if (exportUnreachableNodes)
			for (AbstractNode n : instances)
				n.exportToDot(w, computed);
	}

	/**
	 * @return return node representation options.
	 */
	protected abstract String nodeToDotOptions();

	/**
	 * @return a unique name for each node
	 */
	protected String dotName() {
		return "n_" + nodeId;
	}

}
