package datamining;

public class Type {
	String type;
	
	public Type() {
		type = new String("STRING");
	}
	
	public Type(String t) {
		this.type = new String(t);
	}
	
	public boolean isNumeric() {
		if (type.toUpperCase().contains("NUMERIC"))
			return true;
		return false;
	}
	
	public String get() {
		return type;
	}
	
	public String toString() {
		return type;
	}
}
