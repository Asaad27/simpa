package learner.mealy.hW.traceProcessor;

import main.simpa.Options;
import org.antlr.v4.runtime.misc.Interval;
import tools.loggers.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WSetFromTrace {

    static class InputOutputPair {
        public String input;
        public String output;

        public InputOutputPair(String input, String output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InputOutputPair that = (InputOutputPair) o;
            return Objects.equals(input, that.input) && Objects.equals(output, that.output);
        }

        @Override
        public int hashCode() {
            return Objects.hash(input, output);
        }

        @Override
        public String toString() {
            return "InputOutputPair{" +
                    "input='" + input + '\'' +
                    ", output='" + output + '\'' +
                    '}';
        }
    }

    public static class InputSequence extends ArrayList<String> {
    }

    static class OutputSequence extends ArrayList<String> {
    }

    static class InputOutputSequence extends ArrayList<InputOutputPair> {

        public InputOutputSequence(List<TracePoint> tracePoints) {
            var list = tracePoints.stream().map(tracePoint -> tracePoint.inputOutputPair).collect(Collectors.toList());
            addAll(list);
        }

        public InputSequence getInputSequence() {
            return stream().map(inputOutputPair -> inputOutputPair.input).collect(Collectors.toCollection(InputSequence::new));
        }

        public OutputSequence getOutputSequence() {
            return stream().map(inputOutputPair -> inputOutputPair.output).collect(Collectors.toCollection(OutputSequence::new));
        }
    }

    static class SequenceInfo {
        public Integer size;
        public Integer responses;
        public Integer occurrences;

        public SequenceInfo(Integer size, Integer responses, Integer occurrences) {
            this.size = size;
            this.responses = responses;
            this.occurrences = occurrences;
        }
    }

    static class InfoHolder {
        public InputSequence inputSequence;
        public SequenceInfo sequenceInfo;

        public InfoHolder(InputSequence inputSequence, SequenceInfo sequenceInfo) {
            this.inputSequence = inputSequence;
            this.sequenceInfo = sequenceInfo;
        }
    }

    static class PositionsMap extends HashMap<InputOutputSequence, List<Interval>> {
    }

    static class TracePoint {
        public InputOutputPair inputOutputPair;
        public Integer index;

        public TracePoint(InputOutputPair inputOutputPair, Integer index) {
            this.inputOutputPair = inputOutputPair;
            this.index = index;
        }

        @Override
        public String toString() {
            return "TracePoint{" +
                    "inputOutputPair=" + inputOutputPair +
                    ", index=" + index +
                    '}';
        }
    }

    class FileSplitter implements Iterator<List<TracePoint>> {

        private final Iterator<String> fileIterator;
        private final List<TracePoint> traceCache;
        private Integer currentIndex;

        FileSplitter(Iterator<String> fileIterator) {
            this.fileIterator = fileIterator;
            traceCache = new ArrayList<>();
            currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return fileIterator.hasNext();
        }

        @Override
        public List<TracePoint> next() {
            var nxt = fileIterator.next();
            if (traceCache.size() >= k) {
                traceCache.remove(0);
            }
            var IOPair = nxt.split("/");
            var point = new TracePoint(new InputOutputPair(IOPair[0], IOPair[1]), currentIndex++);
            traceCache.add(point);
            return traceCache;
        }
    }


    private final File traceFile;
    private final Integer k;
    private final Integer n;
    private final PositionsMap positionsMap;

    public WSetFromTrace(File traceFile, Integer k, Integer n) {
        this.traceFile = traceFile;
        this.k = k;
        this.n = n;
        positionsMap = new PositionsMap();
    }

    private Set<List<TracePoint>> getAllSubTraces(List<TracePoint> traceChunk) {
        var startIdx = traceChunk.size() - k;
        if (startIdx < 0) startIdx = 0;
        var subTrace = traceChunk.subList(startIdx, traceChunk.size());
        var subTraces = new HashSet<List<TracePoint>>();
        subTraces.add(subTrace);
        while (!subTrace.isEmpty()) {
            var nxt = subTrace.subList(0, subTrace.size());
            subTraces.add(nxt);
            if (subTrace.size() == 1 || subTrace.size() == 0) break;
            subTrace = subTrace.subList(1, subTrace.size());
        }
        return subTraces;
    }

    private void insertDataPosition(InputOutputSequence inputOutputSequence, Interval interval) {
        if (positionsMap.containsKey(inputOutputSequence)) {
            positionsMap.get(inputOutputSequence).add(interval);
            return;
        }
        var list = new ArrayList<Interval>();
        list.add(interval);
        positionsMap.put(inputOutputSequence, list);
    }

    private HashMap<InputSequence, SequenceInfo> getInputSequencesInfo(List<InputSequence> inputSequences) {
        var groups = new HashMap<InputSequence, List<List<Interval>>>();
        positionsMap.forEach((key, value) -> {
            var inputSequence = key.getInputSequence();
            if (groups.containsKey(inputSequence)) {
                groups.get(inputSequence).add(value);
            } else {
                var list = new ArrayList<List<Interval>>();
                list.add(value);
                groups.put(inputSequence, list);
            }
        });
        var info = new HashMap<InputSequence, SequenceInfo>();
        inputSequences.forEach(inputSequence -> {
            var arr = groups.getOrDefault(inputSequence, null);
            if (arr == null) {
                info.put(inputSequence, new SequenceInfo(0, 0, 0));
            } else {
                info.put(inputSequence, new SequenceInfo(
                        inputSequence.size(),
                        arr.size(),
                        arr.stream().map(List::size).reduce(0, Math::addExact)
                ));
            }
        });
        return info;
    }

    private List<InfoHolder> getAllBestWs(List<InputSequence> allInputSequences) {
        var allWsInfo = getInputSequencesInfo(allInputSequences)
                .entrySet()
                .stream()
                .map(info -> new InfoHolder(info.getKey(), info.getValue()));
        return allWsInfo.sorted(
                Comparator
                        .comparingInt(holder -> -((InfoHolder) holder).sequenceInfo.responses)
                        .thenComparing(holder -> ((InfoHolder) holder).sequenceInfo.size)
                        .thenComparingInt(holder -> -((InfoHolder) holder).sequenceInfo.occurrences)
        ).limit(n).collect(Collectors.toList());
    }

    private Stream<String> getTraceStream() throws IOException {
        return Files.newBufferedReader(traceFile.toPath()).lines();
    }

    private List<InfoHolder> getWSetListInfoFromFile() {
        LogManager.logLine();
        LogManager.logInfo("Starting WSet inference from trace: " + traceFile.getAbsolutePath() + " with K=" + k + " and N=" + n);
        try {
            LogManager.logInfo("The trace has " + getTraceStream().count() + " input/output pairs");
            if (Options.getLogLevel() != Options.LogLevel.LOW) {
                LogManager.logInfo("Original Trace File:");
                LogManager.logStream(getTraceStream());
            }
            var initialTime = System.nanoTime();
            var enumerator = new FileSplitter(getTraceStream().iterator());
            enumerator.forEachRemaining(tracePoints -> {
                        var allSubTraces = getAllSubTraces(tracePoints);
                        allSubTraces.forEach(subTracePoints -> {
                            if (!subTracePoints.isEmpty()) {
                                insertDataPosition(
                                        new InputOutputSequence(subTracePoints),
                                        new Interval(subTracePoints.get(0).index, subTracePoints.get(subTracePoints.size() - 1).index)
                                );
                            }
                        });
                    }
            );
            var allInputSequences = positionsMap.keySet().stream().map(InputOutputSequence::getInputSequence).distinct().collect(Collectors.toList());
            var bestWs = getAllBestWs(allInputSequences);
            var finalTime = System.nanoTime();
            LogManager.logInfo("W-Set learning time : " + (finalTime - initialTime) / 1_000_000_000.0 + "s");
            return bestWs;
        } catch (Exception e) {
            LogManager.logInfo("It was not possible to read the trace file: " + traceFile.getAbsolutePath());
            LogManager.logException("Fatal Error", e);
            return null;
        }
    }

    public List<InputSequence> getWSetListFromFile() {
        var wSet = getWSetListInfoFromFile().stream().map(infoHolder -> infoHolder.inputSequence).collect(Collectors.toList());
        LogManager.logInfo("Learned W-Set:");
        LogManager.logList(wSet);
        return wSet;
    }
}
