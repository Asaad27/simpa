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

import java.util.List;

import options.OptionTree.ArgumentDescriptor;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class MultiArgChoiceOptionItem extends ChoiceOptionItem {
	public MultiArgChoiceOptionItem(String name, String argument,
			GenericMultiArgChoiceOption<?> parent) {
		this(name, argument, parent, null);
	}

	public MultiArgChoiceOptionItem(String name, String argument,
			GenericMultiArgChoiceOption<?> parent, List<OptionTree> subTree) {
		super(name, parent, subTree);
		assert argument.startsWith("-");
		this.argument = new ArgumentDescriptor(AcceptedValues.NONE, argument,
				parent);
	}

	public final ArgumentDescriptor argument;

}
