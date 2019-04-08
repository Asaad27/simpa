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
package options;

import options.valueHolders.IntegerHolder;

public class IntegerOption
		extends SingleValueArgumentOption<Integer, IntegerHolder> {

	public IntegerOption(String argument, String name, String description,
			int defaultValue) {
		super(argument, new IntegerHolder(name, description, defaultValue));
	}

	public void setMaximum(int max) {
		value.setMaximum(max);
	}

	public void setMinimum(int min) {
		value.setMinimum(min);
	}

}
