package drivers.mealy;

import examples.mealy.Test2Mealy;

public class Test2Driver extends MealyDriver{

	public Test2Driver() {
		super(Test2Mealy.getAutomata());
	}
}
