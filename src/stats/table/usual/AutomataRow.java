/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package stats.table.usual;

import stats.attribute.Attribute;

public class AutomataRow extends AttributeTableRow<String> {

	public AutomataRow(String header, String attributeValue) {
		super(header, Attribute.AUTOMATA, attributeValue);
	}

}
