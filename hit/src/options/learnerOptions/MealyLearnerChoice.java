package options.learnerOptions;

import java.util.ArrayList;
import java.util.List;

import learner.mealy.localizerBased.LocalizerBasedOptions;

import options.BooleanOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;

public class MealyLearnerChoice extends MultiArgChoiceOption {

	public MultiArgChoiceOptionItem hW;
	public MultiArgChoiceOptionItem rivestSchapire;
	public LocalizerBasedOptions localizerBased;
	public MultiArgChoiceOptionItem tree;
	public MultiArgChoiceOptionItem lm;
	public MultiArgChoiceOptionItem combinatorial;
	public MultiArgChoiceOptionItem cutCombinatorial;

	public MealyLearnerChoice() {
		List<OptionTree> subTree;

		subTree = new ArrayList<>();
		subTree.add(new BooleanOption("heuristic add h in W", "addHInW", ""));
		hW = new MultiArgChoiceOptionItem("hW", "--hW", this, subTree);

		rivestSchapire = new MultiArgChoiceOptionItem("RS", "--rivestSchapire",
				this);
		localizerBased = new LocalizerBasedOptions(this);
		tree = new MultiArgChoiceOptionItem("tree", "--tree", this);
		lm = new MultiArgChoiceOptionItem("lm", "--lm", this);
		combinatorial = new MultiArgChoiceOptionItem("combinatorial",
				"--combinatorial", this);
		cutCombinatorial = new MultiArgChoiceOptionItem(
				"combinatorial with cutting", "--cutCombinatorial", this);

		addChoice(hW);
		addChoice(localizerBased);
		addChoice(tree);
		addChoice(lm);
		addChoice(combinatorial);
		addChoice(cutCombinatorial);

	}
}
