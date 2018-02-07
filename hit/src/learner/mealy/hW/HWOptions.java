package learner.mealy.hW;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class HWOptions extends MultiArgChoiceOptionItem {
	BooleanOption addHInW;
	public OracleOption oracle;

	public HWOptions(GenericMultiArgChoiceOption<?> parent) {
		super("hW", "--hW", parent);
		addHInW = new BooleanOption("heuristic add h in W", "addHInW", "");
		oracle = new OracleOption(false);
		subTrees.add(oracle);
		subTrees.add(addHInW);
	}

}
