package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class TestMealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();
		
		test.addTransition(new MealyTransition(test, s0, s1, "a", "x"));
		test.addTransition(new MealyTransition(test, s0, s3, "b", "x"));
		
		test.addTransition(new MealyTransition(test, s1, s1, "a", "y"));
		test.addTransition(new MealyTransition(test, s1, s2, "b", "x"));
		
		test.addTransition(new MealyTransition(test, s2, s3, "a", "x"));
		test.addTransition(new MealyTransition(test, s2, s3, "b", "x"));
		
		test.addTransition(new MealyTransition(test, s3, s0, "a", "x"));
		test.addTransition(new MealyTransition(test, s3, s0, "b", "x"));
		
		return test;
	}
}
