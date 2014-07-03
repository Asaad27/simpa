package automata.mealy;

import automata.State;
import automata.Transition;

public class MealyTransition extends Transition {
	private static final long serialVersionUID = 6719440643049324689L;

	protected String output;
	protected Mealy automata;

	public MealyTransition(Mealy automata, State s1, State s2, String input,
			String output) {
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
	
	public boolean isLoop(){
		return from.equals(to);
	}

	@Override
	public String toString() {
		return from + " to " + to + " by " + input + "/" + output;
	}

	public String toDot() {
		System.out.println(from + " -> " + to + "[label=\"" + input + "/" + output + "\"];");
		return from + " -> " + to + "[label=\"" + input + "/" + output + "\"];"; // A -> B[label="a/0"]
	}

	public String getName() {
		return from + "-" + to;
	}
	
	public int hashCode(){
		return (from + input).hashCode();
	}
}
