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

	public boolean isConnex() {
		return isConnex(false);
	}

	public boolean isConnex(boolean verbose) {
		if (transitions.size() == 0)
			return states.size() == 0;
		HashSet<State> reachingAllState = new HashSet<State>();
		for (State s : states){
			if (reachingAllState.contains(s))
				continue;
			LinkedList<Transition> toCheck = new LinkedList<Transition>();
			HashSet<State> crossed = new HashSet<State>();
			for (Transition initialTransition : transitions)
				if (initialTransition.getFrom() == s)
					toCheck.add(initialTransition);
			boolean reachingAll = false;
			while (!toCheck.isEmpty()){
				Transition t = toCheck.poll();
				if (reachingAllState.contains(t.getTo())) {
					reachingAllState.add(t.getFrom());
					reachingAllState.add(s);
					reachingAll = true;
					break;
				}
				if (!crossed.contains(t.getTo())){
					crossed.add(t.getTo());
					for (Transition t2 : transitions)
						if (t2.getFrom() == t.getTo())
							toCheck.add(t2);
				}
			}
			if (reachingAll)
				continue;
			for (State s2 : states) {
				if (!crossed.contains(s2)) {
					if (verbose) {
						assert !crossed.isEmpty();
						HashSet<State> farestReached = new HashSet<State>();
						farestReached.add(s);
						HashSet<State> prevFarestReached = null;
						do {
							prevFarestReached = farestReached;
							farestReached = new HashSet<>();
							for (State s3 : prevFarestReached)
								for (Transition t : transitions)
									if (t.getFrom() == s3)
										farestReached.add(t.getTo());
						} while (!farestReached.equals(prevFarestReached));

						HashSet<State> farestLeading = new HashSet<>();
						farestLeading.add(s2);
						HashSet<State> prevFarestLeading = null;
						do {
							prevFarestLeading = farestLeading;
							farestLeading = new HashSet<>();
							for (State s3 : prevFarestLeading)
								for (Transition t : transitions)
									if (t.getTo() == s3) {
										assert !crossed.contains(t.getFrom());
										farestLeading.add(t.getFrom());
									}
							if (farestLeading.isEmpty())
								farestLeading = prevFarestLeading;
						} while (!farestLeading.containsAll(prevFarestLeading));
						assert !farestLeading.isEmpty() && !farestReached
								.isEmpty() : "the sets are not empty at start and thus, there is at least one transition from this set";
						System.out.println("state " + s2
								+ " is not reacheable from state " + s);
						System.out.println("A longer path is : state "
								+ farestLeading.iterator().next()
								+ " is not reachable from state "
								+ farestReached.iterator().next());
					}
					return false;
				}
			}
			reachingAllState.add(s);
		}
		return true;
	}

	public void invalideateInitialsStates() {
		for (State s : states)
			s.setInitial(false);
	}
}
