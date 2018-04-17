package learner.mealy.hW.dataManager;

import automata.State;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

public class InconsistancyHMappingAndConjectureException extends Exception {
	private static final long serialVersionUID = 7967922441884871530L;

	private State stateBeforeH;
	private State stateAfterH;
	private GenericOutputSequence hResponse;
	private FullyQualifiedState mappedTarget;
	private InputSequence distinctionSequence;
	private OutputSequence targetResponse;
	private OutputSequence traceResponse;

	public InconsistancyHMappingAndConjectureException(State stateBeforeH,
			State stateAfterH, GenericOutputSequence hResponse,
			FullyQualifiedState mappedTarget, InputSequence distinctionSequence,
			OutputSequence targetResponse, OutputSequence traceResponse) {
		this.stateBeforeH = stateBeforeH;
		this.stateAfterH = stateAfterH;
		this.hResponse = hResponse;
		this.mappedTarget = mappedTarget;
		this.distinctionSequence = distinctionSequence;
		this.targetResponse = targetResponse;
		this.traceResponse = traceResponse;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("inconsistency between conjecture and h to W mapping.");
		builder.append(" From state " + stateBeforeH
				+ ", applying homing sequence produce output " + hResponse
				+ " and lead in state " + stateAfterH + ".");
		if (isFromCompleteMapping()) {
			if (mappedTarget.getState() == stateAfterH)
				builder.append("The mapping from this response also lead in "
						+ stateAfterH);
			else
				builder.append(
						" There is a mapping from the same output to state "
								+ mappedTarget);
		} else {
			builder.append("There is a partial mapping from the same response");
		}
		if (distinctionSequence == null) {
			builder.append(" but unfortunately, state " + stateAfterH
					+ " has the same W-responses as recorded in mapping.");
			builder.append(" This mean that transitions from " + stateAfterH
					+ " are not consistant with characterisation");
		} else {
			builder.append(" and the sequence " + distinctionSequence
					+ " has answer " + traceResponse
					+ " in mapping (i.e. in trace) but according to conjecture, state "
					+ stateAfterH + " should answer " + targetResponse + ".");
		}
		return builder.toString();
	}

	public boolean isFromCompleteMapping() {
		return mappedTarget != null;
	}

	public State getStateBeforeH() {
		return stateBeforeH;
	}

	public State getStateAfterH() {
		return stateAfterH;
	}

	public GenericOutputSequence gethResponse() {
		return hResponse;
	}

	public FullyQualifiedState getMappedTarget() {
		return mappedTarget;
	}

	public InputSequence getDistinctionSequence() {
		return distinctionSequence;
	}

	public OutputSequence getTargetResponse() {
		return targetResponse;
	}

	public OutputSequence getTraceResponse() {
		return traceResponse;
	}
}
