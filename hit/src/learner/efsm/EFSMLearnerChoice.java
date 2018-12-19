package learner.efsm;

import learner.efsm.table.LiOptions;
import options.GenericMultiArgChoiceOption;

public class EFSMLearnerChoice
		extends GenericMultiArgChoiceOption<EFSMLearnerItem> {
	LiOptions lilearner = new LiOptions(this);

	public EFSMLearnerChoice() {
		addChoice(lilearner);
	}
}
