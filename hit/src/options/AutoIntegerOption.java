package options;

import options.valueHolders.IntegerHolder;

public class AutoIntegerOption extends AutoOption<Integer, IntegerHolder> {

	public AutoIntegerOption(String argument, String name, String description,
			int defaultValue) {
		super(argument, new IntegerHolder(name, description, defaultValue));
	}

}
