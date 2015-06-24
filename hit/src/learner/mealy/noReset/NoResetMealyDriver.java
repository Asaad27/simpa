package learner.mealy.noReset;

import tools.loggers.LogManager;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import examples.mealy.TestMealy;

public class NoResetMealyDriver extends MealyDriver {

	public NoResetMealyDriver(Mealy automata) {
		super(automata);
	}

	@Override
	public void reset(){
		super.reset();
		if (currentState == null){
			currentState = automata.getStates().get(0);
			if (addtolog){
				LogManager.logInfo("No initial state found, using " + currentState + " instead");
			}
		}
	}

}
