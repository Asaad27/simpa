package automata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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

	public boolean isConnex(){
		if (transitions.size() == 0)
			return states.size() == 0;
		for (State s : states){
			LinkedList<Transition> toCheck = new LinkedList<Transition>();
			HashSet<State> crossed = new HashSet<State>();
			for (Transition initialTransition : transitions)
				if (initialTransition.getFrom() == s)
					toCheck.add(initialTransition);
			while (!toCheck.isEmpty()){
				Transition t = toCheck.poll();
				if (!crossed.contains(t.getTo())){
					crossed.add(t.getTo());
					for (Transition t2 : transitions)
						if (t2.getFrom() == t.getTo())
							toCheck.add(t2);
				}
			}
			for (State s2 : states){
				if (!crossed.contains(s2))
					return false;
			}
		}
		return true;
	}

	public void invalideateInitialsStates() {
		for (State s : states)
			s.setInitial(false);
	}
}
