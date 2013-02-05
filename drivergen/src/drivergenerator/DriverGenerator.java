package drivergenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.html.HTML.Tag;
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

import drivergenerator.Input.Type;

public abstract class DriverGenerator {
	protected List<String> urlsToCrawl = null;
	protected List<Input> inputs = null;
	protected HashMap<String, String> formValues = null;
	protected List<Input> sequence = null;
	protected WebClient client = null;
	protected HashSet<String> errors = null;
	protected ArrayList<Output> outputs;
	protected ArrayList<Transition> transitions;
	protected int currentState;

	protected static Config config = null;

	public DriverGenerator(String configFileName) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(new File("conf//" + configFileName),
				Config.class);
		urlsToCrawl = new ArrayList<>();
		inputs = new ArrayList<Input>();
		sequence = new ArrayList<Input>();
		errors = new HashSet<String>();
		transitions = new ArrayList<Transition>();
		outputs = new ArrayList<Output>();
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
		client.setTimeout(2000);
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
		currentState = 0;
	}

	public static DriverGenerator getDriver(String system) {
		try {
			return (DriverGenerator) Class.forName(
					"drivergenerator.systems." + system)
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

	public List<String> filterUrl(Elements links) {
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
			if (i.isAlmostEquals(in)) return false;
		}
		
		for (String key : in.getParams().keySet()) {
			List<String> values = in.getParams().get(key);
			if (values.isEmpty()) {
				String providedValue = formValues.get(key);
				if (providedValue != null)
					values.add(providedValue);
				else {
					values.add(Utils.randString());
					errors.add("No values for "
							+ key
							+ ", random string used. You may need to provide useful value.");
				}
			}
		}
		inputs.add(in);
		if ((config.getActionByParameter() != null) && (in.getParams().get(config.getActionByParameter()) != null)) {
			for (int i = 0; i < inputs.size()-1; i++) {
				if ((inputs.get(i).getParams().get(config.getActionByParameter()) != null) && (inputs.get(i).getParams().get(config.getActionByParameter()).equals(in.getParams().get(config.getActionByParameter())))
						&& ((inputs.get(i).getParams().size() > in.getParams().size())))
				{
					Input removed = inputs.remove(i);
					for (int t =transitions.size()-1; t>=0; t--){
						if (transitions.get(t).getBy()==removed) transitions.remove(t);
					}
				}
			}
		}
		for (int i = 0; i < inputs.size()-1; i++) {
			if ((inputs.get(i).getAddress().equals(in.getAddress())) && (inputs.get(i).getParams().isEmpty() && in.getParams().size()>0))
			{
				Input removed = inputs.remove(i);
				for (int t =transitions.size()-1; t>=0; t--){
					if (transitions.get(t).getBy()==removed) transitions.remove(t);
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
							errors.add("Multiple values for "
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
	
	public void log(String s){
		System.out.println(s);	
	}
	
	private String submit(Input in) throws MalformedURLException{
		WebRequest request = null;
		HTTPData values = getValuesForInput(in);
		if (in.getType()==Type.FORM){
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());
			HtmlPage page;
			try {
				page = client.getPage(request);
			} catch (Exception e) {
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
			} catch (Exception e) {
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
			if (addInput(in)) crawlInput(in);
		}
		errors.add("Duration : " + ((System.nanoTime()-duration)/1000000000.00) + " secs");

		System.out.println();
		System.out.println("[+] Inputs (" + inputs.size() + ")");
		for (Input in : inputs) {
			if (!config.getRuntimeParameters().isEmpty()) in.cleanRuntimeParameters(config.getRuntimeParameters()); 
			System.out.println("    " + in);
		}

		System.out.println();
		System.out.println("[+] Outputs (" + outputs.size() + ")");

		System.out.println();
		System.out
				.println("[+] Model (" + transitions.size() + " transitions)");
		for (Transition t : transitions) {
			System.out.println("    " + t);
		}

		System.out.println();
		System.out.println("[+] Comments (" + errors.size() + ")");
		Iterator<String> iter = errors.iterator();
		while (iter.hasNext())
			System.out.println("    " + iter.next());
	}

	private int crawl(Document d, Input from, String content) {
		int state = updateOutput(d);
		currentState = state;
		from.setOutput(currentState);
		Element lesson = null;
		if (!config.getLimitSelector().isEmpty()) d.select(config.getLimitSelector()).first();  
		else lesson = d.getAllElements().first();
		if (lesson != null) {
			Elements l = lesson.select("a[href]");
			Elements forms = lesson.select("form");
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
						crawlInput(in);
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
					crawlInput(in);
					sequence.remove(sequence.size() - 1);
				}
			}
		}
		return state;
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

	public void exportToDot() {
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

	private int updateOutput(Document d) {
		Output o = new Output(d);
		if (o.getFilteredSource().length() > 0) {
			for (int i = 0; i < outputs.size(); i++) {
				if (o.isEquivalentTo(outputs.get(i))) {
					return i;
				}
			}
			outputs.add(o);
			System.out.println("        New page !");
			findParameters(sequence, o);
			return outputs.size() - 1;
		}
		return 0;
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
				return null;
			}
			return page.asXml();
		}
		return null;
	}
	
	public void findParameters(List<Input> sequence, Output out) {
		HashSet<String> diff = new HashSet<String>();
		Input inputToFuzz = sequence.remove(sequence.size()-1);
		for (int i=0; i<5; i++){
			try {
				sendSequences();
				Output variant = new Output(submitRandom(inputToFuzz));
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
			pos.add(first.nodeName());
			if (!first.ownText().equals(second.ownText())){
				diff.add(pos.toString());
			}
			for(int i=0; i<first.children().size(); i++){
				pos.add(String.valueOf(i));
				findDifferences(first.child(i), second.child(i), diff, pos);
				pos.remove(pos.size()-1);
			}
			pos.remove(pos.size()-1);
		}
	}

	private void crawlInput(Input in) {
		sequence.add(in);
		System.out.println("    " + (in.getType() == Type.FORM ? "f" : "l") + " " + in.getAddress() + ' ' + in.getParams());

		Document doc = null;
		try {
			String content = submit(in);
			if (content != null){
				doc = Jsoup.parse(content);
				doc.setBaseUri(in.getAddress());
				transitions.add(new Transition(currentState, crawl(doc, in, content), in));
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to get page for " + in, e);
		}
	}

	protected abstract String prettyprint(Input in);

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
            	org.w3c.dom.Element ediff = doc.createElement("diff");
            	ediff.setTextContent(o.getFilteredSource());
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
            StreamResult xml = new StreamResult(new File("abs//" + config.getName()+ ".xml"));
            transformer.transform(source, xml); 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
