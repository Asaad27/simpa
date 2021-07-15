package learner.mealy.hW.refineW;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;
import learner.mealy.hW.refineW.ProductMachine.Node;

import java.util.*;

import static learner.mealy.hW.refineW.PruneW.reduceW;

public class GenWProd implements WSetOptimization {
    private ProductMachine pm;
    private ConjectureWrapper conjectureWrapper;
    private Set<StatePair> distinguishedStatePairs;
    private LmConjecture conjecture;

    @Override
    public Collection<InputSequence> computeSmallerWSet(TotallyFixedW wSet, LmConjecture conjecture) {
        this.conjecture = conjecture;
        pm = new ProductMachine(conjecture);
        conjectureWrapper = new ConjectureWrapper(conjecture);
        List<InputSequence> W = new ArrayList<>();
        List<State> states = conjecture.getStates();


        for (int i = 0; i < states.size(); ++i) {
            for (int j = 0; j < i; ++j) {
                var pair = new StatePair(states.get(i), states.get(j));
                if (areDistinguished(W, pair)) continue;
                W.add(findPath(pair));
            }
        }
        W = reduceW(conjectureWrapper, W);
        return W;
    }

    private boolean areDistinguished(List<InputSequence> W, StatePair pair) {
        for (var w : W) {
            if (!conjectureWrapper.apply(w, pair.s0()).equals(conjectureWrapper.apply(w, pair.s1()))) {
                return true;
            }
        }
        return false;
    }


    private InputSequence findPath(StatePair pair) {
        //BFS
        Map<Node, String> parentInput = new HashMap<>();
        Map<Node, Node> parent = new HashMap<>();
        Queue<Node> queue = new ArrayDeque<>();
        Node start = pm.getOrCreateNode(pair);
        queue.add(start);
        while (!queue.isEmpty()) {
            var current = queue.remove();
            for (String input : conjecture.getInputSymbols()) {
                if (current.isFail(input)) {
                    //found distinguihsing sequence, reconstruct it by backtracking
                    var distinguishingSequence = new InputSequence(input);
                    while (!current.equals(start)) {
                        distinguishingSequence.addInput(parentInput.get(current));
                        current = parent.get(current);
                    }
                    Collections.reverse(distinguishingSequence.sequence);
                    return distinguishingSequence;
                }
                var target = current.getTransitionTargetState(input);
                if (!parent.containsKey(target)) { //was not visited
                    parent.put(target, current);
                    parentInput.put(target, input);
                    queue.add(target);
                }
            }
        }
        throw new IllegalStateException("No distinguishing sequence found for state pair");
    }

}
