package learner.mealy.noReset;

import java.util.List;

import automata.mealy.InputSequence;
import learner.mealy.noReset.dataManager.FullyQualifiedState;

public class StateSplittingTree extends SplittingTree {

	FullyQualifiedState state;

	public StateSplittingTree(FullyQualifiedState state) {

		// TODO Auto-generated constructor stub
		this.state = state;
	}

	public String toString() {
		//now we give empty to state.
		return "";
	}
	// List<String> state;

}
