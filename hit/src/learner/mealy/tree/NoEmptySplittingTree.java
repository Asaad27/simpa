package learner.mealy.tree;

import java.util.ArrayList;

import automata.mealy.InputSequence;

public class NoEmptySplittingTree extends SplittingTree{

	InputSequence inSeq;
	ArrayList<Branch> list;
	// SplittingTree splittingTree;

	public NoEmptySplittingTree(InputSequence in) {
		this.inSeq = in;
		this.list = new ArrayList<Branch>();
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

}
