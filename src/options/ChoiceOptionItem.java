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
package options;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent one possible choice in the list of a
 * {@link GenericChoiceOption}.
 */
public class ChoiceOptionItem {
	public final String displayName;
	public List<OptionTree> subTrees = new ArrayList<>();

	public ChoiceOptionItem(String name, GenericChoiceOption<?> parent,
			List<OptionTree> subTrees) {
		this.displayName = name;
		if (subTrees != null)
			this.subTrees = subTrees;
	}

	public String toString() {
		return displayName;
	}
}
