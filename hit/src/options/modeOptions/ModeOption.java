package options.modeOptions;

import options.MultiArgChoiceOption;

public class ModeOption extends MultiArgChoiceOption {
	public final StatsOptions stats = new StatsOptions(this);
	public final SimpleLearnOption simple = new SimpleLearnOption(this);

	public ModeOption() {
		addChoice(stats);
		addChoice(simple);
		setDefaultItem(simple);
	}
}
