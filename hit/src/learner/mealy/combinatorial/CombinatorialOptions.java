package learner.mealy.combinatorial;

import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class CombinatorialOptions extends MultiArgChoiceOptionItem {
	private OracleOption oracleOptions = new OracleOption(false);

	protected CombinatorialOptions(String name, String argument,
			GenericMultiArgChoiceOption<?> parent) {
		super(name, argument, parent);
		subTrees.add(oracleOptions);
	}

	public CombinatorialOptions(GenericMultiArgChoiceOption<?> parent) {
		this("combinatorial", "--combinatorial", parent);
	}

	public OracleOption getOracleOption() {
		return oracleOptions;
	}
}
