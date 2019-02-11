package drivers.mealy;

import examples.mealy.TestMealy;

public class TestDriver extends AutomatonMealyDriver {

	public TestDriver() {
		super(TestMealy.getAutomata());
	}
}
