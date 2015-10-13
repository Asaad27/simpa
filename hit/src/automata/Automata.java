package automata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Automata implements Serializable {
	private static final long serialVersionUID = -8767507358858312705L;

	protected String name;
	protected List<State> states;
	protected List<Transition> transitions;
	
	public Automata(String name){
		states = new ArrayList<State>();
		transitions = new ArrayList<Transition>();
		this.name = name;			
	}

	public String getName() {
		return name;
	}

	public List<State> getStates() {
		return states;
	}
	
	public int getStateCount(){
		return states.size();
	}
	
	public State getState(int index) {
		return states.get(index);
	}
	
	public State addState(){
		return addState(false);
	}
	
	public State addState(Boolean initial){
		State s = new State("S" + states.size(), initial);
		states.add(s);
		return s;
	}

	public void addState(State s){
		states.add(s);
	}
	
	public Boolean addTransition(Transition t){
		return transitions.add(t);
	}
	
	public State getInitialState(){
		for(State s : states){
			if(s.isInitial()) return s;
		}
		return null;
	}
	
	public void cleanMark(){
		for(State s : states){
			s.cleanMark();
		}
	}

	public void reset(){
	}
}
