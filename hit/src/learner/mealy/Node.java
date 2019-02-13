package learner.mealy;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.simpa.Options;

/**
 * 
 * @param <T>
 *            the type (or a super type) of the class inheriting this class. You
 *            cannot use a type which is not a parent of the class implementing
 *            Node, especially because you will have to implement
 *            {@link #thisAsT()}.
 */
public abstract class Node<T extends Node<T>> {
	public static int GLOBALID = 1;
	public int id = 0;
	public String input = null;
	public String output = null;
	public T parent = null;
	public Map<String, T> children = null;


	public Node() {
		children = new HashMap<>();
		assert thisAsT() == this : "this object must be of type T";
	}

	public Node(String input, String output) {
		this();
		this.input = input;
		this.output = output;
	}

	/**
	 * Return this as a T object. It can be done with casting (i.e.
	 * {@code (T) this}) but implementing this method ensure that this object is
	 * an instance of generic type T : {@code A extends Node<A>} can implement
	 * this method, but {@code B extends Node<A>} cannot return himself as type
	 * A
	 */
	protected abstract T thisAsT();

	public T addChild(T node) {
		children.put(node.input, node);
		node.setParent(thisAsT());
		node.id = GLOBALID++;
		return node;
	}

	public void setParent(T node) {
		parent = node;
	}

	public T childBy(String inputSymbol) {
		return children.get(inputSymbol);
	}

	protected void toDotWrite(Writer w) throws IOException {
		List<Node<T>> queue = new ArrayList<>();
		queue.add(this);
		Node<T> currentNode = null;
		while (!queue.isEmpty()) {
			currentNode = queue.get(0);
			for (Node<T> n : currentNode.children.values()) {
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
			queue.addAll(currentNode.children.values());
		}
	}

}
