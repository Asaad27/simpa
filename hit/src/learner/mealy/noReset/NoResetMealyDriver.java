package learner.mealy.noReset;

import automata.State;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import tools.loggers.LogManager;

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
