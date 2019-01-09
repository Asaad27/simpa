package options.outputOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import main.simpa.Options.LogLevel;
import options.BooleanOption;
import options.FileOption;
import options.FileOption.FileExistance;
import options.FileOption.FileSelectionMode;
import options.OptionsGroup;

public class OutputOptions extends OptionsGroup {
	public final BooleanOption textLoggerOption = new BooleanOption(
			"text logger", "text", "write output log to a .txt file");
	public final BooleanOption autoOpenHTML = new BooleanOption(
			"open html log after learning", "openhtml",
			"automatically open the HTML log at end of learning");
	public final BooleanOption htmlLoggerOption = new BooleanOption(
			"html logger", "html", "write output log to a .html file",
			Arrays.asList(autoOpenHTML), new ArrayList<>());
	public final LogLevelOption logLevel = new LogLevelOption();
	public final FileOption outputDir;

	public OutputOptions() {
		super("outputs");
		textLoggerOption.setEnabledByDefault(false);
		htmlLoggerOption.setEnabledByDefault(false);
		autoOpenHTML.setEnabledByDefault(false);
		logLevel.setDefaultItem(logLevel.getItemForLevel(LogLevel.ALL));
		addSubOption(textLoggerOption);
		addSubOption(htmlLoggerOption);
		addSubOption(logLevel);
		outputDir = new FileOption("--outdir", "output directory",
				new File(System.getProperty("user.dir")),
				FileSelectionMode.DIRECTORIES_ONLY, FileExistance.NO_CHECK);
		// TODO add an option validator to indicate when the "out" directory
		// will be overwritten
		addSubOption(outputDir);
		validateSubOptions();
	}

}
