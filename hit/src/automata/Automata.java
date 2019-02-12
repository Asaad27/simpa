package automata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.loggers.LogManager;

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
		if (connexComponent.size() == states.size()) {
			LogManager.logInfo("Automaton " + this + " is strongly connected.");
			return true;
		}

		HashSet<State> initialStates = new HashSet<State>(states);
		initialStates.removeAll(connexComponent);
		findUnreachableAncestors(initialStates);

		LogManager.logInfo("Automaton " + this + " is not strongly connected : "
				+ "there is no path from " + connexComponent.iterator().next()
				+ " to " + initialStates.iterator().next());
		if (initialStates.size() > 1 || connexComponent.size() > 1)
			LogManager.logInfo("In a more generic idea, any state of "
				+ initialStates + " is not reachable from any state of "
				+ connexComponent);

		return false;
	}

	/**
	 * Compute the set of states reachable from a given state.
	 * 
	 * @param s
	 *            the state leading to all returned states
	 * @return a set of states which can be reached from the state {@code s}
	 */
	public Set<State> getStatesReachableFrom(State s) {
		HashSet<State> reachable = new HashSet<State>();
		reachable.add(s);
		ArrayList<Transition> transitionsToCompute = new ArrayList<>(
				transitions);
		boolean added = false;
		do {
			ArrayList<Transition> unused = new ArrayList<>(
					transitionsToCompute.size());
			added = false;
			for (Transition t : transitionsToCompute) {
				if (reachable.contains(t.from)) {
					reachable.add(t.to);
					added = true;
				} else {
					unused.add(t);
				}
			}
			transitionsToCompute = unused;
		} while (added);
		return reachable;
	}

	/**
	 * Indicate weather there is a path from given state to any state of this
	 * automaton.
	 * 
	 * @param start
	 *            the state at the start of all path
	 * @return {@code true} if it exists a path from the state {@code start} to
	 *         any state of this automaton, {@code false} otherwise.
	 */
	public boolean allStatesAreReachableFrom(State start) {
		Set<State> reachable = getStatesReachableFrom(start);
		assert states.containsAll(reachable);
		assert new HashSet<State>(states).size() == states
				.size() : "some states are recordered twice";
		return reachable.size() == states.size();
	}

	/**
	 * Compute a relative depth in the graph for nodes reachable or reaching the
	 * node start.
	 * 
	 * The automaton do not need to be complete.
	 * 
	 * @param start
	 *            a node to start computation. Useful if the graph is not
	 *            connected. If {@code null}, a random state will be used.
	 * @return a mapping for each state such that each state of depth d is
	 *         reachable from a state of depth lowest than d or can reach a
	 *         state of depth greatest than d
	 */
	public Map<State, Integer> computeDepths(State start) {
		if (start == null)
			start = getState(0);// any state of the automaton
		Map<State, Integer> depth = new HashMap<>();
		depth.put(start, 0);
		boolean updated;
		do {
			updated = false;
			for (Transition curTransition : transitions) {
				State fromState = curTransition.getFrom();
				Integer fromDepth = depth.get(fromState);
				State destState = curTransition.getTo();
				Integer destDepth = depth.get(destState);
				if (fromDepth == null) {
					if (destDepth != null) {
						fromDepth = destDepth - 1;
						depth.put(fromState, fromDepth);
						updated = true;
					} else
						continue;
				}
				if (destDepth == null) {
					depth.put(destState, fromDepth + 1);
					updated = true;
					continue;
				}
				if (destDepth > fromDepth + 1) {
					depth.put(destState, fromDepth + 1);
					updated = true;
					break;
				}
				if (destDepth == fromDepth + 1 || destDepth == fromDepth)
					continue;
				if (destDepth < fromDepth + 1) {
					depth.put(fromState, destDepth);
					updated = true;
					break;
				}
			}
		} while (updated);
		return depth;
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
