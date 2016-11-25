package learner.mealy.tree;

import java.util.List;

import automata.mealy.InputSequence;
import learner.mealy.noReset.dataManager.FullyQualifiedState;

public class State extends SplittingTree {
	
	
	FullyQualifiedState state;
	
	
	public State(InputSequence in, Branch b) {
		super(in, b);
		// TODO Auto-generated constructor stub
	}

	// List<String> state;

}
