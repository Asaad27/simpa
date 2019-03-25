package learner.efsm.table;

import java.util.Arrays;
import java.util.Collections;

import datamining.SupportMinOption;
import drivers.efsm.EFSMDriver;
import learner.Learner;
import learner.efsm.EFSMLearnerChoice;
import learner.efsm.EFSMLearnerItem;
import options.BooleanOption;
import options.OptionCategory;
import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.RandomOption;

public class LiOptions extends EFSMLearnerItem {
	public final WekaOption useWeka = new WekaOption();
	public final SupportMinOption supportMin = new SupportMinOption("ELi_");
	public final BooleanOption reuseOpIfNeeded = new BooleanOption(
			"Reuse op if needed", "ELi_reuse_op",
			"Reuse output parameter for non closed row.",
			Collections.emptyList(), Collections.emptyList(), false) {
		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--ELi_no_reuse_op", this);
		}
	};
	public final RandomOption xssSeed = new RandomOption("--ELi_xss_seed",
			"random source for xss detector");
	public final BooleanOption xssDetection;

	public LiOptions(EFSMLearnerChoice parent) {
		super("Li algorithm", "ELi", parent);
		xssDetection = new BooleanOption("Detect XSS vulnerability", "ELi_xss",
				"Search XSS vulnerabilities during inference.",
				Arrays.asList(xssSeed), Collections.emptyList(), false) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--ELi_no_xss", this);
			}
		};
		subTrees.add(useWeka);
		subTrees.add(supportMin);
		subTrees.add(reuseOpIfNeeded);
		subTrees.add(xssDetection);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_LI);
	}

	@Override
	public Learner getLearner(EFSMDriver d) {
		return new LiLearner(d, this);
	}

}
