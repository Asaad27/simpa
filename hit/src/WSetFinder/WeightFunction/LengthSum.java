package WSetFinder.WeightFunction;

import automata.mealy.InputSequence;

import java.util.List;

/**
 * Created by Jean Bouvattier on 13/07/16.
 * return max word length
 */
public class LengthSum extends WeightFunction{

    @Override
    public int weight(List<InputSequence> wSet) {
        int sum = 0;
        for (InputSequence word: wSet){
            sum += word.getLength();
        }
        return sum;
    }
}
