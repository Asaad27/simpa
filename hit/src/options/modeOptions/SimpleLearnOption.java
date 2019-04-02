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

import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.outputOptions.OutputOptions;

public class SimpleLearnOption extends MultiArgChoiceOptionItem {
	public final OutputOptions outputOptions;

	SimpleLearnOption(MultiArgChoiceOption parent) {
		super("simple learn mode", "--simpleLearn", parent);
		outputOptions = new OutputOptions();
		subTrees.add(outputOptions);
	}

}
