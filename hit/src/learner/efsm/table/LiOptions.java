package learner.efsm.table;

import drivers.Driver;
import learner.Learner;
import learner.efsm.EFSMLearnerItem;
import options.GenericMultiArgChoiceOption;

public class LiOptions extends EFSMLearnerItem {
	public final WekaOption useWeka = new WekaOption();

	public LiOptions(GenericMultiArgChoiceOption<?> parent) {
		super("li", "--li", parent);
		subTrees.add(useWeka);
	}

	@Override
	public Learner getLearner(Driver d) {
		return new LiLearner(d, this);
	}

}
