package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class Test2Mealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();

		test.addTransition(new MealyTransition(test, s0, s1, "a", "0"));
		test.addTransition(new MealyTransition(test, s0, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s1, s2, "a", "0"));
		test.addTransition(new MealyTransition(test, s1, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s2, s3, "a", "0"));
		test.addTransition(new MealyTransition(test, s2, s1, "b", "0"));

		test.addTransition(new MealyTransition(test, s3, s0, "a", "0"));
		test.addTransition(new MealyTransition(test, s3, s3, "b", "1"));

		return test;
	}
}
