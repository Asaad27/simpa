package options.traceOptions;

import options.IntegerOption;
import options.OptionCategory;

public class TraceKOption extends IntegerOption {
    private static final String defaultDescription = "The K value for slicing the trace. Each sequence will have at most K members.";

    public TraceKOption(int defaultValue) {
        this();
        setDefaultValue(defaultValue);
    }

    public TraceKOption() {
        super("--trace_k", "Slicing the trace by K.", defaultDescription, 0);
        setCategory(OptionCategory.ALGO_COMMON);
        setDefaultValue(0);
    }
}
