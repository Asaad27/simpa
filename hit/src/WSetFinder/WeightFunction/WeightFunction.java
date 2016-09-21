package WSetFinder.WeightFunction;

import automata.mealy.InputSequence;

import java.util.List;

/**
 */
public abstract class WeightFunction {
    /**
     * @return the weight of a wSet which should be used in decision functions
     */
    public abstract int weight(List<InputSequence> wSet);
}
