package stats.attribute.restriction;

import stats.StatsEntry;

public class ClassRestriction<T> extends Restriction {
	Class<T> wantedClass;

	public ClassRestriction(Class<T> wantedClass) {
		this.wantedClass = wantedClass;
	}

	@Override
	public boolean contains(StatsEntry s) {
		return s.getClass() == wantedClass;
	}

	@Override
	public String toString() {
		return "class=" + wantedClass.getName();
	}
}
