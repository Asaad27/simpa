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

public class InconsistancyWithConjectureAtEndOfTraceException extends
		RuntimeException {

	private static final long serialVersionUID = -8916411819696993385L;
	private String input;
	private String conjectureOut;
	private String driverOut;
	private TraceTree source;

	public InconsistancyWithConjectureAtEndOfTraceException(String input,
			String conjectureOut, TraceTree source, String driverOut) {
		super();
		this.input = input;
		this.conjectureOut = conjectureOut;
		this.driverOut = driverOut;
		this.source = source;
	}

	public String toString() {
		return "Inconsistancy between trace and conjecture. After '"
				+ input
				+ "' we expected '"
				+ conjectureOut
				+ "' and we get '"
				+ driverOut
				+ "'"
				+ ((source == null) ? ""
						: (" (found from tree " + source + ")"));
	}

}
