package WSetFinder.WSetStrategies.Crawler.inputStrategy;

import WSetFinder.SplittingTree;
import WSetFinder.WSetStrategies.Crawler.States;

import automata.mealy.InputSequence;

import java.util.List;

/**
 * Created by Jean Bouvattier on 07/07/16.
 * A strategy to choose input for searching tree.
 */
public abstract class InputStrategy {

    public abstract List<InputSequence> choseCandidates(States states, SplittingTree tree);

}
