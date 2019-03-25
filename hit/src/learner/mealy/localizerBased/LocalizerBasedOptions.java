package learner.mealy.localizerBased;

import drivers.mealy.MealyDriver;
import java.util.Collections;
import java.util.List;

import automata.mealy.InputSequence;

import java.util.Arrays;
import options.BooleanOption;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.WSetOption;
import options.learnerOptions.StateBoundOption;

public class LocalizerBasedOptions extends OneArgChoiceOptionItem {
	BooleanOption useSpeedUp;
	private final StateBoundOption stateNumberBound = new StateBoundOption();

	private final BooleanOption computeCharacterizationSet;
	private final WSetOption wSet = new WSetOption();

	public LocalizerBasedOptions(GenericOneArgChoiceOption<?> parent) {
		super("localizer-based", "MLocW", parent);
		useSpeedUp = new BooleanOption("speed up", "speedUp",
				"Use speed-up (deduction from trace based on state incompatibilities)\nthis is usefull if you don't know the real state number but only the bound.") {
			@Override
			public String getDisableHelp() {
				return "Disable speed-up.";
			}
		};
		subTrees.add(useSpeedUp);
		subTrees.add(stateNumberBound);
		computeCharacterizationSet = new BooleanOption(
				"Compute characterization set", "compute-W-set",
				"Compute a characterization set from glass-box driver.",
				Collections.emptyList(), Arrays.asList(wSet)) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--given-W-set", this);
			}

			@Override
			public String getDisableHelp() {
				return "Manually enter a W-set.";
			}
		};
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
