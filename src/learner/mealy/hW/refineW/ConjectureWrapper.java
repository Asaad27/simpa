package learner.mealy.hW.refineW;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmConjecture;

import java.util.*;

/**
 * Proxy for a LmConjecture that caches outputs for input sequences.
 */
public class ConjectureWrapper {
    private final LmConjecture conjecture;
    private final Map<State, Map<InputSequence, OutputSequence>> cache;

    public ConjectureWrapper(LmConjecture conjecture) {
        this.conjecture = conjecture;
        cache = new HashMap<>();
    }

    public LmConjecture getConjecture() {
        return conjecture;
    }

    public boolean isWSet(Collection<InputSequence> w) {
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

    public OutputSequence apply(InputSequence is, State s) {
        return cache.computeIfAbsent(s, k -> new HashMap<>())
                .computeIfAbsent(is, k -> conjecture.apply(k, s));
    }

    public Optional<String> findDistinguishingInput(State s1, State s2) {
        for (var in : conjecture.getInputSymbols()) {
            var o1 = conjecture.getTransitionFromWithInput(s1, in).getOutput();
            var o2 = conjecture.getTransitionFromWithInput(s2, in).getOutput();
            if (!o1.equals(o2)) {
                return Optional.of(in);
            }
        }
        return Optional.empty();
    }
}
