package learner.mealy.localizerBased;

import drivers.mealy.MealyDriver;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.learnerOptions.StateBoundOption;

public class LocalizerBasedOptions extends MultiArgChoiceOptionItem {
	BooleanOption useSpeedUp;
	private final StateBoundOption stateNumberBound = new StateBoundOption();

	public LocalizerBasedOptions(GenericMultiArgChoiceOption<?> parent) {
		super("localizer-based", "--localizerBased", parent);
		useSpeedUp = new BooleanOption("speed up", "speedUp",
				"Use speedUp (deduction from trace based on state incompatibilities)\nthis is usefull if you don't know the real state number but only the bound.");
		subTrees.add(useSpeedUp);
		subTrees.add(stateNumberBound);
	}

	public boolean useSpeedUp() {
		return useSpeedUp.isEnabled();
	}

	public int getStateNumberBound() {
		return stateNumberBound.getValue();
	}

	public void updateWithDriver(MealyDriver d) {
		stateNumberBound.updateWithDriver(d);
	}
}
