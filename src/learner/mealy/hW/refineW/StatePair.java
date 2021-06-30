package learner.mealy.hW.refineW;

import automata.State;

import java.util.Arrays;
import java.util.Comparator;

class StatePair {
    State[] states;

    public StatePair(State s1, State s2) {
        states = new State[]{s1, s2};
        Arrays.sort(states, Comparator.comparingInt(State::hashCode));
    }

    public State s0() {
        return states[0];
    }

    public State s1() {
        return states[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatePair statePair = (StatePair) o;
        return Arrays.equals(states, statePair.states);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(states);
    }
}
