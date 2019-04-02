/********************************************************************************
 * Copyright (c) 2014,2019 Institut Polytechnique de Grenoble 
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

public class TestLaurent2 {


	public static Mealy getAutomata() {
		Mealy test = new Mealy("TestLaurent2");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();
		State s4 = test.addState();
		State s5 = test.addState();
		State s6 = test.addState();
		State s7 = test.addState();
		State s8 = test.addState();
	
		test.addTransition(new MealyTransition(test, s0, s0, "a", "x"));
		test.addTransition(new MealyTransition(test, s0, s1, "b", "y"));
		test.addTransition(new MealyTransition(test, s0, s1, "c", "y"));

		test.addTransition(new MealyTransition(test, s1, s2, "a", "y"));
		test.addTransition(new MealyTransition(test, s1, s2, "b", "y"));
		test.addTransition(new MealyTransition(test, s1, s0, "c", "y"));

		test.addTransition(new MealyTransition(test, s2, s2, "c", "x"));
		test.addTransition(new MealyTransition(test, s2, s3, "a", "x"));
		test.addTransition(new MealyTransition(test, s2, s4, "b", "x"));

		test.addTransition(new MealyTransition(test, s3, s3, "c", "y"));
		test.addTransition(new MealyTransition(test, s3, s5, "b", "x"));
		test.addTransition(new MealyTransition(test, s3, s6, "a", "z"));
		
		test.addTransition(new MealyTransition(test, s4, s4, "c", "x"));
		test.addTransition(new MealyTransition(test, s4, s7, "b", "x"));
		test.addTransition(new MealyTransition(test, s4, s8, "a", "x"));
		
		test.addTransition(new MealyTransition(test, s5, s5, "c", "z"));
		test.addTransition(new MealyTransition(test, s5, s5, "b", "y"));
		test.addTransition(new MealyTransition(test, s5, s3, "a", "x"));
		
		test.addTransition(new MealyTransition(test, s6, s6, "c", "z"));
		test.addTransition(new MealyTransition(test, s6, s3, "b", "x"));
		test.addTransition(new MealyTransition(test, s6, s3, "a", "z"));
		
		test.addTransition(new MealyTransition(test, s7, s7, "c", "y"));
		test.addTransition(new MealyTransition(test, s7, s7, "b", "x"));
		test.addTransition(new MealyTransition(test, s7, s7, "a", "z"));
		
		test.addTransition(new MealyTransition(test, s8, s8, "c", "z"));
		test.addTransition(new MealyTransition(test, s8, s8, "b", "z"));
		test.addTransition(new MealyTransition(test, s8, s0, "a", "z"));

		

		return test;
	}
}