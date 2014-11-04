package datamining.free;

public class Type {
	String type;
	
	public Type() {
		type = "STRING";
	}
	
	public Type(String t) {
		this.type = new String(t);
	}
	
	public String get() {
		return type;
	}
	
	public String toString() {
		return type;
	}
}
