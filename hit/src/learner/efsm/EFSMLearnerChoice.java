package learner.efsm;

import learner.efsm.table.LiOptions;
import options.GenericOneArgChoiceOption;
import options.OptionCategory;

public class EFSMLearnerChoice
		extends GenericOneArgChoiceOption<EFSMLearnerItem> {
	public final LiOptions lilearner = new LiOptions(this);

	public EFSMLearnerChoice() {
		super("--algo", "EFSM learner", "Select EFSM learner.");
		setCategory(OptionCategory.INFERENCE);
		addChoice(lilearner);
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg.name.equals("--algo");
		return "Set learner algorithm.";
	}
}
