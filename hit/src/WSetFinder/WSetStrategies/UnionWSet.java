package WSetFinder.WSetStrategies;

import WSetFinder.Node;
import WSetFinder.WeightFunction.WeightFunction;
import automata.mealy.InputSequence;
import drivers.mealy.transparent.TransparentMealyDriver;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jean Bouvattier on 04/07/16.
 * make an union of all input-sequence from a splitting tree to make a w-set
 */
public class UnionWSet extends WSetStrategy {


    public UnionWSet(TransparentMealyDriver driver, boolean doRefine, WeightFunction function) {
        super(driver, doRefine, function);
    }

    @Override
    public List<InputSequence> calculateWSet() {
        List<InputSequence> separatingSequences = new LinkedList<>();
        for(Node node : tree.getNodes()){
            boolean doAdd = true;
            InputSequence sequence = node.getSequence();
            if (!node.isLeaf()){
                for (InputSequence oldSequence : separatingSequences){
                    if(oldSequence.startsWith(sequence)){
                        doAdd = false;
                        break;
                    }
                    if(sequence.startsWith(oldSequence)){
                        separatingSequences.remove(oldSequence);
                        doAdd = true;
                        break;
                    }
                }
                if (doAdd){
                    separatingSequences.add(sequence);
                }
            }
        }
        return separatingSequences;
    }
}
