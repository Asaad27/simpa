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

import options.valueHolders.ValueHolder;

public class OptionsGroup extends NoArgumentOption {
	String groupName;
	List<OptionTree> subOptions = new ArrayList<>();

	public OptionsGroup(String name) {
		this.groupName = name;
	}

	/**
	 * Record sub-option in the group. You should call
	 * {@link validateSubOptions} after adding all your sub-options.
	 * 
	 * @param option
	 *            the option to record in the group.
	 */
	protected void addSubOption(OptionTree option) {
		subOptions.add(option);
	}

	/**
	 * Must be called only one time after adding sub-options.
	 */
	protected void validateSubOptions() {
		assert getSortedChildren().size() == 0;
		addSortedChildren(subOptions);
	}

	@Override
	protected void createMainComponent() {
		mainComponent = null;
		updateSubTreeComponent("options related to " + groupName);
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return subOptions;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert false;
	}

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		return null;
	}

}
