package crawler;


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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import tools.HTTPData;
import tools.Utils;
import tools.loggers.LogManager;

/**
 * This class implements a web site crawling using a Breadth-first search
 */
public class DriverGeneratorBFS extends DriverGenerator {

	/**
	 * Defines if the non-deterministic output parameters (i.e the values that 
	 * are different for the same sequences of inputs) found during the search 
	 * should be filtered out
	 */
	private static final boolean NDV_ANALYSIS = true;
	/**
	 * Defines the maximum depth of the exploration
	 */
	private static final int MAX_DEPTH = 5;

	public DriverGeneratorBFS(String configFileName) throws JsonParseException, JsonMappingException, IOException {
		super(configFileName);
	}

	/**
	 * Sends a sequence of inputs to the web application.
	 * @param input the WebInput containing the sequence to send
	 * @param withoutLast if true, the last input of the sequence (i.e 'input') will not be sent
	 * @return the last obtained output
	 * @throws MalformedURLException 
	 */
	protected WebOutput sendInputChain(WebInput input, boolean withoutLast) throws IOException {
		reset();
		
		//prepares the chain of input to send
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
			if (resultString == null || resultString.equals("")) {
				return null;
			}
			/* If it is possible to reset the application, then sending the same 
			sequence of input should result in the same page with the same output parameters.
			Therefore, if any parameter is different than the last time the same 
			input sequence was sent, it is a non-deterministic value and can be removed */
			if (NDV_ANALYSIS
					&& config.getReset() != null
					&& currentInput != input
					&& currentInput.getOutput() != null
					&& currentInput.getOutput().getParamsNumber() > 0) {
				WebOutput currentOutput = new WebOutput(resultString, config.getLimitSelector());
				WebOutput storedOutput = currentInput.getOutput();
				for (Iterator<String> iter = storedOutput.getParamsIterator(); iter.hasNext();) {
					String paramPath = iter.next();
					if (currentOutput.extractParam(paramPath) == null) {
						continue;
					}
					if (!currentOutput.extractParam(paramPath).equals(storedOutput.getParamValue(paramPath))
							&& !Utils.isCSRFtoken(currentOutput.extractParam(paramPath))) {
						//System.err.println("The param \"" + storedOutput.getParamValue(paramPath) + "\" has been detected as an ndv and is now removed");
						storedOutput.removeParam(paramPath, iter);
					}
				}
			}
		}
		if(withoutLast) {
			return null;
		} else {
			return new WebOutput(resultString, input, config.getLimitSelector());
		}

	}

	protected String sendInput(WebInput input) throws IOException {
		return sendInput(input, false);
	}

	/**
	 * Actually send an input to the web application
	 * @param input the WebInput to send
	 * @param randomized if true, sends random (or ramdomly picked) values for each parameter 
	 * @return the content of the output (most likely HTML)
	 * @throws MalformedURLException 
	 */
	protected String sendInput(WebInput input, boolean randomized) throws IOException {
		Page page;
		HttpMethod method = input.getMethod();

		WebRequest request = new WebRequest(new URL(input.getAddress()), method);
		switch (method) {
			case POST:
				HTTPData values = getHTTPDataFromInput(input, randomized);
				request.setRequestParameters(values.getNameValueData());
				//request.setAdditionalHeader("Connection", "Close");
				break;
			case GET:
				request.setUrl(new URL(input.getAddressWithParameters(randomized)));
				break;
			default:
				throw new UnsupportedOperationException(method + " method not supported yet.");
		}
		try {
			page = client.getPage(request);
		} catch (HttpHostConnectException e){
			LogManager.logError("Connection problem using input " + input);
			throw e;
		} catch (RuntimeException e) {
			//htmlunit throws a RuntimeException whenever it catches a MalformedURLException
			//cf com.gargoylesoftware.htmlunit.util.UrlUtils.toUrlSafe()
			throw new MalformedURLException(e.getLocalizedMessage());
		}
		requests++;
			/* TODO : Task 32
			if (page.getWebResponse().getStatusCode() != 200) {
				return null;
			}
			*/
		
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
		int depth = 0;
		boolean first = true;
		/**
		 * The inputs corresponding to the current depth
		 */
		LinkedList<WebInput> inputsToCrawl = new LinkedList<>();
		/**
		 * The inputs of the next depth
		 */
		LinkedList<WebInput> inputsToCrawlAfter = new LinkedList<>();

		inputsToCrawlAfter.add(start);

		long startingTime = System.currentTimeMillis();
		while (!inputsToCrawlAfter.isEmpty()
				&& depth < MAX_DEPTH
				&& System.currentTimeMillis() - startingTime < main.drivergen.Options.LIMIT_TIME) {
			depth++;
			inputsToCrawl.addAll(inputsToCrawlAfter);
			inputsToCrawlAfter = new LinkedList<>();

			//store new inputs or filter out not worthKeeping inputs
			for (Iterator<WebInput> iter = inputsToCrawl.iterator(); iter.hasNext();) {
				WebInput current = iter.next();
				if (!addInput(current)) {
					iter.remove();
				}
			}

			while (!inputsToCrawl.isEmpty()
					&& System.currentTimeMillis() - startingTime < main.drivergen.Options.LIMIT_TIME) {
				//TODO :
				//extract group of similar inputs (store similar inputs in a collection, returns the parameter that makes them similar)
				//for each input of the group
				//	use the input, store the output
				//	checks if the output is different from the previously encountered one (map<WebOutput, counter>)
				//  if one output has been discovered more that 3 times, keep only one input
				WebInput currentInput = inputsToCrawl.removeFirst();

				System.out.println("Current : " + currentInput);
				System.out.println("\tDepth " + depth);
				//actually crawl the input and stores the output
				WebOutput currentOutput;
				try {
					currentOutput = sendInputChain(currentInput, false);
				} catch (IOException ex) {
					inputs.remove(currentInput);
					continue;
				}
				if (currentOutput == null) {
					comments.add("The input " + currentInput + " has been filtered out. "
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

			}
		}
		if (depth > MAX_DEPTH) {
			System.err.println("Maximum depth reached, stopping crawling");
		}
		if (System.currentTimeMillis() - startingTime > main.drivergen.Options.LIMIT_TIME){
			System.err.println("Maximum time alocated for the crawling elapsed");
		}
		return 0;
	}

	/**
	 * @param d The output document to check
	 * @see updateOutput(WebOutput out, WebInput from)
	 */
	protected void updateOutput(Document d, WebInput from) {
		WebOutput o = new WebOutput(d, from, config.getLimitSelector());
		updateOutput(o, from);
	}

	/**
	 * Checks if the output page has already been seen, and if so, checks if it
	 * is the first time we access it with the given input.
	 *
	 * @param out The output to check
	 * @param from The input used to access the page
	 */
	protected void updateOutput(WebOutput out, WebInput from) {
		WebOutput equivalent = findEquivalentOutput(out);
		if (equivalent != null) {
			Map<String, String> differences = findDifferences(equivalent, out);
			equivalent.addAllParams(differences);
			
			if (equivalent.isNewFrom(from)) {
				findParameters(equivalent, from);
			}
			out.setState(equivalent.getState());
		} else {
			outputs.add(out);
			out.setState(outputs.size() - 1);
			System.out.println("        New page !");
			findParameters(out, from);
		}
	}

	/**
	 * Search and store the position of the output parameters. 
	 * Fuzz the values of an input's parameters, and compares the difference 
	 * between obtained outputs and the reference output.
	 * @param referenceOutput the output obtained the first time inputToFuzz was used
	 * @param inputToFuzz the input whose parameters's values are to be fuzzed
	 */
	private void findParameters(WebOutput referenceOutput, WebInput inputToFuzz) {
		if (inputToFuzz.getParams().isEmpty()) {
			return;
		}
		Map<String, String> diff = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			try {
				sendInputChain(inputToFuzz, true);
				String result = sendInput(inputToFuzz, true);
				WebOutput variant = new WebOutput(result, config.getLimitSelector());
				//TODO : add NDV_ANALYSIS here
				if (referenceOutput.isEquivalentTo(variant)) {
					diff.putAll(findDifferences(referenceOutput, variant));
				}
			} catch (IOException e) {
				LogManager.logException("Should not get IOException", e);
			}

		}
		referenceOutput.addAllParams(diff);
		System.out.println("        " + referenceOutput.getParamsNumber() + " output parameters");
	}

	/**
	 * Find the different values between two equivalent pages
	 * @param first the first page
	 * @param second the second page
	 * @return a structure mapping the paths (in the DOM) of the differences, 
	 * and a sample value (arbitrarily taken from 'first')
	 */
	protected Map<String, String> findDifferences(WebOutput first, WebOutput second) {
		Map<String, String> differences = new HashMap<>();
		LinkedList<String> pos = new LinkedList<>();
		Elements firstE = first.getDocWithoutExcludedNodes();
		Elements secondE = second.getDocWithoutExcludedNodes();
		if (firstE.size() == secondE.size()) {
			for (int i = 0; i < firstE.size(); i++) {
				pos.addLast(String.valueOf(i));
				findDifferencesRec(firstE.get(i), secondE.get(i), differences, pos);
				pos.removeLast();
			}
		}
		return differences;
	}

	/**
	 * Recursive version of findDifferences.
	 * /!\ Very important : the "children()" method returns every child Element 
	 * (i.e. every child which is a tag in the document), whereas the "childNodes()"
	 * method returns every child node (including TextNodes)
	 * @see findDifferences(WebOutput first, WebOutput second)
	 */
	private void findDifferencesRec(Element first, Element second, Map<String, String> diff, LinkedList<String> pos) {
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
				findDifferencesRec(first.child(i), second.child(i), diff, pos);
				pos.removeLast();
			}
		}
		pos.removeLast();
	}

	/**
	 * Find all links and forms in a given page
	 * @param wo The page to be analysed
	 * @return a list of WebInput representing forms or links
	 */
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
		forms.addAll(tree.select("form"));
		if (!forms.isEmpty()) {
			inputsList.addAll(extractInputsFromForms(forms));
		}
		
		//Filter out useless parameters
		removeUselessParameters(inputsList);
		return inputsList;
	}

	/**
	 * Convert the given links into WebInputs.
	 * Reads the provided links Elements, converts them into absolute URL,
	 * filters them using the user-provided filters if any, and creates WebInput
	 * objects.
	 *
	 * @param links The links to transform into WebInput
	 * @return The list of created WebInputs
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

	/**
	 * Convert the given forms into WebInputs.
	 * Reads the provided forms element, make combinations of user-provided and 
	 * already present parameters' values, and creates WebInputs
	 * objects.
	 *
	 * @param links The forms to transform into WebInput
	 * @return The list of created WebInputs
	 */
	private List<WebInput> extractInputsFromForms(Elements forms) {
		LinkedList<WebInput> webInputsList = new LinkedList<>();

		for (Element el : forms) {
			FormElement form = (FormElement) el;
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
			} else {
				//filter out the unwanted urls
				boolean exclude = false;
				for (String filter : config.getNoFollow()) {
					if (address.matches(filter)) {
						exclude = true;
						break;
					}
				}
				if (exclude) {
					continue;
				}
			}

			//The list formed by all the inputs created from the current form
			List<WebInput> currentFormWebInputs = new LinkedList<>();
			currentFormWebInputs.add(new WebInput(method, address, new TreeMap<String, List<String>>()));
			/**
			 * Sometimes, forms' elements are not included in his child nodes, 
			 * so this should fix the problem (https://github.com/jhy/jsoup/issues/249)
			 */
			for (Element elem : form.elements()) {
				form.appendChild(elem);
			}
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

	/**
	 * Verifies if every parameter of the given input has a value.
	 * If not, fills it with a randomly generated one.
	 * @param currentInput 
	 */
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

	private void removeUselessParameters(List<WebInput> inputsList) {
		for (WebInput wi : inputsList) {
			TreeMap<String, List<String>> params = wi.getParams();
			Set<String> keySet = params.keySet();
			for (Iterator<String> iter = keySet.iterator(); iter.hasNext();) {
				String param = iter.next();
				if (config.getUselessParameters().contains(param)) {
					iter.remove();
				}
			}
		}
	}

}
