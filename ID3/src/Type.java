
public class Type {
	String type;
	
	public Type() {
		type = new String("STRING");
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
