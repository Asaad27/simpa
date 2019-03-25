package options.learnerOptions;

import java.util.Arrays;

import drivers.mealy.AutomatonMealyDriver;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import options.BooleanOption;
import options.IntegerOption;
import options.OptionCategory;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.OptionValidator;
import options.automataOptions.TransparentDriverValidator;

public class StateBoundOption extends BooleanOption {

	class NegativeValueValidator extends OptionValidator {

		@Override
		public void check() {
		}

	}

	class DriverValidator extends TransparentDriverValidator {

		@Override
		public void check() {
			if (StateBoundOption.this.isEnabled())
				super.check();
			else
				clear();
		}

	}

	DriverValidator driverValidator = new DriverValidator();

	private final IntegerOption stateNumber;
	private final IntegerOption stateOffset;

	public StateBoundOption() {
		super("compute state number bound from driver", "TM_states_bound",
				"Use a glass-box driver to count the states in the automaton.",
				null, null, false);
		setCategory(OptionCategory.ALGO_COMMON);
		stateNumber = new IntegerOption("--M_states_bound",
				"bound on states number",
				"A bound on the number of states which will be found in the driver.",
				1);
		stateOffset = new IntegerOption("--TM_states_bound_offset",
				"offset of state number bound",
				"This offset will be added to the number of state get from driver. It can be positive or negative.",
				0);
		stateOffset.setMinimum(Integer.MIN_VALUE);
		stateOffset.setDefaultValue(0);

		setSubTreeIfFalse(Arrays.asList(stateNumber));
		setSubTreeIfTrue(Arrays.asList(stateOffset));
		addValidator(driverValidator);
		validateSelectedTree();
	}

	public void updateWithDriver(MealyDriver d) {
		if (isEnabled()) {
			if (d instanceof TransparentMealyDriver) {
				int bound = stateOffset.getValue()
						+ ((AutomatonMealyDriver) d).getStateCount();
				if (bound < 1) {
					bound = 1;
				}
				stateNumber.getValueHolder().setValue(bound);
			}
		}
	}

	public int getValue() {
		return stateNumber.getValue();
	}

	@Override
	public String getDisableHelp() {
		return "Manually set the bound of states number.";
	}

	@Override
	protected void makeArgumentDescriptors(String argument) {
		super.makeArgumentDescriptors(argument);
		disableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--M_manual-state-bound", this);
	}
}
