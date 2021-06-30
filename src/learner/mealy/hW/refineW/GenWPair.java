package learner.mealy.hW.refineW;

import automata.State;
import automata.Transition;
import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

import java.util.*;
import java.util.stream.Collectors;

import static learner.mealy.hW.refineW.ReduceW.reduceW;

public class GenWPair implements WSetOptimization {
    Map<StatePair, InputSequence> distinguishedBy;
    Map<State, Map<String, Set<State>>> reverseInputMapping;
    Set<InputSequence> newW;
    LmConjecture conjecture;

    public Collection<InputSequence> computeSmallerWSet(TotallyFixedW oldW, LmConjecture conjecture) {
        distinguishedBy = new HashMap<>();
        newW = new HashSet<>();
        reverseInputMapping = new HashMap<>();
        this.conjecture = conjecture;
        var conjectureProxy = new ConjectureWrapper(conjecture);

        var states = conjecture.getStates();
        computeReverseInputMapping(conjecture);
        Queue<StatePair> queue = new ArrayDeque<>();
        for (int i = 0; i < states.size(); ++i) {
            for (int j = 0; j < i; ++j) {
                var s1 = states.get(i);
                var s2 = states.get(j);
                var distinguishingInput= conjectureProxy.findDistinguishingInput(s1, s2);
                if (distinguishingInput.isPresent()) {
                    InputSequence is = new InputSequence(distinguishingInput.get());
                    addToW(is);
                    StatePair statePair = new StatePair(s1, s2);
                    queue.add(statePair);
                    distinguishedBy.put(statePair, is);
                }
            }
        }
        while (!queue.isEmpty()) {
            var statePair = queue.remove();
            for (var input : conjecture.getInputSymbols()) {
                for (var predecessor : getPreceedingPairs(statePair, input)) {
                    if (!distinguishedBy.containsKey(predecessor)) {
                        InputSequence distinguishingSequence = new InputSequence(input).addInputSequence(distinguishedBy.get(statePair));
                        distinguishedBy.put(predecessor, distinguishingSequence);
                        queue.add(predecessor);
                        addToW(distinguishingSequence);
                    }
                }
            }
        }

        return reduceW(conjectureProxy, new ArrayList<>(newW));
    }



    private void addToW(InputSequence is) {
        for (int i = 0; i < is.getLength(); ++i) {
            newW.remove(is.getIthPreffix(i));
        }
        newW.add(is);
    }

    private void computeReverseInputMapping(LmConjecture conjecture) {
        for (State s : conjecture.getStates()) {
            for (Transition t : conjecture.getTransitionFrom(s)) {
                var reverseMappingForState = reverseInputMapping.computeIfAbsent(t.getTo(), x -> new HashMap<>());
                var predecessorsForInput = reverseMappingForState.computeIfAbsent(t.getInput(), x -> new HashSet<>());
                predecessorsForInput.add(s);
            }
        }
    }

    public Set<StatePair> getPreceedingPairs(StatePair p, String input) {
        var s1predcessors = reverseInputMapping.getOrDefault(p.getS0(), Map.of()).getOrDefault(input, Set.of());
        var s2predcessors = reverseInputMapping.getOrDefault(p.getS1(), Map.of()).getOrDefault(input, Set.of());

        return s1predcessors.stream()
                .flatMap(s1 -> s2predcessors.stream().map(s2 -> new StatePair(s1, s2)))
                .filter(pair -> !pair.getS0().equals(pair.getS1()))
                .collect(Collectors.toSet());
    }


    private static class StatePair {
        State[] states;

        public StatePair(State s1, State s2) {
            states = new State[]{s1, s2};
            Arrays.sort(states, Comparator.comparingInt(State::hashCode));
        }

        State getS1() {
            return states[0];
        }

        State getS0() {
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
}
