package drivergenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.select.Elements;

import tools.CookieManager;
import tools.Form;
import tools.Utils;
import drivers.efsm.real.LowWebDriver;


public abstract class DriverGenerator extends LowWebDriver{
	protected List<String> urlsToCrawl = null;
	protected List<Form> forms = null;
	protected List<HashMap<String, String>> formValues = null;
	protected List<Form> sequence = null;
	
	protected int systemPort = 1337;
	protected String systemHost = null;
	protected CookieManager cookie;
	
	public DriverGenerator(){
		urlsToCrawl = new ArrayList<String>();
		forms = new ArrayList<Form>();
		formValues = buildFormValues();
		sequence = new ArrayList<Form>();
		
		this.systemHost  = "localhost";
		this.systemPort = 8080;
		cookie = new CookieManager();
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
	
	protected void sendSequences(){
		reset();
		for(Form form : sequence){
			submitForm(form);
		}
	}
	
	protected abstract String submitForm(Form form);
	
	protected boolean addForm(Form form){
		boolean exists = false;
		for(Form f : forms){
			if (f.equals(form)){
				exists = true;
				break;
			}
		}
		for(Form f : forms){
			if (f.getSubmit().get("action").equals(form.getSubmit().get("action"))){
				if (form.getInputs().size()<f.getInputs().size()){
					forms.remove(f);
					forms.add(form);			
				}
				exists = true;
				break;
			}
		}
		
		if (!exists) forms.add(form);
		return !exists;			
	}
	
	protected HashMap<String, String> getValuesForForm(Form form){
		HashMap<String, String> ret = null;
		
		for(int i=0; i<formValues.size(); i++){
			boolean fit = true;
			if (!formValues.get(i).keySet().containsAll(form.getInputs().keySet())) fit = false;
			for(String s : form.getSubmit().keySet()){
				if (!formValues.get(i).containsKey(s) || !formValues.get(i).get(s).equals(form.getSubmit().get(s))) fit = false;
			}
			if (fit) ret = formValues.get(i);
		}
		return ret;
	}
	
	public abstract List<HashMap<String, String>> buildFormValues();

	protected void addUrl(String url){
		urlsToCrawl.add(url);
	}
	
	protected abstract List<String> filterUrl(Elements links);
	
	public void start(){
		System.out.println("Crawling ...");		
		for(String url : urlsToCrawl){
			crawlLink(url);
		}
		for(Form f : forms){
			for(String action : f.getSubmit().keySet()) f.getSubmit().put(action, Utils.decapitalize(f.getSubmit().get(action)));
			System.out.println(f);
		}
	}

	protected abstract void crawlLink(String link);
	protected abstract void crawlForm(Form form);
}
