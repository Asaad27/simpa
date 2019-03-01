package learner.efsm;

import learner.efsm.table.LiOptions;
import options.GenericMultiArgChoiceOption;

public class EFSMLearnerChoice
		extends GenericMultiArgChoiceOption<EFSMLearnerItem> {
	public final LiOptions lilearner = new LiOptions(this);

	public EFSMLearnerChoice() {
		super("EFSM learner");
		addChoice(lilearner);
	}
}
