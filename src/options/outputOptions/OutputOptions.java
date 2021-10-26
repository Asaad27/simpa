/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package options.outputOptions;

import main.simpa.Options.LogLevel;
import options.BooleanOption;
import options.FileOption;
import options.FileOption.FileExistance;
import options.FileOption.FileSelectionMode;
import options.OptionsGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class OutputOptions extends OptionsGroup {
	public final BooleanOption textLoggerOption = new BooleanOption(
			"text logger", "text", "Write output log to a '.txt' file.");
	public final BooleanOption htmlLoggerOption = new BooleanOption(
			"html logger", "html", "Write output log to a '.html' file.",
			Collections.emptyList(), new ArrayList<>());
	public final BooleanOption dataLoggerOption = new BooleanOption("raw data logger", "raw", "Log transitions, h, " +
			"W," +
			" intermediate conjecture, and stats in an easy to parse format");
	public final BooleanOption compactLoggerOption = new BooleanOption("compact logger", "compact", "h, W," +
			" intermediate conjecture, and stats in an easy to parse format");
	public final LogLevelOption logLevel = new LogLevelOption();
	public final FileOption outputDir;

	public OutputOptions() {
		super("outputs");
		textLoggerOption.setEnabledByDefault(false);
		htmlLoggerOption.setEnabledByDefault(false);
		dataLoggerOption.setEnabledByDefault(false);
		compactLoggerOption.setEnabledByDefault(false);
		logLevel.setDefaultItem(logLevel.getItemForLevel(LogLevel.ALL));
		addSubOption(textLoggerOption);
		addSubOption(htmlLoggerOption);
		addSubOption(dataLoggerOption);
		addSubOption(compactLoggerOption);
		addSubOption(logLevel);
		outputDir = new FileOption("--outdir", "Output directory.",
				new File(System.getProperty("user.dir")),
				FileSelectionMode.DIRECTORIES_ONLY, FileExistance.NO_CHECK);
		// TODO add an option validator to indicate when the "out" directory
		// will be overwritten
		addSubOption(outputDir);
		validateSubOptions();
	}

}
