package options.automataOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import drivers.efsm.EFSMDriverChoice;
import drivers.mealy.MealyDriverChoice;
import learner.efsm.EFSMLearnerChoice;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;
import options.learnerOptions.MealyLearnerChoice;

public class AutomataChoice extends MultiArgChoiceOption {
	public MealyLearnerChoice mealyLearnerChoice = new MealyLearnerChoice();
	public MealyDriverChoice mealyDriverChoice = new MealyDriverChoice();
	public MultiArgChoiceOptionItem mealy;
	public EFSMLearnerChoice efsmLearnerChoice = new EFSMLearnerChoice();
	public EFSMDriverChoice efsmDriverChoice = new EFSMDriverChoice();
	public MultiArgChoiceOptionItem efsm;
	public MultiArgChoiceOptionItem scan;

	public AutomataChoice() {
		optionName = "automata type";
		List<OptionTree> subTree;
		subTree = new ArrayList<>();
		subTree.add(mealyLearnerChoice);
		subTree.add(mealyDriverChoice);
		mealy = new MultiArgChoiceOptionItem("Mealy", "--mealy", this, subTree);

		efsm = new MultiArgChoiceOptionItem("EFSM", "--efsm", this,
				Arrays.asList(efsmDriverChoice, efsmLearnerChoice));

		scan = new MultiArgChoiceOptionItem("Scan", "--scan", this);

		addChoice(mealy);
		addChoice(efsm);
		addChoice(scan);
	}
}
