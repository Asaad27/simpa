package drivers.mealy;

import examples.mealy.TestMealy;

public class TestDriver extends MealyDriver {

	public TestDriver() {
		super(TestMealy.getAutomata());
	}
}
