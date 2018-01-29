package learner;

import options.ChoiceOptionItem;
import options.automataOptions.AutomataChoice;
import options.learnerOptions.MealyLearnerChoice;
import learner.efsm.table.LiLearner;
import learner.mealy.rivestSchapire.RivestSchapireLearner;
import learner.mealy.table.LmLearner;
import learner.mealy.tree.ZLearner;
import learner.mealy.combinatorial.CombinatorialLearner;
import learner.mealy.combinatorial.CutterCombinatorialLearner;
import learner.mealy.hW.HWLearner;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import main.simpa.SIMPA;
import stats.StatsEntry;
import stats.attribute.Attribute;
import tools.loggers.LogManager;
import automata.Automata;
import drivers.Driver;
import drivers.mealy.MealyDriver;

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

	public static Learner getLearnerFor(Driver driver) throws Exception {
		AutomataChoice automataChoice = SIMPA.automataChoice;
		ChoiceOptionItem selectedAutomataChoice = automataChoice
				.getSelectedItem();
		if (selectedAutomataChoice == automataChoice.scan) {
			return new ZLearner(driver);
		} else if (selectedAutomataChoice == automataChoice.efsm) {
			return new LiLearner(driver);
		} else if (selectedAutomataChoice == automataChoice.mealy) {
			MealyLearnerChoice learnerChoice = automataChoice.mealyLearnerChoice;
			ChoiceOptionItem selectedLearnerChoice = learnerChoice
					.getSelectedItem();
			if (selectedLearnerChoice == learnerChoice.tree) {
				return new ZLearner(driver);
			} else if (selectedLearnerChoice == learnerChoice.combinatorial) {
				return new CombinatorialLearner((MealyDriver) driver);
			} else if (selectedLearnerChoice == learnerChoice.cutCombinatorial) {
				return new CutterCombinatorialLearner((MealyDriver) driver);
			} else if (selectedLearnerChoice == learnerChoice.rivestSchapire) {
				return new RivestSchapireLearner((MealyDriver) driver);
			} else if (selectedLearnerChoice == learnerChoice.localizerBased) {
				return new LocalizerBasedLearner((MealyDriver) driver,
						learnerChoice.localizerBased);
			} else if (selectedLearnerChoice == learnerChoice.hW) {
				return new HWLearner((MealyDriver)driver);
			} else if (selectedLearnerChoice == learnerChoice.lm) {
				return new LmLearner(driver);
			} else {
				assert false;
				return null;
			}
		} else {
			throw new RuntimeException("there is no driver to use");
		}
	}
}
