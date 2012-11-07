package drivergenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
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
import tools.HTTPResponse;
import tools.Utils;
import tools.HTTPRequest.Method;

public abstract class DriverGenerator{
	protected List<String> urlsToCrawl = null;
	protected List<Form> forms = null;
	protected HashMap<String, String> formValues = null;
	protected List<Object> sequence = null;
	protected HTTPClient client = null;
	protected HashSet<String> errors = null;
	protected ArrayList<String> output;
	
	public DriverGenerator(){
		urlsToCrawl = new ArrayList<String>();
		forms = new ArrayList<Form>();
		formValues = buildFormValues();
		sequence = new ArrayList<Object>();
		errors = new HashSet<String>();
		output = new ArrayList<String>();
	}
	
	public void exportToFile(String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			
			
			
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	protected abstract void reset();
	
	protected void sendSequences(){
		reset();
		for(Object o : sequence){
			if (o instanceof Form) submitForm((Form) o);
			else if (o instanceof String) client.get((String) o);
		}
	}
	
	protected boolean addForm(Form form){
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
	
	protected HTTPData getValuesForForm(Form form){
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
	
	public String submitForm(Form form) {
		HTTPData values = getValuesForForm(form);
		HTTPResponse r = null;
		if (form.getMethod().equals(Method.GET)) r = client.get(form.getAction(), values);
		else r = client.post(form.getAction(), values);
		return r.toString();
	}
	
	public abstract HashMap<String, String> buildFormValues();

	protected void addUrl(String url){
		urlsToCrawl.add(url);
	}
	
	protected abstract List<String> filterUrl(Elements links);
	
	protected abstract String limitToThisSelector();
	
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
		for(int i=0; i<output.size(); i++){
			Utils.saveToFile(output.get(i), i + ".txt");
		}
		
		System.out.println();
		System.out.println("[+] Comments (" + errors.size() + ")");
		Iterator<String> iter = errors.iterator();
	    while (iter.hasNext())
	        System.out.println("    " + iter.next());
	}
	
	private void crawl(Document d){
		updateOutput(d);
		
		Element lesson = d.select(limitToThisSelector()).first();
		if (lesson != null){
			Elements links = lesson.select("a[href]");
			Elements forms = lesson.select("form");
			System.out.println(" ("+ links.size() + " links and " + forms.select("input[type=submit]").size() + " forms)");
			
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
			for(String url : filterUrl(links)) crawlLink(url);
		}
	}

	private void updateOutput(Document d) {
		String content = d.select(limitToThisSelector()).html();
		if (output.isEmpty()) output.add(content);
		else{
			for(String page : output){
				int l = computeLevenshteinDistance(page, content);
				if (l < 50) { return ; }
			}
			output.add(content);
		}
	}

	public static int computeLevenshteinDistance(CharSequence str1,
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

	protected void crawlLink(String link){
		sequence.add(link);		
		System.out.print("    l " + link);
		
		Document doc = Jsoup.parse(client.get(link).toString());
		doc.setBaseUri(link);		
		crawl(doc);
	}
	
	protected void crawlForm(Form form){
		sequence.add(form);
		System.out.print("    f " + form.getAction() + ' ' + form.getInputs());

		Document doc = Jsoup.parse(submitForm(form));
		doc.setBaseUri(form.getAction());		
		crawl(doc);
	}
}
