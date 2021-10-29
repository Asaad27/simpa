package options.traceOptions;

import options.FileOption;

public class TraceOption extends FileOption {
    private static final String defaultDescription = "A TXT trace file with one input/output pair per line.";

    public TraceOption() {
        this(defaultDescription);
    }

    public TraceOption(String detailedDescription) {
        super("--trace_file", detailedDescription, null,
                FileSelectionMode.FILES_ONLY, FileExistance.MUST_EXIST);
    }

    @Override
    public String getHelpByArgument(ArgumentDescriptor arg) {
        return defaultDescription;
    }

}
