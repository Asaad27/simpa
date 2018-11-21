package options.modeOptions;

import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.outputOptions.OutputOptions;

public class SimpleLearnOption extends MultiArgChoiceOptionItem {
	public final OutputOptions outputOptions;

	SimpleLearnOption(MultiArgChoiceOption parent) {
		super("simple learn mode", "--simpleLearn", parent);
		outputOptions = new OutputOptions();
		subTrees.add(outputOptions);
	}

}
