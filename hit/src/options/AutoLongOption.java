package options;

import options.valueHolders.LongHolder;

public class AutoLongOption extends AutoOption<Long, LongHolder> {

	public AutoLongOption(String argument, String name, String description,
			Long defaultValue) {
		super(argument, new LongHolder(name, description, defaultValue));
	}

}
