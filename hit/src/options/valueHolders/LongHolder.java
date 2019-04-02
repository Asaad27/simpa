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
package options.valueHolders;

public class LongHolder extends NumberHolder<Long> {

	public LongHolder(String name, String description, Long defaultValue) {
		super(name, description, defaultValue);
		updateWithValue();
	}

	@Override
	protected Long parse(String s) {
		return Long.valueOf(s);
	}

	@Override
	protected Long toType(int v) {
		return Long.valueOf(v);
	}

	@Override
	protected Long toType(Number v) {
		return v.longValue();
	}

}
