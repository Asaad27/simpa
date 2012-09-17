package drivers.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebSamlSSOSPDriver extends HighWebDriver {
	
	public WebSamlSSOSPDriver() {
		super();
		this.systemHost = "127.0.0.1";
		this.systemPort = 8094;
	}

	public ArrayList<String> getInputSymbols() {
		ArrayList<String> is = new ArrayList<String>();
		is.add("start");
		is.add("selectIDP");
		is.add("login");
		is.add("logout");
		return is;
	}

	public ArrayList<String> getOutputSymbols(){
		ArrayList<String> os = new ArrayList<String>();
		os.add("idpList");
		os.add("credentialsPage");
		os.add("profilePage");
		os.add("loggedOutPage");
		return os;
	}
	
	@Override
	public String getSystemName() {
		return "WebSamlSSO";
	}

	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(){
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;
		
		//start
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("http://127.0.0.1:8094/simplesamlphp-sp/www/module.php/core/authenticate.php?as=default-sp", Types.STRING)));
			defaultParamValues.put("start", params);		
		}
			
		//selectIDP
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("0", Types.NUMERIC)));
			defaultParamValues.put("selectIDP", params);		
		}
		
		//login
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("orlando", Types.STRING), new Parameter("orlandopassword", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("root", Types.STRING), new Parameter("toor", Types.STRING)));
			defaultParamValues.put("login", params);		
		}
		
		//logout
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("none", Types.STRING)));
			defaultParamValues.put("logout", params);		
		}
				
		return defaultParamValues;	
	}

	@Override
	public void reset() {
		super.reset();
		webClient = new WebClient();
		currentPage = null;
	}
	
	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("start", Utils.createArrayList("spAddr"));
		defaultParamNames.put("selectIDP", Utils.createArrayList("idpIndex"));
		defaultParamNames.put("login", Utils.createArrayList("loginUsername", "loginPassword"));
		defaultParamNames.put("logout", Utils.createArrayList("noparam"));
		
		defaultParamNames.put("idpList", Utils.createArrayList("codeIdp"));
		defaultParamNames.put("credentialsPage", Utils.createArrayList("codeCredentialsPage"));
		defaultParamNames.put("profilePage", Utils.createArrayList("codeProfilePage"));
		defaultParamNames.put("loggedOutPage", Utils.createArrayList("codeLoggedOutPage"));
		return defaultParamNames;
	}

	@Override
	public ParameterizedOutput concreteToAbstract(HtmlPage resp) {
		ParameterizedOutput po = null;
		
		if (resp == null){
			po = new ParameterizedOutput();
			
		}else{
			po = new ParameterizedOutput();			
			if (resp.getTitleText().contains("Enter your username and password")){
				po = new ParameterizedOutput("credentialsPage");
				po.getParameters().add(new Parameter("ok", Types.STRING));
				
			}else if (resp.getTitleText().contains("Select your identity provider")){
				po = new ParameterizedOutput("idpList");
				po.getParameters().add(new Parameter("ok", Types.STRING));
				
			}else if (resp.asText().contains("eduPersonAffiliation")){
				po = new ParameterizedOutput("profilePage");
				po.getParameters().add(new Parameter("ok", Types.STRING));
				
			}else if (resp.getTitleText().contains("Logged out")){
				po = new ParameterizedOutput("loggedOutPage");
				po.getParameters().add(new Parameter("ok", Types.STRING));
				
			}else{
				LogManager.logError("ConcreteToAbstract method is missing for this page");
				LogManager.logConcrete(resp.getTitleText());
			}
		}
		 
		LogManager.logInfo("Abstract : " + po);
		return po;
	}

	@Override
	public HtmlPage abstractToConcrete(ParameterizedInput pi) {
		if (!pi.isEpsilonSymbol()){
			LogManager.logInfo("Abstract : " + pi);	
			
			try{	
				if (pi.getInputSymbol().equals("start")){
					currentPage = webClient.getPage(pi.getParameterValue(0));
					
				}else if (pi.getInputSymbol().equals("selectIDP")){
					HtmlForm form = currentPage.getForms().get(Integer.parseInt(pi.getParameterValue(0)));
				    currentPage = form.getInputByValue("Select").click();
					
				}else if (pi.getInputSymbol().equals("login")){
					HtmlForm credForm = currentPage.getForms().get(0);
					credForm.getInputByName("username").setValueAttribute(pi.getParameterValue(0));
					credForm.getInputByName("password").setValueAttribute(pi.getParameterValue(1));
					currentPage = credForm.getInputByValue("Login").click();
					
				}else if (pi.getInputSymbol().equals("logout")){
					currentPage = currentPage.getAnchorByText("Logout").click();
					
				}else{
					LogManager.logError("AbstractToConcrete method is missing for symbol : " + pi.getInputSymbol());
				}				
			}catch (Exception e){
				return null;
			}
		}else{
			LogManager.logError("AbstractToConcrete for Epsilon symbol is impossible in " + pi.getInputSymbol());
		}
		return currentPage;
	}
}
