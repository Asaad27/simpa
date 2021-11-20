package options.traceOptions;

import options.FileOption;
import options.OptionCategory;

import java.io.File;
import java.util.UUID;

public class TraceOption extends FileOption {
    private static final String defaultDescription = "A TXT trace file with one input/output pair per line.";

    public TraceOption() {
        this(defaultDescription);
    }

    public TraceOption(String detailedDescription) {
        super("--trace_file", detailedDescription, getDefaultFile(),
                FileSelectionMode.FILES_ONLY, FileExistance.MUST_EXIST);
        setCategory(OptionCategory.ALGO_COMMON);
    }

    private static File getDefaultFile(){
        try {
            return File.createTempFile(UUID.randomUUID().toString(), "");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getHelpByArgument(ArgumentDescriptor arg) {
        return defaultDescription;
    }

}
