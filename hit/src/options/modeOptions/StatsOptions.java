package options.modeOptions;

import main.simpa.Options.LogLevel;
import options.IntegerOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.outputOptions.OutputOptions;

public class StatsOptions extends MultiArgChoiceOptionItem {
	public final IntegerOption inferenceNb;
	public final OutputOptions outputOptions;

	StatsOptions(MultiArgChoiceOption parent) {
		super("statistics mode", "--stats", parent);
		inferenceNb = new IntegerOption("--nbtest", "number of inferences",
				"Number of inferences to try.", 1);
		subTrees.add(inferenceNb);

		// outputs options are NOT added in subtree to ensure user cannot change
		// it.
		outputOptions = new OutputOptions();
		outputOptions.logLevel.selectChoice(
				outputOptions.logLevel.getItemForLevel(LogLevel.LOW));
		outputOptions.htmlLoggerOption.setEnabled(false);
		outputOptions.textLoggerOption.setEnabled(false);
	}

}
