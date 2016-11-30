package learner.mealy.tree;

import java.util.List;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

public class Branch {

	// OutputSequence outputSeq;
	OutputSequence outputSeq;
	SplittingTree splittingTree;

	public Branch(OutputSequence in, SplittingTree st) {
		// TODO Auto-generated constructor stub
		outputSeq = in;
		splittingTree = st;
	}

	public SplittingTree getSPTree() {
		return splittingTree;
	}

	public OutputSequence getOutputSequence() {
		return outputSeq;
	}

	public String toString() {
		String result = outputSeq.toString() + "(" + splittingTree.toString() + ");";
		return result;
	}

}
