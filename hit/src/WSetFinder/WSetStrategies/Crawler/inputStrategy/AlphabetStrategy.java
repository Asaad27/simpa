package WSetFinder.WSetStrategies.Crawler.inputStrategy;

import WSetFinder.SplittingTree;
import WSetFinder.WSetStrategies.Crawler.States;
import automata.mealy.InputSequence;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Will try every input from the input dictionary
 */
public class AlphabetStrategy extends InputStrategy {
    public static long spentTime = 0;
    @Override
    public List<InputSequence> choseCandidates(States states, SplittingTree tree) {
        long start_time = System.currentTimeMillis();
        List<InputSequence> candidates = tree.getDriver().getInputSymbols().stream()
                .map(InputSequence::new).collect(Collectors.toList());
        long end_time = System.currentTimeMillis();
        spentTime += (end_time - start_time);
        return candidates;

    }
}
