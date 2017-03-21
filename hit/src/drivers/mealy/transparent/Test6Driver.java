package drivers.mealy.transparent;

 import examples.mealy.Test6Mealy;

public class Test6Driver extends TransparentMealyDriver {

	public Test6Driver() {
		super(Test6Mealy.getAutomata());
	}
}
