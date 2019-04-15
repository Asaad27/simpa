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
package learner.mealy.rivestSchapire;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import drivers.mealy.MealyDriver;
import learner.mealy.table.LmOptions;
import options.BooleanOption;
import options.GenericChoiceOption;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.RandomOption;
import options.learnerOptions.StateBoundOption;

public class RivestSchapireOptions extends OneArgChoiceOptionItem {
	private GenericOneArgChoiceOption<OneArgChoiceOptionItem> subLearnerOption;
	public LmOptions lmOptions;
	public final RandomOption seedForProbabilistic = new RandomOption(
			"--MRivest_probabilistic_seed",
			"the probabilistic search of new homing sequence");
	private final StateBoundOption stateBound = new StateBoundOption();
	private final BooleanOption probabilisticRS = new BooleanOption(
			"start with an empty homing sequence", "MRivest_probabilistic",
			"Use probabilistic version of Rivest and Schapire algorithm which computes automatically the homing sequence.",
			Arrays.asList(stateBound, seedForProbabilistic),
			Collections.emptyList()) {
		@Override
		public String getDisableHelp() {
			return "Compute a homing sequence (from glass-box driver) which will be provided to the algorithm.";
		};

		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					ArgumentDescriptor.AcceptedValues.NONE, "--TMRivest_init_h",
					this);
		}
	};

	public RivestSchapireOptions(GenericChoiceOption<?> parent) {
		super("Rivest & Schapire", "MRivest", parent);
		lmOptions = new LmOptions(subLearnerOption);
		subLearnerOption = new GenericOneArgChoiceOption<OneArgChoiceOptionItem>(
				"--Rivest_sub-learner",
				"Sub-learner for Rivest and Schapire algorithm",
				"Select learner (using reset) to emulate on non-ressetable driver (Rivest & Schapire algorithm is written only with Lm, but this implementation should work with others).") {
			{
				addChoice(lmOptions);
				setDefaultItem(lmOptions);
			}

			@Override
			protected List<ArgumentDescriptor> getHelpArguments() {
				// This option is hidden because there only one possible value
				// at this time.
				return Collections.emptyList();
			}
		};
		subTrees.add(subLearnerOption);

		subTrees.add(probabilisticRS);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_RS);
	}

	public void updateWithDriver(MealyDriver driver) {
		stateBound.updateWithDriver(driver);
	}

	public int getStateNumberBound() {
		return stateBound.getValue();
	}

	public boolean probabilisticRS() {
		return probabilisticRS.isEnabled();
	}

	public void setProbabilisticRS(boolean b) {
		probabilisticRS.getValueHolder().setValue(b);
		assert probabilisticRS() == b;
	}

}
