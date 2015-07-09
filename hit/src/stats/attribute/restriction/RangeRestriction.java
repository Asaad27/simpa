package stats.attribute.restriction;

import stats.StatsEntry;
import stats.attribute.Attribute;


public class RangeRestriction<T extends Comparable<T>> extends Restriction {
	T min;
	T max;
	Attribute<T> a;

	public RangeRestriction(Attribute<T> a, T min, T max) {
		assert min.compareTo(max) <= 0;
		this.a = a;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public boolean contains(StatsEntry s) {
		return min.compareTo(s.get(a)) <= 0 && s.get(a).compareTo(max) <= 0;
	}

	public String toString(){
		return min + " <= " + a.getName() + " <= " + max + " " + a.getUnits().getSymbol();
	}
}
