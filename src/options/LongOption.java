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

import options.valueHolders.LongHolder;

public class LongOption extends NumberOption<Long, LongHolder> {

	public LongOption(String argument, String name, String description,
			Long defaultValue) {
		super(argument, new LongHolder(name, description, defaultValue));
	}

}
