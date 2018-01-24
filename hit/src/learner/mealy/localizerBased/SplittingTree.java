package learner.mealy.localizerBased;

import java.util.HashMap;
import java.util.Map;

import learner.mealy.LmTrace;

import automata.mealy.OutputSequence;
import automata.mealy.InputSequence;

public class SplittingTree {
	private int height;
	private final SplittingTree father;
	private final OutputSequence O;// the sequence LEADING to this node
	private final boolean isLeaf;
	private Map<OutputSequence, SplittingTree> children;// (if not leaf)
	private final InputSequence I;// the sequence which labels this node (if not
									// leaf)

	private SplittingTree(boolean isLeaf, OutputSequence O, InputSequence I,
			SplittingTree father) {
		assert (isLeaf || I != null);
		assert (father == null || (O != null
				&& O.getLength() == father.getI().getLength() && !father
					.isLeaf()));
		this.height = 0;
		this.children = new HashMap<>();
		this.isLeaf = isLeaf;
		this.O = O;
		this.I = I;
		this.father = father;
		this.height = 0;// will be updated by children
		if (father != null) {
			assert (!father.children.containsKey(0));
			father.children.put(O, this);
			SplittingTree n = this;
			while (n.father != null) {
				if (n.father.height < n.height + 1)
					n.father.height = n.height + 1;
				n = n.father;
			}
		}
	}

	public SplittingTree(InputSequence I) {
		this(false, null, I, null);
	}

	public SplittingTree(SplittingTree father, OutputSequence O) {
		this(true, O, null, father);
	}

	public SplittingTree(SplittingTree father, OutputSequence O, InputSequence I) {
		this(false, O, I, father);
	}
	
	public int getHeight() {
		return height;
	}

	public SplittingTree getFather() {
		return father;
	}

	public OutputSequence getO() {
		return O;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public Map<OutputSequence, SplittingTree> getChildren() {
		return children;
	}

	public SplittingTree getChild(OutputSequence seq) {
		return children.get(seq);
	}

	public InputSequence getI() {
		return I;
	}

	public boolean contains(InputSequence seq) {
		if (isLeaf)
			return false;
		if (seq.equals(I))
			return true;
		for (SplittingTree subTree : children.values()) {
			if (subTree.contains(seq))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		SplittingTree current = this;
		while (current.father != null) {
			s.insert(0, new LmTrace(current.father.getI(), current.getO())
					+ ", " + "");
			current = current.father;
		}
		s.insert(0, "[");
		s.append("] ");
		if (isLeaf)
			s.append("(leaf)");
		else
			s.append(getI() + "→?");
		s.append(" h=" + height + " ");
		printTree(s);
		return s.toString();
	}

	private void printTree(StringBuilder s) {
		if (isLeaf()) {
			return;
		}
		s.append(I);
		s.append("[");
		for (SplittingTree child : children.values()) {
			child.printTree(s);
			s.append(",");
		}
		s.append("]");
	}
}
