package datamining;

import options.PercentageOption;

public class SupportMinOption extends PercentageOption {

	public SupportMinOption(String argPrefix) {
		super("--" + argPrefix + "minsupport", "Minimal support for relation",
				"Minimal support for relation.", 20);
	}

}
