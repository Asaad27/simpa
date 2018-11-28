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
		return new Integer(s);
	}

	@Override
	protected Integer toType(int v) {
		return new Integer(v);
	}

	@Override
	protected Integer toType(Number v) {
		return v.intValue();
	}
}
