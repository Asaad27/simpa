package automata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	/**
	 * find a strongly connected component of an automaton. This can be used to
	 * check if the automaton is strongly connected.
	 * 
	 * @see {@link Automata#findStronglyConnectedComponent(Set, boolean)}
	 * 
	 * @return a set of state all reachable from the others (considering a
	 *         lonely state to be reachable by itself).
	 */
	public Set<State> findStronglyConnectedComponent() {
		return findStronglyConnectedComponent(new HashSet<>(states), false);
	}

	/**
	 * same as {@link #findStronglyConnectedComponent()} but the search is
	 * restricted in a sub-part of the graph.
	 * 
	 * @param part
	 *            the sub-part in which we should search for strongly connected
	 *            component.
	 * @param stopOnFirst
	 *            do not compute a strongly connected component, only check if
	 *            the part is strongly connected (and return {@code null} in the
	 *            other case).
	 * @return a set of state all reachable from the others (considering a
	 *         lonely state to be reachable by itself) or {@code null} if
	 *         {@codestopOnFirst} is {@code true} and the part is not strongly
	 *         connected
	 */
	public Set<State> findStronglyConnectedComponent(Set<State> part,
			boolean stopOnFirst) {
		HashSet<State> reachingAllState = new HashSet<State>();
		for (State s : part) {
			if (reachingAllState.contains(s))
				continue;
			LinkedList<Transition> toCheck = new LinkedList<Transition>();
			HashSet<State> crossed = new HashSet<State>();
			crossed.add(s);// We consider here that a state is reachable from
							// itself, even if there is no loop transition (can
							// be parameterized if needed).
			for (Transition initialTransition : transitions)
				if (initialTransition.getFrom() == s)
					toCheck.add(initialTransition);
			boolean reachingAll = false;
			while (!toCheck.isEmpty()) {
				Transition t = toCheck.poll();
				assert part.contains(t.getTo());
				if (reachingAllState.contains(t.getTo())) {
					reachingAllState.add(t.getFrom());
					reachingAllState.add(s);
					reachingAll = true;
					break;
				}
				if (!crossed.contains(t.getTo())) {
					crossed.add(t.getTo());
					for (Transition t2 : transitions)
						if (t2.getFrom() == t.getTo())
							toCheck.add(t2);
				}
			}
			if (reachingAll)
				continue;
			if (crossed.size() == part.size()) {
				assert crossed.equals(part);
				reachingAllState.add(s);
				continue;
			}
			assert part.containsAll(crossed) && crossed.size() < part.size();
			if (stopOnFirst)
				return null;
			return findStronglyConnectedComponent(crossed, false);
		}
		assert !part.isEmpty();
		return part;
	}

	/**
	 * Search a set of ancestors of {@code part} such as any state in set can
	 * reach any other state (but they can also reach states out of the set) and
	 * all states outside of the set cannot reach a state in the set.
	 * 
	 * @param part
	 *            a set of states (possibly a sub-set of all automaton states)
	 *            which will be reduced.
	 */
	public void findUnreachableAncestors(Set<State> part) {
		boolean minimal = false;
		HashSet<State> reachingAllState = new HashSet<State>();
		while (!minimal) {
			minimal = true;
			for (State s : part) {
				if (reachingAllState.contains(s))
					continue;
				LinkedList<Transition> toCheck = new LinkedList<Transition>();
				HashSet<State> crossed = new HashSet<State>();
				crossed.add(s);
				for (Transition initialTransition : transitions)
					if (initialTransition.getFrom() == s)
						toCheck.add(initialTransition);
				boolean reachingAll = false;
				while (!toCheck.isEmpty()) {
					Transition t = toCheck.poll();
					if (!part.contains(t.getTo()))
						continue;
					if (reachingAllState.contains(t.getTo())) {
						reachingAllState.add(t.getFrom());
						reachingAll = true;
						break;
					}
					if (!crossed.contains(t.getTo())) {
						crossed.add(t.getTo());
						for (Transition t2 : transitions)
							if (t2.getFrom() == t.getTo())
								toCheck.add(t2);
					}
				}
				if (crossed.size() == part.size()) {
					assert crossed.equals(part);
					reachingAll = true;
				}
				if (reachingAll) {
					reachingAllState.add(s);
				} else {
					part.removeAll(crossed);
					minimal = false;
					break;
				}
			}
		}
		assert reachingAllState.equals(part);
	}

	public boolean isConnex() {
		return isConnex(false);
	}

	public boolean isConnex(boolean verbose) {
		if (transitions.size() == 0)
			return states.size() == 0;
		if (!verbose)
			return findStronglyConnectedComponent(new HashSet<>(states),
					true) != null;
		Set<State> connexComponent = findStronglyConnectedComponent();
		if (connexComponent.size() == states.size())
			return true;

		HashSet<State> initialStates = new HashSet<State>(states);
		initialStates.removeAll(connexComponent);
		findUnreachableAncestors(initialStates);

		System.out.println(
				"A longer path is : state " + initialStates.iterator().next()
						+ " is not reachable from state "
						+ connexComponent.iterator().next());
		System.out.println("In a more generic idea, any state of "
				+ initialStates + " is not reachable from any state of "
				+ connexComponent);

		return false;
	}

	/**
	 * update state s to be an initial state.
	 * 
	 * @param s
	 *            the new initial state
	 */
	public void setInitialState(State s) {
		assert states.contains(s);
		s.setInitial(true);
	}

	public void invalideateInitialsStates() {
		for (State s : states)
			s.setInitial(false);
	}
}
