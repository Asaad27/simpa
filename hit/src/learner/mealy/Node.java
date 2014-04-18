package learner.mealy;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;

public class Node {
	public static int GLOBALID = 1;
	public int id = 0;
	public String input = null;
	public String output = null;
	public Node parent = null;
	public List<Node> children = null;

	public Node() {
		children = new ArrayList<Node>();
	}

	public Node(String input, String output) {
		this();
		this.input = input;
		this.output = output;
	}

	public void clearChildren() {
		children.clear();
	}

	public Node addChild(Node node) {
		children.add(node);
		node.setParent(this);
		node.id = GLOBALID++;
		return node;
	}

	public void setParent(Node node) {
		parent = node;
	}

	public boolean haveChildBy(String inputSymbol) {
		for (Node n : children) {
			if (n.input.equals(inputSymbol))
				return true;
		}
		return false;
	}

	public Node childBy(String inputSymbol) {
		for (Node n : children) {
			if (n.input.equals(inputSymbol))
				return n;
		}
		return null;
	}

	protected void toDotWrite(Writer w) throws IOException {
		List<Node> queue = new ArrayList<Node>();
		queue.add(this);
		Node currentNode = null;
		while (!queue.isEmpty()) {
			currentNode = queue.get(0);
			for (Node n : currentNode.children) {
				w.write("    node"
						+ currentNode.id
						+ " -> node"
						+ n.id
						+ " [color=\""
						+ "black"
						+ "\" label=\""
						+ n.input
						+ "/"
						+ (n.output.length() > 0 ? n.output
								: Options.SYMBOL_OMEGA_LOW) + "\"]\n");
			}
			queue.remove(0);
			queue.addAll(currentNode.children);
		}
	}

}
