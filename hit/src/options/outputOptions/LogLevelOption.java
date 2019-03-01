package options.outputOptions;

import main.simpa.Options.LogLevel;
import options.GenericOneArgChoiceOption;

public class LogLevelOption
		extends GenericOneArgChoiceOption<LogLevelOptionItem> {

	public LogLevelOption() {
		super("--log-level", "Log level");
		this.description = "Set the logging level.";
		for (LogLevel level : LogLevel.values())
			addChoice(LogLevelOptionItem.getItem(level, this));
	}

	public LogLevelOptionItem getItemForLevel(LogLevel level) {
		for (LogLevelOptionItem item : choices)
			if (item.level == level)
				return item;
		return null;
	}

}
