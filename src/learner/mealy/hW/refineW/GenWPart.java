package learner.mealy.hW.refineW;

import automata.State;
import automata.Transition;
import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

import java.util.*;
import java.util.stream.Collectors;

import static learner.mealy.hW.refineW.PruneW.reduceW;

public class GenWPart implements WSetOptimization {
    Map<StatePair, InputSequence> distinguishedBy;
    Map<State, Map<String, Set<State>>> reverseInputMapping;
    List<InputSequence> newW;
    LmConjecture conjecture;

    public Collection<InputSequence> computeSmallerWSet(TotallyFixedW oldW, LmConjecture conjecture) {
        distinguishedBy = new HashMap<>();
        newW = new ArrayList<>();
        reverseInputMapping = new HashMap<>();
        this.conjecture = conjecture;
        var conjectureProxy = new ConjectureWrapper(conjecture);

        var states = conjecture.getStates();
        computeReverseInputMapping(conjecture);
        Queue<StatePair> queue = new ArrayDeque<>();
        for (int i = 0; i < states.size(); ++i) {
            for (int j = 0; j < i; ++j) {
                StatePair statePair = new StatePair(states.get(i), states.get(j));
                var distinguishingSeqInW = conjectureProxy.isDistinguishedByWSet(statePair, newW);
                if (distinguishingSeqInW.isPresent()) {
                    distinguishedBy.put(statePair, distinguishingSeqInW.get());
                    queue.add(statePair);
                } else {
                    var distinguishingInput = conjectureProxy.findDistinguishingInput(statePair);
                    if (distinguishingInput.isPresent()) {
                        InputSequence is = new InputSequence(distinguishingInput.get());
                        addToW(is);
                        queue.add(statePair);
                        distinguishedBy.put(statePair, is);
                    }
                }
            }
        }
        while (!queue.isEmpty()) {
            var statePair = queue.remove();
            for (var input : conjecture.getInputSymbols()) {
                for (var predecessorPair : getPreceedingPairs(statePair, input)) {
                    //is there already a dist. sequence for this pair?
                    if (distinguishedBy.containsKey(predecessorPair)) continue;
                    //if not, is there a sequence in W that distinguishes this pair?
                    var alreadyDistinguishedByOtherSequence = conjectureProxy.isDistinguishedByWSet(predecessorPair,
                            newW);
                    if (alreadyDistinguishedByOtherSequence.isPresent()) {
                        distinguishedBy.put(predecessorPair, alreadyDistinguishedByOtherSequence.get());
                    } else {
                        //Otherwise add longer sequence
                        InputSequence distinguishingSequence =
                                new InputSequence(input).addInputSequence(distinguishedBy.get(statePair));
                        distinguishedBy.put(predecessorPair, distinguishingSequence);
                        addToW(distinguishingSequence);
                    }
                    queue.add(predecessorPair);
                }
            }
        }

        var reducedW = reduceW(conjectureProxy, new ArrayList<>(newW));
        assert conjectureProxy.isWSet(reducedW);
        return reducedW;
    }



    private void addToW(InputSequence is) {
        for (var w : newW) {
            if (w.startsWith(is)) return;
        }
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
        var s1predcessors = reverseInputMapping.getOrDefault(p.s1(), Map.of()).getOrDefault(input, Set.of());
        var s2predcessors = reverseInputMapping.getOrDefault(p.s0(), Map.of()).getOrDefault(input, Set.of());

        return s1predcessors.stream()
                .flatMap(s1 -> s2predcessors.stream().map(s2 -> new StatePair(s1, s2)))
                .filter(pair -> !pair.s1().equals(pair.s0()))
                .collect(Collectors.toSet());
    }


}
