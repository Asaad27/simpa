/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static crawler.DriverGeneratorDFS.config;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.conn.HttpHostConnectException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.HTTPData;
import tools.Utils;
import tools.loggers.LogManager;

/**
 *
 * @author maks
 */
public class DriverGeneratorBFS extends DriverGenerator {

	private static final int MAX_DEPTH = 10;

	public DriverGeneratorBFS(String configFileName) throws JsonParseException, JsonMappingException, IOException {
		super(configFileName);
	}

	protected WebOutput sendInputChain(WebInput input, boolean withoutLast) throws MalformedURLException {
		reset();

		//prepare the chain of input to send
		LinkedList<WebInput> inputChain = new LinkedList<>();
		WebInput curr = input;
		if (withoutLast) {
			curr = curr.getPrev();
		}
		while (curr != null) {
			inputChain.addFirst(curr);
			curr = curr.getPrev();
		}

		String resultString = null;
		for (WebInput currentInput : inputChain) {
			checkInputParameters(currentInput);
			resultString = sendInput(currentInput);
		}

		if (resultString == null || resultString.equals("")) {
			return null;
		} else {
			return new WebOutput(resultString, input, config.getLimitSelector());
		}

	}

	protected String sendInput(WebInput input) throws MalformedURLException {
		return sendInput(input, false);
	}

