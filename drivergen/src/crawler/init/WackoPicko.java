package crawler.init;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import crawler.DriverGenerator;
import crawler.Input;

import tools.loggers.LogManager;

public class WackoPicko extends DriverGenerator{
	
	public WackoPicko() throws JsonParseException, JsonMappingException, IOException{			
		super("wackopicko.json");
		initConnection();
	}
	
	@Override
	public void reset() {
	}
	
	@Override
	public void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
	}

	@Override
	public String prettyprint(Input in) {
		return in.toString();
	}
}