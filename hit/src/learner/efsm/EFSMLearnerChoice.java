package learner.efsm;

import learner.efsm.table.LiOptions;
import options.GenericOneArgChoiceOption;
import options.OptionCategory;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class EFSMLearnerChoice
		extends GenericOneArgChoiceOption<EFSMLearnerItem> {
	public final LiOptions lilearner = new LiOptions(this);

	public EFSMLearnerChoice() {
		super("--algo", "EFSM learner", "Select EFSM learner.");
		setCategory(OptionCategory.INFERENCE);
		addChoice(lilearner);
	}

	@Override
	protected ArgumentDescriptor makeArgumentDescriptor(String argument) {
		return new ArgumentDescriptor(AcceptedValues.ONE, argument, this) {
			@Override
			public String getHelpDisplay() {
				assert super.getHelpDisplay()
						.equals(name + "=<>") : "need update for consistence";
				return name + "=<name>";
			}
		};
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg.name.equals("--algo");
		return "Set learner algorithm.";
	}
}
