package drivers.mealy.simulation;

import automata.mealy.GenericInputSequence;
import automata.mealy.Mealy;
import automata.mealy.distinctionStruct.DistinctionStruct;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import options.OptionsGroup;
import tools.loggers.ILogger;
import tools.loggers.LogManager;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger to log traces of subinference, h, W and intermediate conjectures
 */
public class TransitionLogger implements ILogger {
    private final Path logFolder;
    private Path currentInferenceDir;
    private int subinference;
    private Writer subinferenceTraceWriter;
    private Writer completeTrace;
    private OptionsGroup cliOptions;

    public TransitionLogger(Path logFolder) {
        this.logFolder = logFolder;
    }

    @Override
    public void startNewInference() {
        String name = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        try {
            currentInferenceDir = Files.createDirectory(logFolder.resolve(name));
            subinference = 0;
            Path completeFile = Files.createFile(currentInferenceDir.resolve("completeTrace"));
            completeTrace = Files.newBufferedWriter(completeFile);
            Path optionsFile = Files.createFile(currentInferenceDir.resolve("options"));
            if (cliOptions != null) Files.writeString(optionsFile, cliOptions.buildBackCLILine(false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startNewSubInference() {
        flush();
        String traceFileName = String.format("%03d_trace", subinference++);
        //name = "final";
        try {
            Path currentIterationLogFile = currentInferenceDir.resolve(traceFileName);
            // Files.deleteIfExists(currentIterationLogFile);
            Path currentLogFile = Files.createFile(currentIterationLogFile);
            subinferenceTraceWriter = Files.newBufferedWriter(currentLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logCLIOptions(OptionsGroup allOptions) {
        cliOptions = allOptions;
    }

    private void flush() {
        if (subinferenceTraceWriter != null) {
            try {
                subinferenceTraceWriter.flush();
                completeTrace.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void logH(GenericInputSequence h) {
        Path hFile = currentInferenceDir.resolve(String.format("%03d_h", subinference));
        try {
            Files.writeString(hFile, h.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void withReduceTracesDisabled(Runnable r) {
        int oldValue = Options.REDUCE_DISPLAYED_TRACES;
        Options.REDUCE_DISPLAYED_TRACES = 0;
        r.run();
        Options.REDUCE_DISPLAYED_TRACES = oldValue;
    }

    @Override
    public void logW(DistinctionStruct<? extends GenericInputSequence, ?
            extends GenericInputSequence.GenericOutputSequence> w) {
        Path wFile = currentInferenceDir.resolve(String.format("%03d_W", subinference));
        withReduceTracesDisabled(() -> {
            try {
                Files.writeString(wFile, w.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void logConjecture(LmConjecture conjecture) {
        Path file = currentInferenceDir.resolve(String.format("%03d_conjecture", subinference));
        withReduceTracesDisabled(() -> {
            try (Writer writer = Files.newBufferedWriter(file)) {
                conjecture.writeInDotFormat(writer, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void logStat(String s) {
        Path statsFile = currentInferenceDir.resolve("stats");
        try {
            Files.writeString(statsFile, s + "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logEnd() {
        flush();
    }

    public void logRequest(String in, String out, int n) {
        String logLine = in + "/" + out + "\n";
        try {
            subinferenceTraceWriter.append(logLine);
            completeTrace.append(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
