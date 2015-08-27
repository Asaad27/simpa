package drivers.mealy.transparent;

import examples.mealy.LockerMealy;
import examples.mealy.LockerMealy.OnError;
import examples.mealy.LockerMealy.OutputPolicy;

public class HardRandomLockerMealyDriver extends TransparentMealyDriver {
	public HardRandomLockerMealyDriver(){
		super(LockerMealy.getRandomLockerMealy(OnError.RESET, OutputPolicy.UNLOCK_ONLY));
		automata.exportToDot();
	}
}
