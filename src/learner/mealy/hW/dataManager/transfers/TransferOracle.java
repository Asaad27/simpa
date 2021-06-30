package learner.mealy.hW.dataManager.transfers;

import automata.mealy.InputSequence;
import learner.mealy.hW.HWOptions;
import learner.mealy.hW.dataManager.ConjectureNotConnexException;
import learner.mealy.hW.dataManager.FullyQualifiedState;

import java.io.IOException;
import java.util.Comparator;

public abstract class TransferOracle {

    public static TransferOracle getTransferOracle(HWOptions options) {
        switch (options.getFindPathStrategy()) {
            case "lexicographic":
                return new LexicographicOrder();
            case "interactive":
                return new Interactive();
            case "interactive-record":
                try {
                    return new Interactive(options.getTransferSequenceRecord().get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                throw new IllegalArgumentException("Unknown value for find-path strategy: " + options.getFindPathStrategy());
        }
    }

    protected static int compareNodesLexicographically(Node t1, Node t2) {
        String inputSequence1 = String.join("", t1.path.sequence);
        String inputSequence2 = String.join("", t2.path.sequence);
        return inputSequence1.compareTo(inputSequence2);
    }

    public abstract InputSequence getTransferSequenceToNextNotFullyKnownState(FullyQualifiedState s) throws ConjectureNotConnexException;

    static class Node {
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
