package drivers.mealy;

import examples.mealy.SIPMealy;

public class SIPDriver extends MealyDriver {

	public SIPDriver() {
		super(SIPMealy.getAutomata());
	}
}
