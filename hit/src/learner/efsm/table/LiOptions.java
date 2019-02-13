package learner.efsm.table;

import datamining.SupportMinOption;
import drivers.efsm.EFSMDriver;
import learner.Learner;
import learner.efsm.EFSMLearnerItem;
import options.GenericMultiArgChoiceOption;

public class LiOptions extends EFSMLearnerItem {
	public final WekaOption useWeka = new WekaOption();
	public final SupportMinOption supportMin = new SupportMinOption();

	public LiOptions(GenericMultiArgChoiceOption<?> parent) {
		super("li", "--li", parent);
		subTrees.add(useWeka);
		subTrees.add(supportMin);
	}

	@Override
	public Learner getLearner(EFSMDriver d) {
		return new LiLearner(d, this);
	}

}
