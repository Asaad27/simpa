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

public class PercentageOption extends IntegerOption {

	public PercentageOption(String argument, String name, String description,
			int defaultValue) {
		super(argument, name, description, defaultValue);
		setMaximum(100);
		setMinimum(0);
	}

	/**
	 * get the percentage as an integer between 0 and 100.
	 * 
	 * @return an integer between 0 and 100.
	 */
	public int getIntValue() {
		return super.getValue();
	}

	/**
	 * get the percentage as a double between 0 and 1.
	 * 
	 * @return a double between 0 and 1.
	 */
	public double getDoubleValue() {
		return ((float) super.getValue()) / 100.;
	}

	/**
	 * this method is not precise about the return type. use instead
	 * {@link #getIntValue()} or {@link #getDoubleValue()}.
	 */
	@Override
	@Deprecated
	public Integer getValue() {
		throw new RuntimeException();
	}
}
