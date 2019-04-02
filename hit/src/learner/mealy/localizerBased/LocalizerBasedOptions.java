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
package learner.mealy.localizerBased;

import drivers.mealy.MealyDriver;
import java.util.Collections;
import java.util.List;

import automata.mealy.InputSequence;

import java.util.Arrays;
import options.BooleanOption;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.WSetOption;
import options.learnerOptions.StateBoundOption;

public class LocalizerBasedOptions extends OneArgChoiceOptionItem {
	BooleanOption useSpeedUp;
	private final StateBoundOption stateNumberBound = new StateBoundOption();

	private final BooleanOption computeCharacterizationSet;
	private final WSetOption wSet = new WSetOption();

	public LocalizerBasedOptions(GenericOneArgChoiceOption<?> parent) {
		super("localizer-based", "MLocW", parent);
		useSpeedUp = new BooleanOption("speed up", "MLocW_exact_bound",
				"Use speed-up (deduction from trace based on state incompatibilities).") {
			@Override
			public String getDisableHelp() {
				return "Disable speed-up." + System.lineSeparator()
						+ "This is usefull if you don't know the real state number but only the bound.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--LocW_no-speed-up", this);
			}
		};
		subTrees.add(useSpeedUp);
		subTrees.add(stateNumberBound);
		computeCharacterizationSet = new BooleanOption(
				"Compute characterization set", "TMLocW",
				"Compute a characterization set from glass-box driver.",
				Collections.emptyList(), Arrays.asList(wSet), false) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--MLocW_given_W_set", this);
			}

			@Override
			public String getDisableHelp() {
				return "Manually enter a W-set.";
			}
		};
		subTrees.add(computeCharacterizationSet);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_LOCW);
	}

	public boolean useSpeedUp() {
		return useSpeedUp.isEnabled();
	}

	public int getStateNumberBound() {
		return stateNumberBound.getValue();
	}

	public void updateWithDriver(MealyDriver d) {
		stateNumberBound.updateWithDriver(d);
		if (computeWSet())
			wSet.updateWithDriver(d);
	}

	public boolean computeWSet() {
		return computeCharacterizationSet.isEnabled();
	}

	public List<InputSequence> getWSet() {
		assert !computeWSet();
		return wSet.getValues();
	}
}
