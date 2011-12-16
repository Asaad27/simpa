package automata.mealy;

import automata.State;
import automata.Transition;

public class MealyTransition extends Transition {
	private static final long serialVersionUID = 6719440643049324689L;

	protected String output;
	protected Mealy automata;
	
	public MealyTransition(Mealy automata, State s1, State s2, String input, String output){
		super(s1, s2, input);
		this.output = output;
		this.automata = automata;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setOutput(String symbol) {
		output = symbol;
	}

	@Override
	public String toString(){
		return from + " to " + to + " by " + input + "/" + output;
	}
	
	public String toDot(){
		return from + " -> " + to + "[label=\"" + input + "/" + output + "\"];";
	}
	
	public String getName(){
		return from + "-" + to;
	}
}
