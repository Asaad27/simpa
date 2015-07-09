package stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.Restriction;

public class StatsSet {
	private List<StatsEntry> restrictedStats;
	private List<Restriction> restrictions;

	private StatsSet(List<Restriction> r){
		restrictions = new ArrayList<Restriction>(r);
	}

	public StatsSet(){
		restrictedStats = new ArrayList<StatsEntry>();
		restrictions = new ArrayList<Restriction>();
	}
	
	public StatsSet(StatsSet o){
		restrictedStats = o.restrictedStats;
		restrictions = new ArrayList<Restriction>(o.restrictions);
	}
	
	public void add(StatsEntry s){
		for (Restriction r : restrictions)
			if (!r.contains(s))
				return;
		restrictedStats.add(s);
	}

	public void restrict(Restriction r){
		restrictions.add(r);//TODO check if one restriction is not a subRestriction of another
		restrictedStats = r.apply(restrictedStats);
	}

	protected List<StatsEntry> getStats() {
		return restrictedStats;
	}

	public <T extends Comparable<T>> Map<T,StatsSet> sortByAtribute(Attribute<T> a){
		Map<T,StatsSet> sorted = new HashMap<T,StatsSet>();
		for (StatsEntry s : restrictedStats){
			T key = s.get(a);
			StatsSet Entry = sorted.get(key);
			if (Entry == null){
				Entry = new StatsSet(restrictions);
				Entry.restrict(new EqualsRestriction<T>(a, key));
				sorted.put(key, Entry);
			}
			Entry.add(s);
		}
		return sorted;
	}
	
	public <T extends Comparable<T>> float attributeAVG(Attribute<T> a){
		Float sum = new Float(0);
		for (StatsEntry s : restrictedStats){
			sum += (float) s.get(a);
		}
		return sum/restrictedStats.size();
	}
	
	public <T extends Comparable<T>> T attributeMin(Attribute<T> a){
		assert restrictedStats.size() > 0;
		T min = restrictedStats.get(0).get(a);
		for (StatsEntry s : restrictedStats){
			T current = s.get(a);
			if (min.compareTo(current) > 0)
				min = current;
		}
		return min;
	}
	
	public <T extends Comparable<T>> T attributeMax(Attribute<T> a){
		assert restrictedStats.size() > 0;
		T max = restrictedStats.get(0).get(a);
		for (StatsEntry s : restrictedStats){
			T current = s.get(a);
			if (max.compareTo(current) < 0)
				max = current;
		}
		return max;
	}
	
	public <T extends Comparable<T>> T attributeMedian(Attribute<T> a){
		assert restrictedStats.size() > 0;
		ArrayList<T> values = new ArrayList<T>(restrictedStats.size());
		for (int i = 0; i < restrictedStats.size(); i++){
			values.add(restrictedStats.get(i).get(a));
		}
		Collections.sort(values);
		return values.get(restrictedStats.size()/2);
	}
	
	public int size(){
		return restrictedStats.size();
	}
}
