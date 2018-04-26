package automata.mealy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import main.simpa.Options;
import tools.GraphViz;
import tools.loggers.LogManager;

/**
 * A base class to record an adaptive structure.
 * 
 * This aims to be used for adaptive sequences and splitting tree.
 * 
 * The usage of this class is a little ambiguous because the same class is used
 * to represent a sequence, an input or an output. More precisely : The root of
 * the tree represents at the same time the complete sequence, and the first
 * input to apply (and the output when the tree is empty). Each node in the
 * middle of the tree represents at the same time a partial response and the end
 * of inputs to apply. Each leaf of the tree identify one complete response.
 * 
 * @author Nicolas BREMOND
 */
public abstract class AdaptiveStructure<InputT, OutputT>
		implements GenericSequence<InputT, OutputT>,
		GenericSequence.GenericResponse<InputT, OutputT> {

	protected InputT input = null;
	protected AdaptiveStructure<InputT, OutputT> father = null;
	protected AdaptiveStructure<InputT, OutputT> root;
	protected OutputT output = null; // output leading to this node. null if
										// father is null.
	protected Map<OutputT, AdaptiveStructure<InputT, OutputT>> children = new HashMap<>();

	public AdaptiveStructure() {
		root = this;
	}

	/**
	 * Indicate if a node is a leaf (in which case, there is no more input to
	 * apply).
	 * 
	 * Note that a leaf can be turned into non-leaf when the node is extended.
	 * 
	 * @return true is this node is a leaf at this time.
	 */
	public boolean isFinal() {
		return input == null;
	}

	/**
	 * Indicate if this object represents the complete sequence instead of a
	 * sequence from a partial answer.
	 * 
	 * @return {@code true} if this object represent the complete tree
	 */
	public boolean isRoot() {
		return this == root;
	}

	@Override
	public boolean isEmpty() {
		return this == root && isFinal();
	}

	/**
	 * indicate if this node is an answer (i.e. a child) of {@code input}.
	 * 
	 * @param input
	 *            the possible ancestor of this node. Note that input is not
	 *            mandatory the root of adaptive structure, it can be a partial
	 *            answer to root.
	 * @return true if this node is a possible answer to {@code input}
	 */
	public boolean isAnswerTo(AdaptiveStructure<InputT, OutputT> input) {
		if (input == root)
			return true;
		AdaptiveStructure<InputT, OutputT> ancestor = this;
		while (ancestor != null) {
			if (ancestor == input)
				return true;
			ancestor = ancestor.father;
		}
		return false;
	}

	/**
	 * get the input labeling the current position
	 */
	public InputT getInput() {
		assert !isFinal();
		return input;
	}

	/**
	 * get the output leading to this node
	 * 
	 * @return the output s.t. {@code getFather().getChild(output)} returns this
	 *         node.
	 */
	public OutputT getFromOutput() {
		assert !isRoot();
		assert father.getChild(output) == this;
		return output;
	}

	/**
	 * Get the complete sequence containing this part.
	 * 
	 * @return
	 */
	public AdaptiveStructure<InputT, OutputT> getFullSequence() {
		return root;
	}

	/**
	 * get the parent of this node or null if this is the root.
	 * 
	 * @return the parent of this node.
	 */
	public AdaptiveStructure<InputT, OutputT> getFather() {
		return father;
	}

	protected void invalidateChild(AdaptiveStructure<InputT, OutputT> child) {
		assert !child.isRoot();
		assert getChild(child.getFromOutput()) == child;
		children.remove(child.getFromOutput());
	}

	/**
	 * get a child a create one if none exists
	 * 
	 * @param output
	 * @return
	 */
	public AdaptiveStructure<InputT, OutputT> getChild(OutputT output) {
		assert !isFinal();
		assert checkCompatibility(input, output);
		AdaptiveStructure<InputT, OutputT> child = children.get(output);
		if (child == null) {
			child = createNewNode();
			child.father = this;
			child.output = output;
			child.root = this.root;
			children.put(output, child);
		}
		return child;
	}

	/**
	 * Indicate if a non-leaf node already have a child for the specified
	 * output. This can be used to know if {@link #getChild(Object)} will return
	 * an existing or a new node.
	 * 
	 * @param output
	 *            the output to test
	 * @return true if this (sub-)sequence already have a child for the given
	 *         output.
	 */
	public boolean hasChild(OutputT output) {
		assert !isFinal();
		assert checkCompatibility(input, output);
		return children.get(output) != null;
	}

	/**
	 *
	 * indicate whether a sequence of input/output exists in this structure.
	 * 
	 * @param inputs
	 *            the list of inputs
	 * @param outputs
	 *            the list of outputs, can be one symbol shorter than inputs
	 *            because last symbol is optional
	 * @return true if the sequence of inputs exists in this structure. Can
	 *         return true even if the last output is specified and the
	 *         corresponding node do not exists.
	 */
	public boolean hasPrefix(List<InputT> inputs, List<OutputT> outputs) {
		assert isRoot();
		assert inputs.size() == outputs.size()
				|| inputs.size() == outputs.size() + 1;
		AdaptiveStructure<InputT, OutputT> current = this;
		int i = 0;
		while (i < inputs.size()) {
			if (current.isFinal()) {// lists are longer than this response
				return false;
			} else {
				if (!inputs.get(i).equals(current.input))
					return false;
			}
			if (outputs.size() > i) {
				current = current.getChild(outputs.get(i));
			} else {
				if (i == inputs.size() - 1)// last node do not exists,but we
											// still consider that lists are
											// prefix of this node.
					return true;
				current = null;
			}

			i++;
		}
		return true;
	}

	/**
	 *
	 * indicate whether a sequence of input/output exists in this structure and
	 * leads to the end of this structure (no more input to apply after those
	 * inputs/outputs).
	 * 
	 * @param inputs
	 *            the list of inputs
	 * @param outputs
	 *            the list of outputs, must have the same length as inputs
	 * @return true if the sequence of inputs exists in this structure and lead
	 *         to a leaf. Can return true even if the node corresponding to last
	 *         output does not exists. If lists are continues after discovering
	 *         the leaf, the false is returned
	 */
	public boolean hasAnswer(List<InputT> inputs, List<OutputT> outputs) {
		assert isRoot();
		assert inputs.size() == outputs.size();
		AdaptiveStructure<InputT, OutputT> current = this;
		int i = 0;
		while (i < inputs.size()) {
			if (current.isFinal()) {// lists are longer than this response
				return false;
			} else {
				if (!inputs.get(i).equals(current.input))
					return false;
			}
			if (i == inputs.size() - 1 && !current.hasChild(outputs.get(i))) {
				// last node do not exists, but we consider that we found an
				// answer because the node created by a call to getChild would
				// be a leaf.
				return true;
			}

			current = current.getChild(outputs.get(i));

			i++;
		}
		return current.isFinal();
	}

	/**
	 * Get the answer in this structure to a given sequence of input/output.
	 * 
	 * @param inputs
	 *            the list of inputs. can be null to avoid checking
	 * @param outputs
	 *            the list of outputs, must have the same length as inputs
	 * @return null if the list of inputs is provided and does not match the
	 *         inputs expected or if the sequences are longer than expected.
	 *         Otherwise, returns
	 *         {@code this.getChild(outputs[0]).getChild(outputs[1]) ...}. If
	 *         sequences are too short, the returned node will not be a leaf.
	 */
	public AdaptiveStructure<InputT, OutputT> getAnswer(List<InputT> inputs,
			List<OutputT> outputs) {
		assert isRoot();
		assert inputs.size() == outputs.size();
		AdaptiveStructure<InputT, OutputT> current = this;
		int i = 0;
		while (i < outputs.size()) {
			if (current.isFinal()) {// lists are longer than this response
				return null;
			} else {
				if (inputs != null && !inputs.get(i).equals(current.input))
					return null;
			}
			current = current.getChild(outputs.get(i));

			i++;
		}
		return current;
	}

	/**
	 * Get the list of inputs for nodes from root to this one (if not final).
	 * 
	 * @return the list of inputs applied to get this response.
	 */
	public List<? extends InputT> getInputsList() {
		List<InputT> inputs = new ArrayList<>();
		Stack<? extends AdaptiveStructure<InputT, OutputT>> nodes = getStackFromRoot();
		AdaptiveStructure<InputT, OutputT> current = nodes.pop();
		while (current != this) {
			inputs.add(current.getInput());
			current = nodes.pop();
		}
		assert nodes.isEmpty();
		if (!isFinal()) {
			inputs.add(getInput());
		}
		return inputs;
	}

	/**
	 * Get the list of outputs for node from root to this one.
	 * 
	 * @return the list of outputs composing this answer.
	 */
	public List<? extends OutputT> getOutputsList() {
		List<OutputT> outputs = new ArrayList<>();
		Stack<? extends AdaptiveStructure<InputT, OutputT>> nodes = getStackFromRoot();
		AdaptiveStructure<InputT, OutputT> current = nodes.pop();
		// first node is root and doesn't have an output assigned, so it is
		// discarded
		while (!nodes.isEmpty()) {
			current = nodes.pop();
			outputs.add(current.getFromOutput());
		}
		return outputs;
	}

	/**
	 * should create a node of the same type as this.
	 * 
	 * @return
	 */
	protected abstract AdaptiveStructure<InputT, OutputT> createNewNode();

	/**
	 * Extend a leaf with a new input
	 * 
	 * @param input
	 *            the input which will extend this leaf.
	 */
	protected final void extend_local(InputT input) {
		assert isFinal();
		this.input = input;
	}

	/**
	 * extends a tree with zero, one or several inputs.
	 * 
	 * @param inputs
	 *            the inputs used to label the nodes.
	 * @param outputs
	 *            the outputs indicating which child node should be extended.
	 *            There should be as much output as input or one output less
	 *            (the last one is not needed).
	 * @return the response created if the last output is specified or null if
	 *         the last output is unspecified.
	 */
	public AdaptiveStructure<InputT, OutputT> extend(List<InputT> inputs,
			List<OutputT> outputs) {
		assert inputs.size() == outputs.size()
				|| inputs.size() == outputs.size() + 1;
		int i = 0;
		AdaptiveStructure<InputT, OutputT> current = this;
		while (i < inputs.size()) {
			if (current.isFinal()) {
				current.extend_local(inputs.get(i));
			} else {
				assert inputs.get(i).equals(current.input);
			}
			if (outputs.size() > i) {
				assert checkCompatibility(inputs.get(i), outputs.get(i));
				current = current.getChild(outputs.get(i));
			} else
				current = null;
			i++;
		}
		return current;
	}

	/**
	 * Get the nodes from root to this one, root is on top of the stack and this
	 * at bottom.
	 * 
	 * @return nodes from root to this one.
	 */
	public Stack<? extends AdaptiveStructure<InputT, OutputT>> getStackFromRoot() {
		Stack<AdaptiveStructure<InputT, OutputT>> nodes = new Stack<>();
		AdaptiveStructure<InputT, OutputT> current = this;
		while (current != null) {
			nodes.push(current);
			current = current.father;
		}
		return nodes;
	}

	/**
	 * check if the output is compatible with the input.
	 * 
	 * @param inputT
	 *            the input to test
	 * @param outputT
	 *            the output to test
	 * @return false if there is an incompatibility between input and output.
	 */
	protected abstract boolean checkCompatibility(InputT input, OutputT output);

	/**
	 * Get the all nodes of the tree. Include all nodes from root, even non-leaf
	 * 
	 * @return the collection of all nodes. The order in the collection is not
	 *         specified.
	 */
	public Collection<AdaptiveStructure<InputT, OutputT>> getAllNodes() {
		List<AdaptiveStructure<InputT, OutputT>> all = new ArrayList<>();
		List<AdaptiveStructure<InputT, OutputT>> currentLevel = new ArrayList<>();
		currentLevel.add(root);
		while (!currentLevel.isEmpty()) {
			all.addAll(currentLevel);
			List<AdaptiveStructure<InputT, OutputT>> nextLevel = new ArrayList<>();
			for (AdaptiveStructure<InputT, OutputT> node : currentLevel)
				nextLevel.addAll(node.children.values());
			currentLevel = nextLevel;
		}
		return all;
	}

	@Override
	public int getMaxLength() {
		assert isRoot() : "documentation do not define result for a sub sequence";
		int depth = 0;
		List<AdaptiveStructure<InputT, OutputT>> currentLevel = new ArrayList<>();
		currentLevel.add(root);
		while (!currentLevel.isEmpty()) {
			List<AdaptiveStructure<InputT, OutputT>> nextLevel = new ArrayList<>();
			for (AdaptiveStructure<InputT, OutputT> node : currentLevel) {
				nextLevel.addAll(node.children.values());
			}
			currentLevel = nextLevel;
			depth++;
		}
		return depth - 1;
	}

	@Override
	public boolean checkCompatibilityWith(GenericSequence<InputT, OutputT> in) {
		assert in instanceof AdaptiveStructure<?, ?>;
		AdaptiveStructure<InputT, OutputT> in_ = (AdaptiveStructure<InputT, OutputT>) in;
		return isAnswerTo(in_);
	}

	public StringBuilder toString(StringBuilder s) {
		if (isFinal())
			s.append("leaf");
		else {
			s.append(input);
			s.append(": [");
			for (AdaptiveStructure<InputT, OutputT> child : children.values()) {
				s.append(child.getFromOutput());
				s.append(" → ");
				s.append(child);
				s.append(",");
			}
			s.append("]");
		}
		return s;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		toString(s);
		return s.toString();
	}

	/// Iterable/Iterator functions ///
	public class Iterator implements GenericSequence.Iterator<InputT, OutputT> {
		AdaptiveStructure<InputT, OutputT> current = root;
		boolean isReady = true;

		@Override
		public boolean hasNext() {
			if (!isReady)
				throw new InvalidCallException();
			return !current.isFinal();
		}

		@Override
		public InputT next() {

			isReady = false;
			if (current.isFinal()) {
				throw new NoSuchElementException();
			}
			return current.getInput();
		}

		@Override
		public void setPreviousOutput(OutputT previousOutput) {
			if (isReady)
				throw new InvalidCallException(
						"two consecutive call to 'setPreviousOutput'");
			isReady = true;
			if (!hasNext())
				throw new InvalidCallException("no more output expected");
			current = current.getChild(previousOutput);
		}

		@Override
		public AdaptiveStructure<InputT, OutputT> getResponse() {
			assert !hasNext();
			return current;
		}
	}

	@Override
	public Iterator iterator() {
		return new Iterator();
	}

	/// DOT functions ///
	protected static long dotNodeNumber = 0;
	private String dotName = null;

	public String getDotName() {
		if (dotName == null)
			dotName = "node" + dotNodeNumber++;
		return dotName;
	}

	public void exportToDot() {
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException(
						"unable to create " + dir.getName() + " directory");

			file = new File(dir.getPath() + File.separatorChar
					+ "adaptative_struct.dot");
			exportToDot(file, "adaptative_struct");
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}

	}

	public void exportToDot(File file, String name) {
		try {
			Writer writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph " + GraphViz.id2DotAuto(name) + " {\n");
			root.dot_appendAll(writer);
			writer.write("}\n");
			writer.close();
			LogManager.logInfo(
					"Conjecture has been exported to " + file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dot_appendAll(Writer writer) throws IOException {
		dot_appendNode(writer);
		for (AdaptiveStructure<InputT, OutputT> child : children.values()) {
			dot_appendChild(writer, child);
			child.dot_appendAll(writer);
		}
	}

	protected void dot_appendNode(Writer writer) throws IOException {
		String label = "leaf";
		if (!isFinal()) {
			label = input.toString();
		}
		writer.write(
				getDotName() + "[label=" + GraphViz.id2DotAuto(label) + "];\n");
	}

	protected void dot_appendChild(Writer writer,
			AdaptiveStructure<InputT, OutputT> child) throws IOException {
		writer.write(getDotName() + " -> " + child.getDotName() + "[label="
				+ GraphViz.id2DotAuto(child.output.toString()) + "];\n");
	}

}
