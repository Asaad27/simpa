package drivers.mealy.transparent;

import examples.mealy.SIPMealy;

public class SIPDriver extends TransparentMealyDriver {

	public SIPDriver() {
		super(SIPMealy.getAutomata());
	}
}
