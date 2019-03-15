package learner.mealy.combinatorial;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;

public class CutterCombinatorialOptions extends CombinatorialOptions {
	public final BooleanOption interactive = new BooleanOption(
			"interactive pruning", "interactive-pruning",
			"Prompt user to select pruning sequences.");

	public CutterCombinatorialOptions(GenericMultiArgChoiceOption<?> parent) {
		super("combinatorial with cutting", "--cutCombinatorial", parent);
		subTrees.add(interactive);
	}

	public boolean isInteractive() {
		return interactive.isEnabled();
	}
}
