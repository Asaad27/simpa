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
		super("statistics mode", "--Stats", parent);
		inferenceNb = new IntegerOption("--Snbtest", "number of inferences",
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
