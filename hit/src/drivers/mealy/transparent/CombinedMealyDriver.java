package drivers.mealy.transparent;

import examples.mealy.CombinedMealy;
import examples.mealy.CounterMealy;;

public class CombinedMealyDriver extends TransparentMealyDriver {

	public CombinedMealyDriver() {
		super(new CombinedMealy(new CounterMealy(2, "i"),new CounterMealy(3, "j")));
	}
}
