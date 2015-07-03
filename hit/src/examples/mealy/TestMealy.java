package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class TestMealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test");
		State s1 = test.addState(true);
		State s2 = test.addState();
		State s3 = test.addState();

		test.addTransition(new MealyTransition(test, s1, s2, "a", "1"));
		test.addTransition(new MealyTransition(test, s1, s3, "b", "1"));

		test.addTransition(new MealyTransition(test, s2, s1, "a", "0"));
		test.addTransition(new MealyTransition(test, s2, s1, "b", "0"));

		test.addTransition(new MealyTransition(test, s3, s2, "a", "0"));
		test.addTransition(new MealyTransition(test, s3, s1, "b", "1"));

		test.exportToDot();

		test = CounterMealy.addCounter(test, s1, s2, "i", 2);
		test = CounterMealy.addCounter(test, s3, s2, "j", 3);
		return test;
	}
}
