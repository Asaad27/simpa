/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.mealy.transparent;

import java.util.List;

import tools.StandaloneRandom;
import tools.Utils;
import automata.mealy.Mealy;
import examples.mealy.RandomMealy;
import examples.mealy.RandomMealy.OUTPUT_STYLE;

public class RandomOneOutputDiffMealyDriver extends TransparentMealyDriver {

	public RandomOneOutputDiffMealyDriver() {
		super(RandomMealy.getConnexRandomMealy(new StandaloneRandom(),
				OUTPUT_STYLE.ONE_DIFF_PER_STATE));// option for seed
	}

	public RandomOneOutputDiffMealyDriver(Mealy a) {
		super(a);
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions");
	}

}
