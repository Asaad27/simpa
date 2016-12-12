package drivers.mealy.transparent;

import examples.mealy.Test3Mealy;

public class Test3Driver extends TransparentMealyDriver {

	public Test3Driver() {
		super(Test3Mealy.getAutomata());
	}
}
