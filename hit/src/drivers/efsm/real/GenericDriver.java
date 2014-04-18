package drivers.efsm.real;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.HTTPData;
import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import crawler.Configuration;
import crawler.WebInput;
import crawler.WebInput.Type;
import crawler.WebOutput;

public class GenericDriver extends LowWebDriver {

	protected WebClient client = null;
	public static Configuration config = null;
	public List<WebInput> inputs;
	public List<WebOutput> outputs;

	@SuppressWarnings("deprecation")
	public GenericDriver(String xml) throws IOException {
		inputs = new ArrayList<WebInput>();
		outputs = new ArrayList<WebOutput>();
		config = LoadConfig(xml);
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
	    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setTimeout(10000);
		CookieManager cm = new CookieManager();
		if (config.getCookies() != null){
			String cookieValue = null;
			while (cookieValue == null){
				cookieValue = (String)JOptionPane.showInputDialog(null, "Cookies value required :",
							"Loading test driver ...",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							config.getCookies());
			}
			for (String cookie : cookieValue.split("[; ]")) {
				String[] cookieValues = cookie.split("=");
				cm.addCookie(new Cookie(config.getHost(), cookieValues[0],
						cookieValues[1]));
			}			
		}
		client.setCookieManager(cm);
		BasicCredentialsProvider creds = new BasicCredentialsProvider();
		if (config.getBasicAuthUser() != null
				&& config.getBasicAuthPass() != null) {
			creds.setCredentials(
					new AuthScope(config.getHost(), config.getPort()),
					new UsernamePasswordCredentials(config.getBasicAuthUser(),
							config.getBasicAuthPass()));
		}
		client.setCredentialsProvider(creds);
		client.setJavaScriptEnabled(false);
		client.setCssEnabled(false);
	}

	public String getSystemName() {
		return config.getName();
	}