	protected String sendInput(WebInput input, boolean randomized) throws MalformedURLException {
		Page page;
		HttpMethod method = input.getMethod();

		WebRequest request = new WebRequest(new URL(input.getAddress()), method);
		switch (method) {
			case POST:
				HTTPData values = getHTTPDataFromInput(input, randomized);
				request.setRequestParameters(values.getNameValueData());
				request.setAdditionalHeader("Connection", "Close");
				break;
			case GET:
				request.setUrl(new URL(input.getAddressWithParameters(randomized)));
				break;
			default:
				throw new UnsupportedOperationException(method + " method not supported yet.");
		}
		try {
			page = client.getPage(request);
			requests++;
			/* TODO : Task 32
			if (page.getWebResponse().getStatusCode() != 200) {
				return null;
			}
			*/
		} catch (HttpHostConnectException e) {
			LogManager.logFatalError("Unable to connect to host");
			return null;
		} catch (IOException | FailingHttpStatusCodeException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (page instanceof TextPage) {
			return ((TextPage) page).getContent();
		} else if (page instanceof HtmlPage) {
			return ((HtmlPage) page).asXml();
		} else {
			return "";
		}
	}

	@Override
	protected int crawlInput(WebInput start) {
		/**
		 * The inputs corresponding to the current depth
		 */
		LinkedList<WebInput> inputsToCrawl = new LinkedList<>();
		/**
		 * The inputs of the next depth
		 */
		LinkedList<WebInput> inputsToCrawlAfter = new LinkedList<>();
		int depth = 0;
		boolean first = true;

		inputsToCrawlAfter.add(start);

		while (!inputsToCrawlAfter.isEmpty()) {
			inputsToCrawl.addAll(inputsToCrawlAfter);
			inputsToCrawlAfter = new LinkedList<>();

			//filter out not worthKeeping inputs
			for (Iterator<WebInput> iter = inputsToCrawl.iterator(); iter.hasNext();) {
				WebInput current = iter.next();
				if (!addInput(current)) {
					iter.remove();
				}
			}

			while (!inputsToCrawl.isEmpty()) {
				//TODO :
				//extract group of similar inputs (store similar input in a collection, returns the parameter that makes them similar)
				//for each input of the group
				//	use the input, store the output
				//	checks if the output is different from the previously encountered one (map<WebOutput, counter>)
				//  if one output has been discovered more that 3 times, keep only one input
				try {
					WebInput currentInput = inputsToCrawl.removeFirst();

					System.out.println("Current : " + currentInput);
					System.out.println("\tDepth " + depth);
					//actually crawl the input and stores the output
					WebOutput currentOutput = sendInputChain(currentInput, false);
					if(currentOutput == null){
						comments.add("The input " +  currentInput + " has been filtered out. "
								+ "If it's a link to a non-html page (e.g. a *.jpg), "
								+ "please consider adding a \"noFollow\" pattern "
								+ "to the JSON config file to improve crawling speed");
						inputs.remove(currentInput);
						continue;
					}
					currentInput.setOutput(currentOutput);

					//check if output is new ?
					updateOutput(currentOutput, currentInput);
					int fromState;
					fromState = first ? -1 : currentInput.getPrev().getOutput().getState();
					first = false;
					int toState = currentOutput.getState();
					transitions.add(new WebTransition(fromState, toState, currentInput));

					//find new inputs to crawl
					List<WebInput> extractedInputs = extractInputs(currentOutput);
					for (WebInput wi : extractedInputs) {
						wi.setPrev(currentInput);
					}
					inputsToCrawlAfter.addAll(extractedInputs);
				} catch (MalformedURLException ex) {
					Logger.getLogger(DriverGeneratorBFS.class.getName()).log(Level.SEVERE, null, ex);
				}

			}
			depth++;
			if (depth > MAX_DEPTH) {
				System.err.println("Maximum depth reached, stopping crawling");
				break;
			}
		}
		return 0;
	}

	/**
	 * Checks if the output page has already been seen, and if so, checks if it
	 * is the first time we access it with the given input.
	 *
	 * @param d The output page
	 * @param from The input used to access the page
	 */
	protected void updateOutput(Document d, WebInput from) {
		WebOutput o = new WebOutput(d, from, config.getLimitSelector());
		updateOutput(o, from);
	}

	protected void updateOutput(WebOutput out, WebInput from) {
		WebOutput equivalent = findEquivalentOutput(out);
		if (equivalent != null) {
			if (equivalent.isNewFrom(from)) {
				findParameters(equivalent, from);
			}
			out.setState(equivalent.getState());
			return;
		}
		outputs.add(out);
		out.setState(outputs.size() - 1);
		System.out.println("        New page !");
		findParameters(out, from);
	}

	private void findParameters(WebOutput equivalent, WebInput inputToFuzz) {
		Map<String, String> diff = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			try {
				sendInputChain(inputToFuzz, true);
				String result = sendInput(inputToFuzz, true);
				WebOutput variant = new WebOutput(result, config.getLimitSelector());
				if (equivalent.isEquivalentTo(variant)) {
					diff.putAll(findDifferences(equivalent, variant));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}
		equivalent.addAllParams(diff);
		System.out.println("        " + equivalent.getParamsNumber() + " output parameters");
	}

	protected Map<String, String> findDifferences(WebOutput first, WebOutput second) {
		Map<String, String> differences = new HashMap<>();
		LinkedList<String> pos = new LinkedList<>();
		Elements firstE = first.getDoc();
		Elements secondE = second.getDoc();
		if (firstE.size() == secondE.size()) {
			for (int i = 0; i < firstE.size(); i++) {
				pos.addLast(String.valueOf(i));
				findDifferences(firstE.get(i), secondE.get(i), differences, pos);
				pos.removeLast();
			}
		}
		return differences;
	}

	protected void findDifferences(Element first, Element second, Map<String, String> diff, LinkedList<String> pos) {
		if (!first.nodeName().equals(second.nodeName())) {
			return;
		}

		pos.addLast("/");
		if (!first.ownText().equals(second.ownText())) {
			String xpath = "";
			for (String tag : pos) {
				xpath += tag;
			}
			diff.put(xpath, first.ownText());

		}
		if (first.children().size() == second.children().size()) {
			for (int i = 0; i < first.children().size(); i++) {
				pos.addLast(String.valueOf(i));
				findDifferences(first.child(i), second.child(i), diff, pos);
				pos.removeLast();
			}
		}
		pos.removeLast();
	}

	private List<WebInput> extractInputs(WebOutput wo) {
		List<WebInput> inputsList = new LinkedList<>();
		Element tree = wo.getDoc().first();
		
		//If the CSS selector does not apply to the current page
		if (tree == null) {
			return new LinkedList<>();
		}

		//Extracts links
		Elements links = tree.select("a[href]");
		if (!links.isEmpty()) {
			inputsList.addAll(extractInputsFromLinks(links));
		}

		//Extracts forms
		Elements forms = new Elements();
		forms.addAll(findFormsIn(wo.getSource(), wo.getDoc().first().baseUri()));
		if (!forms.isEmpty()) {
			inputsList.addAll(extractInputsFromForms(forms));
		}
		return inputsList;
	}

	/**
	 * Reads the provided links Elements, converts them into absolute URL,
	 * filters them using the user-provided filters if any, and creates WebInput
	 * objects.
	 *
	 * @param links The links to transform into WebInput
	 * @param baseURI The URI where the links were found
	 * @return The list of WebInput
	 */
	private List<WebInput> extractInputsFromLinks(Elements links) {
		HashSet<String> urls = new HashSet<>();
		for (Element link : links) {
			String url = link.absUrl("href");
			if (url.equals("")) {
				continue;
			}

			//filter out the unwanted urls
			boolean add = true;
			for (String filter : config.getNoFollow()) {
				if (url.matches(filter)) {
					add = false;
					break;
				}
			}

			if (add) {
				urls.add(url);
			}
		}

		List<WebInput> webInputs = new ArrayList<>();
		for (String url : urls) {
			webInputs.add(new WebInput(url));
		}
		return webInputs;
	}

	private List<WebInput> extractInputsFromForms(Elements forms) {
		LinkedList<WebInput> webInputsList = new LinkedList<>();

		for (Element form : forms) {
			//The common method of all these inputs
			HttpMethod method;
			if (!form.attr("method").isEmpty() && form.attr("method").toLowerCase().equals("post")) {
				method = HttpMethod.POST;
			} else {
				method = HttpMethod.GET;
			}

			//The common address of all these inputs
			String address = form.absUrl("action");
			if (address.isEmpty()) {
				address = form.baseUri();
			}

			//The list formed by all the inputs created from the current form
			List<WebInput> currentFormWebInputs = new LinkedList<>();
			currentFormWebInputs.add(new WebInput(method, address, new TreeMap<String, List<String>>()));

			Elements htmlInputs = new Elements();//TODO : add other equivalent input types if any
			htmlInputs.addAll(form.select("input[type=text]"));
			htmlInputs.addAll(form.select("input[type=hidden]"));
			htmlInputs.addAll(form.select("input[type=password]"));
			htmlInputs.addAll(form.select("textarea"));
			htmlInputs.addAll(form.select("select"));

			//iterates over html input parameters
			for (Element input : htmlInputs) {
				boolean inputIsSelectType = input.attr("type").equals("select");
				String paramName = input.attr("name");

				if (paramName.isEmpty()) {
					continue;
				}
				
				//if the user provided a/some value(s) for this parameter
				if (config.getData().containsKey(paramName)) {
					ArrayList<String> providedValues = config.getData().get(paramName);

					/*  if the user provided a single value for this parameter,
					 it's added to every inputs */
					if (providedValues.size() == 1) {
						for (WebInput wi : currentFormWebInputs) {
							wi.getParams().put(paramName, Utils.createArrayList(providedValues.get(0)));
						}
						/*  if the user provided multiple values for this parameter,
						 we generate every combination of parameters by duplicating 
						 the inputs */
					} else {
						LinkedList<WebInput> inputsToAdd = new LinkedList<>();
						for (WebInput wi : currentFormWebInputs) {
							for (String value : providedValues) {
								try {
									WebInput tmp = (WebInput) wi.clone();
									tmp.getParams().put(paramName, Utils.createArrayList(value));
									inputsToAdd.add(tmp);
								} catch (CloneNotSupportedException ex) {
									throw new InternalError("This should never happen");
								}
							}
						}
						currentFormWebInputs = inputsToAdd;
					}

					/*  if no values were provided by the user, we use the value provided 
					 by the form itself (if any) */
				} else {
					if (inputIsSelectType) {
						LinkedList<String> options = new LinkedList<>();
						for (Element option : input.select("option[value]")) {
							options.add(option.attr("value"));
						}
						for (WebInput wi : currentFormWebInputs) {
							wi.getParams().put(paramName, Utils.createArrayList(options.getFirst()));//TODO : change heuristic ?
							comments.add("No values for " + paramName + " (select tag)."
									+ "First option used (" + options.getFirst() + "). You may need to provide useful value.");
						}
					} else if (input.hasAttr("value") && !input.attr("value").isEmpty()) {
						for (WebInput wi : currentFormWebInputs) {
							wi.getParams().put(paramName, Utils.createArrayList(input.attr("value")));
						}
					} else {
						for (WebInput wi : currentFormWebInputs) {
							wi.getParams().put(paramName, new ArrayList<String>());
						}
					}
				}
			}

			/* Duplicate every input for each value of submit input we found */
			Elements htmlSubmits = new Elements();
			htmlSubmits.addAll(form.select("input[type=image]"));
			htmlSubmits.addAll(form.select("input[type=submit]"));

			if (htmlSubmits.size() == 1) {
				Element input = htmlSubmits.get(0);
				String paramName = input.attr("name");
				String paramValue = input.attr("value");

				if (!paramName.isEmpty() && !paramValue.isEmpty()) {
					for (WebInput wi : currentFormWebInputs) {
						wi.getParams().put(paramName, Utils.createArrayList(paramValue));
					}
				}

			} else {
				LinkedList<WebInput> inputsToAdd = new LinkedList<>();
				for (Element input : htmlSubmits) {
					String paramName = input.attr("name");
					String paramValue = input.attr("value");

					if (!paramName.isEmpty() && !paramValue.isEmpty()) {
						for (WebInput wi : currentFormWebInputs) {
							try {
								WebInput tmp = (WebInput) wi.clone();
								tmp.getParams().put(paramName, Utils.createArrayList(paramValue));
								inputsToAdd.add(tmp);
							} catch (CloneNotSupportedException ex) {
								Logger.getLogger(DriverGeneratorBFS.class.getName()).log(Level.SEVERE, null, ex);
								System.exit(1); //This should never happen
							}
						}
					}
				}
				currentFormWebInputs = inputsToAdd;
			}

			webInputsList.addAll(currentFormWebInputs);
		}
		return webInputsList;
	}

	private void checkInputParameters(WebInput currentInput) {
		TreeMap<String, List<String>> params = currentInput.getParams();
		for (String key : params.keySet()) {
			List<String> values = params.get(key);
			if (values.isEmpty()) {
				values.add(Utils.randString());
				comments.add("checkInputParameters() : No values for " + key + ", "
						+ "random string used. You may need to provide useful value.");

			}
		}
	}

}
