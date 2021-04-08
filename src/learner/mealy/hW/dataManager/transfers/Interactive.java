package learner.mealy.hW.dataManager.transfers;

import automata.mealy.InputSequence;
import learner.mealy.hW.dataManager.ConjectureNotConnexException;
import learner.mealy.hW.dataManager.FullyKnownTrace;
import learner.mealy.hW.dataManager.FullyQualifiedState;
import tools.loggers.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Interactive extends TransferOracle {
    private final boolean useLog;
    private Iterator<String> record;
    private BufferedWriter writer;
    private int counter = 0;


    public Interactive() {
        useLog = false;
    }

    public Interactive(Path logFile) throws IOException {
        useLog = true;
        if (!Files.exists(logFile)) Files.createFile(logFile);
        record = Files.readAllLines(logFile).iterator();
        writer = Files.newBufferedWriter(logFile, StandardOpenOption.APPEND);
    }

    @Override
    public InputSequence getTransferSequenceToNextNotFullyKnownState(FullyQualifiedState s) throws ConjectureNotConnexException {
        if (useLog && record.hasNext()) {
                counter++;
                return InputSequence.deserialize(record.next());
        }
        var is = askUser(s);
        if (useLog) {
            try {
                writer.append(is.serialize());
                writer.append('\n');
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        counter++;
        return is;
    }

    private InputSequence askUser(FullyQualifiedState s) throws ConjectureNotConnexException {
        List<Node> transfers = findAllTransfers(s);
        transfers.sort(TransferOracle::compareNodesLexicographically);
        System.out.println("Transfer sequence number " + counter);
        if (transfers.size() == 1) {
            System.out.println("Only one transfer available, choosing " + transfers.get(0).path);
            return transfers.get(0).path;
        }
        System.out.println("Possible shortest transfers are: ");
        ListIterator<Node> it = transfers.listIterator();
        while(it.hasNext()) {
            int index = it.nextIndex();
            Node node = it.next();
            Set<String> unknownTransitions = node.end.getUnknowTransitions();
            System.out.println("[" + index + "] " + node.path + " leading to " + node.end +
                    " with unknown transitions" + unknownTransitions);
        }
        System.out.println("Enter a number (ENTER for default = 0) or custom sequence (inputs seperated by .)");
        var defaultChoice = transfers.get(0).path;
        try {
            Scanner in = new Scanner(System.in);
            if (in.hasNext(Pattern.quote(""))) {
                return defaultChoice;
            } else if (in.hasNextInt()) {
                int userChoice = in.nextInt();
                return transfers.get(userChoice).path;
            } else {
                String customSequence = in.next();
                if (transfers.stream().filter(n -> n.path.sequence.toString().equals(customSequence)).findAny().isEmpty()) {
                    LogManager.logWarning("User chose transfer sequence " + customSequence + " which is not among the" +
                            " possible shortest transfer sequences.");
                }
                return new InputSequence(Arrays.asList(customSequence.split("\\.")));
            }
        } catch (InputMismatchException ex) {
            System.err.println(ex.getMessage());
            System.err.println("Invalid input number, using default "+ defaultChoice + " instead.");
            return defaultChoice;
        }
    }


    /**
     * A modified BFS that finds all transfer sequences of the shortest length to a state with unknown transitions.
     * @param s
     * @return
     * @throws ConjectureNotConnexException
     */
    private List<TransferOracle.Node> findAllTransfers(FullyQualifiedState s) throws ConjectureNotConnexException {
        Queue<TransferOracle.Node> queue = new ArrayDeque<>();
        queue.add(new TransferOracle.Node(s));
        Map<FullyQualifiedState, Integer> levels = new HashMap<>();
        levels.put(s, 0);
        while (!queue.isEmpty()) {
            TransferOracle.Node current = queue.poll();
            final int level = levels.get(current.end);
            if (!current.end.getUnknowTransitions().isEmpty()) {
                //There are unknown transitions, take all paths of same length
                return  Stream.concat(Stream.of(current),
                        queue.stream().takeWhile(n -> levels.get(n.end) == level)
                                .filter(n -> !n.end.getUnknowTransitions().isEmpty()))
                        .collect(Collectors.toList());
            }
            for (FullyKnownTrace t : current.end.getVerifiedTrace()) {
                String input = t.getTrace().getInputsProjection().getFirstSymbol();
                FullyQualifiedState nextState = t.getStart().getKnownTransition(input).getEnd();
                levels.putIfAbsent(nextState, level + 1);
                if (levels.get(nextState) == level + 1) {
                    InputSequence path = new InputSequence();
                    path.addInputSequence(current.path);
                    path.addInputSequence(new InputSequence(input));
                    queue.add(new TransferOracle.Node(nextState, path));
                }
            }
        }
        throw new ConjectureNotConnexException(levels.keySet());
    }


}
