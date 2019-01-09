package options;

public class LongOption extends NumberOption<Long> {

	public LongOption(String argument, String description, long defaultValue) {
		super(argument, description, defaultValue);
	}

	public LongOption(String argument, String description,
			String autoValueLabel) {
		super(argument, description, autoValueLabel);
	}

	@Override
	protected Long parse(String s) {
		return Long.valueOf(s);
	}

	@Override
	protected Long toType(int v) {
		return Long.valueOf(v);
	}

	@Override
	protected Long toType(Number v) {
		return v.longValue();
	}
}
