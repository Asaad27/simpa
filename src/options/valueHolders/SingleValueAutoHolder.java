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

import options.ParseException;

public class SingleValueAutoHolder<T, H extends ValueHolder<T, T> & Stringifyable>
		extends AutoHolder<T, H> implements Stringifyable {

	public SingleValueAutoHolder(H baseHolder) {
		super(baseHolder);
	}

	@Override
	public void setValueFromString(String strValue) throws ParseException {
		if (strValue.equals("auto") || strValue.isEmpty()) {
			useAuto.setValue(true);
		} else {
			useAuto.setValue(false);
			baseHolder.setValueFromString(strValue);
		}
	}

	@Override
	public String getValueAsString(boolean debug) {
		if (useAutoValue() && !debug)
			return "auto";
		return baseHolder.getValueAsString(debug);
	}
}
