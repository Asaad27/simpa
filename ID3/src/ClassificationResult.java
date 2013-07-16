import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class ClassificationResult extends HashMap<String, Prediction> {
	public ClassificationResult() {
		super();
	}
	
	public String toString() {
		String str = new String("");
		Iterator it = this.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Prediction> pairs = (Map.Entry<String, Prediction>)it.next(); 
			str += pairs.getValue().toString() + "\n";
		}
		return str;
	}

}
