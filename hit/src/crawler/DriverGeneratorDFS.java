/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 ********************************************************************************/
package crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.http.conn.HttpHostConnectException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.HTTPData;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crawler.WebInput.Type;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * @deprecated
 * @see DriverGeneratorBFS
 */
@Deprecated
public class DriverGeneratorDFS extends DriverGenerator {

	/**
	 * Current sequence of WebInput used for crawling
	 */
	protected LinkedList<WebInput> sequence = null;
	/**
	 * Stack of the numbers of the nodes used for crawling to the current page
	 * (cf. sequence)
	 */
	protected LinkedList<Integer> currentNode = null;

	public DriverGeneratorDFS(String configFileName) throws JsonParseException, JsonMappingException, IOException {
		super(configFileName);
		sequence = new LinkedList<>();
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
	protected void reset() {
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
	protected List<String> filterUrl(Elements links) {
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
	 * Execute a WebInput object by sending a concrete HTTP request
	 *
	 * @param in
	 * @return
	 * @throws MalformedURLException
	 */
	protected String submit(WebInput in) throws MalformedURLException {
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

	protected int crawl(Document d, WebInput from, String content) {
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
					Logger.getLogger(DriverGeneratorDFS.class.getName()).log(Level.SEVERE, null, ex);
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

	@Override
	protected int crawlInput(WebInput in) {
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
		return updateOutput(o, from);
	}

	private int updateOutput(WebOutput out, WebInput from) {
		WebOutput equivalent = findEquivalentOutput(out);
		if (equivalent != null) {
			if (equivalent.isNewFrom(from)) {
				findParameters(equivalent);
			}
			return equivalent.getState();
		}
		outputs.add(out);
		out.setState(outputs.size() - 1);
		System.out.println("        New page !");
		findParameters(out);
		return outputs.size() - 1;
	}

	private HTTPData getRandomValuesForInput(WebInput in) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			TreeMap<String, List<String>> inputParams = in.getParams();
			for (String key : inputParams.keySet()) {
				List<String> values = inputParams.get(key);
				if (values.size() == 1) {
					data.add(key, values.get(0));
				} else {
					String newValue = null;
					if (values.size() > 1) {
						newValue = rand.randIn(values);
					} else {
						newValue = rand.randString();
					}
					data.add(key, newValue);
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
					link += name + "=" + rand.randIn(in.getParams().get(name))
							+ "&";
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
		out.addAllParams(diff);
		System.out.println("        " + out.getParamsNumber() + " output parameters");
	}

	private Set<String> findDifferences(WebOutput first, WebOutput second) {
		Set<String> diff = new HashSet<>();
		List<String> pos = new ArrayList<>();
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
}
