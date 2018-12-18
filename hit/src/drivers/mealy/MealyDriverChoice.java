package drivers.mealy;

import drivers.ExhaustiveGeneratorOption;
import drivers.mealy.real.SIPDriverIPTel;
import drivers.mealy.real.mqtt.MQTTDriverOption;
import drivers.mealy.transparent.EnumeratedMealyDriver.EnumeratedMealyOption;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentFromDotMealyDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class MealyDriverChoice extends DriverChoice<MealyDriver> {
	DriverChoiceItem<MealyDriver> SIPDriverIpTel = new DriverChoiceItem<MealyDriver>(
			this, SIPDriverIPTel.class);
	public final ExhaustiveGeneratorOption<? extends MealyDriver> exhaustiveDriver = new EnumeratedMealyOption(
			this);

	public MealyDriverChoice() {
		super(MealyDriver.class);
		addChoice(SIPDriverIpTel);
		addChoice(new DriverChoiceItem<MealyDriver>(this,
				RandomMealyDriver.class));
		addChoice(new FromDotMealyDriver.FromDotChoiceItem(this));
		addChoice(new TransparentFromDotMealyDriver.FromDotChoiceItem(this));
		addChoice(exhaustiveDriver);
		addChoice(new MQTTDriverOption(this));
	}

}
