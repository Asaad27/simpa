package options.automataOptions;

import java.util.ArrayList;
import java.util.List;

import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;
import options.learnerOptions.MealyLearnerChoice;

public class AutomataChoice extends MultiArgChoiceOption {
	public MealyLearnerChoice mealyLearnerChoice = new MealyLearnerChoice();
	public MealyDriverChoice mealyDriverChoice = new MealyDriverChoice();
	public MultiArgChoiceOptionItem mealy;
	public MultiArgChoiceOptionItem efsm;
	public MultiArgChoiceOptionItem scan;

	public AutomataChoice() {
		List<OptionTree> subTree;
		subTree = new ArrayList<>();
		subTree.add(mealyLearnerChoice);
		subTree.add(mealyDriverChoice);
		mealy = new MultiArgChoiceOptionItem("Mealy", "--mealy", this, subTree);

		efsm = new MultiArgChoiceOptionItem("EFSM", "--efsm", this);

		scan = new MultiArgChoiceOptionItem("Scan", "--scan", this);

		addChoice(mealy);
		addChoice(efsm);
		addChoice(scan);
	}
}
