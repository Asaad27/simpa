package stats.attribute.restriction;

import stats.StatsEntry;
import stats.attribute.Attribute;

public class EqualsRestriction<T extends Comparable<T>> extends Restriction {
	private T value;
	Attribute<T> a;

	public EqualsRestriction(Attribute<T> a, T value) {
		this.a = a;
		this.value = value;
	}

	@Override
	public boolean contains(StatsEntry s) {
		return s.get(a).equals(value);
	}

	public String toString() {
		return a.toString() + "=" + value;
	}
}
