package options.valueHolders;

public class IntegerHolder extends NumberHolder<Integer> {

	public IntegerHolder(String name, String description,
			Integer defaultValue) {
		super(name, description, defaultValue);
		updateWithValue();
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
