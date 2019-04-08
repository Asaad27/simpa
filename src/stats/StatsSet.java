/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
	private String title="";

	private StatsSet(List<Restriction> r){
		restrictedStats = new ArrayList<StatsEntry>();
		restrictions = new ArrayList<Restriction>(r);
	}

	public StatsSet(){
		restrictedStats = new ArrayList<StatsEntry>();
		restrictions = new ArrayList<Restriction>();
	}

	public StatsSet(StatsSet o){
		restrictedStats = o.restrictedStats;
		restrictions = new ArrayList<Restriction>(o.restrictions);
		title=o.title;
	}

	public StatsSet(StatsSet o, Restriction r) {
		this(o);
		restrict(r);
	}

	public StatsSet(StatsSet o, Restriction[] r) {
		this(o);
		restrict(r);
	}

	public StatsSet(File f){
		this();
		if (! f.getName().endsWith(".csv"))
			throw new RuntimeException("Files names must be ClassName.csv in order to load CSV");
		String className = f.getName().substring(0, f.getName().length()-4);
		Constructor<?> constructor;
		Class<?> statEntryClass;
		try {
			statEntryClass = Class.forName(className);
			constructor = statEntryClass.getConstructor(String.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Files names must be ClassName.csv in order to load CSV. No class " + className + " found.");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(className + " cannot be instanciate with a String so CSV will not be loaded.");
		}
		String CSVHeader;
		try {
			CSVHeader = (String) statEntryClass.getMethod("getCSVHeader_s").invoke(null);
		} catch (Exception e) {
			throw new RuntimeException("unable to get the CSV header of class " + className + ". (" + e.getMessage() + ")");
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String strLine;
			if (!(strLine = br.readLine()).equals(CSVHeader)){
				br.close();
				throw new RuntimeException("the csv file do not have the good header :\n'"+strLine+"'\ninstead of \n'"+CSVHeader+"'");
			}
			while ((strLine = br.readLine()) != null) {
				restrictedStats.add((StatsEntry) constructor.newInstance(strLine));
			}
			br.close();

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void add(StatsEntry s){
		for (Restriction r : restrictions)
			if (!r.contains(s))
				return;
		restrictedStats.add(s);
	}

	public void add(StatsSet s) {
		for (StatsEntry e : s.getStats()) {
			add(e);
		}
	}

	public void restrict(Restriction[] rs){
		for (Restriction r : rs)
			restrict(r);
	}

	public void restrict(Restriction r){
		restrictions.add(r);//TODO check if one restriction is not a subRestriction of another
		restrictedStats = r.apply(restrictedStats);
		if (!title.equals(""))
			title+=", ";
		title+=r.getTitle();
	}

	protected List<StatsEntry> getStats() {
		return restrictedStats;
	}
	
	public StatsEntry get(int n){
		return restrictedStats.get(n);
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
		Float sum = Float.valueOf(0);
		for (StatsEntry s : restrictedStats){
			sum +=  s.getFloatValue(a);
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

	public <T extends Comparable<T>> T attributeFirstQuartille(Attribute<T> a) {
		assert restrictedStats.size() > 0;
		ArrayList<T> values = new ArrayList<T>(restrictedStats.size());
		for (int i = 0; i < restrictedStats.size(); i++) {
			values.add(restrictedStats.get(i).get(a));
		}
		Collections.sort(values);
		return values.get(restrictedStats.size() / 4);
	}

	public <T extends Comparable<T>> T attributeLastQuartille(Attribute<T> a) {
		assert restrictedStats.size() > 0;
		ArrayList<T> values = new ArrayList<T>(restrictedStats.size());
		for (int i = 0; i < restrictedStats.size(); i++) {
			values.add(restrictedStats.get(i).get(a));
		}
		Collections.sort(values);
		return values.get(3 * restrictedStats.size() / 4);
	}

	public int size(){
		return restrictedStats.size();
	}
}
