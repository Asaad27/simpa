package drivers.mealy.transparent;

import examples.mealy.CombinedMealy;
import examples.mealy.CounterMealy;
import examples.mealy.RandomMealy;
import tools.StandaloneRandom;

public class RandomAndCounterMealyDriver extends TransparentMealyDriver {

	public RandomAndCounterMealyDriver() {
		super(new CombinedMealy(new CounterMealy(3, "counter"),
				RandomMealy.getConnexRandomMealy(new StandaloneRandom())));// TODO
																			// manage
																			// seed
	}
}
