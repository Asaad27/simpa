package drivergenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import tools.Form;
import tools.GraphViz;
import tools.HTTPData;
import tools.HTTPRequest.Method;
import tools.Utils;
import tools.loggers.LogManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public abstract class DriverGenerator{
	protected List<String> urlsToCrawl = null;
	protected List<Form> forms = null;
	protected HashSet<String> links = null;
	protected HashMap<String, String> formValues = null;
	protected List<Object> sequence = null;
	protected WebClient client = null;
	protected HashSet<String> errors = null;
	protected ArrayList<Output> outputs;
	protected ArrayList<Transition> transitions;
	protected int currentState;
		
	protected static Config config = null;
	
	public DriverGenerator(String configFileName) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(new File("conf//" + configFileName), Config.class);
		urlsToCrawl = new ArrayList<>();
		forms = new ArrayList<Form>();
		links = new HashSet<String>();
		sequence = new ArrayList<Object>();
		errors = new HashSet<String>();
		transitions = new ArrayList<Transition>();
		outputs = new ArrayList<Output>();
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
		client.setTimeout(5000);
		BasicCredentialsProvider creds = new BasicCredentialsProvider();
		if (config.getBasicAuthUser() != null && config.getBasicAuthPass() != null){
			creds.setCredentials(new AuthScope(config.getHost(), config.getPort()), new UsernamePasswordCredentials(config.getBasicAuthUser(), config.getBasicAuthPass()));
		}
		client.setCredentialsProvider(creds);
		formValues = config.getData();
		addUrl(config.getFirstURL());
		currentState = 0;
	}
	
	public static DriverGenerator getDriver(String system){
		try {
			return (DriverGenerator)Class.forName("drivergenerator.drivers." + system + "Driver").newInstance();
		} catch (InstantiationException e) {
			LogManager.logException("Unable to instantiate " + system + " driver", e);
		} catch (IllegalAccessException e) {
			LogManager.logException("Illegal access to class " + system + " driver", e);
		} catch (ClassNotFoundException e) {
			LogManager.logException("Unable to find " + system + " driver", e);
		}
		return null;
	}
	
	public String getName(){
		return config.getName();
	}
	
	protected abstract void reset();
	
	private void sendSequences(){
		reset();
		for(Object o : sequence){
			try {
				if (o instanceof Form) submitForm((Form) o);
				else if (o instanceof String) client.getPage((String) o);
			} catch (FailingHttpStatusCodeException | IOException e) {
				LogManager.logException("Unable to execute sequence", e);
			}
		}
	}
	
	public List<String> filterUrl(Elements links) {
		List<String> urls = new ArrayList<String>();
		for (Element e : links){
			String to = e.attr("href");
			boolean add = true;
			for(String filter : config.getNoFollow()){
				if (to.toLowerCase().matches(filter)){
					add = false;
					break;
				}
			}
			if (add) urls.add(to);
		}
		return urls;
	}
	
	private boolean addForm(Form form){
		for(Form f : forms){
			if (f.equals(form) ||
					((config.getActionByParameter()!=null) && 
							(f.getInputs().get(config.getActionByParameter()).equals(form.getInputs().get(config.getActionByParameter()))) && 
							 f.getInputs().size()<form.getInputs().size())){
				return false;
			}
		}		
		for (String key : form.getInputs().keySet()){
			List<String> values = form.getInputs().get(key);
			if (values.isEmpty()){
				String providedValue = formValues.get(key); 
				if (providedValue != null) values.add(providedValue);
				else {
					values.add(Utils.randString());
					errors.add("No values for " + key + ", random string used. You may need to provide useful value.");
				}
			}
		}		
		forms.add(form);
		if (config.getActionByParameter() != null){
			for (int i=0; i<forms.size(); i++){
				if ((forms.get(i).getInputs().get(config.getActionByParameter()).equals(form.getInputs().get(config.getActionByParameter()))) && (forms.get(i).getInputs().size()>form.getInputs().size())){
					forms.remove(i);
					break;
				}
			}
		}
		return true;			
	}
	
	private HTTPData getValuesForForm(Form form){
		HTTPData data = new HTTPData();
		HashMap<String, List<String>> inputs = form.getInputs();		
		for (String key : inputs.keySet()){
			List<String> values = inputs.get(key);
			if (values.isEmpty() || values.size()>1){
				String newValue = formValues.get(key);
				if (newValue == null){
					if (values.size()>1){
						newValue = Utils.randIn(values);
					}else{
						errors.add("Multiple values for " + key + ", random string used. Please provide one value.");
						newValue = Utils.randString();
					}
				}
				data.add(key, newValue);				
			}else{
				data.add(key, values.get(0));
			}			
		}
		return data;
	}
	
	private String submitForm(Form form) throws FailingHttpStatusCodeException, IOException {
		WebRequest request = null;
		HTTPData values = getValuesForForm(form);
		request = new WebRequest(new URL(form.getAction()), (form.getMethod().equals(Method.GET)?HttpMethod.GET:HttpMethod.POST));
		request.setRequestParameters(values.getNameValueData());
		try{
			HtmlPage page = client.getPage(request); 
			return page.asXml();
		}catch(ScriptException e){
			return "";
		}
	}

	public void addUrl(String url){
		if (url != null) urlsToCrawl.add(url);
	}
	
	private void banner(){
		System.out.println("---------------------------------------------------------------------");
		System.out.println("|                           SIMPA Crawler                           |");
		System.out.println("---------------------------------------------------------------------");
		System.out.println();
	}
	
	public void start(){
		banner();
		System.out.println("[+] Crawling ...");		
		for(String url : urlsToCrawl){
			crawlLink(url);
		}
		
		System.out.println();
		System.out.println("[+] Inputs (" + forms.size() + ")");
		for(Form f : forms){
			System.out.println("    " + f);
		}
		
		System.out.println();
		System.out.println("[+] Outputs (" + outputs.size() + ")");
		
		System.out.println();
		System.out.println("[+] Model (" + transitions.size() + " transitions)");
		for(Transition t : transitions){
			System.out.println("    " + t);
		}
				
		System.out.println();
		System.out.println("[+] Comments (" + errors.size() + ")");
		Iterator<String> iter = errors.iterator();
	    while (iter.hasNext())
	        System.out.println("    " + iter.next());
	}
	
	private int crawl(Document d){
		int state = updateOutput(d);
		currentState = state;
		
		Element lesson = d.select(config.getLimitSelector()).first();
		if (lesson != null){
			Elements l = lesson.select("a[href]");
			Elements forms = lesson.select("form");
			System.out.println("        "+ l.size() + " links and " + (forms.select("input[type=submit]").size()+forms.select("input[type=image]").size()) + " forms");
			
			for(Element aform: forms){
				List<Form> formList = Form.getFormList(aform);
				for (Form f : formList){
					if (addForm(f)){
						sendSequences();
						crawlForm(f);
						sequence.remove(sequence.size()-1);
					}
				}
			}
			for(String url : filterUrl(l)){
				if (url.startsWith("/")) url = d.baseUri().substring(0, d.baseUri().indexOf("/", 7)) + url;
				else url = d.baseUri().substring(0, d.baseUri().lastIndexOf("/")+1) + url;
				if (!links.contains(url) && !isParamLink(url)){
					sendSequences();					
					crawlLink(url);
					sequence.remove(sequence.size()-1);
				}
			}
		}
		return state;
	}
	
	public void exportToDot(){
		Writer writer = null;
		File file = null;
		File dir = new File("models");
		try {			
			if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("unable to create "+ dir.getName() +" directory");

			file = new File(dir.getPath() + File.separatorChar + config.getName() + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
            writer.write("digraph G {\n");
            for (Transition t : transitions){
            	writer.write("\t" + t.toDot() + "\n");
            }
            writer.write("}\n");
            writer.close();
            File imagePath = GraphViz.dotToFile(file.getPath());
            if (imagePath!= null) LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
            LogManager.logException("Error writing dot file", e);
        }		
	}

	private boolean isParamLink(String url) {
		if (url.indexOf("?") != -1){
			Iterator<String> it = links.iterator();
			while(it.hasNext()){
				String u = (String) it.next();
				if (u.indexOf("?") != -1){
					String[] up = u.split("\\?");
					String[] urlp = url.split("\\?");
					if (up[0].equals(urlp[0])){
						String[] upa = up[1].split("&");
						Arrays.sort(upa);
						String[] urlpa = urlp[1].split("&");
						Arrays.sort(urlpa);
						if (urlpa.length == upa.length){
							for (int i=0; i<upa.length; i++){
								String[] p1 = upa[i].split("=");
								String[] p2 = urlpa[i].split("=");
								if (p1[0].equals(p2[0]) && !p1[1].equals(p2[1]) && p1[1].matches("[\\d]+") && p2[1].matches("[\\d]+")){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private int updateOutput(Document d) {
		Output o = new Output(d);
		if (o.getFilteredSource().length()>0){
			for(int i=0; i<outputs.size(); i++){
				if (o.isEquivalentTo(outputs.get(i))) return i;
			}
			outputs.add(o);
			System.out.println("        New page !");
			return outputs.size()-1;
		}
		return 0;
	}

	private void crawlLink(String link){
		links.add(link);
		sequence.add(link);
		System.out.println("    l " + link);
		
		Document doc;
		HtmlPage p;
		try {
			p = client.getPage(link);
			doc = Jsoup.parse(p.asXml());
			doc.setBaseUri(link);		
			transitions.add(new Transition(currentState, crawl(doc), prettyprint(link)));
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to get page for " + link, e);
		}
	}
	
	private void crawlForm(Form form){
		sequence.add(form);
		System.out.println("    f " + form.getAction() + ' ' + form.getInputs());

		try{
			Document doc = Jsoup.parse(submitForm(form));
			doc.setBaseUri(form.getAction());		
			transitions.add(new Transition(currentState, crawl(doc), prettyprint(form)));
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to get page for " + form, e);
		}
	}

	protected abstract String prettyprint(Object o);
}
