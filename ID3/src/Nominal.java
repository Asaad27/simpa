import java.util.LinkedList;
import java.util.Iterator;

public class Nominal <T> {
	LinkedList<T> m_nominal;
	
	public Nominal () {
		m_nominal = new LinkedList<T>();
	}
	
	public Nominal(T val) {
		m_nominal = new LinkedList<T>();
		m_nominal.add(val);
	}
	
	public void add(T val) {
		m_nominal.add(val);
	}
	
	public void remove(T val) {
		m_nominal.remove(val);
	}
	
	public LinkedList<T> getNominal() {
		return m_nominal;
	}
	
	public int size() {
		return m_nominal.size();
	}
	
	public LinkedList<T> getLinkedList() {
		return m_nominal;
	}
	
	public boolean exist(T val) {
		if (m_nominal.indexOf(val) == -1)
			return false;
		return true;
	}
	
	public String toString() {
		String str = new String();
		Iterator<T> itr = m_nominal.iterator();
		str += "[ ";
		while (itr.hasNext()) {
			str += itr.next();
			str += " ";
		}
		str += "]";
		return str;
	}
}
