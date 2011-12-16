package drivers.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.HTTPRequest;
import tools.HTTPRequest.Method;
import tools.HTTPRequest.Version;
import tools.HTTPResponse;
import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public class WGStoredXSSDriver extends WebDriver {
	
	private String basicAuth = "Basic Z3Vlc3Q6Z3Vlc3Q=";
	private String screen = null;
	
	public WGStoredXSSDriver() {
		super();
		this.systemHost = "localhost";
		this.systemPort = 8080;
		initConnection();
	}

	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");
		
		HTTPRequest res = new HTTPRequest(Method.GET, "/WebGoat/attack", Version.v11);
		res.addHeader("Authorization", basicAuth);
		LogManager.logConcrete(res.toString());
		HTTPResponse resp = executeWeb(res);
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		
		res = new HTTPRequest(Method.POST, "/WebGoat/attack", Version.v11);
		res.addHeader("Authorization", basicAuth);
		res.addHeader("Cookie", cookie.getCookieLine());
		res.addPostData("start", "Start WebGoat");
		LogManager.logConcrete(res.toString());
		resp = executeWeb(res);

		screen = extractScreen(resp.toString(), "Stage 1: Stored XSS");
		LogManager.logInfo("Screen : " + screen);
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		
		LogManager.logInfo("Ready to infer");
	}

	private String extractScreen(String content, String lesson) {
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}

	public ArrayList<String> getInputSymbols() {
		ArrayList<String> is = new ArrayList<String>();
		is.add("Login");
		is.add("Logout");
		is.add("ViewProfile");
		is.add("EditProfile");
		is.add("XSSProfile");
		return is;
	}

	public ArrayList<String> getOutputSymbols(){
		ArrayList<String> os = new ArrayList<String>();
		os.add("Listing");
		os.add("Home");
		os.add("ProfilePage");
		os.add("EditionPage");
		return os;
	}
	
	@Override
	public String getSystemName() {
		return "WebGoat Stored XSS";
	}

	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(){
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;
		
		//Login
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("larry", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("larry", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("john", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("john", Types.STRING)));
			defaultParamValues.put("Login", params);		
		}
		
		//ViewProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("ViewProfile", params);		
		}

		//EditProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("EditProfile", params);		
		}
		
		//XSSProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("<script>alert(1);</script>", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("<script>alert(1);</script>", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING), new Parameter("22 Foo Street", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING), new Parameter("22 Foo Street", Types.STRING)));
			defaultParamValues.put("XSSProfile", params);		
		}
		
		//Logout
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("Logout", params);		
		}
		
		return defaultParamValues;	
	}

	@Override
	public void reset() {
		super.reset();
		HTTPRequest res = new HTTPRequest(Method.GET, "/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen, Version.v11);
		res.addHeader("Authorization", basicAuth);
		res.addHeader("Cookie", cookie.getCookieLine());
		executeWeb(res);
	}
	
	public HTTPRequest abstractToConcrete(ParameterizedInput pi){
		HTTPRequest res = null;
		if (!pi.isEpsilonSymbol()){
			LogManager.logInfo("Abstract : " + pi);			

			if (pi.getInputSymbol().equals("Login")){
				res = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="+screen+"&menu=900", Version.v11);
				res.addPostData("employee_id", pi.getParameterValue(0));
				res.addPostData("password", pi.getParameterValue(1));
				res.addPostData("action", "Login");
				
			}else if (pi.getInputSymbol().equals("ViewProfile")){
				res = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="+screen+"&menu=900", Version.v11);
				res.addPostData("employee_id", pi.getParameterValue(0));
				res.addPostData("action", "ViewProfile");
				
			}else if (pi.getInputSymbol().equals("EditProfile")){
				res = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="+screen+"&menu=900", Version.v11);
				res.addPostData("employee_id", pi.getParameterValue(0));
				res.addPostData("action", "EditProfile");
				
			}else if (pi.getInputSymbol().equals("XSSProfile")){
				res = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="+screen+"&menu=900", Version.v11);
				res.addPostData("employee_id", pi.getParameterValue(0));
				res.addPostData("action", "UpdateProfile");
				res.addPostData("address1", pi.getParameterValue(1));
				res.addPostData("address2", "New York, NY");
				res.addPostData("ccn", "2578546969853547");
				res.addPostData("ccnLimit", "5000");
				res.addPostData("description", "Does not work well with others");
				res.addPostData("disciplinaryDate", "10106");
				res.addPostData("disciplinaryNotes", "Constantly harassing coworkers");
				res.addPostData("firstName", "Larry");
				res.addPostData("lastName", "Stooge");
				res.addPostData("manager", "101");
				res.addPostData("phoneNumber", "443-689-0192");
				res.addPostData("salary", "55000");
				res.addPostData("ssn", "386-09-5451");
				res.addPostData("startDate", "1012000");
				res.addPostData("title", "Technician");
				
			}else if (pi.getInputSymbol().equals("Logout")){
				res = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="+screen+"&menu=900", Version.v11);
				res.addPostData("employee_id", pi.getParameterValue(0));
				res.addPostData("action", "Logout");
			}else{
				LogManager.logError("AbstractToConcrete method is missing for symbol : " + pi.getInputSymbol());
			}
			
			if (res != null){
				res.addHeader("Authorization", basicAuth);
				if (!cookie.isEmpty()) res.addHeader("Cookie", cookie.getCookieLine());
				LogManager.logConcrete(res.toString());
			}
		}else{
			LogManager.logError("AbstractToConcrete for Epsilon symbol is impossible in " + pi.getInputSymbol());
		}
		return res;
	}
	
	public ParameterizedOutput concreteToAbstract(HTTPResponse resp){
		ParameterizedOutput po = null;
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp == null || resp.getCode()==404 || resp.getCode()==503 || resp.getCode()==500){
			po = new ParameterizedOutput();
		}else if (resp.getCode() == 200){
			po = new ParameterizedOutput();			
			if (resp.getContent().contains("Staff Listing Page")){
				po = new ParameterizedOutput("Listing");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else if (resp.getContent().contains("<div id=\"lesson_login\">")){
				po = new ParameterizedOutput("Home");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else if (resp.getContent().contains("Credit Card Limit")){
				po = new ParameterizedOutput("ProfilePage");
				po.getParameters().add(new Parameter(resp.getCodeString(), Types.STRING));
			}else{
				LogManager.logError("Unknown webpage");
				LogManager.logConcrete(resp.toString());
			}
		}
		 
		LogManager.logInfo("Abstract : " + po);
		return po;		
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("Login", Utils.createArrayList("profileIdLogin", "passwordLogin"));
		defaultParamNames.put("ViewProfile", Utils.createArrayList("profileIdProfile"));
		defaultParamNames.put("EditProfile", Utils.createArrayList("profileIdEdit"));
		defaultParamNames.put("XSSProfile", Utils.createArrayList("profileIdXSS", "XSSPayload"));
		defaultParamNames.put("Logout", Utils.createArrayList("profileIdLogout"));
		
		defaultParamNames.put("Listing", Utils.createArrayList("codeListing"));
		defaultParamNames.put("Home", Utils.createArrayList("codeHome"));
		defaultParamNames.put("ProfilePage", Utils.createArrayList("codeProfilePage"));
		defaultParamNames.put("EditionPage", Utils.createArrayList("codeEditionPage"));
		return defaultParamNames;
	}
}
