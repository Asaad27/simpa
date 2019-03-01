package learner.efsm.table;

import java.util.Arrays;
import java.util.Collections;

import datamining.SupportMinOption;
import drivers.efsm.EFSMDriver;
import learner.Learner;
import learner.efsm.EFSMLearnerItem;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.RandomOption;

public class LiOptions extends EFSMLearnerItem {
	public final WekaOption useWeka = new WekaOption();
	public final SupportMinOption supportMin = new SupportMinOption();
	public final BooleanOption reuseOpIfNeeded = new BooleanOption(
			"Reuse op if needed", "reuseop",
			"Reuse output parameter for non closed row.",
			Collections.emptyList(), Collections.emptyList(), false);
	public final RandomOption xssSeed = new RandomOption("--xss-seed",
			"random source for xss detector");
	public final BooleanOption xssDetection;

	public LiOptions(GenericMultiArgChoiceOption<?> parent) {
		super("li", "--li", parent);
		xssDetection = new BooleanOption("Detect XSS vulnerability", "xss",
				"Search XSS vulnerabilities during inference.",
				Arrays.asList(xssSeed), Collections.emptyList(), false);
		subTrees.add(useWeka);
		subTrees.add(supportMin);
		subTrees.add(reuseOpIfNeeded);
		subTrees.add(xssDetection);
	}

	@Override
	public Learner getLearner(EFSMDriver d) {
		return new LiLearner(d, this);
	}

}
