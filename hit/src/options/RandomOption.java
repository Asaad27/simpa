package options;

import options.valueHolders.SeedHolder;
import tools.RandomGenerator;

public class RandomOption extends SingleValueArgumentOption<Long, SeedHolder> {

	public RandomOption(String argument, String name) {
		super(argument, new SeedHolder(name));
		this.description = getValueHolder().getName() + ".";
		assert !this.description.endsWith("..") : "two dots at end of string '"
				+ description + "'";
	}

	public RandomGenerator getRand() {
		return value;
	}
}
