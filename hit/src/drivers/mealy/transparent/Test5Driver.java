package drivers.mealy.transparent;

 import examples.mealy.Test5Mealy;

public class Test5Driver extends TransparentMealyDriver {

	public Test5Driver() {
		super(Test5Mealy.getAutomata());
	}
}
