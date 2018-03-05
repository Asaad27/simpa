package learner.mealy.hW.dataManager;

import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;

public class HZXWSequence {
	
	private final OutputSequence hResponse;
	private final LmTrace transferSequence;
	private final LmTrace transition;
	private final LmTrace wResponse;

	public HZXWSequence(OutputSequence hResponse, LmTrace transferSequence,
			LmTrace transition, LmTrace wResponse) {
		super();
		this.hResponse = hResponse;
		this.transferSequence = transferSequence;
		this.transition = transition;
		this.wResponse = wResponse;
		assert transition.size()==1;
	}

	public OutputSequence gethResponse() {
		return hResponse;
	}

	public LmTrace getTransferSequence() {
		return transferSequence;
	}

	public LmTrace getTransition() {
		return transition;
	}

	public LmTrace getwResponse() {
		return wResponse;
	}
	
	public String toString() {
		return "[h=" + hResponse + ", transfer=" + transferSequence
				+ ", transition=" + transition + ", w=" + wResponse+"]";
	}
}

