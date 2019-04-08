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
package learner.mealy.tree;

import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.learnerOptions.OracleOption;

public class ZOptions extends OneArgChoiceOptionItem {
	public OracleOption oracle;

	public ZOptions(GenericOneArgChoiceOption<?> parent) {
		super("ZQuotient algortihm", "MZQ", parent);
		oracle = new OracleOption(true);
		subTrees.add(oracle);
	}
}
