package drivers.mealy;

import examples.mealy.Test2Mealy;
import examples.mealy.Test3;
import examples.mealy.Test4;
import examples.mealy.TestNoDS3;
import examples.mealy.TestNoDS4;
import examples.mealy.TestNoDS8;

public class Test2Driver extends MealyDriver {

	public Test2Driver() {
		super(TestNoDS3.getAutomata());
	}
}
