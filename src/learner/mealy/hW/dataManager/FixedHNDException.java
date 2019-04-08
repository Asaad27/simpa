/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
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

import automata.mealy.InputSequence;
import learner.mealy.LmTrace;

public class FixedHNDException extends GenericHNDException {

	private static final long serialVersionUID = 3270985693866307108L;
	private LmTrace traceA;
	private LmTrace traceB;
	private InputSequence h;

	public FixedHNDException(LmTrace traceA, LmTrace traceB, InputSequence h) {
		assert traceA.getInputsProjection()
				.equals(traceB.getInputsProjection());
		assert h.getLength() == 0 || traceA.getInputsProjection().startsWith(h);
		assert !traceA.getOutput(traceA.size() - 1).equals(
				traceB.getOutput(traceA.size() - 1)) : "the last output should differ in two traces";
		assert traceA.subtrace(0, traceA.size() - 1).equals(
				traceB.subtrace(0, traceA.size() - 1)) : "only the last output should differ in traces";
		this.traceA = traceA;
		this.traceB = traceB;
		this.h = h;
	}

	public String toString() {
		return "the two traces " + traceA + " and " + traceB
				+ " have same answer to homing sequence " + h
				+ " but differs after applying the same inputs";
	}

	public InputSequence getNewH() {
		return traceA.getInputsProjection();
	}

}