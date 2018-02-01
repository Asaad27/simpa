package options.outputOptions;

import main.simpa.Options.LogLevel;
import options.BooleanOption;
import options.OptionsGroup;

public class OutputOptions extends OptionsGroup {
	public final BooleanOption textLoggerOption = new BooleanOption(
			"text logger", "text", "write output log to a .txt file");
	public final BooleanOption htmlLoggerOption = new BooleanOption(
			"html logger", "html", "write output log to a .html file");
	public final LogLevelOption logLevel = new LogLevelOption();

	public OutputOptions() {
		super("outputs");
		textLoggerOption.setEnabledByDefault(false);
		htmlLoggerOption.setEnabledByDefault(false);
		logLevel.setDefaultItem(logLevel.getItemForLevel(LogLevel.ALL));
		addSubOption(textLoggerOption);
		addSubOption(htmlLoggerOption);
		addSubOption(logLevel);
		validateSubOptions();
	}

}
