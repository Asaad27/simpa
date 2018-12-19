package learner.efsm.table;

import drivers.Driver;
import learner.Learner;
import learner.efsm.EFSMLearnerItem;
import options.GenericMultiArgChoiceOption;

public class LiOptions extends EFSMLearnerItem {

	public LiOptions(GenericMultiArgChoiceOption<?> parent) {
		super("li", "--li", parent);
	}

	@Override
	public Learner getLearner(Driver d) {
		return new LiLearner(d);
	}
}
