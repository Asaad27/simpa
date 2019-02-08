package options;

import options.valueHolders.NumberHolder;

public abstract class NumberOption<T extends Number & Comparable<T>, H extends NumberHolder<T>>
		extends SingleValueArgumentOption<T, H> {

	public NumberOption(String argument, H holder) {
		super(argument, holder);
	}

	public void setMaximum(T max) {
		value.setMaximum(max);
	}

	public void setMinimum(T min) {
		value.setMinimum(min);
	}

}
