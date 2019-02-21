package learner.mealy.localizerBased;

import drivers.mealy.MealyDriver;
import java.util.Collections;
import java.util.List;

import automata.mealy.InputSequence;

import java.util.Arrays;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.WSetOption;
import options.learnerOptions.StateBoundOption;

public class LocalizerBasedOptions extends MultiArgChoiceOptionItem {
	BooleanOption useSpeedUp;
	private final StateBoundOption stateNumberBound = new StateBoundOption();

	private final BooleanOption computeCharacterizationSet;
	private final WSetOption wSet = new WSetOption();

	public LocalizerBasedOptions(GenericMultiArgChoiceOption<?> parent) {
		super("localizer-based", "--localizerBased", parent);
		useSpeedUp = new BooleanOption("speed up", "speedUp",
				"Use speedUp (deduction from trace based on state incompatibilities)\nthis is usefull if you don't know the real state number but only the bound.");
		subTrees.add(useSpeedUp);
		subTrees.add(stateNumberBound);
		computeCharacterizationSet = new BooleanOption(
				"Compute characterization set", "compute-W-set",
				"compute a characterization set from Glass-Box driver",
				Collections.emptyList(), Arrays.asList(wSet));
		subTrees.add(computeCharacterizationSet);
	}

	public boolean useSpeedUp() {
		return useSpeedUp.isEnabled();
	}

	public int getStateNumberBound() {
		return stateNumberBound.getValue();
	}

	public void updateWithDriver(MealyDriver d) {
		stateNumberBound.updateWithDriver(d);
		if (computeWSet())
			wSet.updateWithDriver(d);
	}

	public boolean computeWSet() {
		return computeCharacterizationSet.isEnabled();
	}

	public List<InputSequence> getWSet() {
		assert !computeWSet();
		return wSet.getValues();
	}
}
