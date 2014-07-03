package learner.mealy.alpha;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

public class CharTable {
	
	public Map<StateLabel,Map<InputSequence,OutputSequence>> map;
	public Set<InputSequence> distinguishingSet;
	
	public CharTable(){
		map= new HashMap<>();
		distinguishingSet =new HashSet<InputSequence>();
	}
	
	public List<StateLabel> getStatesforAlpha(InputSequence alpha, OutputSequence out){
		List<StateLabel> l = new LinkedList<>();
		for(Entry<StateLabel, Map<InputSequence, OutputSequence>> entry : map.entrySet()){
			if(entry.getValue().containsKey(alpha) && entry.getValue().get(alpha).equals(out)){
				l.add(entry.getKey());
			}
		}
		return l;
	}

	@Override
	public String toString() {
		String res ="{ ";
		for(Entry<StateLabel,Map<InputSequence,OutputSequence>> e : map.entrySet() ){
			res += "[";
			res += e.getKey().name + ":";
			for(Entry<InputSequence,OutputSequence> eio : e.getValue().entrySet()){
				res += eio.getKey()+", "+eio.getValue()+"; ";
			}
			res += "]";
		}
		res += "}";
		return res;
	}

	public boolean containsOutput(OutputSequence out) {
		for(Map<InputSequence,OutputSequence> m : map.values()){
			if(m.containsValue(out)) return true;
		}
		return false;
	}
	
	

}
