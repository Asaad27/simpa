package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.HTTPRequest.Method;

public class Form {
	private String action;
	private Method method;
	private HashMap<String, List<String>> inputs;
	private HashMap<String, String> submit;

	
	public Form(String id, Method m, String action, HashMap<String, List<String>> inputs, HashMap<String, String> submit){
		this.method = m;
		this.action = action;
		this.inputs = inputs;
		this.submit = submit;
	}
	
	public static List<Form> getFormList(Element form) {
		List<Form> l = new ArrayList<Form>();
		
		Method method = Method.GET;
		if (form.attr("method").toLowerCase().equals("post")) method = Method.POST;
		
		HashMap<String, List<String>> inputs = new HashMap<>();
		String id = form.attr("id");
		if (id.isEmpty()) id = form.attr("name");
		
		String action = form.attr("action");
		if (!action.startsWith("/")){
			action = form.baseUri().substring(0, form.baseUri().lastIndexOf("/")+1) + action;
		}
		if (action.equals("")) action = form.baseUri();
		
		for (Element input : form.select("input[type=text]")){
			inputs.put(input.attr("name"), Utils.createArrayList((input.hasAttr("value")?input.attr("value"):"")));
		}
		for (Element input : form.select("input[type=hidden]")){
			inputs.put(input.attr("name"), Utils.createArrayList((input.hasAttr("value")?input.attr("value"):"")));
		}
		for (Element input : form.select("input[type=password]")){
			inputs.put(input.attr("name"), Utils.createArrayList((input.hasAttr("value")?input.attr("value"):"")));
		}
		for (Element input : form.select("select")){
			List<String> values = new ArrayList<String>();
			for (Element option : input.select("option[value]")){
				values.add(option.attr("value"));
			}
			inputs.put(input.attr("name"), values);
		}
		
		Elements submits = form.select("input[type=submit]");
		for(Element submit : submits){
			HashMap<String, String> s = new HashMap<String, String>();
			s.put(submit.attr("name"), submit.attr("value"));
			l.add(new Form(id, method, action, inputs, s));
		}
		
		return l;
	}
	
	public HashMap<String, List<String>> getInputs() {
		return inputs;
	}
	
	public HashMap<String, String> getSubmit() {
		return submit;
	}
	
	public Method getMethod() {
		return method;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String toString(){
		return "["+action + ", " + inputs+", "+ submit +"]";
	}

	public boolean equals(Form to){
		if (!action.equals(to.action)) return false;
		for(String input : inputs.keySet()){
			if (to.inputs.get(input) == null) return false;
		}
		if (inputs.size() != to.inputs.size()) return false;
		for(String s : submit.keySet()){
			if (to.submit.containsKey(s)) return submit.get(s).equals(to.submit.get(s));
			else return false;
		}
		return true; 
	}
}
