package learner.efsm.table;

import java.util.Collections;

import datamining.SupportMinOption;
import drivers.efsm.EFSMDriver;
import learner.Learner;
import learner.efsm.EFSMLearnerItem;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;

public class LiOptions extends EFSMLearnerItem {
	public final WekaOption useWeka = new WekaOption();
	public final SupportMinOption supportMin = new SupportMinOption();
	public final BooleanOption reuseOpIfNeeded = new BooleanOption(
			"Reuse op if needed", "reuseop",
			"Reuse output parameter for non closed row",
			Collections.emptyList(), Collections.emptyList(), false);

	public LiOptions(GenericMultiArgChoiceOption<?> parent) {
		super("li", "--li", parent);
		subTrees.add(useWeka);
		subTrees.add(supportMin);
		subTrees.add(reuseOpIfNeeded);
	}

	@Override
	public Learner getLearner(EFSMDriver d) {
		return new LiLearner(d, this);
	}

}
