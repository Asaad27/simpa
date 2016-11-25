package learner.mealy.tree;

import automata.mealy.InputSequence;

public class SplittingTree {
	
	InputSequence inSeq;
	Branch branch;
//	SplittingTree splittingTree;
	

	public SplittingTree(InputSequence in, Branch b) {
		this.inSeq = in;
		this.branch = b ;	
	}
	
	
	public InputSequence getInputSequence(){
		return inSeq;
	}

	public Branch getBranch(){
		return branch;
	}
}
