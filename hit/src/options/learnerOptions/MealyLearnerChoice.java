package options.learnerOptions;

import learner.mealy.combinatorial.CombinatorialOptions;
import learner.mealy.combinatorial.CutterCombinatorialOptions;
import learner.mealy.hW.HWOptions;
import learner.mealy.localizerBased.LocalizerBasedOptions;
import learner.mealy.rivestSchapire.RivestSchapireOptions;
import learner.mealy.table.LmOptions;
import learner.mealy.tree.ZOptions;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;

public class MealyLearnerChoice
		extends GenericOneArgChoiceOption<OneArgChoiceOptionItem> {

	public HWOptions hW;
	public RivestSchapireOptions rivestSchapire;
	public LocalizerBasedOptions localizerBased;
	public ZOptions tree;
	public LmOptions lm;
	public CombinatorialOptions combinatorial;
	public final CutterCombinatorialOptions cutCombinatorial;

	public MealyLearnerChoice() {
		super("--algo", "Mealy learner",
				"The learner to use to infer Mealy systems.");

		hW = new HWOptions(this);

		rivestSchapire = new RivestSchapireOptions(this);
		localizerBased = new LocalizerBasedOptions(this);
		tree = new ZOptions(this);
		lm = new LmOptions(this);
		combinatorial = new CombinatorialOptions(this);
		cutCombinatorial = new CutterCombinatorialOptions(this);

		addChoice(hW);
		addChoice(localizerBased);
		addChoice(rivestSchapire);
		addChoice(tree);
		addChoice(lm);
		addChoice(combinatorial);
		addChoice(cutCombinatorial);
		setDefaultItem(hW);

	}
}
