package drivergenerator;

import java.io.IOException;
import java.net.URL;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import tools.HTTPData;
import tools.HTTPResponse;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;

public class WGStoredXSSDriverGenerator extends DriverGenerator{
	
	private String screen = null;
	
	public WGStoredXSSDriverGenerator(Config c) throws JsonParseException, JsonMappingException, IOException{
		super("webgoat_stored_xss.json");
		initConnection();
		addUrl("/WebGoat/attack?Screen="+screen+"&menu=900");
	}
	
	private String extractScreen(HTTPResponse resp, String lesson) {
		String content = resp.toString();
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}
	
	@Override
	public void reset() {
		try {
			client.getPage("/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen);
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to reset the system", e);
		}
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
		try {
			client.getPage("/WebGoat/attack");		
			WebRequest request = new WebRequest(new URL("/WebGoat/attack"), HttpMethod.GET);
			request.setRequestParameters(new HTTPData("start", "Start WebGoat").getNameValueData());
			HTTPResponse r = new HTTPResponse(client.getPage(request).getWebResponse().toString());		
			screen = extractScreen(r, "Stage 1: Stored XSS");
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Error initializing connectin to the system", e);
		}	
	}
}
