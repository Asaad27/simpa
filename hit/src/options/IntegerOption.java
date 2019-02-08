package options;

import options.valueHolders.IntegerHolder;

public class IntegerOption
		extends SingleValueArgumentOption<Integer, IntegerHolder> {

	public IntegerOption(String argument, String name, String description,
			int defaultValue) {
		super(argument, new IntegerHolder(name, description, defaultValue));
	}

	public void setMaximum(int max) {
		value.setMaximum(max);
	}

	public void setMinimum(int min) {
		value.setMinimum(min);
	}

}
