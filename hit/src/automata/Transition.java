package automata;

import java.io.Serializable;

public abstract class Transition implements Serializable {
	private static final long serialVersionUID = -7954062378513113845L;

	protected State from;
	protected State to;
	protected String input;
	
	public Transition(State from, State to, String input){
		this.from = from;
		this.to = to;
		this.input = input;
	}
	
	public State getFrom() {
		return from;
	}

	public State getTo() {
		return to;
	}

	public String getInput() {
		return input;
	}

	public String toDot() {
		return from + " -> " + to + "[label=\"" + input + "\"];";
	}

	@Override
	public String toString(){
		return from + " to " + to + " by " + input;
	}
}
