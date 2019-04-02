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
package datamining;

import options.PercentageOption;

public class SupportMinOption extends PercentageOption {

	public SupportMinOption(String argPrefix) {
		super("--" + argPrefix + "minsupport", "Minimal support for relation",
				"Minimal support for relation.", 20);
	}

}
