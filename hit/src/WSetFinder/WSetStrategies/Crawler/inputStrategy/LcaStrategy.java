package WSetFinder.WSetStrategies.Crawler.inputStrategy;

import WSetFinder.Node;
import WSetFinder.SplittingTree;
import WSetFinder.WSetStrategies.Crawler.States;
import automata.mealy.InputSequence;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Will return every minimal word known to be able to distinct state
 * warning : should not be used with depth > 1
 */
public class LcaStrategy extends InputStrategy{
    public static long spentTime = 0;
    private Set<InputSequence> candidates = null;
    @Override
    public List<InputSequence> choseCandidates(States states, SplittingTree tree)
    {
        if (candidates == null){
            candidates = new HashSet<>();
            candidates.addAll(tree.getNodes().stream()
                    .filter(node -> !node.isLeaf())
                    .map(Node::getSequence)
                    .collect(Collectors.toList()));
        }
        return new ArrayList<>(candidates);
    }
}
