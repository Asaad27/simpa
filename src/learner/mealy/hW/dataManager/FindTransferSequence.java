package learner.mealy.hW.dataManager;

import automata.mealy.InputSequence;
import tools.loggers.LogManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class FindTransferSequence {

    private static int compareNodesLexicographically(Node t1, Node t2) {
        String inputSequence1 = String.join("", t1.path.sequence);
        String inputSequence2 = String.join("", t2.path.sequence);
        return inputSequence1.compareTo(inputSequence2);
    }

    /**
     * Searches the closest reachable state using Dijkstra's algorithm. If there are mutliple transfer sequences of
     * the same shortest length, the lexciographically smallest is returned.
     *
     * @param s the state to search from
     * @return an input sequence leading to not fully known state
     * @throws ConjectureNotConnexException if no state with unknown transitions can be reached
     */
    public static InputSequence getTransferSequenceToNextNotFullyKnownState(FullyQualifiedState s)
            throws ConjectureNotConnexException {
        return findShortestTransfers(s).min(FindTransferSequence::compareNodesLexicographically).get().path;
    }

    private static Stream<Node> findShortestTransfers(FullyQualifiedState s) throws ConjectureNotConnexException {
        assert s != null;

        PriorityQueue<Node> paths = new PriorityQueue<Node>(10,
                new PathComparator());
        List<FullyQualifiedState> reachedStates = new ArrayList<FullyQualifiedState>();
        paths.add(new Node(s, new InputSequence()));
        while (!paths.isEmpty()) {
            Node current = paths.poll();
            if (reachedStates.contains(current.end))
                continue;
            reachedStates.add(current.end);
            if (!current.end.getUnknowTransitions().isEmpty()) {
                LogManager.logInfo("shortest alpha is " + current
                        + " that lead in " + current.end);
                return Stream.concat(Stream.of(current),
                        paths.stream() //take all paths to states with unknown transitions of same length
                                .filter(n -> !n.end.getUnknowTransitions().isEmpty())
                                .takeWhile(n -> n.path.getLength() == current.path.getLength()));
            }
            for (FullyKnownTrace t : current.end.getVerifiedTrace()) {
                Node childNode = new Node(t.getEnd());
                childNode.end = t.getEnd();
                childNode.path = new InputSequence();
                childNode.path.addInputSequence(current.path);
                childNode.path.addInputSequence(t.getTrace()
                        .getInputsProjection());
                paths.add(childNode);
            }
        }
        throw new ConjectureNotConnexException(reachedStates);
    }

    private static class Node {
        public InputSequence path;
        public FullyQualifiedState end;

        public Node(FullyQualifiedState end) {
            this.end = end;
            this.path = new InputSequence();
        }

        public Node(FullyQualifiedState end, InputSequence path) {
            this.path = path;
            this.end = end;
        }

        public boolean equals(Object o) {
            if (o instanceof Node)
                return equals((Node) o);
            return false;
        }

        public boolean equals(Node o) {
            return path.equals(o.path) && end.equals(o.end);
        }

        public String toString() {
            return path.toString() + "→" + end.toString();
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }
    }

    static class PathComparator implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            return o1.path.getLength() - o2.path.getLength();
        }
    }
}
