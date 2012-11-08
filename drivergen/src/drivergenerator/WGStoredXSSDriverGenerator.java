package drivergenerator;

import tools.HTTPData;
import tools.HTTPResponse;
import tools.loggers.LogManager;

public class WGStoredXSSDriverGenerator extends DriverGenerator{
	
	private String screen = null;
	
	public WGStoredXSSDriverGenerator(Config c){
		super(c);
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
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
		client.get("/WebGoat/attack");		
		HTTPResponse r = client.post("/WebGoat/attack", new HTTPData("start", "Start WebGoat"));		
		screen = extractScreen(r, "Stage 1: Stored XSS");
	}
}
