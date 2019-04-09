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

import options.valueHolders.SeedHolder;
import options.valueHolders.SingleValueAutoHolder;
import tools.RandomGenerator;

public class RandomOption extends SingleValueArgumentOption<Long, SeedHolder> {

	public RandomOption(String argument, String name) {
		super(argument, new SeedHolder(name));
		this.description = getValueHolder().getName() + ".";
		assert !this.description.endsWith("..") : "two dots at end of string '"
				+ description + "'";
	}

	public RandomGenerator getRand() {
		return value;
	}

	@Override
	protected ArgumentValue getDefaultValue() {
		ArgumentValue value = new ArgumentValue(argument);
		value.addValue(SingleValueAutoHolder.AUTO_VALUE);
		return value;
	}
}
