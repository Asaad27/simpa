package drivers.mealy.transparent;

import java.util.Iterator;

import drivers.EnumeratedDriver;
import drivers.ExhaustiveGeneratorOption;
import drivers.mealy.MealyDriver;
import examples.mealy.EnumeratedMealy;
import options.automataOptions.DriverChoice;

public class EnumeratedMealyDriver extends TransparentMealyDriver
		implements EnumeratedDriver {
	EnumeratedMealy automaton;

	EnumeratedMealyDriver(EnumeratedMealy automaton) {
		super(automaton);
		this.automaton = automaton;
	}

	static public class EnumeratedMealyOption
			extends ExhaustiveGeneratorOption<EnumeratedMealyDriver> {

		public EnumeratedMealyOption(
				DriverChoice<? extends MealyDriver> parent) {
			super(parent, EnumeratedMealyDriver.class);
		}

		@Override
		public Iterator<EnumeratedMealyDriver> iterator() {
			return new Iterator<EnumeratedMealyDriver>() {
				EnumeratedMealy.ProducerThread generator = new EnumeratedMealy.ProducerThread();

				@Override
				public boolean hasNext() {
					return generator.hasNext();
				}

				@Override
				public EnumeratedMealyDriver next() {
					return new EnumeratedMealyDriver(generator.next());
				}

			};
		}
	}

	@Override
	public long getSeed() {
		return automaton.getSeed();
	}

}
