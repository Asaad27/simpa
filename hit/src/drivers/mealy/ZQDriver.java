package drivers.mealy;

import examples.mealy.ZQPaper;

public class ZQDriver extends MealyDriver{

	public ZQDriver() {
		super(ZQPaper.getAutomata());
	}
}
