package learner.mealy.localizerBased;

import automata.State;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import tools.loggers.LogManager;

public class LocalizerBasedMealyDriver extends MealyDriver {

	public LocalizerBasedMealyDriver(Mealy automata) {
		super(automata);
	}

	@Override
	public void reset(){
		LogManager.logError("Tried to reset the driver");
	}
	
	public void setCurrentState(State s){
		assert automata.getStates().contains(s);
		currentState = s;
	}

}
