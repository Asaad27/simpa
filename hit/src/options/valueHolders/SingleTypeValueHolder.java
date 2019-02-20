package options.valueHolders;

public abstract class SingleTypeValueHolder<T> extends ValueHolder<T, T> {

	SingleTypeValueHolder(String name, String description) {
		super(name, description);
	}

	public SingleTypeValueHolder(String name, String description,
			T startValue) {
		super(name, description, startValue);
	}

	@Override
	protected T InnerToUser(T i) {
		return i;
	}

	@Override
	protected T UserToInnerType(T u, T previous) {
		return u;
	}
}
