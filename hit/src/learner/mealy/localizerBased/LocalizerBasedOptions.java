package learner.mealy.localizerBased;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;

public class LocalizerBasedOptions extends MultiArgChoiceOptionItem {
	BooleanOption useSpeedUp;

	public LocalizerBasedOptions(GenericMultiArgChoiceOption<?> parent) {
		super("localizer-based", "--localizerBased", parent);
		useSpeedUp = new BooleanOption("speed up", "speedUp",
				"Use speedUp (deduction from trace based on state incompatibilities)\nthis is usefull if you don't know the real state number but only the bound.");
		subTrees.add(useSpeedUp);
	}

	public boolean useSpeedUp() {
		return useSpeedUp.isEnabled();
	}
}
