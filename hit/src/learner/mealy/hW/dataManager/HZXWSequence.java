package learner.mealy.hW.dataManager;

import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmTrace;

public class HZXWSequence {
	
	private final GenericOutputSequence hResponse;
	private final LmTrace transferSequence;
	private final LmTrace transition;
	private final LmTrace wResponse;

	public HZXWSequence(GenericOutputSequence hResponse,
			LmTrace transferSequence, LmTrace transition, LmTrace wResponse) {
		super();
		this.hResponse = hResponse;
		this.transferSequence = transferSequence;
		this.transition = transition;
		this.wResponse = wResponse;
		assert transition.size()==1;
	}

	public GenericOutputSequence gethResponse() {
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

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof HZXWSequence)
			return equals((HZXWSequence) o);
		return false;
	}

	public boolean equals(HZXWSequence o) {
		return hResponse.equals(o.hResponse)
				&& transferSequence.equals(o.transferSequence)
				&& transition.equals(o.transition)
				&& wResponse.equals(o.wResponse);
	}
}
