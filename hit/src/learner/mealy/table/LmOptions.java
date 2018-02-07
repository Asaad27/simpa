package learner.mealy.table;

import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class LmOptions extends MultiArgChoiceOptionItem {
	public OracleOption oracle;

	public LmOptions(GenericMultiArgChoiceOption<?> parent) {
		this(parent, "--lm");
	}

	public LmOptions(GenericMultiArgChoiceOption<?> parent, String argument) {
		super("lm", argument, parent);
		oracle = new OracleOption(true);
		subTrees.add(oracle);
	}
}
