package drivers.mealy.simulation;

import learner.mealy.LmConjecture;
import learner.mealy.hW.dataManager.SimplifiedDataManager;
import tools.loggers.ILogger;
import tools.loggers.LogManager;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransitionLogger implements ILogger {
    private final Path logFolder;
    private Path currentInferenceDir;
    private int backboneIteration;
    private Writer currentLogWriter;
    private Writer statsWriter;
    private String h;
    private String W;
    private Writer completeTrace;

    public TransitionLogger(Path logFolder) {
        this.logFolder = logFolder;
        LogManager.addLogger(this);
    }

    public void startNewInference() {
        String name = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        try {
            currentInferenceDir = Files.createDirectory(logFolder.resolve(name));
            backboneIteration = 0;
            Path statsFile = Files.createFile(currentInferenceDir.resolve("stats"));
            statsWriter = Files.newBufferedWriter(statsFile);
            Path completeFile = Files.createFile(currentInferenceDir.resolve("completeTrace"));
            completeTrace = Files.newBufferedWriter(completeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startBackboneIteration() {
        flush();
        String name = String.format("backboneIteration_%03d", backboneIteration++);
        name = "final";
        try {
            Path currentIterationLogFile = currentInferenceDir.resolve(name);
            Files.deleteIfExists(currentIterationLogFile);
            Path currentLogFile = Files.createFile(currentIterationLogFile);
            currentLogWriter = Files.newBufferedWriter(currentLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flush() {
        if (currentLogWriter != null) {
            try {
                currentLogWriter.flush();
                statsWriter.flush();
                completeTrace.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void logStep(int step, Object o) {
        if ("Starting new learning".equals(o)) {
            startBackboneIteration();
        }
    }

    @Override
    public void logInfo(String s) {
        if (s.startsWith("Maximum counter example length")){
            startNewInference();
        } else if (s.startsWith("Using homing sequence")) {
            h = s;
        } else if (s.startsWith("Using characterization struct")) {
            W = s;
        }
    }

    @Override
    public void logStat(String s) {
        try {
            statsWriter.append(s).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logEnd() {
        try {
            statsWriter.append("\n")
                    .append(W).append("\n")
                    .append(h).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        flush();
    }

    public void logRequest(String in, String out, int n) {
        String logLine = in + "/" + out +"\n";
        try {
            currentLogWriter.append(logLine);
            completeTrace.append(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
