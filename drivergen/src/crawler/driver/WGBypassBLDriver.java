package crawler.driver;

import java.io.IOException;
import java.net.URL;

import tools.HTTPData;
import tools.loggers.LogManager;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

import crawler.Input;

public class WGBypassBLDriver extends GenericDriver {
	
	private String screen;

	public WGBypassBLDriver() throws IOException {
		super("..//drivergen//abs//WebGoat_Bypass_BL.xml");
	}

	@Override
	public void reset() {
		try {
			client.getPage("http://localhost:8080/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen);
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Unable to reset the system", e);
		}
	}
	
	private String extractScreen(WebResponse resp) {
		String content = resp.getContentAsString();
		int pos = content.indexOf("LAB: Role Based Access Control");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos+7, content.indexOf("&", pos));
	}
			
	public void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
		try {			
			client.getPage("http://localhost:8080/WebGoat/attack");		
			WebRequest request = new WebRequest(new URL("http://localhost:8080/WebGoat/attack"), HttpMethod.POST);
			request.setRequestParameters(new HTTPData("start", "Start WebGoat").getNameValueData());	
			screen = extractScreen(client.getPage(request).getWebResponse());
			client.getPage("http://localhost:8080/WebGoat/attack?Screen="+screen+"&menu=900&Stage=1");
		} catch (FailingHttpStatusCodeException | IOException e) {
			LogManager.logException("Error initializing connection to the system", e);
		}	
	}

	@Override
	protected void updateParameters() {
		for (Input i : inputs){
			i.setAddress(i.getAddress().replace("%%__RUNTIME__Screen__%%", screen));
			for(String name : i.getParams().keySet()){
				for(int o=0; o<i.getParams().get(name).size(); o++){
					i.getParams().get(name).set(o,  i.getParams().get(name).get(o).replace("%%__RUNTIME__Screen__%%", screen));
				}
			}
		}
	}



}
