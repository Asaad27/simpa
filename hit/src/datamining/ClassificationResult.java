package datamining;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClassificationResult extends HashMap<String, Prediction> {
	private static final long serialVersionUID = 5242833613182727636L;

	public ClassificationResult() {
		super();
	}
	
	public String toString() {
		String str = new String("");
		Iterator<Map.Entry<String, Prediction>> it = this.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Prediction> pairs = it.next(); 
			str += pairs.getValue().toString() + "\n";
		}
		return str;
	}

}
