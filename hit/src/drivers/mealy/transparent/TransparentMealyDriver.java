package drivers.mealy.transparent;

import drivers.mealy.MealyDriver;
import automata.mealy.Mealy;
import automata.State;

public class TransparentMealyDriver extends MealyDriver {
	public TransparentMealyDriver(Mealy automata){
		super(automata);
	}
	
	public Mealy getAutomata(){
		return automata;
	}
	
	public State getCurrentState(){
		return currentState;
	}
	
	public void setCurrentState(State s){
		currentState = s;
	}
}
