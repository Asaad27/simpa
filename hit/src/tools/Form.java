package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jsoup.nodes.Element;
import tools.HTTPRequest.Method;

public class Form {
	private String action;
	private Method method;
	private HashMap<String, List<String>> inputs;

	
	public Form(String id, Method m, String action, HashMap<String, List<String>> inputs){
		this.method = m;
		this.action = action;
		this.inputs = inputs;
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
			inputs.put(input.attr("name"), (!input.hasAttr("value") || input.attr("value").length()==0?new ArrayList<String>():Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("input[type=hidden]")){
			inputs.put(input.attr("name"), (!input.hasAttr("value") || input.attr("value").length()==0?new ArrayList<String>():Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("input[type=password]")){
			inputs.put(input.attr("name"), (!input.hasAttr("value") || input.attr("value").length()==0?new ArrayList<String>():Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("select")){
			List<String> values = new ArrayList<String>();
			for (Element option : input.select("option[value]")){
				values.add(option.attr("value"));
			}
			inputs.put(input.attr("name"), values);
		}
		for(Element submit : form.select("input[type=submit]")){
			HashMap<String, List<String>> inputsCopy = new HashMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty()) inputsCopy.put(submit.attr("name"), Utils.createArrayList(submit.attr("value")));
			l.add(new Form(id, method, action, inputsCopy));
		}
		for(Element submit : form.select("input[type=image]")){
			HashMap<String, List<String>> inputsCopy = new HashMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty()) inputsCopy.put(submit.attr("name"), Utils.createArrayList(submit.attr("value")));
			l.add(new Form(id, method, action, inputsCopy));
		}
		
		return l;
	}
	
	public HashMap<String, List<String>> getInputs() {
		return inputs;
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
		return "["+action + ", " + inputs +"]";
	}

	public boolean equals(Form to){
		if (!action.equals(to.action)) return false;
		for(String input : inputs.keySet()){
			if (to.inputs.get(input) == null) return false;
			if (to.inputs.get(input).size()==1 && inputs.get(input).size()==1 && (!to.inputs.get(input).equals(inputs.get(input)))) return false;
		}
		if (inputs.size() != to.inputs.size()) return false;
		return true; 
	}
}
