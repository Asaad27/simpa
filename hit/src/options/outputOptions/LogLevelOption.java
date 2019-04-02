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
import options.GenericOneArgChoiceOption;

public class LogLevelOption
		extends GenericOneArgChoiceOption<LogLevelOptionItem> {

	public LogLevelOption() {
		super("--log", "Log level", "Set the logging level.");
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
