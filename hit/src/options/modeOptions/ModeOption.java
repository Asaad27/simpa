package options.modeOptions;

import java.util.List;

import options.MultiArgChoiceOption;
import options.OptionValidator;
import options.automataOptions.AutomataChoice;

public class ModeOption extends MultiArgChoiceOption {
	public final SimpleLearnOption simple = new SimpleLearnOption(this);
	public final StatsOptions stats = new StatsOptions(this);

	public ModeOption(AutomataChoice automataChoice) {
		super("Inference mode");
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
								.getSelectedItem() == automataChoice.mealyLearnerChoice.combinatorial
						&& automataChoice.mealyLearnerChoice.combinatorial
								.withCut()
						&& automataChoice.mealyLearnerChoice.combinatorial
								.isInteractive()) {
					setCriticality(CriticalityLevel.WARNING);
					setMessage(
							"interactive pruning may produce biased results for stats.");
				}
			}
		});
	}

	@Override
	protected List<ArgumentDescriptor> getHelpArguments() {
		List<ArgumentDescriptor> r = super.getHelpArguments();
		r.remove(simple.argument);
		return r;
	}
}
