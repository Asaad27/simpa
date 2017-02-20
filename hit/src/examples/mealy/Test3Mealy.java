package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class Test3Mealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test_6_States");

		State s0 = new State("S0", true);
		State s1 = new State("S1", false);
		State s2 = new State("S2", false);
		State s3 = new State("S3", false);
		State s4 = new State("S4", false);
		State s5 = new State("S5", false);

		test.addState(s0);
		test.addState(s1);
		test.addState(s2);
		test.addState(s3);
		test.addState(s4);
		test.addState(s5);

		/*
		 * test.addTransition(new MealyTransition(test, s1, s2, "a", "0"));
		 * test.addTransition(new MealyTransition(test, s1, s3, "b", "1"));
		 * 
		 * test.addTransition(new MealyTransition(test, s2, s3, "a", "1"));
		 * test.addTransition(new MealyTransition(test, s2, s1, "b", "1"));
		 * 
		 * test.addTransition(new MealyTransition(test, s3, s3, "a", "1"));
		 * test.addTransition(new MealyTransition(test, s3, s2, "b", "1"));
		 */

		test.addTransition(new MealyTransition(test, s0, s1, "a", "0"));
		test.addTransition(new MealyTransition(test, s0, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s1, s2, "a", "1"));
		test.addTransition(new MealyTransition(test, s1, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s2, s3, "a", "0"));
		test.addTransition(new MealyTransition(test, s2, s3, "b", "0"));

		test.addTransition(new MealyTransition(test, s3, s4, "a", "1"));
		test.addTransition(new MealyTransition(test, s3, s4, "b", "0"));

		test.addTransition(new MealyTransition(test, s4, s5, "a", "0"));
		test.addTransition(new MealyTransition(test, s4, s5, "b", "1"));

		test.addTransition(new MealyTransition(test, s5, s0, "a", "1"));
		test.addTransition(new MealyTransition(test, s5, s0, "b", "0"));

		return test;
	}
}
