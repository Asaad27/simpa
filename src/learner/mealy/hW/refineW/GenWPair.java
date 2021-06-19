package learner.mealy.hW.refineW;

import automata.State;
import automata.Transition;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;
import tools.loggers.LogManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GenWPair implements WSetOptimization {
    Map<StatePair, InputSequence> distinguishedBy;
    Map<State, Map<String, Set<State>>> reverseInputMapping;
    Set<InputSequence> w;
    LmConjecture conjecture;
    private Map<State, Map<InputSequence, OutputSequence>> cache;

    public  void optimizePresetW(TotallyFixedW wSet, LmConjecture conjecture) {
        if (!conjecture.isFullyKnown()) {
            LogManager.logInfo("Skip reducing W, because Conjecture is not complete");
            return;
        }
        distinguishedBy = new HashMap<>();
        w = new HashSet<>();
        reverseInputMapping = new HashMap<>();
        this.conjecture = conjecture;
        cache = new HashMap<>();


        var states = conjecture.getStates();
        computeReverseInputMapping(conjecture);
        Queue<StatePair> queue = new ArrayDeque<>();
        for (int i = 0; i < states.size(); ++i) {
            for (int j = 0; j < i; ++j) {
                var s1 = states.get(i);
                var s2 = states.get(j);
                var distinguishingInput= findDistinguishingInput(conjecture, s1, s2);
//                = conjecture.getInputSymbols().stream()
//                        .filter(input -> !conjecture.getTransitionFromWithInput(s1, input).getOutput().equals(conjecture.getTransitionFromWithInput(s2, input).getOutput()))
//                        .findAny();
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
        LogManager.logInfo(String.format("Reduced W. Old size %d (%d). New size: %d (%d)", wSet.size(), wSet.stream().mapToInt(InputSequence::getLength).sum(),
                w.size(), w.stream().mapToInt(InputSequence::getLength).sum()));

        wSet.clear();
        wSet.addAll(w);
        if (wSet.isEmpty()) {
            wSet.add(new InputSequence());
        }
        if (!isCharacterizing(wSet)) {
            throw new IllegalStateException("reduced w-set not characterizing");
        }
    }

    private OutputSequence apply(InputSequence is, State s) {
        return cache.computeIfAbsent(s, k -> new HashMap<>())
                .computeIfAbsent(is, k -> conjecture.apply(k, s));
    }

    private boolean isCharacterizing(Collection<InputSequence> w) {
        List<State> states = conjecture.getStates();
        for (int i = 0; i < states.size(); ++i) {
            for (int j = 0; j < i; ++j) {
                State s1 = states.get(i);
                State s2 = states.get(j);
                //check if s1 and s2 are not distinguished
                if (w.stream().allMatch(is -> apply(is, s1).equals(apply(is, s2)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Optional<String> findDistinguishingInput(LmConjecture conjecture, State s1, State s2) {
        for (var in : conjecture.getInputSymbols()) {
            var o1 = conjecture.getTransitionFromWithInput(s1, in).getOutput();
            var o2 = conjecture.getTransitionFromWithInput(s2, in).getOutput();
            if (!o1.equals(o2)) {
                return Optional.of(in);
            }
        }
        return Optional.empty();
    }

    private void addToW(InputSequence is) {
        for (int i = 0; i < is.getLength(); ++i) {
            w.remove(is.getIthPreffix(i));
        }
        w.add(is);
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
