package tools;

import java.util.HashMap;

public class HTTPData {
	private HashMap<String, String> data = null;
	
	public HTTPData(){
		data = new HashMap<String, String>();
	}
	
	public HTTPData(String name, String value){
		this();
		data.put(name, value);
	}
	
	public HTTPData(HashMap<String, String> paramValues) {
		this();
		data.putAll(paramValues);
	}

	public void add(String name, String value){
		data.put(name, value);
	}
	
	public HashMap<String, String> getData(){
		return data;
	}

}
