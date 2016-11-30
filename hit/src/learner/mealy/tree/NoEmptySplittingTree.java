package learner.mealy.tree;

import java.util.ArrayList;

import automata.mealy.InputSequence;
import main.simpa.Options;

public class NoEmptySplittingTree extends SplittingTree {

	InputSequence inSeq;
	ArrayList<Branch> list;

	public NoEmptySplittingTree(InputSequence in) {
		this.inSeq = in;
		this.list = new ArrayList<Branch>();
	}

	public NoEmptySplittingTree(InputSequence in, ArrayList<Branch> b) {
		this.inSeq = in;
		this.list = b;
	}

	public InputSequence getInputSequence() {
		return inSeq;
	}

	public Branch getBranch(int i) {
		return list.get(i);
	}

	public void addBranch(Branch b) {
		list.add(b);
	}

	public int getBranchSize() {
		return list.size();
	}

	public String toString() {
		String result = inSeq.toString() + "(";
		for (Branch b : list) {
			result += b.toString();
		}
		result += ")";
		return result;
	}
}
