package learner.mealy.noReset.dataManager;

import java.util.List;
import java.util.Set;

public class ConjectureNotConnexException extends RuntimeException {

	private static final long serialVersionUID = 9158571872132308078L;
	private List<FullyQualifiedState> reachedStates;
	private Set<FullyQualifiedState> notFullyKnownStates;

	public ConjectureNotConnexException(
			List<FullyQualifiedState> reachedStates,
			Set<FullyQualifiedState> notFullyKnownStates) {
		this.reachedStates = reachedStates;
		this.notFullyKnownStates = notFullyKnownStates;
	}

	public String toString() {
		return "The infered automata seems to be not totaly connex : we reached "
				+ reachedStates + " but not " + notFullyKnownStates;
	}
}
