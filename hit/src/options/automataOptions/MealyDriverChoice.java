package options.automataOptions;

import drivers.mealy.FromDotMealyDriver;
import drivers.mealy.MealyDriver;
import drivers.mealy.real.SIPDriverIPTel;
import drivers.mealy.transparent.RandomMealyDriver;

public class MealyDriverChoice extends DriverChoice<MealyDriver> {
	DriverChoiceItem<MealyDriver> SIPDriverIpTel = new DriverChoiceItem<MealyDriver>(
			this, SIPDriverIPTel.class);

	public MealyDriverChoice() {
		super(MealyDriver.class);
		addChoice(SIPDriverIpTel);
		addChoice(new DriverChoiceItem<MealyDriver>(this,
				RandomMealyDriver.class));
		addChoice(new FromDotMealyDriver.FromDotChoiceItem(this));
	}

}
