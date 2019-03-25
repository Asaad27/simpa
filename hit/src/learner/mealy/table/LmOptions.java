package learner.mealy.table;

import options.GenericChoiceOption;
import options.OneArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class LmOptions extends OneArgChoiceOptionItem {
	public OracleOption oracle;

	public LmOptions(GenericChoiceOption<?> parent) {
		super("Mealy Shabaz algorithm (Lm)", "MLm", parent);
		oracle = new OracleOption(true);
		subTrees.add(oracle);
	}
}
