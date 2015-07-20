package stats;

import stats.attribute.Attribute;

public abstract class StatsEntry {

	/**
	 * this should be a static method
	 * @return the attributes available for this kind of stats
	 */
	public Attribute<?>[] getAttributes(){
		return getAttributesIntern();
	}
	
	protected abstract Attribute<?>[] getAttributesIntern();

	/**
	 * get the value of a given attribute
	 * @param a an attribute in {@link StatsEntry#getAttributes()}
	 * @return the value of the Attribute a.
	 */
	public abstract <T extends Comparable<T>> T get(Attribute<T> a);
	
	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a){
		throw new RuntimeException();
	}
	
	public String toCSV() {
		StringBuilder r = new StringBuilder();
		for (Attribute<?> a : getAttributesIntern()){
			if (a.isVirtualParameter())
				continue;
			r.append(get(a).toString() + ",");
		}
		return r.toString();
	}

	public String getCSVHeader(){
		return makeCSVHeader(getAttributes());
	}
	
	protected static String makeCSVHeader(Attribute<?>[] A){
		StringBuilder r = new StringBuilder();
		for (Attribute<?> a : A){
			r.append(a.getName());
			r.append(",");
		}
		return r.toString();
	}

	public abstract GraphGenerator getDefaultsGraphGenerator();
}
