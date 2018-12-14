package drivers.mealy.transparent;

import examples.mealy.LockerMealy;
import examples.mealy.LockerMealy.OnError;
import examples.mealy.LockerMealy.OutputPolicy;

public class EasyRandomLockerMealyDriver extends TransparentMealyDriver {
	public EasyRandomLockerMealyDriver(){
		super(LockerMealy.getRandomLockerMealy(OnError.STAY_IN_PLACE,
				OutputPolicy.UNLOCK_GOOD_BAD, null));
		automata.exportToDot();
	}
}
