package options;

import options.valueHolders.SeedHolder;
import tools.RandomGenerator;

public class RandomOption extends SingleValueArgumentOption<Long, SeedHolder> {

	public RandomOption(String argument, String name) {
		super(argument, new SeedHolder(name));
		this.description = getValueHolder().getName();
	}

	public RandomGenerator getRand() {
		return value;
	}
}
