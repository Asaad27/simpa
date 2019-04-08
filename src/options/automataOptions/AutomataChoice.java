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
package options.automataOptions;

import java.util.ArrayList;
//EFSM//import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import drivers.efsm.EFSMDriverChoice;
import drivers.mealy.MealyDriverChoice;
//EFSM//import learner.efsm.EFSMLearnerChoice;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.learnerOptions.MealyLearnerChoice;

public class AutomataChoice extends MultiArgChoiceOption {
	public MealyLearnerChoice mealyLearnerChoice = new MealyLearnerChoice();
	public MealyDriverChoice mealyDriverChoice = new MealyDriverChoice();
	public MultiArgChoiceOptionItem mealy;
//EFSM//	public EFSMLearnerChoice efsmLearnerChoice = new EFSMLearnerChoice();
	public EFSMDriverChoice efsmDriverChoice = new EFSMDriverChoice();
//EFSM//	public MultiArgChoiceOptionItem efsm;

	public AutomataChoice() {
		super("Automata type");
		setCategory(OptionCategory.GLOBAL);
		List<OptionTree> subTree;
		subTree = new ArrayList<>();
		subTree.add(mealyLearnerChoice);
		subTree.add(mealyDriverChoice);
		mealy = new MultiArgChoiceOptionItem("Mealy", "--Mealy", this, subTree);

//EFSM//		efsm = new MultiArgChoiceOptionItem("EFSM", "--Efsm", this,
//EFMS//				Arrays.asList(efsmDriverChoice, efsmLearnerChoice));

		addChoice(mealy);
//EFSM//		addChoice(efsm);
		setDefaultItem(mealy);
	}

	public OptionTree getDriverOptions() {
		if (getSelectedItem() == mealy)
			return mealyDriverChoice;
		else {
//EFSM//			assert getSelectedItem() == efsm;
//EFSM//			return efsmLearnerChoice;
			assert false;// EFSM//
			return null;// EFSM//
		}
	}

	@Override
	protected List<ArgumentDescriptor> getHelpArguments() {
		return Collections.emptyList();
	}
}
