package drivergenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tools.Form;
import tools.HTTPClient;
import tools.HTTPData;
import tools.HTTPRequest.Method;
import tools.HTTPResponse;
import tools.Utils;

public abstract class DriverGenerator{
	protected List<String> urlsToCrawl = null;
	protected List<Form> forms = null;
	protected HashSet<String> links = null;
	protected HashMap<String, String> formValues = null;
	protected List<Object> sequence = null;
	protected HTTPClient client = null;
	protected HashSet<String> errors = null;
	protected ArrayList<String> output;
	
	protected Config config = null;
	
	public DriverGenerator(Config config){
		this.config = config;
		urlsToCrawl = new ArrayList<String>();
		forms = new ArrayList<Form>();
		links = new HashSet<String>();
		sequence = new ArrayList<Object>();
		errors = new HashSet<String>();
		output = new ArrayList<String>();
		client = new HTTPClient(config.getHost(), config.getPort());
		if (config.getBasicAuthUser() != null && config.getBasicAuthPass() != null)
			client.setCredentials(config.getBasicAuthUser(), config.getBasicAuthPass());
		formValues = config.getData();
	}
	
	public String getName(){
		return config.getName();
	}
	
	public void exportToFile(String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);		
			// NYI
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	protected abstract void reset();
	
	private void sendSequences(){
		reset();
		for(Object o : sequence){
			if (o instanceof Form) submitForm((Form) o);
			else if (o instanceof String) client.get((String) o);
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
		boolean exists = false;
		for(Form f : forms){
			if (f.equals(form)){
				exists = true;
				break;
			}
		}
		if (!exists) forms.add(form);
		return !exists;			
	}
	
	private HTTPData getValuesForForm(Form form){
		HTTPData data = new HTTPData();
		HashMap<String, List<String>> inputs = form.getInputs();		
		for (String key : inputs.keySet()){
			List<String> values = inputs.get(key);
			if (values.size()>1 || values.isEmpty()){
				String newValue = formValues.get(key);
				if (newValue == null){
					newValue = Utils.randString();
					if (values.size()>1) errors.add("Multiple values for " + key + ", random string used. Please provide one value.");
					else errors.add("No values for " + key + ", random string used. You may need to provide useful value.");
				}
				data.add(key, newValue);				
			}else{
				data.add(key, values.get(0));
			}			
		}
		return data;
	}
	
	private String submitForm(Form form) {
		HTTPData values = getValuesForForm(form);
		HTTPResponse r = null;
		if (form.getMethod().equals(Method.GET)) r = client.get(form.getAction(), values);
		else r = client.post(form.getAction(), values);
		return r.toString();
	}

	public void addUrl(String url){
		urlsToCrawl.add(url);
	}
	
	protected String limitSelector(){
		return config.getLimitSelector();
	}
	
	private void banner(){
		System.out.println("---------------------------------------------------------------------");
		System.out.println("|                      Weissmuller: SIMPA Crawler                   |");
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
		System.out.println("[+] Outputs (" + output.size() + ")");
				
		System.out.println();
		System.out.println("[+] Comments (" + errors.size() + ")");
		Iterator<String> iter = errors.iterator();
	    while (iter.hasNext())
	        System.out.println("    " + iter.next());
	}
	
	private void crawl(Document d){
		updateOutput(d);
		
		Element lesson = d.select(limitSelector()).first();
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
				if (!links.contains(url) && !isParamLink(url)){
					sendSequences();
					crawlLink(url);
					sequence.remove(sequence.size()-1);
				}
			}
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

	private void updateOutput(Document d) {
		String content = filter(d.select(limitSelector()));
		if (content.length()>0){
			if (output.isEmpty()) output.add(content);
			else{
					for(String page : output){
						double l = (double)computeLevenshteinDistance(page, content);
						double c = l / ((double)(page.length()+content.length()) /2.0);
						if (c < 0.10) { return ; }
					}
					output.add(content);
			}
			System.out.println("        New page !");
		}
	}

	private String filter(Elements selected) {
		String s = "";
		for(Element e : selected){
			s += e.tagName();
			if (e.tagName().equals("form")){
				s += e.attr("action");
			}
			if (e.tagName().equals("input")){
				s += e.attr("name");
			}
			s += filter(e.children());
		}
		return s;
	}

	private int computeLevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = Utils
						.minimum(
								distance[i - 1][j] + 1,
								distance[i][j - 1] + 1,
								distance[i - 1][j - 1]
										+ ((str1.charAt(i - 1) == str2
												.charAt(j - 1)) ? 0 : 1));

		return distance[str1.length()][str2.length()];
	}

	private void crawlLink(String link){
		links.add(link);
		sequence.add(link);
		System.out.println("    l " + link);
		
		Document doc = Jsoup.parse(client.get(link).toString());
		doc.setBaseUri(link);		
		crawl(doc);
	}
	
	private void crawlForm(Form form){
		sequence.add(form);
		System.out.println("    f " + form.getAction() + ' ' + form.getInputs());

		Document doc = Jsoup.parse(submitForm(form));
		doc.setBaseUri(form.getAction());		
		crawl(doc);
	}
}
