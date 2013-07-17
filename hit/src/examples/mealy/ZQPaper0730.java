package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class ZQPaper0730 {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("ZQPaper0730");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		State s3 = test.addState();
		State s4 = test.addState();
		State s5 = test.addState();
		State s6 = test.addState();
		State s7 = test.addState();

		test.addTransition(new MealyTransition(test, s0, s1, "a", "1"));
		test.addTransition(new MealyTransition(test, s0, s0, "b", "0"));

		test.addTransition(new MealyTransition(test, s1, s2, "a", "2"));
		test.addTransition(new MealyTransition(test, s1, s4, "b", "0"));

		test.addTransition(new MealyTransition(test, s2, s0, "a", "3"));
		test.addTransition(new MealyTransition(test, s2, s3, "b", "0"));

		test.addTransition(new MealyTransition(test, s3, s2, "b", "0"));
		test.addTransition(new MealyTransition(test, s3, s2, "a", "3"));

		test.addTransition(new MealyTransition(test, s4, s2, "a", "2"));
		test.addTransition(new MealyTransition(test, s4, s5, "b", "0"));

		test.addTransition(new MealyTransition(test, s5, s1, "b", "0"));
		test.addTransition(new MealyTransition(test, s5, s6, "a", "2"));

		test.addTransition(new MealyTransition(test, s6, s7, "b", "1"));
		test.addTransition(new MealyTransition(test, s6, s7, "a", "3"));

		test.addTransition(new MealyTransition(test, s7, s3, "b", "0"));
		test.addTransition(new MealyTransition(test, s7, s3, "a", "1"));

		return test;
	}
}
