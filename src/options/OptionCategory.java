/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package options;

public enum OptionCategory {
	GLOBAL("GLOBAL OPTIONS :"),
	INFERENCE("INFERENCE OPTIONS :"),
	DRIVER("DRIVER OPTIONS:"),
	ORACLE("ORACLE OPTIONS :"),
	PER_ALGO("SPECIFIC OPTIONS per ALGORITHM"),
	ALGO_LI("OPTIONS for Li"),
	ALGO_COMB("OPTIONS for Combinatorial"),
	ALGO_RS("OPTIONS for Rivest&Schapire"),
	ALGO_LOCW("OPTIONS for LocW algorithm"),
	ALGO_HW("OPTIONS for hW-inference"),
	ALGO_COMMON("OPTIONS common to several algorithms :"),
	STATS("Specific STAT OPTIONS :"),

	;
	String title;

	private OptionCategory(String title) {
		this.title = title;
	}
}
