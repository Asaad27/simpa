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
			return new LogLevelOptionItem("Log everything", "all", level,
					parent);
		case DO_NOT_COMPLEXIFY:
			return new LogLevelOptionItem(
					"Log only things which can be logged in a constant-time",
					"do-not-complexify", level, parent);
		case LOW:
			return new LogLevelOptionItem("Log almost nothing", "low", level,
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
