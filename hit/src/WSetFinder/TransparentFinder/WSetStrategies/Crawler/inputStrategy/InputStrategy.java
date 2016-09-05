package WSetFinder.TransparentFinder.WSetStrategies.Crawler.inputStrategy;

import WSetFinder.TransparentFinder.SplittingTree;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.States;

import automata.mealy.InputSequence;

import java.util.List;

/**
 * Created by Jean Bouvattier on 07/07/16.
 */
public abstract class InputStrategy {

    public abstract List<InputSequence> choseCandidates(States states, SplittingTree tree);

}
