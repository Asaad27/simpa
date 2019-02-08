package options.valueHolders;

public class LongHolder extends NumberHolder<Long> {

	public LongHolder(String name, String description, Long defaultValue) {
		super(name, description, defaultValue);
		updateWithValue();
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
