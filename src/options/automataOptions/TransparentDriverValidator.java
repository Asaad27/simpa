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
package options.automataOptions;

import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import options.OptionValidator;

public class TransparentDriverValidator extends OptionValidator
		implements PostDriverValidator {
	MealyDriver lastDriver = null;

	@Override
	public void check() {
		clear();
		if (lastDriver == null)
			return;
		if (!(lastDriver instanceof TransparentMealyDriver)) {
			setMessage(
					"This option needs a transparent Mealy driver. (NB: this message is computed with last tried driver)");
			setCriticality(CriticalityLevel.ERROR);
		}
	}

	public void setLastDriver(MealyDriver d) {
		lastDriver = d;
		check();
	}
}
