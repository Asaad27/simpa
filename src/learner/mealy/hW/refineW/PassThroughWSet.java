package learner.mealy.hW.refineW;

import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

public class PassThroughWSet implements WSetOptimization {

    @Override
    public TotallyFixedW optimizePresetW(TotallyFixedW wSet, LmConjecture conjecture) {
        return wSet;
    }

    @Override
    public TotallyAdaptiveW optimizeAdaptiveW(TotallyAdaptiveW wSet, LmConjecture conjecture) {
        return wSet;
    }
}
