package learner.mealy.hW;

import automata.State;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import tools.loggers.LogManager;

public class HWtMealyDriver extends MealyDriver {

	public HWtMealyDriver(Mealy automata) {
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
