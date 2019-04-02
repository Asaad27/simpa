/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.transparent;

import examples.mealy.CombinedMealy;
import examples.mealy.CounterMealy;
import examples.mealy.RandomMealy;
import tools.StandaloneRandom;

public class RandomAndCounterMealyDriver extends TransparentMealyDriver {

	public RandomAndCounterMealyDriver() {
		super(new CombinedMealy(new CounterMealy(3, "counter"),
				RandomMealy.getConnexRandomMealy(new StandaloneRandom())));// TODO
																			// manage
																			// seed
	}
}
