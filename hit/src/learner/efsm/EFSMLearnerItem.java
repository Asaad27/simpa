package learner.efsm;

import drivers.Driver;
import learner.Learner;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;

public abstract class EFSMLearnerItem extends MultiArgChoiceOptionItem {

	public EFSMLearnerItem(String name, String argument,
			GenericMultiArgChoiceOption<?> parent) {
		super(name, argument, parent);
	}

	public abstract Learner getLearner(Driver d);
}
