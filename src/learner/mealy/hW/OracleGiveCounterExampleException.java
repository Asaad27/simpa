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
package learner.mealy.hW;

import java.util.List;

import learner.mealy.LmTrace;
import learner.mealy.hW.dataManager.GenericHNDException;

public class OracleGiveCounterExampleException extends Exception {
	private static final long serialVersionUID = -3824536424109364033L;

	private LmTrace counterExampletrace;
	private List<GenericHNDException> hExceptions;

	public OracleGiveCounterExampleException(LmTrace counterExampleTrace,
			List<GenericHNDException> hExceptions) {
		this.counterExampletrace = counterExampleTrace;
		this.hExceptions = hExceptions;
	}

	public LmTrace getCounterExampletrace() {
		return counterExampletrace;
	}

	public List<GenericHNDException> gethExceptions() {
		return hExceptions;
	}

}
