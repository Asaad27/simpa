package options;

import options.valueHolders.LongHolder;

public class LongOption extends NumberOption<Long, LongHolder> {

	public LongOption(String argument, String name, String description,
			Long defaultValue) {
		super(argument, new LongHolder(name, description, defaultValue));
	}

}
