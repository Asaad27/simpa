package drivers.efsm.real;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

import main.drivergen.Options;

import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCredentialsProvider;

import tools.HTTPData;
import tools.loggers.LogManager;
import automata.efsm.Parameter;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crawler.WebInput;
import crawler.WebInput.Type;
import drivers.efsm.EFSMDriver;

public class ScanDriver extends EFSMDriver {
	
	private WebClient client = null;
	private CookieManager cm = null;

	public ScanDriver(String system) {
		super(null);
		type = DriverType.SCAN;
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
	    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		client = new WebClient();
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setTimeout(Options.TIMEOUT);
		client.getOptions().setCssEnabled(Options.CSS);
		client.getOptions().setJavaScriptEnabled(Options.JS);
		cm = new CookieManager();
		client.setCookieManager(cm);
		BasicCredentialsProvider creds = new BasicCredentialsProvider();
		creds.setCredentials(new AuthScope("127.0.0.1", 8080),
				new UsernamePasswordCredentials("guest", "guest"));
		client.setCredentialsProvider(creds);
	}
	
	@Override
	public void reset_implem(){
		cm.clearCookies();
	}
	
	private String submit(WebInput in, HTTPData values) throws MalformedURLException{
		WebRequest request = null;
		if (in.getType()==Type.FORM){
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());
			request.setAdditionalHeader("Connection", "Close");
			Page page;
			try {
				page = client.getPage(request);
				if (page.getWebResponse().getStatusCode() != 200) return null;
			}catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if (page instanceof TextPage) return ((TextPage)page).getContent();
			else return ((HtmlPage)page).asXml();
		}else if (in.getType()==Type.LINK){		
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()){
				for(String name : in.getParams().keySet()){
					for(String value : in.getParams().get(name)){
						link += name + "=" + value + "&";
					}
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length()-1));
				if (page.getWebResponse().getStatusCode() != 200) return null;
			}catch (HttpHostConnectException e) {
				LogManager.logFatalError("Unable to connect to host");
				return null;
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return page.asXml();
		}
		return null;
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
