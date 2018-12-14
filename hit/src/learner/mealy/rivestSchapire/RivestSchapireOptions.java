package learner.mealy.rivestSchapire;

import learner.mealy.table.LmOptions;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.RandomOption;

public class RivestSchapireOptions extends MultiArgChoiceOptionItem {
	private MultiArgChoiceOption subLearnerOption;
	public LmOptions lmOptions;
	public final RandomOption seedForProbabilistic;

	public RivestSchapireOptions(GenericMultiArgChoiceOption<?> parent) {
		super("RS", "--rivestSchapire", parent);
		lmOptions = new LmOptions(subLearnerOption, "--RS-with-lm");
		subLearnerOption = new MultiArgChoiceOption() {
			{
				addChoice(lmOptions);
			}
		};
		subTrees.add(subLearnerOption);

		seedForProbabilistic = new RandomOption(
				"--RS-seed-for-probabilistic-search",
				"seed for the probabilistic search of new h");
		subTrees.add(seedForProbabilistic);
	}
}
