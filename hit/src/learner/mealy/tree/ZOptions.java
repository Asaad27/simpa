package learner.mealy.tree;

import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class ZOptions extends OneArgChoiceOptionItem {
	public OracleOption oracle;

	public ZOptions(GenericOneArgChoiceOption<?> parent) {
		super("ZQuotient algortihm", "MZQ", parent);
		oracle = new OracleOption(true);
		subTrees.add(oracle);
	}
}
