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
package learner.mealy.combinatorial;

import java.util.Arrays;
import java.util.Collections;

import options.BooleanOption;
import options.GenericChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.learnerOptions.OracleOption;

public class CombinatorialOptions extends OneArgChoiceOptionItem {
	private OracleOption oracleOptions = new OracleOption(false);

	public final BooleanOption interactive = new BooleanOption(
			"interactive pruning", "MComb_interactive",
			"Prompt user to select pruning sequences.") {
		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--MComb_no-interactive", this);
		};
	};
	public final BooleanOption withCutting = new BooleanOption(
			"combinatorial with cutting", "MCombCut",
			"Search sequences to cut parts of combinatorial tree.",
			Arrays.asList(interactive), Collections.emptyList(), false) {
		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--MCombNo_Cut", this);
		};
	};

	protected CombinatorialOptions(String name, String argument,
			GenericChoiceOption<?> parent) {
		super(name, argument, parent);
		subTrees.add(oracleOptions);
		subTrees.add(withCutting);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_COMB);
	}

	public CombinatorialOptions(GenericChoiceOption<?> parent) {
		this("combinatorial", "MComb", parent);
	}

	public OracleOption getOracleOption() {
		return oracleOptions;
	}

	public boolean withCut() {
		return withCutting.isEnabled();
	}

	public boolean isInteractive() {
		assert withCutting.isEnabled();
		return interactive.isEnabled();
	}
}
