package drivers.mealy.transparent;

import examples.mealy.CounterMealy;;

public class CounterMealyDriver extends TransparentMealyDriver {

	public CounterMealyDriver() {
		super(new CounterMealy(5, "l"));
	}
}
