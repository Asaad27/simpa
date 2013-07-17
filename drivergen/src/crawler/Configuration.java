package crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import tools.loggers.LogManager;

public class Configuration {
	private String host = "";
	private int port = 0;
	private String basicAuthUser = null;
	private String basicAuthPass = null;
	private String name = null;
	private String limitSelector = "html";
	private HashMap<String, ArrayList<String>> paramValues = null;
	private ArrayList<String> noFollow = null;
	private ArrayList<String> urls = null;
	private String actionByParameter = null;
	private String cookies = null;
	private boolean mergeInputs = false;	
	
	public ArrayList<String> getURLs(){
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

	public ArrayList<String> getNoFollow() {
		if (noFollow == null) noFollow = new ArrayList<String>();
		return noFollow;
	}

	public String getName() {
		return name;
	}
	
	public String getCookies() {
		if (cookies.endsWith(";") || cookies.endsWith(" ")) return cookies;
		else return cookies + ";";
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

	public HashMap<String, ArrayList<String>> getData() {
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
				if (host.isEmpty()){
					host = aURL.getHost();
				}
				if (port == 0){
					port = aURL.getPort();
				}
			} catch (MalformedURLException e) {
				LogManager.logError("Unable to parse the url provided.");
				e.printStackTrace();
			}		
		}else{
			LogManager.logError("No url provided. Please check the configuration file.");
		}
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
}
