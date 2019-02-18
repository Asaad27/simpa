package options.automataOptions;

import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import options.OptionValidator;

public class TransparentDriverValidator extends OptionValidator {
	MealyDriver lastDriver = null;

	@Override
	public void check() {
		clear();
		if (lastDriver == null)
			return;
		if (!(lastDriver instanceof TransparentMealyDriver)) {
			setMessage(
					"This option needs a transparent Mealy driver. (NB: this message is computed with last tried driver)");
			setCriticality(CriticalityLevel.WARNING);
		}
	}

	public void setLastDriver(MealyDriver d) {
		lastDriver = d;
		check();
	}
}
