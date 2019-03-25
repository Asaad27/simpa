package learner.mealy.combinatorial;

import options.GenericChoiceOption;
import options.OneArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class CombinatorialOptions extends OneArgChoiceOptionItem {
	private OracleOption oracleOptions = new OracleOption(false);

	protected CombinatorialOptions(String name, String argument,
			GenericChoiceOption<?> parent) {
		super(name, argument, parent);
		subTrees.add(oracleOptions);
	}

	public CombinatorialOptions(GenericChoiceOption<?> parent) {
		this("combinatorial", "MComb", parent);
	}

	public OracleOption getOracleOption() {
		return oracleOptions;
	}
}
