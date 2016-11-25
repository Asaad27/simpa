package learner.mealy.tree;

import java.util.List;

import automata.mealy.OutputSequence;

public class Branch {
	
//	OutputSequence outputSeq;
	OutputSequence outputSeq;
	SplittingTreeReader splittingTree;
	// Branch b;

	public Branch(OutputSequence oSeq, SplittingTreeReader sTree) {
		this.outputSeq = oSeq;
		this.splittingTree = sTree;
	}

	public void addBranche(Branch b) {
//		if (inputSequence.size() != b.outputSeq.size()) {
//			throw new RuntimeException("Length of input sequence is diffrent from output sequence.");
//		}
//		branchs.add(b);
	}

}
