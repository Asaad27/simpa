import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;


public class Entropy {
	public static float _Entropy(LinkedList<String> column, Nominal<String> enum_values) {
		float entropy = 0;
		float column_size;
		Map<String, Float> frequencies = new HashMap<String, Float>();
		
		//init frequencies
		LinkedList<String> LL_values = enum_values.getLinkedList();
		Iterator<String> itr = LL_values.iterator();
		while (itr.hasNext()) {
			frequencies.put(itr.next(), (float)0);
		}
		
		//calculate frequencies
		column_size = column.size();
		Iterator<String> itr_freq = column.iterator();
		while (itr_freq.hasNext()) {
			String current = itr_freq.next();
			float freq = frequencies.get(current);
			frequencies.remove(current);
			frequencies.put(current, freq + 1 / column_size);
		}
		
		// Apply Shannon Entropy's formula
		Iterator it = frequencies.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Float> pairs = (Map.Entry<String, Float>)it.next(); 
			if (pairs.getValue() != 0 && pairs.getValue() < 1)
				entropy -= pairs.getValue() * Math.log(pairs.getValue()) / Math.log(2);
			//it.remove();
		}
		
		return entropy;
	}
	
	public static float gain (LinkedList<String> column_class, LinkedList<String> column, Nominal enum_class, Nominal enum_col, float global_entropy) {
		float gain = global_entropy;
		
		LinkedList<String> possible_values = enum_col.getLinkedList();
		
		LinkedList<String> Sv = new LinkedList<String>();
		Iterator<String> itr = possible_values.iterator();
		while (itr.hasNext()) {
			String current_value = itr.next();
			Sv.clear();
			Iterator<String> it = column.iterator();
			Iterator<String> it_class = column_class.iterator();
			while (it.hasNext() && it_class.hasNext()) {
				String class_current = it_class.next();
				if (current_value.equals(it.next())) {
					Sv.add(class_current);
				}
			}
			float entropy_Sv = Entropy._Entropy(Sv, enum_class);
			float card_Sv = Sv.size();
			gain -= (card_Sv / (column_class.size())) * entropy_Sv;
		}
		return gain;
	}	
	
	public static float gainO (LinkedList<String> column_class, LinkedList<String> column, Nominal enum_class, Nominal enum_col, float global_entropy) {
		float gain = global_entropy;
		Map<String, Integer> HM_index = new HashMap<String, Integer>();
		LinkedList<String> possible_values = enum_col.getLinkedList();
		
		//init Structures
		ArrayList<LinkedList<String>> Svs = new ArrayList<LinkedList<String>>();
		for(int i = 0; i < enum_col.size(); i++) {
			Svs.add(new LinkedList<String>());
		}
		Iterator<String> itr = possible_values.iterator();
		int i = 0;
		while (itr.hasNext()) {
			HM_index.put(itr.next(), i);
			i++;
		}
		
		//calcul 
		Iterator<String> itr_col = column.iterator();
		Iterator<String> itr_class = column_class.iterator();
		while (itr_col.hasNext()) {
			String current = itr_col.next();
			String current_Sv = itr_class.next();
			int index = HM_index.get(current);
			Svs.get(index).add(current_Sv);
		}
		
		Iterator<LinkedList<String>> it = Svs.iterator();
		while (it.hasNext()) {
			LinkedList<String> tmp = it.next();
			int card = tmp.size();
			float entropy = Entropy._Entropy(tmp, enum_class);
			gain -= (card / (column_class.size())) * entropy;
		}
		return gain;
	}
}
