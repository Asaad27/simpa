package drivers.mealy.transparent;

import automata.State;
import automata.mealy.Mealy;
import drivers.mealy.AutomatonMealyDriver;

public class TransparentMealyDriver extends AutomatonMealyDriver {
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
