package drivergenerator;

import java.util.ArrayList;
import java.util.HashMap;

public class Config {
	private String host = "localhost";
	private int port = 80;
	private int timeout = 1000;
	private String basicAuthUser = null;
	private String basicAuthPass = null;
	private String name = null;
	private String limitSelector = "html";
	private HashMap<String, String> paramValues = null;
	private ArrayList<String> noFollow = null;
	private ArrayList<String> runtimeParameters;
	private String firstURL = null;
	private boolean enableCSS = false;
	private boolean enableJS = false;
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isEnableCSS() {
		return enableCSS;
	}

	public void setEnableCSS(boolean enableCSS) {
		this.enableCSS = enableCSS;
	}

	public boolean isEnableJS() {
		return enableJS;
	}

	public void setEnableJS(boolean enableJS) {
		this.enableJS = enableJS;
	}

	private String actionByParameter = null;

	public Config() {
		paramValues = new HashMap<String, String>();
		noFollow = new ArrayList<String>();
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

	public void setLimitSelector(String limitSelector) {
		this.limitSelector = limitSelector;
	}

	public void setNoFollow(ArrayList<String> noFollow) {
		this.noFollow = noFollow;
	}

	public void setRuntimeParameters(ArrayList<String> runtimeParameters) {
		this.runtimeParameters = runtimeParameters;
	}

	public void setFirstURL(String firstURL) {
		this.firstURL = firstURL;
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
	
	public ArrayList<String> getRuntimeParameters() {
		if (runtimeParameters == null) runtimeParameters = new ArrayList<String>();
		return runtimeParameters;
	}

	public String getFirstURL() {
		return firstURL;
	}

	public String getName() {
		return name;
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
		return limitSelector;
	}

	public HashMap<String, String> getData() {
		if (paramValues == null) paramValues = new HashMap<String, String>();
		return paramValues;
	}
}
