package drivers.mealy.transparent;

import examples.mealy.CombinedMealy;
import examples.mealy.CounterMealy;
import examples.mealy.RandomMealy;

public class RandomAndCounterMealyDriver extends TransparentMealyDriver {

	public RandomAndCounterMealyDriver() {
		super(new CombinedMealy(new CounterMealy(3, "counter"),RandomMealy.getConnexRandomMealy()));
	}
}
