package learner;

import learner.efsm.table.LiLearner;
import learner.mealy.rivestSchapire.RivestSchapireLearner;
import learner.mealy.table.LmLearner;
import learner.mealy.tree.ZLearner;
import learner.mealy.combinatorial.CombinatorialLearner;
import learner.mealy.noReset.NoResetLearner;
import main.simpa.Options;
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

	public static Learner getLearnerFor(Driver driver) throws Exception {
		switch (driver.type) {
		case SCAN:
			return new ZLearner(driver);
		case EFSM:
			return new LiLearner(driver);
		case MEALY:
			if (Options.TREEINFERENCE)
				return new ZLearner(driver);
			else if (Options.COMBINATORIALINFERENCE)
				return new CombinatorialLearner((MealyDriver)driver);
			else if (Options.RIVESTSCHAPIREINFERENCE)
				return new RivestSchapireLearner((MealyDriver)driver);
			else if (Options.NORESETINFERENCE)
				return new NoResetLearner((MealyDriver)driver);
			else if (Options.LMINFERENCE)
				return new LmLearner(driver);
		default:
			return null;
		}
	}
}
