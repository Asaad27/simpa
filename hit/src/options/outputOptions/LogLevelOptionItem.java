package options.outputOptions;

import main.simpa.Options.LogLevel;
import options.GenericChoiceOption;
import options.OneArgChoiceOptionItem;

public class LogLevelOptionItem extends OneArgChoiceOptionItem {
	public final LogLevel level;

	public static LogLevelOptionItem getItem(LogLevel level,
			GenericChoiceOption<?> parent) {
		switch (level) {
		case ALL:
			return new LogLevelOptionItem("Log everything", "3", level, parent);
		case DO_NOT_COMPLEXIFY:
			return new LogLevelOptionItem(
					"Log only things which can be logged in a constant-time",
					"2", level, parent);
		case LOW:
			return new LogLevelOptionItem("Minimum log level", "1", level,
					parent);
		}
		assert false;
		return null;
	}

	public LogLevelOptionItem(String name, String argValue, LogLevel level,
			GenericChoiceOption<?> parent) {
		super(name, argValue, parent);
		this.level = level;
	}

}
