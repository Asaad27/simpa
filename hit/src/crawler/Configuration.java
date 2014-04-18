package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.Utils;
import tools.loggers.LogManager;

public class Configuration {
	private String host = null;
	private int port = 0;
	private String basicAuthUser = null;
	private String basicAuthPass = null;
	private String name = null;
	private String limitSelector = null;
	private HashMap<String, ArrayList<String>> paramValues = null;
	private ArrayList<String> noFollow = null;
	private ArrayList<String> urls = null;
	private String actionByParameter = null;
	private String cookies = null;
	private String reset = null;
	private boolean mergeInputs = false;
	private boolean smallestSet = false;
	
	public boolean keepSmallSet() {
		return smallestSet;
	}

	public void setsmallestSet(boolean b) {
		this.smallestSet = b;
	}

	public List<String> getURLs(){
		return urls;
	}

	public Configuration() {
		paramValues = new HashMap<String, ArrayList<String>>();
		noFollow = new ArrayList<String>();
		urls = new ArrayList<String>();
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getReset() {
		return reset;
	}

	public void setReset(String reset) {
		this.reset = reset;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBasicAuthUser(String basicAuthUser) {
		this.basicAuthUser = basicAuthUser;
	}

	public void setBasicAuthPass(String basicAuthPass) {
		this.basicAuthPass = basicAuthPass;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMergeInputs(boolean m) {
		this.mergeInputs = m;
	}
	
	public void setLimitSelector(String limitSelector) {
		this.limitSelector = limitSelector;
	}

	public void setNoFollow(ArrayList<String> noFollow) {
		this.noFollow = noFollow;
	}

	public void setActionByParameter(String actionByParameter) {
		this.actionByParameter = actionByParameter;
	}

	public String getActionByParameter() {
		return actionByParameter;
	}

	public List<String> getNoFollow() {		
		return noFollow;
	}

	public String getName() {
		return name;
	}
	
	public String getCookies() {
		if (cookies != null){
			if (cookies.endsWith(";") || cookies.endsWith(" ")) return cookies;
			else return cookies + ";";
		}else
			return null;
	}


	public String getBasicAuthUser() {
		return basicAuthUser;
	}

	public String getBasicAuthPass() {
		return basicAuthPass;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getLimitSelector() {
		if (limitSelector == null) return "";
		return limitSelector;
	}

	public Map<String, ArrayList<String>> getData() {
		if (paramValues == null) paramValues = new HashMap<String, ArrayList<String>>();
		return paramValues;
	}

	public boolean getMerge() {
		return mergeInputs;
	}

	public void check() {
		URL aURL;
		if (!urls.isEmpty()){
			try {
				aURL = new URL(urls.get(0));
				host = aURL.getHost();
				port = aURL.getPort();
			} catch (MalformedURLException e) {
				LogManager.logException("Unable to parse the url provided.", e);
			}		
		}else{
			LogManager.logFatalError("No url provided. Please check the configuration file.");
		}
		if (name==null || name.isEmpty()) LogManager.logFatalError("No system name provided. Please check the configuration file.");
		if (limitSelector==null || limitSelector.isEmpty()) limitSelector = "html";
		if (actionByParameter != null && actionByParameter.isEmpty()) actionByParameter = null;
		if (cookies != null && cookies.isEmpty()) cookies = null;
		if (reset != null && reset.isEmpty()) reset = null;
		if (noFollow == null) noFollow = new ArrayList<String>();
		Utils.generateCombinationOfSet(paramValues);
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
}
