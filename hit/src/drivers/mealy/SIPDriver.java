package drivers.mealy;

import examples.mealy.SIPMealy;

public class SIPDriver extends AutomatonMealyDriver {

	public SIPDriver() {
		super(SIPMealy.getAutomata());
	}
}
