package crawler;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

import main.drivergen.Options;
import tools.GraphViz;
import tools.HTTPData;
import tools.Utils;
import tools.loggers.LogManager;

public abstract class DriverGenerator {

	protected WebClient client = null;
	/**
	 * Data provided by the user that will be given as input for forms
	 */
	protected Map<String, ArrayList<String>> formValues = null;
	/**
	 * Number of sent requests
	 */
	protected int requests = 0;
	/**
	 * List of already used inputs
	 */
	protected List<WebInput> inputs = null;
	/**
	 * List of already encountered output pages
	 */
	protected ArrayList<WebOutput> outputs = null;
	protected HashSet<String> comments = null;
	protected List<String> colors = null;
	/**
	 * The user-provided urls used as entry points
	 */
	protected List<String> urlsToCrawl = null;

	/**
	 * List of transitions between pages, defined by two node numbers and one
	 * input
	 */
	protected ArrayList<WebTransition> transitions = null;
	protected static Configuration config = null;

	@SuppressWarnings("deprecation")
	public DriverGenerator(String configFileName) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(new File(configFileName),
				Configuration.class);
		config.check();
		urlsToCrawl = new ArrayList<>();
		inputs = new ArrayList<>();
		comments = new HashSet<>();
		transitions = new ArrayList<>();
		outputs = new ArrayList<>();
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		client = new WebClient();
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setTimeout(Options.TIMEOUT);
		client.getOptions().setCssEnabled(Options.CSS);
		client.getOptions().setJavaScriptEnabled(Options.JS);
		client.getOptions().setUseInsecureSSL(true);
		CookieManager cm = new CookieManager();
		if (config.getCookies() != null) {
			String cookieValue = null;
			while (cookieValue == null) {
				cookieValue = (String) JOptionPane.showInputDialog(null, "Cookies value required :",
						"Loading configuration ...",
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
		formValues = config.getData();
		for (String url : config.getURLs()) {
			addUrl(url);
		}
	}

	public void start() {
		System.out.println("[+] Crawling ...");
		long duration = System.nanoTime();
		reset();
		for (String url : urlsToCrawl) {
			WebInput in = new WebInput(url);
			crawlInput(in);
		}

		if (config.getMerge()) {
			System.out.println();
			System.out.println("[+] Merging inputs");
			mergeInputs();
		}

		System.out.println();
		System.out.println("[+] Inputs (" + inputs.size() + ")");
		for (WebInput in : inputs) {
			System.out.println("    " + in);
		}

		System.out.println();
		System.out.println("[+] Outputs (" + outputs.size() + ")");
		for (WebOutput out : outputs) {
			int paramsNumber = out.getParamsNumber();
			if (paramsNumber == 0) {
				continue;
			}
			System.out.println("    " + out.getState() + " : " + paramsNumber + " outputs parameters : ");
			for (Iterator<String> iter = out.getParamsIterator(); iter.hasNext();) {
				String paramPath = iter.next();
				System.out.println("        " + out.getParamValue(paramPath));
			}
		}

		System.out.println();
		System.out.println("[+] Stats");
		System.out.println("    Duration : " + ((System.nanoTime() - duration) / 1000000000.00) + " secs");
		System.out.println("    Requests : " + requests);

		System.out.println();
		System.out
				.println("[+] Model (" + transitions.size() + " transitions)");
		for (WebTransition t : transitions) {
			System.out.println("    " + t);
		}

		System.out.println();
		System.out.println("[+] Comments (" + comments.size() + ")");
		Iterator<String> iter = comments.iterator();
		while (iter.hasNext()) {
			System.out.println("    " + iter.next());
		}
		System.out.flush();

	}

	/**
	 * Start the crawling of the web application from a given entry point
	 *
	 * @param start the input to start the crawling with
	 * @return
	 */
	abstract protected int crawlInput(WebInput start);

	/**
	 * The set of requirements verified on an input in order to be added
	 * (heuristics from Karim's experiments)
	 *
	 * @param in
	 * @return
	 */
	protected final boolean isInputWorthKeeping(WebInput in) {
		TreeMap<String, List<String>> inParams = in.getParams();

		for (WebInput i : inputs) {
			TreeMap<String, List<String>> iParams = i.getParams();

			if (i.equals(in)) {
				return false;
			}

			//If the navigation is done by an "action parameter" ...
			if ((config.getActionByParameter() != null)) {
				List<String> inActionParameterValues = inParams.get(config.getActionByParameter());
				List<String> iActionParameterValues = iParams.get(config.getActionByParameter());

				//... and there is another input with the same values for its action parameter, but with less parameters
				if ((inActionParameterValues != null)
						&& (iActionParameterValues != null)
						&& (iActionParameterValues.equals(inActionParameterValues))
						&& iParams.size() < inParams.size()) {
					return false;
				}
			}

			//If there is another input with the same address and at least one parameter, whereas the candidate input has no parameters
			if (i.getAddress().equals(in.getAddress())
					&& !iParams.isEmpty()
					&& inParams.isEmpty()) {
				return false;
			}
		}

		for (WebInput i : inputs) {
			TreeMap<String, List<String>> iParams = i.getParams();

			if ((config.getActionByParameter() != null)) {
				List<String> inActionParameterValues = inParams.get(config.getActionByParameter());
				List<String> iActionParameterValues = iParams.get(config.getActionByParameter());

				// If there is another input with the same values for its action parameter, and almost equal to the candidate input
				if ((inActionParameterValues != null)
						&& (iActionParameterValues != null)
						&& (iActionParameterValues.equals(inActionParameterValues))
						&& i.isAlmostEquals(in)) {
					return false;
				}
			}
		}

		for (WebInput i : inputs) {
			TreeMap<String, List<String>> iParams = i.getParams();
			//do not add if another input with the same address has exactly one
			//different parameter value, and this value is numeric (to avoid "gallery" pages)
			//TODO : modify this to match the specs in Karim's paper page 82
			if (!iParams.keySet().equals(inParams.keySet())) {
				continue;
			}
			if (!i.getAddress().equals(in.getAddress()) || !i.getMethod().equals(in.getMethod())) {
				continue;
			}
			
			int diff = 0;
			for (String paramName : iParams.keySet()) {
				if (inParams.get(paramName) != null && iParams.get(paramName) != null) {
					if (inParams.get(paramName).size() == 1 && iParams.get(paramName).size() == 1) {
						//a different parameter value is found
						if (!inParams.get(paramName).get(0).equals(iParams.get(paramName).get(0))) {
							if (Utils.isNumeric(inParams.get(paramName).get(0)) && Utils.isNumeric(iParams.get(paramName).get(0))) {
								diff++;
							} else {
								break;
							}
						}
					}
				}
			}
			if (diff == 1) {
				return false;
			}

		}

		return true;
	}

	/**
	 * Verifies if the given input meets a set of requirements (cf. method
	 * body). If so, the input is added to the list of stored inputs, and
	 * corresponding inputs already stored might be deleted.
	 *
	 * @param in The input to add
	 * @return True if the input was successfully added, False otherwise
	 */
	protected boolean addInput(WebInput in) {
		if (!isInputWorthKeeping(in)) {
			return false;
		}

		TreeMap<String, List<String>> inParams = in.getParams();
		//For each parameter without value, we assign a user-provided value, or a random string otherwise.
		for (String key : inParams.keySet()) {
			List<String> values = inParams.get(key);
			if (values.isEmpty()) {
				// TODO
				String providedValue;
				if (formValues.get(key) == null || formValues.get(key).isEmpty()) {
					providedValue = Utils.randString();
					comments.add("No values for "
							+ key
							+ ", random string used. You may need to provide useful value.");

				} else {
					providedValue = formValues.get(key).get(0);
				}
				values.add(providedValue);
			}
		}
		inputs.add(in);

		Iterator<WebInput> iterInputs;
		//If the navigation is done by an action parameter, and the option "keep smallest set" is activated
		if (config.getActionByParameter() != null && config.keepSmallSet()) {
			List<String> inActionParameterValues = inParams.get(config.getActionByParameter());

			iterInputs = inputs.iterator();
			while (iterInputs.hasNext()) {
				WebInput wi = iterInputs.next();
				TreeMap<String, List<String>> iParams = wi.getParams();
				List<String> iActionParameterValues = iParams.get(config.getActionByParameter());

				//If there is another input with the same address, but more parameters, we remove it
				if ((wi.getAddress().equals(in.getAddress()))
						&& (wi.getMethod().equals(in.getMethod()))
						&& (inActionParameterValues != null)
						&& (iActionParameterValues != null)
						&& (iActionParameterValues.equals(inActionParameterValues))
						&& (iParams.size() > inParams.size())) {
					iterInputs.remove();
					for (int t = transitions.size() - 1; t >= 0; t--) {
						if (transitions.get(t).getBy() == wi) {
							transitions.get(t).setBy(in);
						}
					}
				}
			}
		}

		iterInputs = inputs.iterator();
		while (iterInputs.hasNext()) {
			WebInput wi = iterInputs.next();
			TreeMap<String, List<String>> iParams = wi.getParams();

			//If there is another input with the same address, no parameters, and the added input has at least one parameter, it is removed
			if ((wi.getAddress().equals(in.getAddress()))
					&& (wi.getMethod().equals(in.getMethod()))
					&& iParams.isEmpty()
					&& !inParams.isEmpty()) {
				iterInputs.remove();
				for (int t = transitions.size() - 1; t >= 0; t--) {
					if (transitions.get(t).getBy() == wi) {
						transitions.get(t).setBy(in);
					}
				}
			}
		}
		return true;
	}

	public final void addUrl(String url) {
		if (url != null && !"".equals(url)) {
			urlsToCrawl.add(url);
		}
	}

	protected HTTPData getHTTPDataFromInput(WebInput in) {
		return getHTTPDataFromInput(in, false);
	}

	/**
	 * If the input uses POST method, verifies for each parameter if there is an
	 * unique assigned value. If multiple values are present, picks one already
	 * present in the WebInput, or an user-provided one. Otherwise, generates a
	 * random value.
	 *
	 * @param in
	 * @param randomized If true, randomize the parameters values
	 * @return An HTTPData object with every parameter/value couples
	 */
	protected HTTPData getHTTPDataFromInput(WebInput in, boolean randomized) {
		HTTPData data = new HTTPData();
		if (in.getMethod() == HttpMethod.POST) {
			TreeMap<String, List<String>> params = in.getParams();
			for (String key : params.keySet()) {
				List<String> values = params.get(key);
				//If a single value for this parameter is present in the input...
				if (values.size() == 1) {
					//... this value is used.
					data.add(key, values.get(0));
					//If no or multiple values are present in this input ...
				} else {
					String newValue;
					// ... and the user did not provide any value ...
					if (randomized || formValues.get(key) == null || formValues.get(key).isEmpty()) {
						// ... and multiple values are present in this input ...
						if (values.size() > 1) {
							// ... a random value from those present is used.
							newValue = Utils.randIn(values);
							// ... and no value is present in the input ...
						} else {
							// ... a random string is used.
							if (!randomized) {
								comments.add("No values for "
										+ key
										+ ", random string used. Please provide one value.");
							}
							newValue = Utils.randString();
						}
						//... and the user provided one or multiple values ...
					} else {
						//... the first value is used
						newValue = formValues.get(key).get(0);
					}
					data.add(key, newValue);
				}
			}
		} else {
			System.err.println("Internal error : form with GET method not yet implemented");
		}
		return data;
	}

	/**
	 * Search if in the previously visited outputs there is one which is
	 * equivalent to the provided output
	 *
	 * @param to the output to be compared to the others
	 * @return an equivalent output if any
	 */
	protected WebOutput findEquivalentOutput(WebOutput to) {
		for (WebOutput current : outputs) {
			if (to.isEquivalentTo(current)) {
				return current;
			}
		}
		return null;
	}

	/**
	 * Manually search HTML forms in a document
	 *
	 * @param content
	 * @param baseUri
	 * @return
	 */
	protected Collection<Element> findFormsIn(String content, String baseUri) {
		List<Element> el = new ArrayList<>();
		int nextForm = content.indexOf("<form ");
		while (nextForm != -1) {
			Document d = Jsoup.parseBodyFragment(content.substring(nextForm, content.indexOf("</form", nextForm)));
			d.setBaseUri(baseUri);
			el.addAll(d.select("form"));
			nextForm = content.indexOf("<form ", nextForm + 1);
		}
		nextForm = content.indexOf("<FORM ");
		while (nextForm != -1) {
			Document d = Jsoup.parseBodyFragment(content.substring(nextForm, content.indexOf("</FORM", nextForm)));
			d.setBaseUri(baseUri);
			el.addAll(d.select("FORM"));
			nextForm = content.indexOf("<FORM ", nextForm + 1);
		}
		return el;
	}

	private void initColor() {
		final float saturation = 0.2f;
		final float luminance = 0.9f;
		float hue;
		colors = new ArrayList<>();
		for (int i = 0; i < outputs.size(); i++) {
			hue = ((0.8f / outputs.size()) * i) + 0.1f;
			Color c = Color.getHSBColor(hue, saturation, luminance);
			colors.add(Integer.toHexString(c.getRed()) + Integer.toHexString(c.getGreen()) + Integer.toHexString(c.getBlue()));
		}
		Collections.shuffle(colors);
	}

	public void exportToDot() {
		initColor();
		Writer writer = null;
		File file = null;
		File dir = new File("models");
		try {
			if (!dir.isDirectory() && !dir.mkdirs()) {
				throw new IOException("unable to create " + dir.getName()
						+ " directory");
			}

			file = new File(dir.getPath() + File.separatorChar
					+ config.getName() + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			exportToDotCreateNodes(writer);
			for (WebTransition t : transitions) {
				writer.write("\t" + t.getFrom() + " -> " + t.getTo() + " [label=\"" + prettyprint(t.getBy()) + "\"]" + "\n");
			}
			writer.write("}\n");
			writer.close();
			GraphViz.dotToFile(file.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	private void exportToDotCreateNodes(Writer w) throws IOException {
		for (int i = 0; i < outputs.size(); i++) {
			w.write("    " + i + " [style=\"filled\", fillcolor=\"#" + colors.get(outputs.get(i).getState()) + "\"]\n");
		}
	}

	public void exportToXML() {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = icBuilder.newDocument();
			org.w3c.dom.Element edriver = doc.createElement("driver");
			edriver.setAttribute("version", Options.VERSION);
			edriver.setAttribute("generator", Options.NAME);
			edriver.setAttribute("date", String.valueOf(new Date().getTime()));
			org.w3c.dom.Element esettings = doc.createElement("settings");
			Node n = doc.createElement("target");
			n.setTextContent(config.getName());
			esettings.appendChild(n);
			n = doc.createElement("host");
			n.setTextContent(config.getHost());
			esettings.appendChild(n);
			n = doc.createElement("port");
			n.setTextContent(String.valueOf(config.getPort()));
			esettings.appendChild(n);
			n = doc.createElement("reset");
			n.setTextContent(config.getReset());
			esettings.appendChild(n);
			n = doc.createElement("limitSelector");
			n.setTextContent(config.getLimitSelector());
			esettings.appendChild(n);
			n = doc.createElement("cookies");
			n.setTextContent(config.getCookies());
			esettings.appendChild(n);
			n = doc.createElement("basicAuthUser");
			n.setTextContent(config.getBasicAuthUser());
			esettings.appendChild(n);
			n = doc.createElement("basicAuthPass");
			n.setTextContent(config.getBasicAuthPass());
			esettings.appendChild(n);
			edriver.appendChild(esettings);
			org.w3c.dom.Element einputs = doc.createElement("inputs");
			LinkedList<WebInput> inputsCopy = new LinkedList<>(inputs);
			while (!inputsCopy.isEmpty()) {
				WebInput current = inputsCopy.getFirst();

				org.w3c.dom.Element einput = doc.createElement("input");
				einput.setAttribute("type", String.valueOf(current.getType()));
				einput.setAttribute("method", String.valueOf(current.getMethod()));
				einput.setAttribute("address", current.getAddress());
				einputs.appendChild(einput);

				org.w3c.dom.Element eparams = doc.createElement("parameters");
				einput.appendChild(eparams);

				for (Iterator<WebInput> iter = inputsCopy.iterator(); iter.hasNext();) {
					WebInput i = iter.next();
					if (i.isLike(current)) {
						org.w3c.dom.Element eparamsComb = doc.createElement("parametersCombination");
						eparams.appendChild(eparamsComb);

						for (String name : i.getParams().keySet()) {
							for (String value : i.getParams().get(name)) {
								org.w3c.dom.Element eparam = doc.createElement("parameter");
								eparam.setAttribute("name", name);
								eparam.setTextContent(value);
								eparamsComb.appendChild(eparam);
								break;//TODO : rendre clair le fait qu'on a jamais deux valeurs pour un parametre
							}
						}
						iter.remove();
					}
				}
			}
			edriver.appendChild(einputs);

			org.w3c.dom.Element eoutputs = doc.createElement("outputs");
			for (WebOutput o : outputs) {
				org.w3c.dom.Element eoutput = doc.createElement("output");
				org.w3c.dom.Element ediff = doc.createElement("source");
				ediff.setTextContent(o.getSource());
				eoutput.appendChild(ediff);
				org.w3c.dom.Element eparams = doc.createElement("parameters");

				for (Iterator<String> iter = o.getParamsIterator(); iter.hasNext();) {
					String value = iter.next();
					org.w3c.dom.Element eparam = doc.createElement("parameter");
					eparam.setTextContent(value);
					eparams.appendChild(eparam);
				}

				eoutput.appendChild(eparams);
				eoutputs.appendChild(eoutput);
			}
			edriver.appendChild(eoutputs);
			doc.appendChild(edriver);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);

			File dir = new File("abs");
			if (!dir.isDirectory() && !dir.mkdirs()) {
				throw new IOException("unable to create " + dir.getName()
						+ " directory");
			}
			StreamResult xml = new StreamResult(new File("abs" + File.separator + config.getName() + ".xml").toURI().getPath());
			transformer.transform(source, xml);
		} catch (Exception e) {
			LogManager.logException("Error writing xml file", e);
		}
	}

	private String prettyprint(WebInput by) {
		if (config.getActionByParameter() == null) {
			String[] split = by.getAddress().split("/");
			return split[split.length - 1];
		}
		List<String> possibleActions = by.getParams().get(config.getActionByParameter());
		if (possibleActions == null || possibleActions.isEmpty()) {
			return by.getAddress();
		} else {
			return possibleActions.get(0);
		}
	}

	private void mergeInputs() {
		List<WebInput> mergedInput = new ArrayList<>();
		WebInput discintIn, anInput;
		List<String> paramValues;
		while (inputs.size() > 0) {
			discintIn = inputs.get(0);
			for (int j = inputs.size() - 1; j >= 0; j--) {
				anInput = inputs.get(j);
				if (discintIn.getMethod().equals(anInput.getMethod()) && discintIn.getAddress().equals(anInput.getAddress())) {
					for (String param : anInput.getParams().keySet()) {
						paramValues = anInput.getParams().get(param);
						if (discintIn.getParams().get(param) == null) {
							discintIn.getParams().put(param, paramValues);
						} else {
							for (String paramValue : paramValues) {
								if (!discintIn.getParams().get(param).contains(paramValue)) {
									discintIn.getParams().get(param).add(paramValue);
								}
							}
						}
					}
					inputs.remove(j);
				}
			}
			mergedInput.add(discintIn);
		}
		this.inputs = mergedInput;
	}

	/**
	 * Reset the application using the provided url (if any).
	 */
	protected void reset() {
		if (config.getCookies() == null) {
			client.getCookieManager().clearCookies();
		}
		if (config.getReset() != null && !config.getReset().isEmpty()) {
			try {
				client.getPage(config.getReset());
			} catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
			} catch (Exception e) {
				LogManager.logException("Error resetting application", e);
			}
		}
	}
}
