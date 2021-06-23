package learner.mealy.hW.refineW;

import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;
import tools.loggers.LogManager;

import java.util.Collection;

public interface WSetOptimization {

    default void optimizeW(DistinctionStruct<? extends GenericInputSequence, ?
            extends GenericInputSequence.GenericOutputSequence> wSet, LmConjecture conjecture) {

        if(!(wSet instanceof  TotallyFixedW)) {
            throw new IllegalStateException("W set reduction not implemented for adaptive W yet");
        }
        var w = (TotallyFixedW) wSet;

        if (!conjecture.isFullyKnown()) {
            LogManager.logInfo("Skip reducing W, because Conjecture is not complete");
            return;
        }

        int size = w.size();
        int totalLength = w.stream().mapToInt(InputSequence::getLength).sum();
        var tentativeW = computeSmallerWSet(w, conjecture);

        LogManager.logInfo(String.format("Reduced W. Old size %d (%d). New size: %d (%d)", size, totalLength,
                tentativeW.size(), tentativeW.stream().mapToInt(InputSequence::getLength).sum()));

        w.clear();
        w.addAll(tentativeW);
        if (w.isEmpty()) {
            w.add(new InputSequence());
        }
        LogManager.logInfo("New W:" + w);
    }

    Collection<InputSequence> computeSmallerWSet(TotallyFixedW wSet, LmConjecture conjecture);

}
