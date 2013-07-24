package datamining;

public class Tag {
	String m_attribute;
	String m_value;
	
	public Tag() {
		m_attribute = new String("");
		m_value = new String("");
	}
	
	public Tag(String attribute, String value) {
		m_attribute = new String(attribute);
		m_value = new String(filterStr(value));
	}
	
	public String getValue() {
		return m_value;
	}
	
	public String getAttribute() {
		return m_attribute;
	}
	
	public void setValue(String value) {
		m_value = new String(filterStr(value));
	}
	
	public void setAttribute(String attribute) {
		m_attribute = new String(filterStr(attribute));
	}
	
	private String filterStr(String str) {
		String [] t_str = str.split("'");
		if (t_str.length > 1)
			return t_str[1];
		else
			return t_str[0];
	}
	
	public String toString() {
		String str = new String ("");
		str += "("+ m_attribute + " = " + filterStr(m_value)+ ")";
		return str;
	}
}
