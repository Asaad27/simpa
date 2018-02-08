package learner;

import learner.efsm.table.LiLearner;
import learner.mealy.rivestSchapire.RivestSchapireLearner;
import learner.mealy.table.LmLearner;
import learner.mealy.tree.ZLearner;
import learner.mealy.combinatorial.CombinatorialLearner;
import learner.mealy.combinatorial.CutterCombinatorialLearner;
import learner.mealy.hW.HWLearner;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import main.simpa.Options;
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
	
	public StatsEntry getStats(){
		return null;
	}
	
	public void logStats(){
		StatsEntry s = getStats();
		if (s == null){
			LogManager.logInfo("unable to get learner stats");
			return;
		}
		LogManager.logLine();
		for (Attribute<?> a : s.getAttributes()){
			LogManager.logStat(a.getName() + " : " + s.get(a) + " " + a.getUnits());
		}
		LogManager.logLine();
	}

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
			else if (Options.CUTTERCOMBINATORIALINFERENCE)
					return new CutterCombinatorialLearner((MealyDriver)driver);
			else if (Options.RIVESTSCHAPIREINFERENCE)
				return new RivestSchapireLearner((MealyDriver)driver);
			else if (Options.LOCALIZER_BASED_INFERENCE)
				return new LocalizerBasedLearner((MealyDriver)driver);
			else if (Options.HW_INFERENCE)
				return new HWLearner((MealyDriver)driver);
			else if (Options.LMINFERENCE)
				return new LmLearner(driver);
		default:
			throw new RuntimeException("there is no driver to use");
		}
	}
}
