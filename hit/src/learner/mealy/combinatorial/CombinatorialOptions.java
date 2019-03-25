package learner.mealy.combinatorial;

import java.util.Arrays;
import java.util.Collections;

import options.BooleanOption;
import options.GenericChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.learnerOptions.OracleOption;

public class CombinatorialOptions extends OneArgChoiceOptionItem {
	private OracleOption oracleOptions = new OracleOption(false);

	public final BooleanOption interactive = new BooleanOption(
			"interactive pruning", "MComb_interactive",
			"Prompt user to select pruning sequences.") {
		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--MComb_no-interactive", this);
		};
	};
	public final BooleanOption withCutting = new BooleanOption(
			"combinatorial with cutting", "MCombCut",
			"Search sequences to cut parts of combinatorial tree.",
			Arrays.asList(interactive), Collections.emptyList(), false) {
		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--MCombNo_Cut", this);
		};
	};

	protected CombinatorialOptions(String name, String argument,
			GenericChoiceOption<?> parent) {
		super(name, argument, parent);
		subTrees.add(oracleOptions);
		subTrees.add(withCutting);
	}

	public CombinatorialOptions(GenericChoiceOption<?> parent) {
		this("combinatorial", "MComb", parent);
	}

	public OracleOption getOracleOption() {
		return oracleOptions;
	}

	public boolean withCut() {
		return withCutting.isEnabled();
	}

	public boolean isInteractive() {
		assert withCutting.isEnabled();
		return interactive.isEnabled();
	}
}
