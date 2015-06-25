package drivers.mealy.transparent;

import examples.mealy.TestMealy;

public class TestDriver extends TransparentMealyDriver {

	public TestDriver() {
		super(TestMealy.getAutomata());
	}
}
