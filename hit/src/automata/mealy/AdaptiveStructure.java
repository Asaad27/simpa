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
public abstract class AdaptiveStructure<InputT, OutputT> {

	InputT input = null;
	AdaptiveStructure<InputT, OutputT> father = null;
	AdaptiveStructure<InputT, OutputT> root;
	OutputT output = null; // output leading to this node. null if father is
							// null.
	Map<OutputT, AdaptiveStructure<InputT, OutputT>> children = new HashMap<>();

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
	private void extend_local(InputT input) {
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
	 */
	public void extend(List<InputT> inputs, List<OutputT> outputs) {
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

	protected static long dotNodeNumber = 0;
	private String dotName = null;

	protected String getDotName() {
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

	protected void dot_appendAll(Writer writer) throws IOException {
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
				getDotName() + "[label=" + GraphViz.id2DotAuto(label) + "];");
	}

	protected void dot_appendChild(Writer writer,
			AdaptiveStructure<InputT, OutputT> child) throws IOException {
		writer.write(getDotName() + " -> " + child.getDotName() + "[label="
				+ GraphViz.id2DotAuto(child.output.toString()) + "];");
	}

	public boolean isRoot() {
		return this == root;
	}

}
