import java.util.Iterator;
import java.util.ArrayList;

public class Prediction extends ArrayList<Condition>{
	String final_state;
	
	public Prediction (String f_state) {
		super();
		final_state = new String(f_state);
	}
	
	public Prediction (Condition c, String f_state) {
		super();
		final_state = new String(f_state);
		this.add(c);
	}
	
	public String toString() {
		String str = new String("");
		str += final_state+" :\n";
		Iterator<Condition> itr = this.iterator();
		if (itr.hasNext()) {
			str += "   (";
			str += itr.next().toString();
			str += ")\n";
		}
		while (itr.hasNext()) {
			str += "or (";
			str += itr.next().toString();
			str += ")\n";
		}
		return str;
	}
}
