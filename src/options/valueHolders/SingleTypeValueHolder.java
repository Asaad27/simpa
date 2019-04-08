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

public abstract class SingleTypeValueHolder<T> extends ValueHolder<T, T> {

	SingleTypeValueHolder(String name, String description) {
		super(name, description);
	}

	public SingleTypeValueHolder(String name, String description,
			T startValue) {
		super(name, description, startValue);
	}

	@Override
	protected T InnerToUser(T i) {
		return i;
	}

	@Override
	protected T UserToInnerType(T u, T previous) {
		return u;
	}
}
