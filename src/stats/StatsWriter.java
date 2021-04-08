package stats;

import main.simpa.Options;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class StatsWriter implements Closeable {
    private Writer writer;

    private String buildFilename(StatsEntry stats) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return  Options.getStatsCSVDir() + File.separator
                + stats.getClass().getName() + "_" + formatter.format(date) + ".csv";
    }

    public void append(StatsEntry stats) throws IOException {
        if (writer == null) {
            File file = new File(buildFilename(stats));
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            writer = new BufferedWriter(new FileWriter(file));
            writer.append(stats.getCSVHeader()).append("\n");
        }
        writer.append(stats.toCSV()).append("\n");
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(writer)) writer.close();
    }
}
