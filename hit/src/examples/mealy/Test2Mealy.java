package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class Test2Mealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test2");
		State s0 = test.addState(true);
		State s1 = test.addState();
		

		test.addTransition(new MealyTransition(test, s0, s1, "a", "0"));
		test.addTransition(new MealyTransition(test, s0, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s1, s1, "a", "1"));
		test.addTransition(new MealyTransition(test, s1, s0, "b", "0"));

		

		return test;
	}
}
