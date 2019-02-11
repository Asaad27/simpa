package drivers.mealy;

import examples.mealy.SIP2Mealy;

public class SIP2Driver extends AutomatonMealyDriver {

	public SIP2Driver() {
		super(SIP2Mealy.getAutomata());
	}
}
