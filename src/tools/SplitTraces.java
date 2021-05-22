package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class SplitTraces {
    private final Path traceFile;
    private BufferedReader trace;
    private List<String> h;
    private BufferedWriter currentWriter;
    private Map<List<String>, Integer> responses = new HashMap<>();
    private Map<List<String>, Integer> responseNumbers;
    private List<String> mostFrequentResponse;
    private boolean onlySplitForRMax;
    private Path outputFile;
    private BufferedReader currentReader;

    public SplitTraces(Path traceFile, Path outputFile, List<String> h,
                       boolean onlySplitForRMax) throws IOException {
        this.h = h;
        this.outputFile = outputFile;
        this.traceFile = traceFile;
        this.onlySplitForRMax = onlySplitForRMax;
    }

    public void run() throws IOException {
        forEachWindow(this::countResponses);
        if (responses.isEmpty()) {
            System.err.println("homing sequence was not found: " + h);
            return;
        }
        responseNumbers = enumerateResponses();
        mostFrequentResponse = mostFrequentResponse();
        this.currentWriter = Files.newBufferedWriter(outputFile);
        forEachWindow(this::splitTrace);
        currentWriter.flush();
        currentWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Path folder = Path.of("/home/moritz/Studium/Masterarbeit/scanetteTraces/");
        for (int i = 1; i < 11; ++i) {
            System.out.println(i);
            Path finalTrace = folder.resolve(String.format("%02d_final", i));
            Path splitTrace = folder.resolve(String.format("%02d_split", i));
            Path statFile = folder.resolve(String.format("%02d_stats", i));
            List<String> h = extractH(statFile);
            SplitTraces splitTraces = new SplitTraces(finalTrace, splitTrace, h, false);
            splitTraces.run();
            splitTrace = folder.resolve(String.format("%02d_split_same_R", i));
            splitTraces = new SplitTraces(finalTrace, splitTrace, h, true);
            splitTraces.run();
        }
    }

    private static List<String> extractH(Path statsFile) throws IOException {
        try (Scanner s = new Scanner(Files.newInputStream(statsFile))) {
            s.nextLine();
            s.next();
            return Arrays.asList(s.next().split("\\."));
        }
    }



    public void forEachWindow(BiConsumer<LinkedList<String>, LinkedList<String>> process) throws IOException {
        trace = Files.newBufferedReader(traceFile);
        LinkedList<String> input = new LinkedList<>();
        LinkedList<String> output = new LinkedList<>();
        process.accept(input, output);
        while (trace.ready()) {
            if (input.size() == h.size()) {
                input.removeFirst();
                output.removeFirst();
            }
            String[] split = trace.readLine().split("/");
            input.add(split[0]);
            output.add(split[1]);
            process.accept(input, output);
        }
    }

    public void countResponses(List<String> in, List<String> out) {
        if (!h.equals(in)) return;
        List<String> key = List.copyOf(out);
        responses.put(key, responses.getOrDefault(key, 0) + 1);
    }

    public void splitTrace(LinkedList<String> in, LinkedList<String> out) {
        if (in.size() == 0) return;
        try {
            currentWriter.append(String.format("%s/%s ", in.getLast(), out.getLast()));
            if (in.equals(h) && (!onlySplitForRMax || out.equals(mostFrequentResponse))) {
                assert responseNumbers.containsKey(out);
                currentWriter.append("\n");
                currentWriter.append(String.format("%03d, ", responseNumbers.get(out)));
                currentWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> mostFrequentResponse() {
        return responses.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
    }

    public Map<List<String>, Integer> enumerateResponses() {
        List<List<String>> asList = new ArrayList<>(responses.keySet());
        return IntStream.range(0, asList.size())
                .boxed()
                .collect(Collectors.toMap(asList::get, i -> i));
    }

}
