import java.util.Iterator;
import java.util.LinkedList;

public class Condition extends LinkedList<Tag> {
	public Condition() {
		super();
	}
	
	public Condition(Tag t) {
		super();
		this.add(t);
	}
	
	public Condition(Condition c) {
		super();
		Iterator<Tag> itr = c.iterator();
		while (itr.hasNext()) {
			this.add(itr.next());
		}
	}
	
	public String toString() {
		String str = new String("");
		Iterator<Tag> itr = this.iterator();
		if (itr.hasNext()) {
			str += itr.next().toString();
		}
		while (itr.hasNext()) {
			str += " and ";
			str += itr.next().toString();
		}
		return str;
	}
}
