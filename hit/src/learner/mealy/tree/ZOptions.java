package learner.mealy.tree;

import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class ZOptions extends MultiArgChoiceOptionItem {
	public OracleOption oracle;

	public ZOptions(GenericMultiArgChoiceOption<?> parent) {
		super("tree", "--tree", parent);
		oracle = new OracleOption(true);
		subTrees.add(oracle);
	}
}
