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
package learner.mealy.hW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import drivers.mealy.MealyDriver;
import options.BooleanOption;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.automataOptions.TransparentDriverValidator;
import options.learnerOptions.OracleOption;

public class HWOptions extends OneArgChoiceOptionItem {
	class PreComputedW extends BooleanOption {
		private final TransparentDriverValidator driverValidator = new TransparentDriverValidator() {
			@Override
			public void check() {
				if (PreComputedW.this.isEnabled())
					super.check();
				else
					clear();
			}
		};

		public PreComputedW() {
			super("use a computed W-set", "TMhW_with_computed_W",
					"Compute a W-set before starting inference. This needs a transparent driver.",
					Collections.emptyList(), Collections.emptyList(), false);
			addValidator(driverValidator);
		}

		@Override
		public String getDisableHelp() {
			return "Do not use a glass-box driver to compute a W-set.";
		}

		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--MhW_without_computed_W", this);
		}

		void updateWithDriver(MealyDriver d) {
			driverValidator.setLastDriver(d);
			validateSelectedTree();
		}
	};

	public final BooleanOption addHInW;
	public final BooleanOption useReset;
	public final BooleanOption searchCeInTrace;
	public final BooleanOption checkInconsistenciesHMapping;
	public final BooleanOption useDictionary;
	private final PreComputedW usePrecomputedW;
	private final BooleanOption addIInW;
	private final BooleanOption useAdaptiveH;
	private final BooleanOption useAdaptiveW;

	public final OracleOption getOracleOption() {
		if (useReset.isEnabled())
			return oracleWhenUsingReset;
		else
			return oracleWithoutReset;
	}

	public boolean useAdaptiveH() {
		return !addHInW.isEnabled() && useAdaptiveH.isEnabled();
	}

	public void setUseAdaptiveH(boolean b) {
		if (b)
			addHInW.getValueHolder().setValue(false);
		useAdaptiveH.getValueHolder().setValue(b);
		assert (useAdaptiveH() == b);

	}

	public boolean addIInW() {
		return !useAdaptiveW() && addIInW.isEnabled();
	}

	public boolean useAdaptiveW() {
		return !addHInW.isEnabled() && useAdaptiveW.isEnabled();
	}

	public void setUseAdaptiveW(boolean enable) {
		if (enable)
			addHInW.getValueHolder().setValue(false);
		useAdaptiveW.getValueHolder().setValue(enable);
		assert useAdaptiveW() == enable;
	}

	public boolean usePrecomputedW() {
		return usePrecomputedW.isEnabled();
	}

	public void setUsePrecomputedW(boolean enable) {
		usePrecomputedW.getValueHolder().setValue(enable);
		assert usePrecomputedW() == enable;
	}

	private final OracleOption oracleWhenUsingReset;
	private final OracleOption oracleWithoutReset;

	public HWOptions(GenericOneArgChoiceOption<?> parent) {
		super("hW", "MhW", parent);
		checkInconsistenciesHMapping = new BooleanOption(
				"search 3rd inconsistencies", "MhW_hC_inc",
				"Search inconsistencies between homing sequence and conjecture.",
				Collections.emptyList(), Collections.emptyList(), true) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--MhW_no_hC_inc", this);
			}
		};
		searchCeInTrace = new BooleanOption("search counter example in trace",
				"MhW_trace_ce",
				"Try to execute the traces observed on conjecture to see if it makes a counter example.",
				Collections.emptyList(), Collections.emptyList(), true) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--MhW_no_trace_CE", this);
			}
		};
		useDictionary = new BooleanOption("use dictionary", "Mhw_use_dict",
				"Record the sequences of form 'h z x w' and 'h w' to avoid re-executing them on the SUI.",
				Collections.emptyList(), Collections.emptyList(), true) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--MhW_no_dict", this);
			}
		};
		usePrecomputedW = new PreComputedW();
		addIInW = new BooleanOption("add input symbols in W", "MhW_add_I_in_W",
				"Before starting inference, all inputs symbols of SUI are added to W-set as new input sequences.") {
			@Override
			public String getDisableHelp() {
				return "Start with an empty W-set.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--MhW_do_not_add_I_in_W", this);
			}
		};
		useAdaptiveH = new BooleanOption("use adaptive homing sequence",
				"Mhw_adaptive_h",
				"Use an adaptive homing sequence instead of a preset sequence.",
				Collections.emptyList(), Collections.emptyList(), true) {
			@Override
			public String getDisableHelp() {
				return "Use a preset homing sequence.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE,
						"--MhW_preset_h", this);
			}
		};
		useAdaptiveW = new BooleanOption("use adaptive W-tree",
				"MhW_adaptive_W",
				"Use an adaptive W-tree instead of a preset W-set.",
				new ArrayList<OptionTree>(), Arrays.asList(addIInW), true) {
			@Override
			public String getDisableHelp() {
				return "Use a preset W-set.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE,
						"--MhW_preset_W", this);
			}
		};
		addHInW = new BooleanOption("heuristic add h in W", "MhW_add_h_in_W",
				"Add homing sequence in W-set.", new ArrayList<OptionTree>(),
				Arrays.asList(useAdaptiveH, useAdaptiveW), false) {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE,
						"--MhW_do_not_add_h_in_W", this);
			}

		};
		oracleWithoutReset = new OracleOption(false);
		oracleWhenUsingReset = new OracleOption(true);

		useReset = new BooleanOption("use reset", "MhW_use_reset",
				"Allows the algorithm to use reset when it seems to be necessary "
						+ "(the oracle will also use reset to check validity of conjecture).",
				Arrays.asList((OptionTree) oracleWhenUsingReset),
				Arrays.asList((OptionTree) oracleWithoutReset), false) {
			@Override
			public String getDisableHelp() {
				return "Infer without reseting the driver (assuming that the SUL is connected).";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE,
						"--MhW_without_reset", this);
			}
		};
		subTrees.add(useReset);
		subTrees.add(usePrecomputedW);
		subTrees.add(addHInW);
		subTrees.add(useDictionary);
		subTrees.add(checkInconsistenciesHMapping);
		subTrees.add(searchCeInTrace);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_HW);
	}

	public void updateWithDriver(MealyDriver d) {
		usePrecomputedW.updateWithDriver(d);
		oracleWhenUsingReset.updateWithDriver(d);
		oracleWithoutReset.updateWithDriver(d);
	}
}
