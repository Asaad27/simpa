import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import java.io.File;
	
public class ArffReader {
	
	private Scanner m_reader;
	private File arff_file;
	private Map<String, Type> type_attributes;
	private Map<Integer, String> array_attributes;
	private Map<String[], String> hash_train;
	private ArrayList<LinkedList> columns_attributes;
	private Map<String, Nominal<String>> map_attributes;
	private String relation;
	
		
	public ArffReader(String r_file) throws java.io.IOException {
		arff_file = new File(r_file);
		m_reader = new Scanner(arff_file);
		array_attributes = new HashMap<Integer, String>();
		hash_train = new HashMap<String[], String>();
		map_attributes = new HashMap<String, Nominal<String>>();
		type_attributes = new HashMap<String, Type>();
		ArffReadHeader();
		ArffReadData();
		GenerateMapAttributes();
		relation = ArffReadRelation();
	}
	
	public String getRelation(){
		return relation;
	}
	
	public Map<String, Type> getTypeAttributes() {
		return type_attributes;
	}
	
	public Map<Integer, String> getArrayAttributes() {
		return array_attributes;
	}
	
	public ArrayList<LinkedList> getColumnsAttributes() {
		return columns_attributes;
	}
	
	public Map<String, Nominal<String>> getMapAttributes() {
		return map_attributes;
	}
	
	public Map<String[], String> getHashTrain() {
		return hash_train;
	}
	
	private String ArffReadRelation() throws java.io.IOException {
		boolean end_Relation = false;
		String current;
		String relation = new String("");
		
		while (m_reader.hasNext() && !(end_Relation)) {
			current = m_reader.next();

			if (current.toUpperCase().equals("@RELATION")) {
				end_Relation = true;
				relation = m_reader.next();
			}
		}
		return relation;
	}
	
	private void GenerateMapAttributes() {
		for (int i = 0 ; i < columns_attributes.size(); i++) {
			LinkedList<String> column = columns_attributes.get(i);
			Nominal<String> nominal = new Nominal<String>();
			
			Iterator<String> itr = column.iterator();
			while (itr.hasNext()){
				String current = itr.next();
				if (!nominal.exist(current))
					nominal.add(current);
			}			
			map_attributes.put(array_attributes.get(i), nominal);
		}
	}
	
	private void ArffReadHeader() throws java.io.IOException {
		boolean end_Header = false;
		String currentAttribut = new String ("");
		String current;
		
		int index_attribute = 0;
		
		while (m_reader.hasNext() && ! m_reader.next().toUpperCase().equals("@ATTRIBUTE"));
		
		while (m_reader.hasNext() && !(end_Header)) {
			m_reader.reset();
			current = m_reader.next();
			
			//Lecture du nom de l'attribut
			array_attributes.put(index_attribute, current);
			currentAttribut = current;
			index_attribute++;
			
			
			//Lecture du type
			if (m_reader.hasNext())
				current = m_reader.next();
			if (current.toUpperCase().contains("STRING"))
				type_attributes.put(currentAttribut, new Type("STRING"));
			else if (current.toUpperCase().contains("NUMERIC"))
				type_attributes.put(currentAttribut, new Type("NUMERIC"));
			else
				type_attributes.put(currentAttribut, new Type("NOMINAL"));
			
			//Lecture Nominaux
			if (current.contains("NOMINAL")) {
				Nominal<String> nominal = new Nominal<String>();
				
				m_reader.useDelimiter("[{,} \\n\\r]+");
				if (m_reader.hasNext())
					current = m_reader.next();
				
				while (m_reader.hasNext() && !current.toUpperCase().contains("@DATA") && !current.toUpperCase().contains("@ATTRIBUTE")) {
					nominal.add(current);
					current = m_reader.next();
				}
				
				if (current.toUpperCase().contains("@DATA"))
					end_Header = true;
				
				map_attributes.put(currentAttribut, nominal);
				m_reader.reset();
			}
			else {
				if (m_reader.hasNext())
					current = m_reader.next();
				if (current.toUpperCase().contains("@DATA"))
					end_Header = true;
			}
		}
		m_reader.reset();
	}
	
	private void ArffReadData()  throws java.io.IOException {
		String current = new String();
		int size = array_attributes.size();
		
		columns_attributes = new ArrayList<LinkedList>();
		for (int i = 0; i < size; i++) 
            columns_attributes.add(new LinkedList<String>());
		
		m_reader.useDelimiter("[{,} \\n\\r]+");
		
		while (m_reader.hasNext()) {
			String [] tab_sample = new String[size];
			int index_sample = 0;
			
			if (m_reader.hasNext())
				current = m_reader.next();
			int current_col = 0;
			while (m_reader.hasNext() && (int)current.charAt(0) != 73 && (int)current.charAt(0) != 83 && index_sample < size-1)
			{
				tab_sample[index_sample] = current;
				index_sample++;
				columns_attributes.get(current_col).add(current);
				
				current = m_reader.next();
				current_col++;
			}

			columns_attributes.get(current_col).add(current.substring(0,current.length()));
			hash_train.put(tab_sample, current);
		}
		m_reader.reset();
	}
}
