package drivergenerator.driver;

import java.io.IOException;
import java.net.URL;

import tools.HTTPData;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

public class WGStoredXSSDriver extends GenericDriver {
	
	private String screen = null;

	public WGStoredXSSDriver(String xml) throws IOException {
		super(xml);
	}

	@Override
	public void reset() {
		try {
			client.getPage("http://localhost:8080/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen);
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to reset the system", e);
		}
	}
	
	private String extractScreen(WebResponse resp, String lesson) {
		String content = resp.getContentAsString();
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}
		
	public void initConnection() {
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

}
