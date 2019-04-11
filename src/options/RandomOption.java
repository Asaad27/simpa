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

/**
 * This option is used as a random source. If the user does not provide any
 * value, a random seed is used to initialize the random generator. This seed is
 * used and can be forced by user to reproduce some scenario.
 * 
 * This option is based on {@link options.valueHolders.SeedHolder}
 * 
 * @author Nicolas BREMOND
 *
 */
public class RandomOption extends SingleValueArgumentOption<Long, SeedHolder> {

	/**
	 * @param argument
	 *            the argument to use in CLI.
	 * @param seedUse
	 *            a text describing what the seed is used for (not describing
	 *            the seed itself).
	 */
	public RandomOption(String argument, String seedUse) {
		super(argument, new SeedHolder(seedUse));
		this.description = getValueHolder().getName() + ".";
		assert !this.description.endsWith("..") : "two dots at end of string '"
				+ description + "'";
	}

	/**
	 * Get the random source.
	 * 
	 * @return the random source.
	 */
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
