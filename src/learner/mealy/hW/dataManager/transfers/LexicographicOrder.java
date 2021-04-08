package learner.mealy.hW.dataManager.transfers;

import automata.mealy.InputSequence;
import learner.mealy.hW.dataManager.ConjectureNotConnexException;
import learner.mealy.hW.dataManager.FullyKnownTrace;
import learner.mealy.hW.dataManager.FullyQualifiedState;
import tools.loggers.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LexicographicOrder extends TransferOracle {

    @Override
    public InputSequence getTransferSequenceToNextNotFullyKnownState(FullyQualifiedState s) throws ConjectureNotConnexException {
        return findShortestTransfers(s).stream().min(TransferOracle::compareNodesLexicographically).get().path;
    }

    private List<Node> findShortestTransfers(FullyQualifiedState s) throws ConjectureNotConnexException {
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
                                .takeWhile(n -> n.path.getLength() == current.path.getLength()))
                        .collect(Collectors.toList());
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

}
