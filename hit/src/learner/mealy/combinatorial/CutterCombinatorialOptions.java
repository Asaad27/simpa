package learner.mealy.combinatorial;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;

public class CutterCombinatorialOptions extends MultiArgChoiceOptionItem {
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
