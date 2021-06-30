package learner.mealy.hW.refineW;

import automata.mealy.InputSequence;
import learner.mealy.LmConjecture;

import java.util.HashMap;
import java.util.Map;

/**
 * Prodcut machine (PM) for a given Mealy machine. The product machine contains a node for each pair of states of the
 * wrapped machine. A prodcut machine node has ang edge for an input i, if the corresponding states have the same
 * output o for input i. Otherwise the edge is marked as *fail*.
 * <p>
 * The PM caches repsonses from the wrapped machine.
 */
public class ProductMachine {
    LmConjecture machine;
    Map<StatePair, Node> nodes;

    public ProductMachine(LmConjecture machine) {
        this.machine = machine;
        nodes = new HashMap<>();

    }

    public Node getOrCreateNode(StatePair states) {
        return nodes.computeIfAbsent(states, Node::new);
    }

    /**
     * Node in a product machine. Corresponds to a pair of states in the real machine.
     */
    public class Node {
        private final StatePair states;
        Map<String, Node> transition = new HashMap<>();
        Map<String, String> output = new HashMap<>();

        public Node(StatePair states) {
            this.states = states;
        }

        StatePair getStates() {
            return states;
        }

        boolean isSameStatePair() {
            return states.s0().equals(states.s1());
        }

        String getTransitionOutput(String input) {
            if (isFail(input)) {
                throw new IllegalStateException("States in this prodcut state do not have a common output for input "
                        + input);
            }
            return output.computeIfAbsent(input, x -> machine.apply(new InputSequence(input), states.s0()).toString());
        }

        boolean isFail(String input) {
            var is = new InputSequence(input);
            return !machine.apply(is, states.s0()).equals(machine.apply(is, states.s1()));
        }

        Node getTransitionTargetState(String input) {
            return transition.computeIfAbsent(input, x -> {
                var is = new InputSequence(input);
                var end = new StatePair(machine.applyGetState(is, states.s0()), machine.applyGetState(is, states.s1()));
                return getOrCreateNode(end);
            });
        }
    }

}

