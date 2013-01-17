package drivergenerator;
import java.util.ArrayList;
import java.util.HashMap;

public class Config {
	private String host = "localhost";
	private int port = 80;
	private String basicAuthUser = null;
	private String basicAuthPass = null;
	private String name = null;
	private String limitSelector = "html";
	private HashMap<String, String> paramValues = null;
	private ArrayList<String> noFollow = null;
	private String firstURL = null;
	private String actionByParameter = null;
	
	public Config(){
		paramValues = new HashMap<String, String>();
		noFollow = new ArrayList<String>();
	}
	
	public String getActionByParameter(){
		return actionByParameter;
	}
	
	public ArrayList<String> getNoFollow(){
		return noFollow;
	}
	
	public String getFirstURL(){
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
	
	public HashMap<String, String> getData(){
		return paramValues;
	}
}
