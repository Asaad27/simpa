package drivergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.nodes.Element;

import com.gargoylesoftware.htmlunit.HttpMethod;

import tools.Utils;

public class Input {
	public enum Type {
		LINK, FORM;
	}
	
	private Type type = null;
	private HttpMethod method = null;
	private String address = null;
	private HashMap<String, List<String>> params = null;

	public Input(String link) {
		this.type = Type.LINK;
		this.method = HttpMethod.GET;
		this.params = new HashMap<String, List<String>>();
		if (link.indexOf("?") != -1) {
			this.address = link.split("\\?")[0];
			String[] params = link.split("\\?")[1].split("&");
			for (int i = 0; i < params.length; i++) {
				String[] name_value = params[i].split("=");
				if (this.params.get(name_value[0]) == null){
					this.params.put(name_value[0], new ArrayList<String>());
				}
				this.params.get(name_value[0]).add(name_value[1]);
			}
		}else{
			this.address = link;
		}
	}

	public Input(HttpMethod m, String address, HashMap<String, List<String>> params) {
		this.type = Type.FORM;
		this.method = m;
		this.address = address;
		this.params = params;
	}

	public static List<Input> extractInputsFromForm(Element form) {
		List<Input> l = new ArrayList<Input>();

		HttpMethod method = HttpMethod.GET;
		if (form.attr("method").toLowerCase().equals("post"))
			method = HttpMethod.POST;

		HashMap<String, List<String>> inputs = new HashMap<>();

		String address = form.attr("action");
		if (address.startsWith("/")) {
			address = form.baseUri().substring(0,
					form.baseUri().indexOf("/", 7))
					+ address;
		} else {
			address = form.baseUri().substring(0,
					form.baseUri().lastIndexOf("/") + 1)
					+ address;
		}
		if (address.equals(""))
			address = form.baseUri();

		for (Element input : form.select("input[type=text]")) {
			inputs.put(
					input.attr("name"),
					(!input.hasAttr("value")
							|| input.attr("value").length() == 0 ? new ArrayList<String>()
							: Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("input[type=hidden]")) {
			inputs.put(
					input.attr("name"),
					(!input.hasAttr("value")
							|| input.attr("value").length() == 0 ? new ArrayList<String>()
							: Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("input[type=password]")) {
			inputs.put(
					input.attr("name"),
					(!input.hasAttr("value")
							|| input.attr("value").length() == 0 ? new ArrayList<String>()
							: Utils.createArrayList(input.attr("value"))));
		}
		for (Element input : form.select("select")) {
			List<String> values = new ArrayList<String>();
			for (Element option : input.select("option[value]")) {
				values.add(option.attr("value"));
			}
			inputs.put(input.attr("name"), values);
		}
		for (Element submit : form.select("input[type=submit]")) {
			HashMap<String, List<String>> inputsCopy = new HashMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty())
				inputsCopy.put(submit.attr("name"),
						Utils.createArrayList(submit.attr("value")));
			l.add(new Input(method, address, inputsCopy));
		}
		for (Element submit : form.select("input[type=image]")) {
			HashMap<String, List<String>> inputsCopy = new HashMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty())
				inputsCopy.put(submit.attr("name"),
						Utils.createArrayList(submit.attr("value")));
			l.add(new Input(method, address, inputsCopy));
		}

		return l;
	}

	public HashMap<String, List<String>> getParams() {
		return params;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public Type getType() {
		return type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String toString() {
		return "[" + method + ", " + address + ", " + params + "]";
	}

	public boolean equals(Input to) {
		if (!address.equals(to.address))
			return false;
		for (String input : params.keySet()) {
			if (to.params.get(input) == null)
				return false;
			if (to.params.get(input).size() == 1
					&& params.get(input).size() == 1
					&& (!to.params.get(input).equals(params.get(input))))
				return false;
		}
		if (params.size() != to.params.size())
			return false;
		return true;
	}

}
