package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gargoylesoftware.htmlunit.util.NameValuePair;

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
	
	public List<NameValuePair> getNameValueData(){
		List<NameValuePair> l = new ArrayList<NameValuePair>();
		for (String name : data.keySet()){
			l.add(new NameValuePair(name,  data.get(name)));
		}
		return l;
	}

}
