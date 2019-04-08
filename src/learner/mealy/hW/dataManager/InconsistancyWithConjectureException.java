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

import learner.mealy.LmTrace;

public class InconsistancyWithConjectureException extends RuntimeException {

	private static final long serialVersionUID = 8658553725594257279L;
	private LmTrace conjecture;
	private LmTrace driver;

	public InconsistancyWithConjectureException(LmTrace conjecture,
			LmTrace driver) {
		this.conjecture = conjecture;
		this.driver = driver;
	}

	public String toString() {
		return "Inconsistancy between trace and conjecture. We expected "
				+ conjecture + " and we get " + driver;
	}

}
