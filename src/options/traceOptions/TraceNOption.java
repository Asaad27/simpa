package options.traceOptions;

import options.IntegerOption;
import options.OptionCategory;

public class TraceNOption extends IntegerOption {
    private static final String defaultDescription = "The N value for selecting the best initial W-Set. The computed initial W-Set from the trace will have N sequences.";

    public TraceNOption(int defaultValue) {
        this();
        setDefaultValue(defaultValue);
    }

    public TraceNOption() {
        super("--trace_n", "Selecting the best N Ws from trace.", defaultDescription, 0);
        setCategory(OptionCategory.ALGO_COMMON);
        setDefaultValue(0);
    }
}
