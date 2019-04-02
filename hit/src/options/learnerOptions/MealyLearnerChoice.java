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
package options.learnerOptions;

import learner.mealy.combinatorial.CombinatorialOptions;
import learner.mealy.hW.HWOptions;
import learner.mealy.localizerBased.LocalizerBasedOptions;
import learner.mealy.rivestSchapire.RivestSchapireOptions;
import learner.mealy.table.LmOptions;
import learner.mealy.tree.ZOptions;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class MealyLearnerChoice
		extends GenericOneArgChoiceOption<OneArgChoiceOptionItem> {

	public HWOptions hW;
	public RivestSchapireOptions rivestSchapire;
	public LocalizerBasedOptions localizerBased;
	public ZOptions tree;
	public LmOptions lm;
	public CombinatorialOptions combinatorial;

	public MealyLearnerChoice() {
		super("--algo", "Mealy learner", "Select Mealy learner.");
		setCategory(OptionCategory.INFERENCE);

		hW = new HWOptions(this);

		rivestSchapire = new RivestSchapireOptions(this);
		localizerBased = new LocalizerBasedOptions(this);
		tree = new ZOptions(this);
		lm = new LmOptions(this);
		combinatorial = new CombinatorialOptions(this);

		addChoice(hW);
		addChoice(localizerBased);
		addChoice(rivestSchapire);
		addChoice(tree);
		addChoice(lm);
		addChoice(combinatorial);
		setDefaultItem(hW);

	}

	@Override
	protected ArgumentDescriptor makeArgumentDescriptor(String argument) {
		return new ArgumentDescriptor(AcceptedValues.ONE, argument, this) {
			@Override
			public String getHelpDisplay() {
				assert super.getHelpDisplay()
						.equals(name + "=<>") : "need update for consistence";
				return name + "=<name>";
			}
		};
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg.name.equals("--algo");
		return "Set learner algorithm.";
	}
}
