package drivers.mealy.simulation;

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

public class TransitionLogger implements ILogger {
    private Path logFolder;
    private Path currentInferenceDir;
    private int backboneIteration;
    private Path currentLogFile;
    private Writer currentLogWriter;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startBackboneIteration() {
        flush();
        String name = String.format("backboneIteration_%03d", backboneIteration++);
        try {
            currentLogFile = Files.createFile(currentInferenceDir.resolve(name));
            currentLogWriter = Files.newBufferedWriter(currentLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flush() {
        if (currentLogWriter != null) {
            try {
                currentLogWriter.flush();
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
        }
    }

    @Override
    public void logEnd() {
        flush();
    }

    public void logRequest(String in, String out, int n) {
        String logLine = in + "/" + out +"\n";
        try {
            currentLogWriter.append(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
