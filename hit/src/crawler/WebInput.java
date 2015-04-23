package crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.jsoup.nodes.Element;

import tools.Utils;

import com.gargoylesoftware.htmlunit.HttpMethod;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebInput implements Cloneable {

	public enum Type {

		LINK, FORM;
	}

	private int nbValues = 0;
	private Type type = null;
	private HttpMethod method = null;
	private String address = null;
	private TreeMap<String, List<String>> params = null;
	/**
	 * The previous input that led to this one
	 */
	private WebInput prev = null;
	/**
	 * The associated output
	 */
	private WebOutput output = null;

	public WebInput() {
		params = new TreeMap<>();
	}
	
	public WebInput getPrev() {
		return prev;
	}

	public void setPrev(WebInput prev) {
		this.prev = prev;
	}

	public WebOutput getOutput() {
		return output;
	}

	public void setOutput(WebOutput output) {
		this.output = output;
	}

	public void setNbValues(int val) {
		this.nbValues = val;
	}
	
	public int getNbValues() {
		return this.nbValues;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public void setParams(TreeMap<String, List<String>> params) {
		this.params = params;
	}

	public WebInput(String link) {
		this.type = Type.LINK;
		this.method = HttpMethod.GET;
		this.params = new TreeMap<>();
		if (link.contains("?")) {
			this.address = link.split("\\?")[0];
			if (link.split("\\?").length > 1) {
				String[] parameters = link.split("\\?")[1].split("&");
				for (String parameter : parameters) {
					String[] name_value = parameter.split("=");
					if (this.params.get(name_value[0]) == null) {
						this.params.put(name_value[0], new ArrayList<String>());
					}
					if (name_value.length == 2) {
						this.params.get(name_value[0]).add(name_value[1]);
					} else {
						this.params.get(name_value[0]).add("");
					}
				}
			}
		} else {
			this.address = link;
		}
	}

	public WebInput(HttpMethod m, String address, TreeMap<String, List<String>> params) {
		this.type = Type.FORM;
		this.method = m;
		this.address = address;
		this.params = params;
	}

	public static List<WebInput> extractInputsFromForm(Element form) {
		List<WebInput> l = new ArrayList<>();

		HttpMethod method = HttpMethod.GET;
		if (form.attr("method").toLowerCase().equals("post")) {
			method = HttpMethod.POST;
		}

		HashMap<String, List<String>> inputs = new HashMap<>();

		String address = form.attr("action");
		try {
			address = new URL(new URL(form.baseUri()), address).toString();
		} catch (MalformedURLException ex) {
			Logger.getLogger(DriverGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}

		for (Element textarea : form.select("textarea")){
			List<String> values = new ArrayList<String>();
			if(!"".equals(textarea.text())){
				values.add(textarea.text());
			}
			inputs.put(textarea.attr("name"), values);
		}
		
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
			List<String> values = new ArrayList<>();
			for (Element option : input.select("option[value]")) {
				values.add(option.attr("value"));
			}
			inputs.put(input.attr("name"), values);
		}
		for (Element submit : form.select("input[type=submit]")) {
			TreeMap<String, List<String>> inputsCopy = new TreeMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty()) {
				inputsCopy.put(submit.attr("name"),
					Utils.createArrayList(submit.attr("value")));
			}
			l.add(new WebInput(method, address, inputsCopy));
		}
		for (Element submit : form.select("input[type=image]")) {
			TreeMap<String, List<String>> inputsCopy = new TreeMap<>();
			inputsCopy.putAll(inputs);
			if (submit.hasAttr("name") && !submit.attr("name").isEmpty()) {
				inputsCopy.put(submit.attr("name"),
					Utils.createArrayList(submit.attr("value")));
			}
			l.add(new WebInput(method, address, inputsCopy));
		}

		return l;
	}

	public TreeMap<String, List<String>> getParams() {
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

	public String getAddressWithParameters() {
		return getAddressWithParameters(false);
	}
	
	/**
	 * Returns the input address concatenated with its parameters
	 *
	 * @param randomized If set, randomly pick a single value for each parameter from existing ones
	 * @return The address with parameters
	 */
	public String getAddressWithParameters(boolean randomized) {
		if (this.method != HttpMethod.GET) {
			throw new IllegalArgumentException("Internal error : this method should "
				+ "not be called for an input with a method other than GET");
		}
		String addr = this.address + "?";
		for (String key : params.keySet()) {
			if (randomized) {
				addr += key + "=" + Utils.randIn(params.get(key)) + "&";
			} else {
				for (String value : params.get(key)) {
					addr += key + "=" + value + "&";
				}
			}
		}
		addr = addr.substring(0, addr.length() - 1);
		return addr;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "[" + method + ", " + address + ", " + params + "]";
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof WebInput)) {
			return false;
		}
		WebInput to = (WebInput) other;
		if (!address.equals(to.address)) {
			return false;
		}
		for (String input : params.keySet()) {
			if (to.params.get(input) == null) {
				return false;
			}
			if (to.params.get(input).size() == 1
				&& params.get(input).size() == 1
				&& (!to.params.get(input).equals(params.get(input)))) {
				return false;
			}
		}
		return params.size() == to.params.size();
	}

	public void cleanRuntimeParameters(List<String> rtParams) {
		if (type == Type.LINK) {
			for (String name : params.keySet()) {
				for (String runtime : rtParams) {
					if (name.equals(runtime)) {
						params.get(name).clear();
						params.get(name).add("%%__RUNTIME__" + name + "__%%");
					}
				}
			}
		} else if (type == Type.FORM) {
			for (String name : params.keySet()) {
				for (String runtime : rtParams) {
					if (name.equals(runtime)) {
						params.get(name).clear();
						params.get(name).add("%%__RUNTIME__" + name + "__%%");
					}
				}
			}
			int pos = address.indexOf("?");
			if (pos != -1) {
				while (pos < address.length()) {
					String name = address.substring(pos + 1, address.indexOf("=", pos + 1));
					for (String runtime : rtParams) {
						if (name.equals(runtime)) {
							address = address.substring(0, address.indexOf("=", pos + 1) + 1) + "%%__RUNTIME__" + name + "__%%" + address.substring(address.indexOf("=", pos + 1) - 1 + name.length());
						}
					}
					pos = address.indexOf("&", pos + 1);
					if (pos == -1) {
						break;
					}
				}
			}
		}
	}

	public boolean isAlmostEquals(WebInput to) {
		if (!address.equals(to.address)) {
			return false;
		}

		int NbEquals = 0;
		int Nb = 0;
		for (String input : params.keySet()) {
			if (to.params.get(input) == null) {
				return false;
			}
			if (to.params.get(input).size() == 1 && params.get(input).size() == 1) {
				Nb++;
				if (to.params.get(input).equals(params.get(input))) {
					NbEquals++;
				}
			}

		}
		if (params.size() != to.params.size()) {
			return false;
		} else {
			return Nb - NbEquals <= 1;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		WebInput wi = (WebInput) super.clone();
		wi.params = new TreeMap<>();

		for (Map.Entry<String, List<String>> entrySet : params.entrySet()) {
			String key = entrySet.getKey();
			List<String> value = entrySet.getValue();
			ArrayList<String> newList = new ArrayList<>();
			for (String s : value){
				newList.add(s);
			}
			wi.params.put(key, newList);
		}

		wi.output = output;
		wi.prev = prev;
		return wi;
	}
}
