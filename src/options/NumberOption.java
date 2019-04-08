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

import options.valueHolders.NumberHolder;

public abstract class NumberOption<T extends Number & Comparable<T>, H extends NumberHolder<T>>
		extends SingleValueArgumentOption<T, H> {

	public NumberOption(String argument, H holder) {
		super(argument, holder);
	}

	public void setMaximum(T max) {
		value.setMaximum(max);
	}

	public void setMinimum(T min) {
		value.setMinimum(min);
	}

}
