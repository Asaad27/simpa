package stats;

import stats.attribute.Attribute;

public interface StatsEntry {
	
	/**
	 * this should be a static method
	 * @return the attributes available for this kind of stats
	 */
	public Attribute<?>[] getAttributes();

	/**
	 * get the value of a given attribute
	 * @param a an attribute in {@link StatsEntry#getAttributes()}
	 * @return the value of the Attribute a.
	 */
	public abstract <T extends Comparable<T>> T get(Attribute<T> a);
}
