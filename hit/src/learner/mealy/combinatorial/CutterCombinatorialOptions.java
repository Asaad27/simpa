package learner.mealy.combinatorial;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;

public class CutterCombinatorialOptions extends MultiArgChoiceOptionItem {
	public final BooleanOption interactive = new BooleanOption(
			"interactive pruning", "interactive-pruning",
			"prompt to select pruning sqequences");

	public CutterCombinatorialOptions(GenericMultiArgChoiceOption<?> parent) {
		super("combinatorial with cutting", "--cutCombinatorial", parent);
		subTrees.add(interactive);
	}

	public boolean isInteractive() {
		return interactive.isEnabled();
	}
}
