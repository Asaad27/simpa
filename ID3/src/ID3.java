import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

 
public class ID3 {
	
	private int indexBestAttribut;
	
	public static DecisionTree Run(ArrayList<LinkedList> columns_attributes,  LinkedList<String> col_class,
			Map<String, Nominal<String>> map_attributes, Map<Integer, String> array_attributes, int index_class) {
		/*System.out.println("----------------- Running ID3 ------------------------");
		System.out.println("\n--> array_attributes : ");
		System.out.println(array_attributes.toString());
		System.out.println("\n--> map_attributes : ");
		System.out.println(map_attributes.toString());
		System.out.println("\n--> columns attributes : ");
		System.out.println(columns_attributes.toString());
		System.out.println("\n--> col class : ");
		System.out.println(col_class.toString());
		System.out.println("\n--> index class : ");
		System.out.println(index_class);
		System.out.println("\n--> enum_class : ");
		System.out.println(map_attributes.get(array_attributes.get(index_class)).toString());*/

		float Global_Entropy = Entropy._Entropy(col_class, 
								map_attributes.get(array_attributes.get(index_class)));
		
		DecisionTree NewNode = new DecisionTree();
		
		if (columns_attributes.get(0).size() < 1) {
			Tag leaf = new Tag(new String("Error"), new String("Error"));
			DecisionTree.AddChildrenNode(NewNode, new DecisionTree(leaf));
		}
		else if (columns_attributes.size() == 1)  {// attributNonCible vide
			Nominal<String> possible_values = map_attributes.get(array_attributes.get(0));
			String value = MostPresent(columns_attributes.get(0), possible_values.getLinkedList());
			String attribute = new String("class");
			Tag leaf = new Tag(attribute, value);
			DecisionTree.AddChildrenNode(NewNode, new DecisionTree(leaf));
		}
		else if (Global_Entropy == 0) {
			String value = (String)col_class.get(0);
			String attribute = new String("class");
			Tag leaf = new Tag(attribute, value);
			DecisionTree.AddChildrenNode(NewNode, new DecisionTree(leaf));
		}
		else {

			int best_column = SelectBestColumn (col_class, columns_attributes, map_attributes, array_attributes, index_class, Global_Entropy);
			
			// Create array_attribute_next
			Map<Integer, String> array_attributes_next = new HashMap<Integer, String>(FilterArrAtt(array_attributes, best_column));
			
			// Create index_class_next
			int index_class_next = index_class;
			if (index_class > best_column)
				index_class_next--;
			
			// Create map_attribute_next
			Map<String, Nominal<String>> map_attributes_next = new HashMap<String, Nominal<String>>(map_attributes);
			map_attributes_next.remove(array_attributes.get(best_column));
			
			LinkedList<String> enum_value = new LinkedList<String>(map_attributes.get(array_attributes.get(best_column)).getLinkedList());
			Iterator<String> itr = enum_value.iterator();

			while (itr.hasNext()) {
				String current = new String(itr.next());
				
				// Create columns_attributes_next
				ArrayList<LinkedList> columns_attributes_next = new ArrayList<LinkedList>(FilterColAtt(columns_attributes, current, best_column));

				// Create col_class_next 
				LinkedList<String> col_class_next = new LinkedList<String>(columns_attributes_next.get(index_class_next));
			
				String attribute_name = array_attributes.get(best_column);
				String value = current;
				Tag tag_child = new Tag(attribute_name, current);

				DecisionTree Child = ID3.Run(columns_attributes_next, col_class_next, map_attributes_next, array_attributes_next, index_class_next);
				Child.setTag(tag_child);
				DecisionTree.AddChildrenNode(NewNode, Child);
			}
		}
		return NewNode;
	}
	
	private static String MostPresent(LinkedList<String> column, LinkedList<String> possible_values) {
		String most_present = new String("");
		int nb_most_present = 0;
		Map<String, Integer> counter = new HashMap<String, Integer>();
		
		//init HashMap
		Iterator<String> itr = possible_values.iterator();
		while (itr.hasNext()) {
			counter.put(itr.next(), 0);
		}
		
		//count 
		Iterator<String> it = column.iterator();
		while(it.hasNext()) {
			String current = it.next();
			int current_counter = counter.get(current);
			counter.remove(current);
			counter.put(current, current_counter+1);
		}
		
		//Select best
		Iterator it2 = counter.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it2.next();
			if (pairs.getValue() > nb_most_present) {
				nb_most_present = pairs.getValue();
				most_present = pairs.getKey();
			}
			//it.remove();
		}
		return most_present;
	}
	
	private static int SelectBestColumn (LinkedList<String> column_class, ArrayList<LinkedList> columns_attributes, Map<String, 
			Nominal<String>> map_attributes, Map<Integer, String> array_attributes, int index_class, float Global_Entropy) {
		int best_column = 0;
		float best_gain = 0;
		for (int i = 0; i < columns_attributes.size(); i++) {
			if (i != index_class) {
				float gain = Entropy.gain(column_class, columns_attributes.get(i), map_attributes.get(array_attributes.get(index_class)), map_attributes.get(array_attributes.get(i)), Global_Entropy);
				
				if (gain > best_gain) {
					best_gain = gain;
					best_column = i;
				}
			}
		}
		return best_column;
	}
	
	private static int GetIndexColAtt (int index, int index_class) {
		if (index < index_class)
			return index;
		return index - 1;
	}
	
	private static ArrayList<LinkedList> DeleteColumn (ArrayList<LinkedList> columns_attributes, int index) {
		int size = columns_attributes.size();
		ArrayList<LinkedList> col_att = new ArrayList<LinkedList>();
		for (int i = 0; i < size; i++) {
			if (i != index) {
	            col_att.add(new LinkedList<String>(columns_attributes.get(i)));
			}
		}
		return col_att;
	}
	
	private static ArrayList<LinkedList> FilterColAtt(ArrayList<LinkedList> columns_attributes, String value, int index) {
		ArrayList<Iterator> LL_it = new ArrayList<Iterator>();
		String [] LL_values = new String[columns_attributes.size()];
		ArrayList<LinkedList> filter = new ArrayList<LinkedList>();
		
		for (int i = 0; i < columns_attributes.size(); i++) {
			LL_it.add(columns_attributes.get(i).iterator());
			LL_values[i] = new String("");
			filter.add(new LinkedList<String>());
		}
		
		while (LL_it.get(index).hasNext()) {
			for(int i = 0; i < LL_it.size(); i++) {
				if (LL_it.get(i).hasNext())
					LL_values[i] = (String)LL_it.get(i).next();
			}
			
			if (LL_values[index].equals(value)) {
				for(int i = 0; i < filter.size(); i++){
					filter.get(i).add(LL_values[i]);
				}
			}
		}
		
		return DeleteColumn(filter, index);
	}
	
	private static Map<Integer, String> FilterArrAtt(Map<Integer, String> array_attributes, int best_column) {
		Map<Integer, String> arr_att = new HashMap<Integer, String>();
		
		Iterator it = array_attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>)it.next(); 
			if (pairs.getKey() < best_column) {
				arr_att.put(pairs.getKey(), pairs.getValue());
			}
			else if (pairs.getKey() > best_column)
				arr_att.put(pairs.getKey()-1, pairs.getValue());
			//it.remove();
		}
		return arr_att;
	}
}
