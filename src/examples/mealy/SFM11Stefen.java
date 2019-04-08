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
 ********************************************************************************/
package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class SFM11Stefen {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("SFM11Stefen");
		State a = test.addState(true);
		State b = test.addState();
		State c = test.addState();
		State d = test.addState();
		State e = test.addState();
		State f = test.addState();

		test.addTransition(new MealyTransition(test, a, f, "button", "err"));
		test.addTransition(new MealyTransition(test, a, a, "clean", "ok"));
		test.addTransition(new MealyTransition(test, a, c, "water", "ok"));
		test.addTransition(new MealyTransition(test, a, b, "pod", "ok"));

		test.addTransition(new MealyTransition(test, b, f, "button", "err"));
		test.addTransition(new MealyTransition(test, b, a, "clean", "ok"));
		test.addTransition(new MealyTransition(test, b, b, "pod", "ok"));
		test.addTransition(new MealyTransition(test, b, d, "water", "ok"));

		test.addTransition(new MealyTransition(test, c, f, "button", "err"));
		test.addTransition(new MealyTransition(test, c, a, "clean", "ok"));
		test.addTransition(new MealyTransition(test, c, d, "pod", "ok"));
		test.addTransition(new MealyTransition(test, c, c, "water", "ok"));

		test.addTransition(new MealyTransition(test, d, e, "button", "coffee"));
		test.addTransition(new MealyTransition(test, d, a, "clean", "ok"));
		test.addTransition(new MealyTransition(test, d, d, "pod", "ok"));
		test.addTransition(new MealyTransition(test, d, d, "water", "ok"));

		test.addTransition(new MealyTransition(test, e, f, "button", "err"));
		test.addTransition(new MealyTransition(test, e, a, "clean", "ok"));
		test.addTransition(new MealyTransition(test, e, f, "pod", "err"));
		test.addTransition(new MealyTransition(test, e, f, "water", "err"));

		test.addTransition(new MealyTransition(test, f, f, "button", "err"));
		test.addTransition(new MealyTransition(test, f, f, "clean", "err"));
		test.addTransition(new MealyTransition(test, f, f, "pod", "err"));
		test.addTransition(new MealyTransition(test, f, f, "water", "err"));

		return test;
	}
}
