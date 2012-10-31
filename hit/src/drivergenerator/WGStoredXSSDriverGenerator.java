package drivergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tools.Form;
import tools.HTTPRequest;
import tools.HTTPRequest.Method;
import tools.HTTPRequest.Version;
import tools.HTTPResponse;
import tools.TCPSend;
import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public class WGStoredXSSDriverGenerator extends DriverGenerator{
	
	private String basicAuth = "Basic Z3Vlc3Q6Z3Vlc3Q="; // guest:guest in base64
	private String screen = null;
	
	public WGStoredXSSDriverGenerator(){
		super();
		initConnection();
		this.systemHost = "localhost";
		this.systemPort = 8080;
		addUrl("/WebGoat/attack?Screen="+screen+"&menu=900");
	}
	
	private String extractScreen(String content, String lesson) {
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}
	
	@Override
	public void reset() {
		HTTPRequest res = new HTTPRequest(Method.GET, "/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen, Version.v11);
		res.addHeader("Authorization", basicAuth);
		res.addHeader("Cookie", cookie.getCookieLine());
		TCPSend.Send(systemHost, systemPort, res);
	}
	
	public ArrayList<String> getOutputSymbols(){
		ArrayList<String> os = new ArrayList<String>();
		os.add("listing");
		os.add("home");
		os.add("profilePage");
		os.add("editionPage");
		return os;
	}
	
	public String getSystemName() {
		return "WebGoat Stored XSS";
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");
		
		HTTPRequest req = new HTTPRequest(Method.GET, "/WebGoat/attack", Version.v11);
		req.addHeader("Authorization", basicAuth);
		HTTPResponse resp = new HTTPResponse(TCPSend.Send(systemHost, systemPort, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		
		req = new HTTPRequest(Method.POST, "/WebGoat/attack", Version.v11);
		req.addHeader("Authorization", basicAuth);
		req.addHeader("Cookie", cookie.getCookieLine());
		req.addData("start", "Start WebGoat");
		resp = new HTTPResponse(TCPSend.Send(systemHost, systemPort, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		
		screen = extractScreen(resp.toString(), "Stage 1: Stored XSS");
	}
	
	@Override
	public List<String> filterUrl(Elements links) {
		List<String> urls = new ArrayList<String>();
		for (Element e : links){
			String to = e.attr("href");
			if (to.toLowerCase().startsWith("javascript:")) continue;
			if (to.toLowerCase().startsWith("http://")) continue;
			urls.add(to);
		}
		return urls;
	}
	
	public void crawlLink(String link){
		Document doc = null;
		HTTPRequest req = null;
		System.out.print("    l " + link);
			
		req = new HTTPRequest(link);
		req.addHeader("Authorization", basicAuth);
		if (!cookie.isEmpty()) req.addHeader("Cookie", cookie.getCookieLine());
		
		doc = Jsoup.parse(TCPSend.Send(systemHost, systemPort, req));
		doc.setBaseUri(req.getUrl());
		
		Element lesson = doc.select("#lessonArea").first();
		Elements links = lesson.select("a[href]");
		Elements forms = lesson.select("form[action]");
		System.out.println(" ("+ links.size() + " links and " + forms.select("input[type=submit]").size() + " forms)");
		
		for(Element form: forms){
			List<Form> formList = Form.getFormList(form);
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
	
	public void crawlForm(Form form){
		Document doc = null;
		sequence.add(form);

		System.out.print("    f " + form.getAction() + ' ' + form.getSubmit());

		doc = Jsoup.parse(submitForm(form));
		doc.setBaseUri(form.getAction());
		
		Element lesson = doc.select("#lessonArea").first();
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
		}else System.out.println();
	}

	@Override
	public List<HashMap<String, String>> buildFormValues() {
		List<HashMap<String, String>> v = new ArrayList<HashMap<String, String>>();
		
		HashMap<String, String> login = new HashMap<String, String>();
		login.put("employee_id", "111");
		login.put("password", "john");
		login.put("action", "Login");
		v.add(login);
		
		HashMap<String, String> view = new HashMap<String, String>();
		view.put("employee_id", "111");
		view.put("action", "ViewProfile");
		v.add(view);
	
		HashMap<String, String> edit = new HashMap<String, String>();
		edit.put("employee_id", "111");
		edit.put("action", "EditProfile");
		v.add(edit);
		
		HashMap<String, String> list = new HashMap<String, String>();
		list.put("employee_id", "111");
		list.put("action", "ListStaff");
		v.add(list);
		
		HashMap<String, String> search = new HashMap<String, String>();
		search.put("employee_id", "101");
		search.put("action", "SearchStaff");
		v.add(search);

		HashMap<String, String> create = new HashMap<String, String>();
		create.put("employee_id", "101");
		create.put("action", "CreateProfile");
		v.add(create);
		
		HashMap<String, String> delete = new HashMap<String, String>();
		delete.put("employee_id", "101");
		delete.put("action", "DeleteProfile");
		v.add(delete);
		
		HashMap<String, String> logout = new HashMap<String, String>();
		logout.put("employee_id", "101");
		logout.put("action", "Logout");
		v.add(logout);
		
		HashMap<String, String> searchName = new HashMap<String, String>();
		searchName.put("search_name", "john");
		searchName.put("action", "FindProfile");
		v.add(searchName);
		
		HashMap<String, String> update = new HashMap<String, String>();
		update.put("employee_id", "101");
		update.put("action", "UpdateProfile");
		update.put("address1", "50 black street");
		update.put("address2", "New York, NY");
		update.put("ccn", "2578546969853547");
		update.put("ccnLimit", "5000");
		update.put("description", "Does not work well with others");
		update.put("disciplinaryDate", "10106");
		update.put("disciplinaryNotes", "Constantly harassing coworkers");
		update.put("firstName", "Larry");
		update.put("lastName", "Stooge");
		update.put("manager", "101");
		update.put("phoneNumber", "443-689-0192");
		update.put("salary", "55000");
		update.put("ssn", "386-09-5451");
		update.put("startDate", "1012000");
		update.put("title", "Technician");
		v.add(update);
		
		return v;
	}

	@Override
	public String submitForm(Form form) {
		HashMap<String, String> values = getValuesForForm(form);
		if (values != null){			
				HTTPRequest req = new HTTPRequest(form.getAction());
				req.setMethod(form.getMethod());
				req.addHeader("Authorization", basicAuth);
				if (!cookie.isEmpty()) req.addHeader("Cookie", cookie.getCookieLine());
				
				for (String param : values.keySet()){
					req.addData(param, values.get(param));
				}			
				
				return TCPSend.Send(systemHost, systemPort, req);
		}
		return "";
	}
	
	public ArrayList<String> getInputSymbols() {
		ArrayList<String> is = new ArrayList<String>();
		for(Form form : forms){
			is.add(form.getSubmit().get("action"));
		}
		return is;
	}
	
	public HTTPRequest abstractToConcrete(ParameterizedInput pi){
		HTTPRequest req = null;
		if (!pi.isEpsilonSymbol()){
			LogManager.logInfo("Abstract : " + pi);			

			for(Form form : forms){
				if (form.getSubmit().get("action").equals(pi.getInputSymbol())){
					HashMap<String, String> values = getValuesForForm(form);
					req = new HTTPRequest(form.getAction());
					req.setMethod(form.getMethod());
					req.addHeader("Authorization", basicAuth);
					if (!cookie.isEmpty()) req.addHeader("Cookie", cookie.getCookieLine());
					
					int i = 0;
					for (String param : values.keySet()){
						req.addData(param, pi.getParameterValue(i++));
					}			
				}			
			}
			if (req == null){
				LogManager.logError("AbstractToConcrete method is missing for symbol : " + pi.getInputSymbol());
			}else{
				LogManager.logConcrete(req.toString());
			}
		}else{
			LogManager.logError("AbstractToConcrete for Epsilon symbol is impossible in " + pi.getInputSymbol());
		}
		return req;
	}
	
	public ParameterizedOutput concreteToAbstract(HTTPResponse resp){
		ParameterizedOutput po = null;
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp == null || resp.getCode()==404 || resp.getCode()==503 || resp.getCode()==500){
			po = new ParameterizedOutput();
		}else if (resp.getCode() == 200){
			po = new ParameterizedOutput();			
			if (resp.getContent().contains("Staff Listing Page")){
				po = new ParameterizedOutput("listing");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else if (resp.getContent().contains("<div id=\"lesson_login\">")){
				po = new ParameterizedOutput("home");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else if (resp.getContent().contains("value=\"UpdateProfile\"")){
				po = new ParameterizedOutput("editionPage");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else if (resp.getContent().contains("Credit Card Limit")){
				po = new ParameterizedOutput("profilePage");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else{
				LogManager.logError("ConcreteToAbstract method is missing for this page");
				LogManager.logConcrete(resp.toString());
			}
		}
		 
		LogManager.logInfo("Abstract : " + po);
		return po;		
	}
	
	public TreeMap<String, List<String>> getParameterNames() {
		HashMap<String, String> suffix = new HashMap<String, String>();
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		
		for(Form form : forms){
			List<String> names = new ArrayList<String>();
			for(String key : form.getInputs().keySet()){
				if (suffix.get(key) == null){
					suffix.put(key, "2");
					names.add(key);
				}else{
					names.add(suffix.get(key));
					suffix.put(key, String.valueOf(Integer.valueOf(suffix.get(key))+1));					
				}				
			}
			for(String key : form.getSubmit().keySet()){
				if (suffix.get(key) == null){
					suffix.put(key, "2");
					names.add(key);
				}else{
					names.add(suffix.get(key));
					suffix.put(key, String.valueOf(Integer.valueOf(suffix.get(key))+1));					
				}	
			}
			defaultParamNames.put("login", names);
		}

		return defaultParamNames;
	}

	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;

		for(Form f : forms){
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("larry", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("larry", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("john", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("john", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("foo", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("foo", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("666", Types.STRING), new Parameter("foo", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("666", Types.STRING), new Parameter("larry", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("666", Types.STRING), new Parameter("john", Types.STRING)));
			defaultParamValues.put(f.getSubmit().get("action"), params);
		}		
		
		return defaultParamValues;	
	}
}