	public ParameterizedOutput execute(ParameterizedInput pi) {
		numberOfAtomicRequest++;
		WebInput in = inputs.get(Integer.parseInt(pi.getInputSymbol().substring(
				pi.getInputSymbol().indexOf("_") + 1)));

		String source = null;
		try {
			source = submit(in, pi);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		ParameterizedOutput po = null;
		WebOutput out = new WebOutput(source, false, config.getLimitSelector());
		for (int i = 0; i < outputs.size(); i++) {
			if (out.isEquivalentTo(outputs.get(i))) {
				po = new ParameterizedOutput(getOutputSymbols().get(i));
				if (outputs.get(i).getParams().isEmpty()) {
					po.getParameters().add(new Parameter("200", Types.STRING));
				} else {
					for (String p : outputs.get(i).getParams()) {
						po.getParameters().add(
								new Parameter(extractParam(out, p),
										Types.STRING));
					}
				}
			}
		}

		if (po == null) {
			// System.out.println(pi);
			// System.out.println(source);
			// System.out.println(out.getPageTree());
			po = new ParameterizedOutput(getOutputSymbols().get(0));
			for (String p : outputs.get(0).getParams()) {
				po.getParameters().add(
						new Parameter(extractParam(out, p), Types.STRING));
			}
		}

		LogManager.logRequest(pi, po);
		return po;
	}

	private String extractParam(WebOutput out, String p) {
		String path[] = p.split("/");
		org.jsoup.nodes.Element e = out.getDoc().get(Integer.parseInt(path[0]));
		for (int i = 1; i < path.length; i++) {
			e = e.child(Integer.parseInt(path[i]));
		}
		return e.text();
	}

	private HTTPData getValuesForInput(WebInput in, ParameterizedInput pi) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			TreeMap<String, List<String>> inputs = in.getParams();
			int i = 0;
			for (String key : inputs.keySet()) {
				data.add(key, pi.getParameterValue(i++));
			}
		}
		return data;
	}

	private String submit(WebInput in, ParameterizedInput pi)
			throws MalformedURLException {
		WebRequest request = null;
		HTTPData values = getValuesForInput(in, pi);
		if (in.getType() == Type.FORM) {
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());

			HtmlPage page;
			try {
				page = client.getPage(request);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		} else if (in.getType() == Type.LINK) {
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()) {
				for (String name : in.getParams().keySet()) {
					for (String value : in.getParams().get(name)) {
						link += name + "=" + value + "&";
					}
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length() - 1));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}

	private Configuration LoadConfig(String xml) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		config = new Configuration();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(null);
			org.w3c.dom.Document dom = db.parse(xml);
			Element root = dom.getDocumentElement();
			config.setName(root.getElementsByTagName("target").item(0)
					.getTextContent());
			config.setHost(root.getElementsByTagName("host").item(0)
					.getTextContent());
			config.setPort(Integer.parseInt(root.getElementsByTagName("port")
					.item(0).getTextContent()));
			config.setBasicAuthUser(root.getElementsByTagName("basicAuthUser")
					.item(0).getTextContent());
			config.setBasicAuthPass(root.getElementsByTagName("basicAuthPass")
					.item(0).getTextContent());
			config.setLimitSelector(root.getElementsByTagName("limitSelector")
					.item(0).getTextContent());
			config.setCookies(root.getElementsByTagName("cookies").item(0)
					.getTextContent());

			NodeList inputs = root.getElementsByTagName("inputs").item(0)
					.getChildNodes();
			for (int i = 0; i < inputs.getLength(); i++) {
				if (inputs.item(i).getNodeName().equals("input")) {
					WebInput in = new WebInput();
					in.setType(Type.valueOf(inputs.item(i).getAttributes()
							.getNamedItem("type").getNodeValue()));
					in.setAddress(inputs.item(i).getAttributes()
							.getNamedItem("address").getNodeValue());
					in.setMethod(HttpMethod.valueOf(inputs.item(i)
							.getAttributes().getNamedItem("method")
							.getNodeValue()));
					in.setType(Type.valueOf(inputs.item(i).getAttributes()
							.getNamedItem("type").getNodeValue()));

					int nbValue = 0;
					for (int j = 0; j < inputs.item(i).getChildNodes().item(1)
							.getChildNodes().getLength(); j++) {
						if (inputs.item(i).getChildNodes().item(1)
								.getChildNodes().item(j).getNodeName()
								.equals("parametersCombination")) {
							nbValue++;
							for (int k = 0; k < inputs.item(i).getChildNodes()
									.item(1).getChildNodes().item(j)
									.getChildNodes().getLength(); k++) {
								if (inputs.item(i).getChildNodes().item(1)
										.getChildNodes().item(j)
										.getChildNodes().item(k).getNodeName()
										.equals("parameter")) {
									String name = inputs.item(i)
											.getChildNodes().item(1)
											.getChildNodes().item(j)
											.getChildNodes().item(k)
											.getAttributes()
											.getNamedItem("name")
											.getNodeValue();
									String value = inputs.item(i)
											.getChildNodes().item(1)
											.getChildNodes().item(j)
											.getChildNodes().item(k)
											.getTextContent();
									if (in.getParams().get(name) == null)
										in.getParams().put(name,
												new ArrayList<String>());
									in.getParams().get(name).add(value);
								}
							}
						}
					}
					in.setNbValues(nbValue);
					if (in.getParams().isEmpty()) {
						in.getParams().put("noparam",
								Utils.createArrayList("novalue"));
						in.setNbValues(1);
					}
					this.inputs.add(in);
				}
			}

			NodeList outputs = root.getElementsByTagName("outputs").item(0)
					.getChildNodes();
			for (int i = 0; i < outputs.getLength(); i++) {
				if (outputs.item(i).getNodeName().equals("output")) {
					WebOutput out = new WebOutput(outputs.item(i).getChildNodes()
							.item(1).getTextContent(), true,
							config.getLimitSelector());
					for (int j = 0; j < outputs.item(i).getChildNodes().item(3)
							.getChildNodes().getLength(); j++) {
						if (outputs.item(i).getChildNodes().item(3)
								.getChildNodes().item(j).getNodeName()
								.equals("parameter")) {
							String value = outputs.item(i).getChildNodes()
									.item(3).getChildNodes().item(j)
									.getTextContent();
							out.getParams().add(value);
						}
					}
					this.outputs.add(out);
				}
			}
		}catch (ParserConfigurationException|SAXException e){
			LogManager.logException("Error parsing the xml file \"" + xml + "\"",  e);
		}catch (IOException e) {
			LogManager.logException("Unable to read the file \"" + xml + "\"",  e);
		}
		return config;
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		ArrayList<ArrayList<Parameter>> params = null;
		ArrayList<Parameter> one = null;
		int index = 0;
		for (WebInput i : inputs) {
			params = new ArrayList<ArrayList<Parameter>>();
			int nbParam = i.getNbValues();
			for (int k = 0; k < nbParam; k++) {
				one = new ArrayList<Parameter>();
				for (String key : i.getParams().keySet()) {
					one.add(new Parameter(i.getParams().get(key).get(k),
							Types.STRING));
				}
				params.add(one);
			}
			defaultParamValues.put("input_" + String.valueOf(index), params);
			index++;
		}
		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> res = new TreeMap<String, List<String>>();
		int index = 0;
		for (WebInput i : inputs) {
			List<String> names = new ArrayList<String>();
			for (String key : i.getParams().keySet()) {
				names.add(key);
			}
			res.put("input_" + String.valueOf(index), names);
			index++;
		}
		index = 0;
		for (WebOutput o : outputs) {
			List<String> names = new ArrayList<String>();
			if (o.getParams().isEmpty()) {
				names.add("output_" + String.valueOf(index) + "_status");
			} else {
				for (int n = 0; n < o.getParams().size(); n++) {
					names.add("output_" + String.valueOf(index) + "_param"
							+ String.valueOf(n));
				}
			}
			res.put("output_" + String.valueOf(index), names);
			index++;
		}
		return res;
	}

	@Override
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		for (int i = 0; i < inputs.size(); i++) {
			is.add("input_" + i);
		}
		return is;
	}

	@Override
	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		for (int i = 0; i < outputs.size(); i++) {
			os.add("output_" + i);
		}
		return os;
	}
}
