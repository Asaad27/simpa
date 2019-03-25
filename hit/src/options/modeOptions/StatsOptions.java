package options.modeOptions;

import java.util.Collections;

import main.simpa.Options.LogLevel;
import options.BooleanOption;
import options.IntegerOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.outputOptions.OutputOptions;

public class StatsOptions extends MultiArgChoiceOptionItem {
	public final IntegerOption inferenceNb;
	public final BooleanOption makeGraphs;
	public final OutputOptions outputOptions;

	StatsOptions(MultiArgChoiceOption parent) {
		super("statistics mode", "--stats", parent);
		inferenceNb = new IntegerOption("--nbtest", "number of inferences",
				"Number of inferences to try.", 1);
		subTrees.add(inferenceNb);
		makeGraphs = new BooleanOption("plot graphs after stats", "Sgraph",
				"Use the stored inference results to plot graphs.",
				Collections.emptyList(), Collections.emptyList(), false);
		subTrees.add(makeGraphs);

		// outputs options are NOT added in subtree to ensure user cannot change
		// it.
		outputOptions = new OutputOptions();
		outputOptions.logLevel.selectChoice(
				outputOptions.logLevel.getItemForLevel(LogLevel.LOW));
		outputOptions.htmlLoggerOption.setEnabled(false);
		outputOptions.textLoggerOption.setEnabled(false);
	}

}
