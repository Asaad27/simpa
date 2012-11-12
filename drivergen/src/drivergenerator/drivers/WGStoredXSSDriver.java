package drivergenerator.drivers;

import java.io.IOException;
import java.net.URL;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import tools.Form;
import tools.HTTPData;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

import drivergenerator.DriverGenerator;

public class WGStoredXSSDriver extends DriverGenerator{
	
	private String screen = null;
	
	public WGStoredXSSDriver() throws JsonParseException, JsonMappingException, IOException{
		super("webgoat_stored_xss.json");
		initConnection();
		addUrl("http://localhost:8080/WebGoat/attack?Screen="+screen+"&menu=900");
	}
	
	private String extractScreen(WebResponse resp, String lesson) {
		String content = resp.getContentAsString();
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}
	
	@Override
	public void reset() {
		try {
			client.getPage("http://localhost:8080/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen);
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to reset the system", e);
		}
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
		try {
			client.getPage("http://localhost:8080/WebGoat/attack");		
			WebRequest request = new WebRequest(new URL("http://localhost:8080/WebGoat/attack"), HttpMethod.POST);
			request.setRequestParameters(new HTTPData("start", "Start WebGoat").getNameValueData());	
			screen = extractScreen(client.getPage(request).getWebResponse(), "Stage 1: Stored XSS");
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Error initializing connectin to the system", e);
		}	
	}

	@Override
	protected String prettyprint(Object o) {
		if (o != null){
			if (o instanceof String){
				return ((String) o).substring(21);
			}else if (o instanceof Form){
				Form f = (Form)o;
				return f.getInputs().get("action").get(0);
			}
		}
		return null;
	}
}
