package stats.attribute.restriction;

import java.util.ArrayList;
import java.util.List;

import stats.StatsEntry;

public abstract class Restriction {
	protected String title=null;
	
	public abstract boolean contains(StatsEntry s);
	
	public List<StatsEntry> apply(List<StatsEntry> set){
		List<StatsEntry> kept = new ArrayList<StatsEntry>();
		for (StatsEntry s : set){
			if (contains(s)){
				kept.add(s);
			}
		}
		return kept;
	}
	
	public void setTitle(String newTitle){
		title=newTitle;
	}
	public String getTitle(){
		if (title==null)return toString();
		return title;
	}
}
