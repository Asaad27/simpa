package WSetFinder.TransparentFinder.WeightFunction;

import automata.mealy.InputSequence;
import jdk.internal.util.xml.impl.Input;

import java.util.List;

/**
 * Created by Jean Bouvattier on 13/07/16.
 * return max word length
 */
public class MaxLength extends WeightFunction{

    @Override
    public int weight(List<InputSequence> wSet) {
        int max = 0;
        for (InputSequence word: wSet){
            if (word.getLength() >= max ){
                max = word.getLength();
            }
        }
        return max;
    }
}
