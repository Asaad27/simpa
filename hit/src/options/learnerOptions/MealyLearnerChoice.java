package options.learnerOptions;

import learner.mealy.hW.HWOptions;
import learner.mealy.localizerBased.LocalizerBasedOptions;
import learner.mealy.rivestSchapire.RivestSchapireOptions;
import learner.mealy.table.LmOptions;
import learner.mealy.tree.ZOptions;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;

public class MealyLearnerChoice extends MultiArgChoiceOption {

	public HWOptions hW;
	public RivestSchapireOptions rivestSchapire;
	public LocalizerBasedOptions localizerBased;
	public ZOptions tree;
	public LmOptions lm;
	public MultiArgChoiceOptionItem combinatorial;
	public MultiArgChoiceOptionItem cutCombinatorial;

	public MealyLearnerChoice() {

		hW = new HWOptions(this);

		rivestSchapire = new RivestSchapireOptions(this);
		localizerBased = new LocalizerBasedOptions(this);
		tree = new ZOptions(this);
		lm = new LmOptions(this);
		combinatorial = new MultiArgChoiceOptionItem("combinatorial",
				"--combinatorial", this);
		cutCombinatorial = new MultiArgChoiceOptionItem(
				"combinatorial with cutting", "--cutCombinatorial", this);

		addChoice(hW);
		addChoice(localizerBased);
		addChoice(rivestSchapire);
		addChoice(tree);
		addChoice(lm);
		addChoice(combinatorial);
		addChoice(cutCombinatorial);

	}
}
