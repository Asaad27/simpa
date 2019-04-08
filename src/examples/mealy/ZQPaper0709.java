/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
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

public class ZQPaper0709 {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("ZQPaper0709");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();

		test.addTransition(new MealyTransition(test, s0, s1, "a", "x"));
		test.addTransition(new MealyTransition(test, s0, s3, "b", "x"));
		test.addTransition(new MealyTransition(test, s0, s1, "c", "x"));

		test.addTransition(new MealyTransition(test, s1, s1, "a", "y"));
		test.addTransition(new MealyTransition(test, s1, s2, "b", "x"));
		test.addTransition(new MealyTransition(test, s1, s2, "c", "x"));

		test.addTransition(new MealyTransition(test, s2, s3, "a", "x"));
		test.addTransition(new MealyTransition(test, s2, s3, "b", "x"));
		test.addTransition(new MealyTransition(test, s2, s2, "c", "y"));

		test.addTransition(new MealyTransition(test, s3, s0, "a", "x"));
		test.addTransition(new MealyTransition(test, s3, s0, "b", "x"));
		test.addTransition(new MealyTransition(test, s3, s0, "c", "x"));

		return test;
	}
}
