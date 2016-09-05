package WSetFinder.TransparentFinder.WeightFunction;

import automata.mealy.InputSequence;

import java.util.List;

/**
 * Created by Jean Bouvattier on 13/07/16.
 * no idea what it does, need debugging and cross validation
 * user should be careful as behaviour is still undetermined.
 */
public class WSetLength extends WeightFunction{
    @Override
    public int weight(List<InputSequence> wSet) {
        return wSet.size();
    }
}
