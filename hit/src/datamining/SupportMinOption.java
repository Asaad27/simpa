package datamining;

import options.PercentageOption;

public class SupportMinOption extends PercentageOption {

	public SupportMinOption() {
		super("--supportmin", "Minimal support for relation", 20);
	}

}
