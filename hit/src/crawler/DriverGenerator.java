package crawler;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import main.drivergen.Options;

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
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import tools.GraphViz;
import tools.HTTPData;
import tools.Utils;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import crawler.WebInput.Type;
import java.util.LinkedList;
import java.util.logging.Logger;

public class DriverGenerator {

	/**
	 * The user-provided urls used as entry points
	 */
	protected List<String> urlsToCrawl = null;
	/**
	 * List of already used inputs
	 */
	protected List<WebInput> inputs = null;
	/**
	 * Data provided by the user that will be given as input for forms
	 */
	protected Map<String, ArrayList<String>> formValues = null;
	/**
	 * Current sequence of WebInput used for crawling
	 */
	protected LinkedList<WebInput> sequence = null;
	protected WebClient client = null;
	protected HashSet<String> comments = null;
	/**
	 * List of already encountered output pages
	 */
	protected ArrayList<WebOutput> outputs = null;
	/**
	 * List of transitions between pages, defined by two node numbers and one
	 * input
	 */
	protected ArrayList<WebTransition> transitions = null;
	/**
	 * Stack of the numbers of the nodes used for crawling to the current page
	 * (cf. sequence)
	 */
	protected LinkedList<Integer> currentNode = null;
	protected List<String> colors = null;
	/**
	 * Number of sent requests
	 */
	protected int requests = 0;

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
		sequence = new LinkedList<>();
		comments = new HashSet<>();
		transitions = new ArrayList<>();
		outputs = new ArrayList<>();
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
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
		currentNode = new LinkedList<>();
		currentNode.add(0);
	}

	public String getName() {
		return config.getName();
	}

	/**
	 * Reset the web application (if possible), and send all the inputs in
	 * sequence.
	 */
	private void sendSequences() {
		reset();
		for (WebInput in : sequence) {
			try {
				submit(in);
			} catch (FailingHttpStatusCodeException | IOException e) {
				LogManager.logException("Unable to execute sequence", e);
			}
		}
	}

	/**
	 * Reset the application using the provided url (if any).
	 */
	private void reset() {
		if (config.getReset() != null) {
			try {
				client.getPage(config.getReset());
			} catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
			} catch (Exception e) {
				LogManager.logException("Error resetting application", e);
			}
		}
	}

	/**
	 * Retrieve the addresses from links, and filter out those which match the
	 * given regexs.
	 *
	 * @param links The links elements to be filtered
	 * @return A list of filtered urls (relative or absolute)
	 */
	private List<String> filterUrl(Elements links) {
		List<String> urls = new ArrayList<>();
		for (Element e : links) {
			String to = e.attr("href");
			if (to.equals("")) {
				continue;
			}
			boolean add = true;
			for (String filter : config.getNoFollow()) {
				if (to.toLowerCase().matches(filter)) {
					add = false;
					break;
				}
			}
			if (add) {
				urls.add(to);
			}
		}
		return urls;
	}

	/**
	 * Verifies if the given input meets a set of requirements (cf. method
	 * body). If so, the input is added to the list of stored inputs, and
	 * corresponding inputs already stored might be deleted.
	 *
	 * @param in The input to add
	 * @return True if the input was successfully added, False otherwise
	 */
	private boolean addInput(WebInput in) {
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
			//do not add if another input with the same address has at least one
			//different numeric parameter value (to avoid "gallery" pages)
			//TODO : modify this to match the specs in Karim's paper page 82
			if (i.getAddress().equals(in.getAddress())) {
				int diff = 0;
				for (String paramName : iParams.keySet()) {
					if (inParams.get(paramName) != null && iParams.get(paramName) != null) {
						if (inParams.get(paramName).size() == 1 && iParams.get(paramName).size() == 1) {
							if (Utils.isNumeric(inParams.get(paramName).get(0)) && Utils.isNumeric(iParams.get(paramName).get(0))) {
								if (!inParams.get(paramName).get(0).equals(iParams.get(paramName).get(0))) {
									diff++;
								}
							}
						}
					}
				}
				if (diff >= 1) {
					return false;
				}
			}
		}

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
			while(iterInputs.hasNext()){
				WebInput wi = iterInputs.next();
				TreeMap<String, List<String>> iParams = wi.getParams();
				List<String> iActionParameterValues = iParams.get(config.getActionByParameter());

				//If there is another input with the same address, but more parameters, we remove it
				if ((wi.getAddress().equals(in.getAddress()))
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
		while(iterInputs.hasNext()){
			WebInput wi = iterInputs.next();
			TreeMap<String, List<String>> iParams = wi.getParams();

			//If there is another input with the same address, no parameters, and the added input has at least one parameter, it is removed
			if ((wi.getAddress().equals(in.getAddress()))
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

	/**
	 * If the input is a form, verifies for each parameter if there is an unique
	 * assigned value. If multiple values are present, picks one already present
	 * in the WebInput, or an user-provided one. Otherwise, generates a random
	 * value.
	 *
	 * @return An HTTPData object with every parameter/value couples
	 */
	private HTTPData getHTTPDataFromInput(WebInput in) {
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
					// TODO
					String newValue;
					// ... and the user did not provide any value ...
					if (formValues.get(key) == null || formValues.get(key).isEmpty()) {
						// ... and multiple values are present in this input ...
						if (values.size() > 1) {
							// ... a random value from those present is used.
							newValue = Utils.randIn(values);
						// ... and no value is present in the input ...
						} else {
							// ... a random string is used.
							comments.add("Multiple values for "
								+ key
								+ ", random string used. Please provide one value.");
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
	 * Execute a WebInput object by sending a concrete HTTP request
	 *
	 * @param in
	 * @return
	 * @throws MalformedURLException
	 */
	private String submit(WebInput in) throws MalformedURLException {
		WebRequest request;
		if (in.getMethod() == HttpMethod.POST) {
			HTTPData values = getHTTPDataFromInput(in);
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());
			request.setAdditionalHeader("Connection", "Close");
			Page page;
			try {
				page = client.getPage(request);
				if (page.getWebResponse().getStatusCode() != 200) {
					return null;
				}
				requests++;
			} catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if (page instanceof TextPage) {
				return ((TextPage) page).getContent();
			} else {
				return ((HtmlPage) page).asXml();
			}
		} else if (in.getMethod() == HttpMethod.GET) {
			String link = in.getAddressWithParameters();
			HtmlPage page;
			try {
				page = client.getPage(link);
				if (page.getWebResponse().getStatusCode() != 200) {
					return null;
				}
				requests++;
			} catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}

	public void addUrl(String url) {
		if (url != null && !"".equals(url)) {
			urlsToCrawl.add(url);
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

	private int crawl(Document d, WebInput from, String content) {
		int node = updateOutput(d, from);
		currentNode.addLast(node);
		Element lesson;
		if (!config.getLimitSelector().isEmpty()) {
			lesson = d.select(config.getLimitSelector()).first();
		} else {
			lesson = d.getAllElements().first();
		}
		if (lesson != null) {
			Elements links = lesson.select("a[href]");
			Elements forms = new Elements();
			forms.addAll(findFormsIn(content, d.baseUri())); // https://github.com/jhy/jsoup/issues/249
			System.out.println("        "
				+ links.size()
				+ " links and "
				+ (forms.select("input[type=submit]").size() + forms
				.select("input[type=image]").size()) + " forms");

			for (Element aform : forms) {
				List<WebInput> formList = WebInput.extractInputsFromForm(aform);
				for (WebInput in : formList) {
					addProvidedParameter(in);
					if (addInput(in)) {
						sendSequences();
						int next = crawlInput(in);
						if (next == -1) {
							inputs.remove(in);
						}
						sequence.removeLast();
					}
				}
			}
			for (String url : filterUrl(links)) {
				try {
					URL absURL = new URL(new URL(d.baseUri()), url);
					url = absURL.toString();
				} catch (MalformedURLException ex) {
					Logger.getLogger(DriverGenerator.class.getName()).log(Level.SEVERE, null, ex);
					continue;
				}
				WebInput in = new WebInput(url);
				if (addInput(in)) {
					sendSequences();
					int next = crawlInput(in);
					if (next == -1) {
						inputs.remove(in);
					}
					sequence.removeLast();
				}
			}
		}
		currentNode.removeLast();
		return node;
	}

	/**
	 * Add or replace the existing parameters by those provided by the user. If
	 * input parameter values contains all the provided values, then they will
	 * be replaced by the provided values only. Else, the provided values will
	 * be added
	 *
	 * @param in The WebInput to modify
	 */
	private void addProvidedParameter(WebInput in) {
		for (String name : in.getParams().keySet()) {
			if (config.getData().get(name) != null) {
				if (in.getParams().get(name).containsAll(config.getData().get(name))) {
					in.getParams().put(name, config.getData().get(name));
				} else {
					for (String value : config.getData().get(name)) {
						if (!in.getParams().get(name).contains(value)) {
							in.getParams().get(name).add(value);
						}
					}
				}
			}
		}
	}

	private Collection<Element> findFormsIn(String content, String baseUri) {
		List<Element> el = new ArrayList<Element>();
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

	private void exportToDotCreateNodes(Writer w) throws IOException {
		for (int i = 0; i < outputs.size(); i++) {
			w.write("    " + i + " [style=\"filled\", fillcolor=\"#" + colors.get(outputs.get(i).getState()) + "\"]\n");
		}
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

	private String prettyprint(WebInput by) {
		if (config.getActionByParameter() == null) {
			return by.getAddress();
		}
		List<String> possibleActions = by.getParams().get(config.getActionByParameter());
		if (possibleActions == null || possibleActions.isEmpty()) {
			return by.getAddress();
		} else {
			return possibleActions.get(0);
		}
	}

	/**
	 * Checks if the output page has already been seen, and if so, checks if it
	 * is the first time we access it with the given input.
	 *
	 * @param d The output page
	 * @param from The input used to access the page
	 * @return The index of the output page in the collection of all the
	 * previously encountered pages
	 */
	private int updateOutput(Document d, WebInput from) {
		WebOutput o = new WebOutput(d, from, config.getLimitSelector());
		if (d.toString().isEmpty()) {
			return 0;
		} else {
			for (int i = 0; i < outputs.size(); i++) {
				if (o.isEquivalentTo(outputs.get(i))) {
					if (outputs.get(i).isNewFrom(from)) {
						findParameters(outputs.get(i));
					}
					return i;
				}
			}
		}
		outputs.add(o);
		System.out.println("        New page !");
		findParameters(o);
		return outputs.size() - 1;
	}

	private HTTPData getRandomValuesForInput(WebInput in) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			TreeMap<String, List<String>> inputParams = in.getParams();
			for (String key : inputParams.keySet()) {
				List<String> values = inputParams.get(key);
				if (values.isEmpty() || values.size() > 1) {
					String newValue = null;
					if (values.size() > 1) {
						newValue = Utils.randIn(values);
					} else {
						newValue = Utils.randString();
					}
					data.add(key, newValue);
				} else {
					data.add(key, values.get(0));
				}
			}
		}
		return data;
	}

	private String submitRandom(WebInput in) throws MalformedURLException {
		HTTPData values = getRandomValuesForInput(in);
		if (in.getType() == Type.FORM) {
			WebRequest request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());

			HtmlPage page;
			try {
				page = client.getPage(request);
				requests++;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		} else if (in.getType() == Type.LINK) {
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()) {
				for (String name : in.getParams().keySet()) {
					link += name + "=" + Utils.randIn(in.getParams().get(name)) + "&";
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length() - 1));
				requests++;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}

	private void findParameters(WebOutput out) {
		Set<String> diff = new HashSet<>();
		WebInput inputToFuzz = sequence.removeLast();
		for (int i = 0; i < 5; i++) {
			try {
				sendSequences();
				WebOutput variant = new WebOutput(submitRandom(inputToFuzz), config.getLimitSelector());
				if (out.isEquivalentTo(variant)) {
					diff.addAll(findDifferences(out, variant));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}
		sequence.addLast(inputToFuzz);
		out.getParams().addAll(diff);
		System.out.println("        " + out.getParams().size() + " output parameters");
	}

	private Set<String> findDifferences(WebOutput first, WebOutput second) {
		Set<String> diff = new HashSet<String>();
		List<String> pos = new ArrayList<String>();
		Elements firstE = first.getDoc();
		Elements secondE = second.getDoc();
		if (firstE.size() == secondE.size()) {
			for (int i = 0; i < firstE.size(); i++) {
				pos.add(String.valueOf(i));
				findDifferences(firstE.get(i), secondE.get(i), diff, pos);
				pos.remove(pos.size() - 1);
			}
		}
		return diff;
	}

	private void findDifferences(Element first, Element second, Set<String> diff, List<String> pos) {
		if (first.nodeName().equals(second.nodeName())) {
			pos.add("/");
			if (!first.ownText().equals(second.ownText())) {
				String xpath = "";
				for (String tag : pos) {
					xpath += tag;
				}
				diff.add(xpath);
			}
			if (first.children().size() == second.children().size()) {
				for (int i = 0; i < first.children().size(); i++) {
					pos.add(String.valueOf(i));
					findDifferences(first.child(i), second.child(i), diff, pos);
					pos.remove(pos.size() - 1);
				}
			}
			pos.remove(pos.size() - 1);
		}
	}

	private int crawlInput(WebInput in) {
		sequence.addLast(in);
		System.out.println("    " + (in.getType() == Type.FORM ? "f" : "l") + " " + in.getAddress() + ' ' + in.getParams());

		try {
			String content = submit(in);
			if (content != null) {
				Document doc = Jsoup.parse(content);
				doc.setBaseUri(in.getAddress());
				int output = crawl(doc, in, content);
				transitions.add(new WebTransition(currentNode.getLast(), output, in));
				return output;
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to get page for " + in, e);
		}
		return -1;
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
			for (WebInput i : inputs) {
				org.w3c.dom.Element einput = doc.createElement("input");
				einput.setAttribute("type", String.valueOf(i.getType()));
				einput.setAttribute("method", String.valueOf(i.getMethod()));
				einput.setAttribute("address", i.getAddress());
				org.w3c.dom.Element eparams = doc.createElement("parameters");
				org.w3c.dom.Element eparamsComb = doc.createElement("parametersCombination");
				for (String name : i.getParams().keySet()) {
					for (String value : i.getParams().get(name)) {
						org.w3c.dom.Element eparam = doc.createElement("parameter");
						eparam.setAttribute("name", name);
						eparam.setTextContent(value);
						eparamsComb.appendChild(eparam);
						break;
					}
				}
				eparams.appendChild(eparamsComb);
				einput.appendChild(eparams);
				einputs.appendChild(einput);
			}
			edriver.appendChild(einputs);
			org.w3c.dom.Element eoutputs = doc.createElement("outputs");
			for (WebOutput o : outputs) {
				org.w3c.dom.Element eoutput = doc.createElement("output");
				org.w3c.dom.Element ediff = doc.createElement("source");
				ediff.setTextContent(o.getSource());
				eoutput.appendChild(ediff);
				org.w3c.dom.Element eparams = doc.createElement("parameters");

				for (String value : o.getParams()) {
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
}