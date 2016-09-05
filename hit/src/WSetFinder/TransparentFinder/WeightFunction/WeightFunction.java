package WSetFinder.TransparentFinder.WeightFunction;

import automata.mealy.InputSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public abstract class WeightFunction {
    /**
     * return the weight of a wSet which should be used in decision functions
     * @param wSet
     * @return
     */
    public abstract int weight(List<InputSequence> wSet);
}
