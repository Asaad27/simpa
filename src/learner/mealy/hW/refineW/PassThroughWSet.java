package learner.mealy.hW.refineW;

import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

import java.util.Collection;

public class PassThroughWSet implements WSetOptimization {

    @Override
    public void optimizeW(DistinctionStruct<? extends GenericInputSequence, ?
            extends GenericInputSequence.GenericOutputSequence> wSet, LmConjecture conjecture) {
        //intentionally do nothing here
    }

    @Override
    public Collection<InputSequence> computeSmallerWSet(TotallyFixedW wSet, LmConjecture conjecture) {
        throw new IllegalStateException("should not be called, since we do not reduce W");
    }
}