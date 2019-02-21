package options.valueHolders;

import java.util.ArrayList;
import java.util.List;

public abstract class ListHolder<E, H extends ValueHolder<E, ?> & Stringifyable>
		extends SequenceHolder<E, List<E>, H> implements Stringifyable {

	public ListHolder(String name, String description) {
		this(name, description, ',', '\\');
	}

	public ListHolder(String name, String description, char separator,
			char escape) {
		super(name, description, new ArrayList<>(), separator, escape);
	}

	@Override
	public void clear() {
		setValue(new ArrayList<>());
	}

	@Override
	public void addElementFromString(E element) {
		List<E> newValue = new ArrayList<>(getValue());
		newValue.add(element);
		setValue(newValue);
	}

	@Override
	protected List<E> InnerToUser(List<H> inner) {
		return holdersTypeToList(inner);
	}

	@Override
	protected List<H> UserToInnerType(List<E> user, List<H> previousValue) {
		return listToHolder(user, previousValue);
	}

}
