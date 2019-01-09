package options;

public class IntegerOption extends NumberOption<Integer> {

	public IntegerOption(String argument, String description,
			int defaultValue) {
		super(argument, description, defaultValue);
	}

	public IntegerOption(String argument, String description,
			String autoValueLabel) {
		super(argument, description, autoValueLabel);
	}

	@Override
	protected Integer parse(String s) {
		return Integer.valueOf(s);
	}

	@Override
	protected Integer toType(int v) {
		return Integer.valueOf(v);
	}

	@Override
	protected Integer toType(Number v) {
		return v.intValue();
	}
}
