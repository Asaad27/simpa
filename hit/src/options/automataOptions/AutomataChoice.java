package options.automataOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import drivers.efsm.EFSMDriverChoice;
import drivers.mealy.MealyDriverChoice;
import learner.efsm.EFSMLearnerChoice;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.learnerOptions.MealyLearnerChoice;

public class AutomataChoice extends MultiArgChoiceOption {
	public MealyLearnerChoice mealyLearnerChoice = new MealyLearnerChoice();
	public MealyDriverChoice mealyDriverChoice = new MealyDriverChoice();
	public MultiArgChoiceOptionItem mealy;
	public EFSMLearnerChoice efsmLearnerChoice = new EFSMLearnerChoice();
	public EFSMDriverChoice efsmDriverChoice = new EFSMDriverChoice();
	public MultiArgChoiceOptionItem efsm;

	public AutomataChoice() {
		super("Automata type");
		setCategory(OptionCategory.GLOBAL);
		List<OptionTree> subTree;
		subTree = new ArrayList<>();
		subTree.add(mealyLearnerChoice);
		subTree.add(mealyDriverChoice);
		mealy = new MultiArgChoiceOptionItem("Mealy", "--mealy", this, subTree);

		efsm = new MultiArgChoiceOptionItem("EFSM", "--efsm", this,
				Arrays.asList(efsmDriverChoice, efsmLearnerChoice));

		addChoice(mealy);
		addChoice(efsm);
		setDefaultItem(mealy);
	}

	public OptionTree getDriverOptions() {
		if (getSelectedItem() == mealy)
			return mealyDriverChoice;
		else {
			assert getSelectedItem() == efsm;
			return efsmLearnerChoice;
		}
	}
}
