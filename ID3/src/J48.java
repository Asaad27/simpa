import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class J48 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String file = "Test/test.arff";
			int index_class = 100;
			
			System.out.println("Loading Data From "+ file + "...");
			ArffReader reader = new ArffReader(file);
			
			System.out.println("Creating Data Structures...");
			//Get Relation
			String relation = reader.getRelation();
			
			//Get Attributes and enum
			Map<String, Nominal<String>> map_attributes = reader.getMapAttributes();
			
			//Get Types
			Map<String, Type> type_attributes = reader.getTypeAttributes();

			//Get Attributes List 
			Map<Integer, String> array_attributes = reader.getArrayAttributes();

			//Get Data columns
			ArrayList<LinkedList> columns_attributes = reader.getColumnsAttributes();

			//Create Col_class
			LinkedList<String> col_class = new LinkedList<String>(columns_attributes.get(index_class));
			float global_entropy = Entropy._Entropy(col_class, map_attributes.get(array_attributes.get(index_class)));
			float g = Entropy.gain(col_class, columns_attributes.get(2), map_attributes.get(array_attributes.get(index_class)), map_attributes.get(array_attributes.get(2)), global_entropy);
			//float f = Entropy.gainO(col_class, columns_attributes.get(2), map_attributes.get(array_attributes.get(index_class)), map_attributes.get(array_attributes.get(2)), global_entropy);
			
			
			//ID3
			System.out.println("Running ID3 ...");
			
			DecisionTree DT = ID3.Run(columns_attributes, col_class, map_attributes, array_attributes, index_class);
			
			System.out.println("\n----------------------- Decision Tree -----------------------");
			System.out.println(DecisionTree.toString(DT));
			
			
			//Optimization
			DT = DecisionTree.Optimize(DT);
			
			System.out.println("\n\n----------------------- Optimized Decision Tree -----------------------");
			System.out.println(DecisionTree.toString(DT));
			
			//Prediction
			ClassificationResult result = DecisionTree.Predict(DT);
			
			System.out.println("\n\n----------------------- Conditions find -----------------------");
			System.out.println(result.toString());
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
