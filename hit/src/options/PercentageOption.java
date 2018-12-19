package options;

public class PercentageOption extends IntegerOption {

	public PercentageOption(String argument, String description,
			int defaultValue) {
		super(argument, description, defaultValue);
		setMaximum(100);
		setMinimum(0);
	}

	/**
	 * get the percentage as an integer between 0 and 100.
	 * 
	 * @return an integer between 0 and 100.
	 */
	public int getIntValue() {
		return super.getValue();
	}

	/**
	 * get the percentage as a double between 0 and 1.
	 * 
	 * @return a double between 0 and 1.
	 */
	public double getDoubleValue() {
		return ((float) super.getValue()) / 100.;
	}

	/**
	 * this method is not precise about the return type. use instead
	 * {@link #getIntValue()} or {@link #getDoubleValue()}.
	 */
	@Override
	@Deprecated
	public Integer getValue() {
		throw new RuntimeException();
	}
}
