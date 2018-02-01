package options.outputOptions;

import options.BooleanOption;
import options.OptionsGroup;

public class OutputOptions extends OptionsGroup {
	public final BooleanOption textLoggerOption = new BooleanOption(
			"text logger", "text", "write output log to a .txt file");
	public final BooleanOption htmlLoggerOption = new BooleanOption(
			"html logger", "html", "write output log to a .html file");

	public OutputOptions() {
		super("outputs");
		addSubOption(textLoggerOption);
		addSubOption(htmlLoggerOption);
		validateSubOptions();
	}

}
