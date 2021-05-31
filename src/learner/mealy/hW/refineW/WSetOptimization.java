package learner.mealy.hW.refineW;

import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

import javax.swing.*;

public interface WSetOptimization {

    public default DistinctionStruct<? extends GenericInputSequence, ? extends GenericInputSequence.GenericOutputSequence> optimizeW(DistinctionStruct<? extends GenericInputSequence, ? extends GenericInputSequence.GenericOutputSequence> wSet, LmConjecture conjecture) {
        if (wSet instanceof  TotallyFixedW) {
            return optimizePresetW((TotallyFixedW) wSet, conjecture);
        } else if (wSet instanceof TotallyAdaptiveW){
            return optimizeAdaptiveW((TotallyAdaptiveW) wSet, conjecture);
        } else {
            throw new IllegalStateException("No refinement strategy for class " + wSet.getClass().getTypeName());
        }
    }

    public default TotallyFixedW optimizePresetW(TotallyFixedW wSet, LmConjecture conjecture) {
        throw new IllegalStateException("refinement for preset W is not implmented");
    }

    public default TotallyAdaptiveW optimizeAdaptiveW(TotallyAdaptiveW wSet, LmConjecture conjecture) {
        throw new IllegalStateException("refinement for adaptive W is not implmented");
    }



}
