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
package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class SIPMealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();

		test.addTransition(new MealyTransition(test, s0, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s0, s0, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s0, s1, "INVITE", "407"));
		test.addTransition(new MealyTransition(test, s0, s1, "BYE", "407"));
		
		test.addTransition(new MealyTransition(test, s1, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s1, s1, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s1, s2, "INVITE", "200"));
		test.addTransition(new MealyTransition(test, s1, s1, "BYE", "407"));
		
		test.addTransition(new MealyTransition(test, s2, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s2, s2, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s2, s3, "INVITE", "407"));
		test.addTransition(new MealyTransition(test, s2, s3, "BYE", "407"));
		
		test.addTransition(new MealyTransition(test, s3, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s3, s3, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s3, s2, "INVITE", "482"));
		test.addTransition(new MealyTransition(test, s3, s3, "BYE", "407"));

		return test;
	}
}
