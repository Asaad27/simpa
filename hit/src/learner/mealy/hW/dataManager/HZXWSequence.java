package learner.mealy.hW.dataManager;

import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmTrace;

public class HZXWSequence {

	private final GenericOutputSequence hResponse;
	private final LmTrace transferSequence;
	private final LmTrace transition;
	private final LmTrace wResponse;

	/**
	 * record a sequence of h z x w or 'reset' z x w
	 * 
	 * @param hResponse
	 *            the response to homing sequence or {@code null} if the
	 *            sequence was observed after a reset
	 * @param transferSequence
	 *            the sequence used for moving in an incomplete state.
	 * @param transition
	 *            the transitions observed
	 * @param wResponse
	 *            the sequence characterizing the state at end of transition.
	 */
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
		return "[" + (hResponse == null ? "after reset" : "h=" + hResponse)
				+ ", transfer=" + transferSequence + ", transition="
				+ transition + ", w=" + wResponse + "]";
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof HZXWSequence)
			return equals((HZXWSequence) o);
		return false;
	}

	public boolean equals(HZXWSequence o) {
		return ((hResponse == null && o.hResponse == null)
				|| (hResponse != null && hResponse.equals(o.hResponse)))
				&& transferSequence.equals(o.transferSequence)
				&& transition.equals(o.transition)
				&& wResponse.equals(o.wResponse);
	}
}
