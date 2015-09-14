package drivers.mealy.transparent;

import examples.mealy.CombinedMealy;
import examples.mealy.CounterMealy;;

public class CounterMealyDriver extends TransparentMealyDriver {

	public CounterMealyDriver() {
		super(new CombinedMealy(new CounterMealy(3, "j"), new CounterMealy(2,"i")));
	}
}
