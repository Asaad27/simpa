/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package learner.mealy.combinatorial;

import drivers.mealy.MealyDriver;

public class CutterCombinatorialStatsEntry extends CombinatorialStatsEntry {

	public CutterCombinatorialStatsEntry(String line) {
		super(line);
	}

	protected CutterCombinatorialStatsEntry(MealyDriver d,
			CombinatorialOptions options) {
		super(d, options);
		traceLength = 0;
	}

	public void addTraceLength(int l){
		traceLength += l;
	}
}
