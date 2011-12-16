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

public class WebSamlSSOSPDriver extends WebDriver {
	
	public WebSamlSSOSPDriver() {
		super();
		this.systemHost = "127.0.0.1";
		this.systemPort = 8094;
	}

	public ArrayList<String> getInputSymbols() {
		ArrayList<String> is = new ArrayList<String>();
		is.add("askRessource");
		is.add("getSAMLRequest");
		is.add("sendSAMLResp");
		return is;
	}

	public ArrayList<String> getOutputSymbols(){
		ArrayList<String> os = new ArrayList<String>();
		os.add("IdPList");
		os.add("SAMLRequest");
		os.add("error");
		return os;
	}
	
	@Override
	public String getSystemName() {
		return "WebSamlSSO SP (" + systemHost + ":" + systemPort + ")";
	}

	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(){
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;
		
		//askRessource
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("myAttributes", Types.STRING)));
			defaultParamValues.put("askRessource", params);		
		}
		
		//getSAMLRequest
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("IdP", Types.STRING), new Parameter("defaultSAMLSessionID", Types.STRING), new Parameter("defaultAuthID", Types.STRING)));
			defaultParamValues.put("getSAMLRequest", params);		
		}
		
		//sendSAMLResp
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("SAMLResp", Types.STRING), new Parameter("defaultSAMLSessionID", Types.STRING)));
			defaultParamValues.put("sendSAMLResp", params);		
		}
				
		return defaultParamValues;	
	}

	@Override
	public void reset() {
		super.reset();
		cookie.reset();
	}
	
	public HTTPRequest abstractToConcrete(ParameterizedInput pi){
		HTTPRequest res = null;
		if (!pi.isEpsilonSymbol()){
			if (pi.getInputSymbol().equals("askRessource")){
				if (pi.getParameterValue(0).equals("myAttributes")) res = new HTTPRequest(Method.GET, "/simplesamlphp-sp/www/module.php/core/authenticate.php?as=default-sp", Version.v11);
			}else if (pi.getInputSymbol().equals("getSAMLRequest")){
				if (pi.getParameterValue(0).equals("IdP")) res = new HTTPRequest(Method.GET, "/simplesamlphp-sp/www/module.php/saml/disco.php?entityID=http%3A%2F%2F127.0.0.1%3A8094%2Fsimplesamlphp-sp%2Fwww%2Fmodule.php%2Fsaml%2Fsp%2Fmetadata.php%2Fdefault-sp&return=http%3A%2F%2F127.0.0.1%3A8094%2Fsimplesamlphp-sp%2Fwww%2Fmodule.php%2Fsaml%2Fsp%2Fdiscoresp.php%3FAuthID%3D_"+pi.getParameterValue(2)+"%253Ahttp%253A%252F%252F127.0.0.1%253A8094%252Fsimplesamlphp-sp%252Fwww%252Fmodule.php%252Fcore%252Fas_login.php%253FAuthId%253Ddefault-sp%2526ReturnTo%253Dhttp%25253A%25252F%25252F127.0.0.1%25253A8094%25252Fsimplesamlphp-sp%25252Fwww%25252Fmodule.php%25252Fcore%25252Fauthenticate.php%25253Fas%25253Ddefault-sp&returnIDParam=idpentityid&idpentityid=http%3A%2F%2Flocalhost%3A8092%2Fsimplesamlphp-idp%2Fwww%2Fsaml2%2Fidp%2Fmetadata.php", Version.v11);
				cookie.set("SimpleSAMLSessionID-SP", pi.getParameterValue(1));
			}else if (pi.getInputSymbol().equals("sendSAMLResp")){
				if (pi.getParameterValue(0).equals("SAMLResp")) res = new HTTPRequest(Method.POST, "/simplesamlphp-sp/www/module.php/saml/sp/saml2-acs.php/default-sp", Version.v11);
				cookie.set("SimpleSAMLSessionID-SP", pi.getParameterValue(1));
			}else{
				LogManager.logError("AbstractToConcrete method is missing for symbol : " + pi.getInputSymbol());
			}
			if (res!=null && !cookie.isEmpty()) res.addHeader("Cookie", cookie.getCookieLine());
		}else{
			LogManager.logError("AbstractToConcrete for Epsilon symbol is impossible in " + pi.getInputSymbol());
		}		
		LogManager.logInfo("Abstract : " + pi);
		if (res!=null) LogManager.logConcrete(res.toString());
		return res;
	}
	
	public ParameterizedOutput concreteToAbstract(HTTPResponse resp){
		ParameterizedOutput po = null;
		LogManager.logConcrete(resp.toString());
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp == null || resp.getCode()==404 || resp.getCode()==503 || resp.getCode()==500){
			po = new ParameterizedOutput();
		}else if (resp.getCode() == 302 || resp.getCode() == 303){
			String location = resp.getHeader("Location");
			if (location.startsWith("http://localhost:8092/simplesamlphp-idp/www/saml2/idp/SSOService.php?SAMLRequest=")){
				po = new ParameterizedOutput("SAMLRequest");
				po.getParameters().add(new Parameter((cookie.get("SimpleSAMLSessionID-SP")==null?"no_SAMLSessionID":cookie.get("SimpleSAMLSessionID-SP")), Types.STRING));
			}else{
				return concreteToAbstract(executeWeb(new HTTPRequest(Method.GET, resp.getHeader("Location"), Version.v11)));
			}
		}else if (resp.getCode() == 200){
			po = new ParameterizedOutput();
			po.getParameters().add(new Parameter((cookie.get("SimpleSAMLSessionID-SP")==null?"no_SAMLSessionID":cookie.get("SimpleSAMLSessionID-SP")), Types.STRING));
			if (resp.getContent().contains("<title>Select your identity provider</title>")){
				po = new ParameterizedOutput("IdPList");
				po.getParameters().add(new Parameter(resp.getContent().substring(resp.getContent().indexOf("AuthID%3D_")+10, resp.getContent().indexOf("%253A", resp.getContent().indexOf("AuthID%3D_"))), Types.STRING));
			}else if (resp.getContent().contains("<title>Logged out</title>")){
				po = new ParameterizedOutput("webPage");
				po.getParameters().add(new Parameter("LoggedOut", Types.STRING));
			}else if (resp.getContent().contains("<title>Entrez votre identifiant et votre mot de passe</title>")){
				po = new ParameterizedOutput("SAMLRequest");
				po.getParameters().add(new Parameter(resp.getContent().substring(resp.getContent().indexOf("name=\"AuthState\"")+24, resp.getContent().indexOf(":http", resp.getContent().indexOf("name=\"AuthState\"")+30)), Types.STRING));
				po.getParameters().add(new Parameter(resp.getContent().substring(resp.getContent().indexOf("cookieTime")+11, resp.getContent().indexOf("&", resp.getContent().indexOf("cookieTime")+15)), Types.STRING));
			}else{
				LogManager.logError("Unknown webpage");
			}
		}
		 
		LogManager.logInfo("Abstract : " + po);
		return po;		
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
