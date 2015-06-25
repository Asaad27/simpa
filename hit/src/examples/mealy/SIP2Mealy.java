package examples.mealy;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class SIP2Mealy {

	public static Mealy getAutomata() {
		Mealy test = new Mealy("Test");
		State s0 = new State("knownS0",true);
		State s1 = new State("knownS1",false);
		State s2 = new State("knownS2",false);
		State s3 = new State("knownS3",false);
		test.addState(s0);
		test.addState(s1);
		test.addState(s2);
		test.addState(s3);

		test.addTransition(new MealyTransition(test, s0, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s0, s0, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s0, s1, "INVITE", "407"));
		test.addTransition(new MealyTransition(test, s0, s0, "BYE", "482"));
		
		test.addTransition(new MealyTransition(test, s1, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s1, s1, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s1, s2, "INVITE", "200"));
		test.addTransition(new MealyTransition(test, s1, s0, "BYE", "482"));
		
		test.addTransition(new MealyTransition(test, s2, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s2, s2, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s2, s3, "INVITE", "407"));
		test.addTransition(new MealyTransition(test, s2, s2, "BYE", "TIMEOUT"));
		
		test.addTransition(new MealyTransition(test, s3, s0, "REGISTER", "200"));
		test.addTransition(new MealyTransition(test, s3, s3, "ACK", "TIMEOUT"));
		test.addTransition(new MealyTransition(test, s3, s0, "INVITE", "491"));
		test.addTransition(new MealyTransition(test, s3, s2, "BYE", "482"));

		return test;
	}
}
