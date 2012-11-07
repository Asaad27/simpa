package drivergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.HTTPClient;
import tools.HTTPData;
import tools.HTTPResponse;
import tools.loggers.LogManager;

public class WGStoredXSSDriverGenerator extends DriverGenerator{
	
	private String screen = null;
	
	public WGStoredXSSDriverGenerator(){
		super();
		client = new HTTPClient("localhost", 8080);
		client.setCredentials("guest", "guest");
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
		client.get("/WebGoat/attack?Screen="+screen+"&menu=900&Restart="+screen);
	}
	
	public String getSystemName() {
		return "WebGoat Stored XSS";
	}
	
	protected String limitToThisSelector() {
		return "#lessonArea";
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
		client.get("/WebGoat/attack");		
		HTTPResponse r = client.post("/WebGoat/attack", new HTTPData("start", "Start WebGoat"));		
		screen = extractScreen(r, "Stage 1: Stored XSS");
	}
	
	@Override
	public List<String> filterUrl(Elements links) {
		List<String> urls = new ArrayList<String>();
		for (Element e : links){
			String to = e.attr("href");
			if (to.toLowerCase().startsWith("javascript:")) continue;
			if (to.toLowerCase().startsWith("http://")) continue;
			urls.add(to);
		}
		return urls;
	}

	@Override
	public HashMap<String, String> buildFormValues() {
		HashMap<String, String> v = new HashMap<String, String>();
		
		v.put("password", "john");
		v.put("employee_id", "111");
		v.put("manager", "110");

		return v;
	}

}
