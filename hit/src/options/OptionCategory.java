package options;

public enum OptionCategory {
	GLOBAL("GLOBAL OPTIONS :"),
	INFERENCE("INFERENCE OPTIONS :"),
	DRIVER("DRIVER OPTIONS:"),
	ORACLE("ORACLE OPTIONS :"),
	PER_ALGO("SPECIFIC OPTIONS per ALGORITHM"),
	ALGO_LI("OPTIONS for Li"),
	ALGO_COMB("OPTIONS for Combinatorial"),
	ALGO_RS("OPTIONS for Rivest&Schapire"),
	ALGO_LOCW("OPTIONS for LocW algorithm"),
	ALGO_HW("OPTIONS for hW-inference"),
	STATS("Specific STAT OPTIONS :"),

	;
	String title;

	private OptionCategory(String title) {
		this.title = title;
	}
}
