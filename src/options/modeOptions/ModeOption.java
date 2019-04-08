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
package options.modeOptions;

import java.util.List;
import options.MultiArgChoiceOption;
import options.OptionCategory;
import options.OptionTree;
import options.OptionValidator;
import options.automataOptions.AutomataChoice;

public class ModeOption extends MultiArgChoiceOption {
	public final SimpleLearnOption simple = new SimpleLearnOption(this);
	public final StatsOptions stats = new StatsOptions(this);

	public ModeOption(AutomataChoice automataChoice) {
		super("Inference mode");
		setCategory(OptionCategory.GLOBAL);
		addChoice(stats);
		for (OptionTree option : stats.subTrees)
			option.setCategory(OptionCategory.STATS);
		addChoice(simple);
		setDefaultItem(simple);
		addValidator(new OptionValidator() {
			@Override
			public void check() {
				clear();
				if (getSelectedItem() == stats
						&& automataChoice
								.getSelectedItem() == automataChoice.mealy
						&& automataChoice.mealyLearnerChoice
								.getSelectedItem() == automataChoice.mealyLearnerChoice.combinatorial
						&& automataChoice.mealyLearnerChoice.combinatorial
								.withCut()
						&& automataChoice.mealyLearnerChoice.combinatorial
								.isInteractive()) {
					setCriticality(CriticalityLevel.WARNING);
					setMessage(
							"interactive pruning may produce biased results for stats.");
				}
			}
		});
	}

	@Override
	protected List<ArgumentDescriptor> getHelpArguments() {
		List<ArgumentDescriptor> r = super.getHelpArguments();
		r.remove(simple.argument);
		return r;
	}

}
