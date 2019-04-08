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
package learner.mealy.table;

import drivers.mealy.MealyDriver;
import learner.mealy.tree.ZStatsEntry;

public class LmStatsEntry extends ZStatsEntry {

	public LmStatsEntry(MealyDriver d, LmOptions options) {
		super(d, options.oracle);
	}

	public LmStatsEntry(String s) {
		super(s);
	}

}
