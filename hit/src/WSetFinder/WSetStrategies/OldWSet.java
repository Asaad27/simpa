package WSetFinder.WSetStrategies;

import WSetFinder.WeightFunction.LocaliseWeightFunction;
import automata.mealy.InputSequence;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.noReset.NoResetLearner;

import java.util.List;

/**
 * Created by Jean Bouvattier on 05/07/16.
 * old deprecated w-set calculator
 */
public class OldWSet extends WSetStrategy {


    public OldWSet(TransparentMealyDriver driver, boolean doRefine, LocaliseWeightFunction function) {
        super(driver,doRefine,function);
    }

    @Override
    public List<InputSequence> calculateWSet() {
        return NoResetLearner.computeCharacterizationSet(tree.getDriver());
    }
}