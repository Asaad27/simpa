package options.modeOptions;

import options.MultiArgChoiceOption;
import options.OptionValidator;
import options.automataOptions.AutomataChoice;

public class ModeOption extends MultiArgChoiceOption {
	public final SimpleLearnOption simple = new SimpleLearnOption(this);
	public final StatsOptions stats = new StatsOptions(this);

	public ModeOption(AutomataChoice automataChoice) {
		addChoice(stats);
		addChoice(simple);
		setDefaultItem(simple);
		addValidator(new OptionValidator() {
			@Override
			public void check() {
				clear();
				if (getSelectedItem() == stats
						&& automataChoice
								.getSelectedItem() == automataChoice.mealy
						&& automataChoice.mealyLearnerChoice
								.getSelectedItem() == automataChoice.mealyLearnerChoice.cutCombinatorial
						&& automataChoice.mealyLearnerChoice.cutCombinatorial
								.isInteractive()) {
					setCriticality(CriticalityLevel.WARNING);
					setMessage(
							"interactive pruning may produce biased results for stats.");
				}
			}
		});
	}
}
