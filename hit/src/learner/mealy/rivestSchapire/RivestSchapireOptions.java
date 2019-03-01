package learner.mealy.rivestSchapire;

import java.util.Arrays;
import java.util.Collections;

import drivers.mealy.MealyDriver;
import learner.mealy.table.LmOptions;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.RandomOption;
import options.learnerOptions.StateBoundOption;

public class RivestSchapireOptions extends MultiArgChoiceOptionItem {
	private MultiArgChoiceOption subLearnerOption;
	public LmOptions lmOptions;
	public final RandomOption seedForProbabilistic = new RandomOption(
			"--MRS_probabilistic_seed",
			"the probabilistic search of new homing sequence");
	private final StateBoundOption stateBound = new StateBoundOption();
	private final BooleanOption probabilisticRS = new BooleanOption(
			"start with an empty homing sequence", "MRS_probabilistic",
			"Use probabilistic version of Rivest and Schapire algorithm which computes automatically the homing sequence.",
			Arrays.asList(stateBound, seedForProbabilistic),
			Collections.emptyList()) {
		@Override
		public String getDisableHelp() {
			return "Compute a homing sequence (from glass-box driver) which will be provided to the algorithm.";
		};

		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					ArgumentDescriptor.AcceptedValues.NONE, "--TMRS_init_h",
					this);
		}
	};

	public RivestSchapireOptions(GenericMultiArgChoiceOption<?> parent) {
		super("Rivest and Schapire", "--rivestSchapire", parent);
		lmOptions = new LmOptions(subLearnerOption, "--RS-with-lm");
		subLearnerOption = new MultiArgChoiceOption() {
			{
				addChoice(lmOptions);
				optionName = "sub-learner for Rivest and Schapire algorithm";
			}
		};
		subTrees.add(subLearnerOption);

		subTrees.add(probabilisticRS);
	}

	public void updateWithDriver(MealyDriver driver) {
		stateBound.updateWithDriver(driver);
	}

	public int getStateNumberBound() {
		return stateBound.getValue();
	}

	public boolean probabilisticRS() {
		return probabilisticRS.isEnabled();
	}

}
