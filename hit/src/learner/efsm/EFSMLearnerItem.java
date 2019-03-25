package learner.efsm;

import drivers.efsm.EFSMDriver;
import learner.Learner;
import options.OneArgChoiceOptionItem;

public abstract class EFSMLearnerItem extends OneArgChoiceOptionItem {

	public EFSMLearnerItem(String name, String argument,
			EFSMLearnerChoice parent) {
		super(name, argument, parent);
	}

	public abstract Learner getLearner(EFSMDriver d);
}
