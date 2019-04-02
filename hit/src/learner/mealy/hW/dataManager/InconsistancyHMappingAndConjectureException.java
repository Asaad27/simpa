/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.hW.dataManager;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;

public class InconsistancyHMappingAndConjectureException extends Exception {
	private static final long serialVersionUID = 7967922441884871530L;

	private State stateBeforeH;
	private State stateAfterH;
	private GenericOutputSequence hResponse;
	private FullyQualifiedState mappedTarget;
	private GenericInputSequence distinctionSequence;
	private GenericOutputSequence targetResponse;
	private GenericOutputSequence traceResponse;

	public InconsistancyHMappingAndConjectureException(State stateBeforeH,
			State stateAfterH, GenericOutputSequence hResponse,
			FullyQualifiedState mappedTarget,
			GenericInputSequence distinctionSequence,
			GenericOutputSequence targetResponse,
			GenericOutputSequence traceResponse) {
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

	public GenericInputSequence getDistinctionSequence() {
		return distinctionSequence;
	}

	public GenericOutputSequence getTargetResponse() {
		return targetResponse;
	}

	public GenericOutputSequence getTraceResponse() {
		return traceResponse;
	}
}
