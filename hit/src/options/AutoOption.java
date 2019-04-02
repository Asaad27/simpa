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
package options;

import options.valueHolders.SingleValueAutoHolder;
import options.valueHolders.Stringifyable;
import options.valueHolders.ValueHolder;

public class AutoOption<T, B extends ValueHolder<T, T> & Stringifyable>
		extends SingleValueArgumentOption<T, SingleValueAutoHolder<T, B>> {

	private AutoValueValidator<T, B> autoValueValidator;

	public AutoOption(String argument, B holder) {
		super(argument, new SingleValueAutoHolder<T, B>(holder));
		autoValueValidator = new AutoValueValidator<T, B>();
	}

	public boolean useAutoValue() {
		return value.useAutoValue();
	}

	public void setValueAuto(T v) {
		value.setValue(v);
	}

	public void setAutoValueError(String string) {
		autoValueValidator.setError(string);
		validateSelectedTree();
	}

	public void clearAutoValueError() {
		autoValueValidator.clearError();
		validateSelectedTree();
	}
}

class AutoValueValidator<T, H extends ValueHolder<T, T>>
		extends OptionValidator {

	public AutoValueValidator() {
	}

	public void clearError() {
		clear();
	}

	public void setError(String string) {
		setCriticality(CriticalityLevel.WARNING);
		setMessage(string);

	}

	@Override
	public void check() {
	}
}
