package learner.mealy.hW.refineW;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;
import tools.loggers.LogManager;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReduceW implements WSetOptimization {

    private Map<State, Map<InputSequence, OutputSequence>> cache;
    private LmConjecture conjecture;
    private int numberOfCalls;
    private final int samplingRate;

    /**
     * A new instance to reduce W. The parameter k determines, that the reduce algorithm is performed only on every
     * k-th call, i.e. the number of executing reduced is decreased by factor k.
     *
     * @param samplingRate k, default = 1
     */
    public ReduceW(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public ReduceW() {
        this(1);
    }

    private boolean runThisReduce() {
        return samplingRate != 0 && numberOfCalls++ % samplingRate == 0;
    }

    @Override
    public void optimizePresetW(TotallyFixedW wSet, LmConjecture conjecture) {
        if (!conjecture.isFullyKnown()) {
            LogManager.logInfo("Skip reducing W, because Conjecture is not complete");
            return;
        }
        if (!runThisReduce()) {
            LogManager.logInfo("Intentionally skip reducing W.");
            return;
        }
        int size = wSet.size();
        int totalLength = wSet.stream().mapToInt(InputSequence::getLength).sum();

        List<InputSequence> tentativeW = new ArrayList<>(wSet);
        this.conjecture = conjecture;
        cache = new HashMap<>();

        while (true) {
            var smallerW = oneSeqOut(tentativeW).filter(this::isCharacterizing).findAny();
            if (smallerW.isEmpty()) break;
            tentativeW = smallerW.get();
        }

        while (true) {
            var smallerW = oneEventOut(tentativeW).filter(this::isCharacterizing).findAny();
            if (smallerW.isEmpty()) break;
            tentativeW = smallerW.get();
        }
        LogManager.logInfo(String.format("Reduced W. Old size %d (%d). New size: %d (%d)", size, totalLength,
                tentativeW.size(), tentativeW.stream().mapToInt(InputSequence::getLength).sum()));

        wSet.clear();
        wSet.addAll(tentativeW);
        if (wSet.isEmpty()) {
            wSet.add(new InputSequence());
        }
        LogManager.logInfo("New W:" + wSet);
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

    private Stream<List<InputSequence>> oneSeqOut(List<InputSequence> w) {
        return IntStream.range(0, w.size()).mapToObj(i -> {
            var res = new ArrayList<>(w.subList(0, i));
            res.addAll(w.subList(i + 1, w.size()));
            return res;
        });
    }

    private Stream<List<InputSequence>> oneEventOut(List<InputSequence> w) {
        return IntStream.range(0, w.size()).mapToObj(i -> {
            var res = new ArrayList<>(w);
            var shorterSequence = new ArrayList<>(w.get(i).sequence.subList(0, w.get(i).getLength() - 1));
            res.set(i, new InputSequence(shorterSequence));
            return res;
        });
    }
}
