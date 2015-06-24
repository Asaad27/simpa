package learner.mealy.noReset;

import tools.loggers.LogManager;
import automata.mealy.Mealy;
import automata.State;
import drivers.mealy.MealyDriver;

public class NoResetMealyDriver extends MealyDriver {

	public NoResetMealyDriver(Mealy automata) {
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
