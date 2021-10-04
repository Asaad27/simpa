/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 *     Roland GROZ
 ********************************************************************************/
package learner;

import automata.Automata;
import drivers.Driver;
import drivers.mealy.CompleteMealyDriver;
import drivers.mealy.PartialMealyDriver;
import learner.mealy.combinatorial.CombinatorialLearner;
import learner.mealy.combinatorial.CutterCombinatorialLearner;
import learner.mealy.hW.HWLearner;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import learner.mealy.rivestSchapire.RivestSchapireLearner;
import learner.mealy.table.LmLearner;
import learner.mealy.tree.ZLearner;
import main.simpa.SIMPA;
import options.ChoiceOptionItem;
import options.automataOptions.AutomataChoice;
import options.learnerOptions.MealyLearnerChoice;
import stats.StatsEntry;
import stats.attribute.Attribute;
import tools.loggers.LogManager;

public abstract class Learner {
	protected boolean addtolog = true;

	public void stopLog() {
		addtolog = false;
	}

	public void startLog() {
		addtolog = true;
	}

	public abstract Automata createConjecture();

	public abstract void learn();

	public StatsEntry getStats() {
		return null;
	}

	public void logStats() {
		StatsEntry s = getStats();
		if (s == null) {
			LogManager.logInfo("unable to get learner stats");
			return;
		}
		LogManager.logLine();
		for (Attribute<?> a : s.getAttributes()) {
			LogManager.logStat(a.getName() + " : " + s.get(a) + " "
					+ a.getUnits());
		}
		LogManager.logLine();
	}

	public static Learner getLearnerFor(Driver<?, ?> driver) {
		AutomataChoice automataChoice = SIMPA.automataChoice;
		ChoiceOptionItem selectedAutomataChoice = automataChoice
				.getSelectedItem();
//EFSM//		if (selectedAutomataChoice == automataChoice.efsm) {
//EFSM//			return automataChoice.efsmLearnerChoice.getSelectedItem()
//EFSM//					.getLearner((EFSMDriver) driver);
//EFSM//		} else 
		if (selectedAutomataChoice == automataChoice.mealy) {
			MealyLearnerChoice learnerChoice = automataChoice.mealyLearnerChoice;
			ChoiceOptionItem selectedLearnerChoice = learnerChoice
					.getSelectedItem();
			if (selectedLearnerChoice == learnerChoice.hW) {
				return new HWLearner((PartialMealyDriver) driver, learnerChoice.hW);
			}
			CompleteMealyDriver mDriver = (CompleteMealyDriver) driver;
			if (selectedLearnerChoice == learnerChoice.tree) {
				return new ZLearner(mDriver, learnerChoice.tree);
			} else if (selectedLearnerChoice == learnerChoice.combinatorial) {
				if (learnerChoice.combinatorial.withCut())
					return new CombinatorialLearner(mDriver,
							learnerChoice.combinatorial);
				else
					return new CutterCombinatorialLearner(mDriver,
							learnerChoice.combinatorial);
			} else if (selectedLearnerChoice == learnerChoice.rivestSchapire) {
				return new RivestSchapireLearner(mDriver,
						learnerChoice.rivestSchapire);
			} else if (selectedLearnerChoice == learnerChoice.localizerBased) {
				return new LocalizerBasedLearner(mDriver,
						learnerChoice.localizerBased);
			} else if (selectedLearnerChoice == learnerChoice.lm) {
				return new LmLearner(mDriver, learnerChoice.lm);
			} else {
				assert false;
				return null;
			}
		} else {
			assert false : "unhandled value for automata choice option";
			return null;
		}
	}
}
