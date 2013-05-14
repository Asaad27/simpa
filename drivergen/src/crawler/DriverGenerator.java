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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import main.Main;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crawler.Input.Type;
import crawler.configuration.Configuration;


public abstract class DriverGenerator {
	protected List<String> urlsToCrawl = null;
	protected List<Input> inputs = null;
	protected HashMap<String, String> formValues = null;
	protected List<Input> sequence = null;
	protected WebClient client = null;
	protected HashSet<String> comments = null;
	protected ArrayList<Output> outputs = null;
	protected ArrayList<Transition> transitions = null;
	protected List<Integer> currentNode = null;
	protected List<String> colors = null;
	protected int requests = 0;

	protected static Configuration config = null;

	public DriverGenerator(String configFileName) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(new File("conf" + File.separator + configFileName),
				Configuration.class);
		urlsToCrawl = new ArrayList<>();
		inputs = new ArrayList<Input>();
		sequence = new ArrayList<Input>();
		comments = new HashSet<String>();
		transitions = new ArrayList<Transition>();
		outputs = new ArrayList<Output>();
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
		client.setTimeout(config.getTimeout());
		client.setCssEnabled(config.isEnableCSS());
		client.setJavaScriptEnabled(config.isEnableJS());
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
		addUrl(config.getFirstURL());
		currentNode = new ArrayList<Integer>();
		currentNode.add(0);
	}

	public static DriverGenerator getDriver(String system) {
		try {
			return (DriverGenerator) Class.forName(
					"crawler.init." + system)
					.newInstance();
		} catch (InstantiationException e) {
			LogManager.logException("Unable to instantiate " + system
					+ " driver", e);
		} catch (IllegalAccessException e) {
			LogManager.logException("Illegal access to class " + system
					+ " driver", e);
		} catch (ClassNotFoundException e) {
			LogManager.logException("Unable to find " + system + " driver", e);
		}
		return null;
	}

	public String getName() {
		return config.getName();
	}

	protected abstract void reset();
	
	protected abstract void initConnection();
	
	protected abstract String prettyprint(Input in);

	private void sendSequences() {
		reset();
		for (Input in : sequence) {
			try {
				submit(in);
			} catch (FailingHttpStatusCodeException | IOException e) {
				LogManager.logException("Unable to execute sequence", e);
			}
		}
	}

	private List<String> filterUrl(Elements links) {
		List<String> urls = new ArrayList<String>();
		for (Element e : links) {
			String to = e.attr("href");
			boolean add = true;
			for (String filter : config.getNoFollow()) {
				if (to.toLowerCase().matches(filter)) {
					add = false;
					break;
				}
			}
			if (add)
				urls.add(to);
		}
		return urls;
	}

	private boolean addInput(Input in) {
		for (Input i : inputs) {
			if (i.equals(in)
					|| ((config.getActionByParameter() != null) && (in.getParams().get(config.getActionByParameter()) != null) && (i.getParams().get(config.getActionByParameter()) != null)
							&& (i.getParams()
									.get(config.getActionByParameter())
									.equals(in.getParams().get(
											config.getActionByParameter()))) && i
							.getParams().size() < in.getParams().size())) {
				return false;
			}
			if ((i.getAddress().equals(in.getAddress())) && (!i.getParams().isEmpty() && in.getParams().isEmpty())) return false;
		}
		
		for (Input i : inputs) {
			if ((config.getActionByParameter() != null) &&
			    (in.getParams().get(config.getActionByParameter()) != null) &&
			    (i.getParams().get(config.getActionByParameter()) != null)  &&
			    (i.getParams().get(config.getActionByParameter()).equals(in.getParams().get(config.getActionByParameter()))) && i.isAlmostEquals(in)) return false;
		}
		
		for (Input i : inputs) {
			if (i.getAddress().equals(in.getAddress())){
				int diff = 0;
				for(String paramName : i.getParams().keySet()){
					if (in.getParams().get(paramName) != null && i.getParams().get(paramName) != null){
						if (in.getParams().get(paramName).size() == 1 && i.getParams().get(paramName).size() == 1){
							if (Utils.isNumeric(in.getParams().get(paramName).get(0)) && Utils.isNumeric(i.getParams().get(paramName).get(0))){
								if (!in.getParams().get(paramName).get(0).equals(i.getParams().get(paramName).get(0))) diff++;
							}
						}
					}
				}
				if (diff >= 1) return false;	
			}
		}
		
		for (String key : in.getParams().keySet()) {
			List<String> values = in.getParams().get(key);
			if (values.isEmpty()) {
				String providedValue = formValues.get(key);
				if (providedValue != null)
					values.add(providedValue);
				else {
					values.add(Utils.randString());
					comments.add("No values for "
							+ key
							+ ", random string used. You may need to provide useful value.");
				}
			}
		}
		inputs.add(in);
		if (config.getActionByParameter() != null) {
			for (int i = 0; i < inputs.size()-1; i++) {
				if ((inputs.get(i).getAddress().equals(in.getAddress())) &&
					(in.getParams().get(config.getActionByParameter()) != null) &&
					(inputs.get(i).getParams().get(config.getActionByParameter()) != null) &&
					(inputs.get(i).getParams().get(config.getActionByParameter()).equals(in.getParams().get(config.getActionByParameter()))) &&
					((inputs.get(i).getParams().size() > in.getParams().size())))
				{
					Input removed = inputs.remove(i);
					for (int t =transitions.size()-1; t>=0; t--){
						if (transitions.get(t).getBy()==removed){
							transitions.get(t).setBy(in);
						}
					}
				}
			}
		}
		for (int i = 0; i < inputs.size()-1; i++) {
			if ((inputs.get(i).getAddress().equals(in.getAddress())) && (inputs.get(i).getParams().isEmpty() && in.getParams().size()>0))
			{
				Input removed = inputs.remove(i);
				for (int t =transitions.size()-1; t>=0; t--){
					if (transitions.get(t).getBy()==removed){
						transitions.get(t).setBy(in);
					}
				}
			}
		}
		return true;
	}

	private HTTPData getValuesForInput(Input in) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			HashMap<String, List<String>> inputs = in.getParams();
			for (String key : inputs.keySet()) {
				List<String> values = inputs.get(key);
				if (values.isEmpty() || values.size() > 1) {
					String newValue = formValues.get(key);
					if (newValue == null) {
						if (values.size() > 1) {
							newValue = Utils.randIn(values);
						} else {
							comments.add("Multiple values for "
									+ key
									+ ", random string used. Please provide one value.");
							newValue = Utils.randString();
						}
					}
					data.add(key, newValue);
				} else {
					data.add(key, values.get(0));
				}
			}
		}
		return data;
	}
	
	private String submit(Input in) throws MalformedURLException{
		WebRequest request = null;
		HTTPData values = getValuesForInput(in);
		if (in.getType()==Type.FORM){
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());
			request.setAdditionalHeader("Connection", "Close");
			HtmlPage page;
			try {
				page = client.getPage(request);
				if (page.getWebResponse().getStatusCode() != 200) return null;
				requests++;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}else if (in.getType()==Type.LINK){		
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()){
				for(String name : in.getParams().keySet()){
					for(String value : in.getParams().get(name)){
						link += name + "=" + value + "&";
					}
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length()-1));
				if (page.getWebResponse().getStatusCode() != 200) return null;
				requests++;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}

	public void addUrl(String url) {
		if (url != null)
			urlsToCrawl.add(url);
	}

	private void banner() {
		System.out
				.println("---------------------------------------------------------------------");
		System.out
				.println("|                    "+Main.NAME+"                    |");
		System.out
				.println("---------------------------------------------------------------------");
		System.out.println();
	}

	public void start() {
		banner();
		System.out.println("[+] Crawling ...");
		long duration = System.nanoTime();
		for (String url : urlsToCrawl) {
			Input in = new Input("http://" + config.getHost() + ":" + config.getPort() + url);
			crawlInput(in);
		}

		System.out.println();
		System.out.println("[+] Merging inputs");
		//mergeInputs();
		
		System.out.println();
		System.out.println("[+] Inputs (" + inputs.size() + ")");
		for (Input in : inputs) {
			if (!config.getRuntimeParameters().isEmpty()) in.cleanRuntimeParameters(config.getRuntimeParameters()); 
			System.out.println("    " + in);
		}

		System.out.println();
		System.out.println("[+] Outputs (" + outputs.size() + ")");

		System.out.println();
		System.out.println("[+] Stats");
		System.out.println("    Duration : " + ((System.nanoTime()-duration)/1000000000.00) + " secs");
		System.out.println("    Requests : " + requests);
		
		System.out.println();
		System.out
				.println("[+] Model (" + transitions.size() + " transitions)");
		for (Transition t : transitions) {
			System.out.println("    " + t);
		}
		
	
		System.out.println();
		System.out.println("[+] Comments (" + comments.size() + ")");
		Iterator<String> iter = comments.iterator();
		while (iter.hasNext())
			System.out.println("    " + iter.next());
		System.out.flush();
		
	}

	private void mergeInputs() {
		List<Input> mergedInput = new ArrayList<Input>();
		Input discintIn, anInput;
		List<String> paramValues;
		while (inputs.size()>0){
			discintIn  = inputs.get(0);
			for(int j=inputs.size()-1; j>=0; j--){
				anInput = inputs.get(j);
				if (discintIn.getMethod().equals(anInput.getMethod()) && discintIn.getAddress().equals(anInput.getAddress())){
					for (String param : anInput.getParams().keySet()){
						paramValues = anInput.getParams().get(param);
						if (discintIn.getParams().get(param) == null) discintIn.getParams().put(param, paramValues);
						else{							
							for(String paramValue : paramValues){
								if (!discintIn.getParams().get(param).contains(paramValue)) discintIn.getParams().get(param).add(paramValue); 
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
		colors = new ArrayList<String>();
		for(int i=0; i<outputs.size(); i++){
			hue = ((0.8f/outputs.size())*i)+0.1f;
			Color c = Color.getHSBColor(hue, saturation, luminance);
			colors.add(Integer.toHexString(c.getRed())+Integer.toHexString(c.getGreen())+Integer.toHexString(c.getBlue()));
		}		
		Collections.shuffle(colors);
	}

	private int crawl(Document d, Input from, String content) {
		int node = updateOutput(d, from);
		currentNode.add(node);
		Element lesson = null;
		if (!config.getLimitSelector().isEmpty()) lesson = d.select(config.getLimitSelector()).first();  
		else lesson = d.getAllElements().first();
		if (lesson != null) {
			Elements l = lesson.select("a[href]");
			Elements forms = new Elements();
			forms.addAll(findFormsIn(content, d.baseUri())); // https://github.com/jhy/jsoup/issues/249
			System.out.println("        "
					+ l.size()
					+ " links and "
					+ (forms.select("input[type=submit]").size() + forms
							.select("input[type=image]").size()) + " forms");

			for (Element aform : forms) {
				List<Input> formList = Input.extractInputsFromForm(aform);
				for (Input in : formList) {
					if (addInput(in)) {
						sendSequences();
						int next = crawlInput(in);
						if (next == -1) inputs.remove(in);
						sequence.remove(sequence.size() - 1);
					}
				}
			}
			for (String url : filterUrl(l)) {
				if (url.startsWith("/"))
					url = d.baseUri().substring(0, d.baseUri().indexOf("/", 7))
							+ url;
				else
					url = d.baseUri().substring(0,
							d.baseUri().lastIndexOf("/") + 1)
							+ url;
				Input in = new Input(url);
				if (addInput(in)) {
					sendSequences();
					int next = crawlInput(in);
					if (next == -1) inputs.remove(in);
					sequence.remove(sequence.size() - 1);
				}
			}
		}
		currentNode.remove(currentNode.size()-1);
		return node;
	}

	private Collection<Element> findFormsIn(String content, String baseUri) {
		List<Element> el = new ArrayList<Element>();
		int nextForm = content.indexOf("<form ");
		while (nextForm != -1){
			Document d = Jsoup.parseBodyFragment(content.substring(nextForm, content.indexOf("</form", nextForm)));
			d.setBaseUri(baseUri);
			el.addAll(d.select("form"));
			nextForm = content.indexOf("<form ", nextForm+1);
		}
		nextForm = content.indexOf("<FORM ");
		while (nextForm != -1){
			Document d = Jsoup.parseBodyFragment(content.substring(nextForm, content.indexOf("</FORM", nextForm)));
			d.setBaseUri(baseUri);
			el.addAll(d.select("FORM"));
			nextForm = content.indexOf("<FORM ", nextForm+1);
		}
		return el;
	}
	
	private void exportToDotCreateNodes(Writer w) throws IOException{
		for (int i=0; i<outputs.size(); i++){        	
			w.write("    " + i + " [style=\"filled\", fillcolor=\"#"+ colors.get(outputs.get(i).getState()) + "\"]\n");
		}
	}

	public void exportToDot() {
		initColor();
		Writer writer = null;
		File file = null;
		File dir = new File("models");
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
						+ " directory");

			file = new File(dir.getPath() + File.separatorChar
					+ config.getName() + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			exportToDotCreateNodes(writer);
			for (Transition t : transitions) {
				writer.write("\t" + t.getFrom() + " -> " + t.getTo() + " [label=\"" + prettyprint(t.getBy()) + "\"]" + "\n");
			}
			writer.write("}\n");
			writer.close();
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	private int updateOutput(Document d, Input from) {
		Output o = new Output(d, from);
		if (d.toString().isEmpty()) return 0;
		else{
			for (int i = 0; i < outputs.size(); i++) {
				if (o.isEquivalentTo(outputs.get(i))) {
					if (outputs.get(i).isNewFrom(from)) findParameters(sequence, outputs.get(i));
					return i;
				}
			}
		}
		outputs.add(o);
		System.out.println("        New page !");
		findParameters(sequence, o);
		return outputs.size() - 1;
	}
	
	private HTTPData getRandomValuesForInput(Input in) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			HashMap<String, List<String>> inputs = in.getParams();
			for (String key : inputs.keySet()) {
				List<String> values = inputs.get(key);
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
	
	private String submitRandom(Input in) throws MalformedURLException{
		WebRequest request = null;
		HTTPData values = getRandomValuesForInput(in);
		if (in.getType()==Type.FORM){
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
		}else if (in.getType()==Type.LINK){
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()){
				for(String name : in.getParams().keySet()){
					link += name + "=" + Utils.randIn(in.getParams().get(name)) + "&";
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length()-1));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}
	
	private void findParameters(List<Input> sequence, Output out) {
		HashSet<String> diff = new HashSet<String>();
		Input inputToFuzz = sequence.remove(sequence.size()-1);
		for (int i=0; i<5; i++){
			try {
				sendSequences();
				Output variant = new Output(submitRandom(inputToFuzz), false);
				if (out.isEquivalentTo(variant)){
					diff.addAll(findDifferences(out, variant));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sequence.add(inputToFuzz);
		out.getParams().addAll(diff);
		System.out.println("        " + out.getParams().size() + " output parameters");
	}

	private List<String> findDifferences(Output first, Output second) {
		List<String> diff = new ArrayList<String>();
		List<String> pos = new ArrayList<String>();
		Elements firstE = first.getDoc();
		Elements secondE = second.getDoc();
		if (firstE.size() == secondE.size()){
			for(int i=0; i<firstE.size(); i++){
				pos.add(String.valueOf(i));
				findDifferences(firstE.get(i), secondE.get(i), diff, pos);
				pos.remove(pos.size()-1);
			}		
		}
		return diff;
	}
	
	private void findDifferences(Element first, Element second, List<String> diff, List<String> pos) {
		if (first.nodeName().equals(second.nodeName())){
			pos.add("/");
			if (!first.ownText().equals(second.ownText())){
				String xpath = "";
				for(String tag : pos) xpath += tag;
				diff.add(xpath);
			}
			if (first.children().size() == second.children().size()){
				for(int i=0; i<first.children().size(); i++){
					pos.add(String.valueOf(i));
					findDifferences(first.child(i), second.child(i), diff, pos);
					pos.remove(pos.size()-1);
				}
			}
			pos.remove(pos.size()-1);
		}
	}

	private int crawlInput(Input in) {
		sequence.add(in);
		System.out.println("    " + (in.getType() == Type.FORM ? "f" : "l") + " " + in.getAddress() + ' ' + in.getParams());

		Document doc = null;
		try {
			String content = submit(in);
			if (content != null){
				doc = Jsoup.parse(content);
				doc.setBaseUri(in.getAddress());
				int output = crawl(doc, in, content);
				transitions.add(new Transition(currentNode.get(currentNode.size()-1), output, in));
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
            edriver.setAttribute("version", Main.VERSION);
            edriver.setAttribute("generator", Main.NAME);
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
            n = doc.createElement("limitSelector");
            n.setTextContent(config.getLimitSelector());
            esettings.appendChild(n);
            n = doc.createElement("basicAuthUser");
            n.setTextContent(config.getBasicAuthUser());
            esettings.appendChild(n);
            n = doc.createElement("basicAuthPass");
            n.setTextContent(config.getBasicAuthPass());
            esettings.appendChild(n);
            n = doc.createElement("runtimeParameters");
            for(String rt : config.getRuntimeParameters()){
            	org.w3c.dom.Element eparams = doc.createElement("parameter");
            	eparams.setTextContent(rt);
            	n.appendChild(eparams);
            }            
            esettings.appendChild(n);
            edriver.appendChild(esettings);            
            org.w3c.dom.Element einputs = doc.createElement("inputs");
            for (Input i : inputs){
            	org.w3c.dom.Element einput = doc.createElement("input");
            	einput.setAttribute("type", String.valueOf(i.getType()));
            	einput.setAttribute("method", String.valueOf(i.getMethod()));
            	einput.setAttribute("address", i.getAddress());
            	org.w3c.dom.Element eparams = doc.createElement("parameters");
            	for(String name : i.getParams().keySet()){
            		for (String value : i.getParams().get(name)){
            			org.w3c.dom.Element eparam = doc.createElement("parameter");
            			eparam.setAttribute("name", name);
            			eparam.setTextContent(value);
            			eparams.appendChild(eparam);
            		}
            	}
            	einput.appendChild(eparams);
            	einputs.appendChild(einput);
            }            
            edriver.appendChild(einputs);            
            org.w3c.dom.Element eoutputs = doc.createElement("outputs");
            for (Output o : outputs){
            	org.w3c.dom.Element eoutput = doc.createElement("output");
            	org.w3c.dom.Element ediff = doc.createElement("source");
            	ediff.setTextContent(o.getSource());
            	eoutput.appendChild(ediff);
            	org.w3c.dom.Element eparams = doc.createElement("parameters");
            	for (String value : o.getParams()){
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
            StreamResult xml = new StreamResult(new File("abs" + File.separatorChar + config.getName()+ ".xml").toURI().getPath());
            transformer.transform(source, xml); 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
