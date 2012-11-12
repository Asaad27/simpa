package drivergenerator;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import tools.loggers.LogManager;

public class WackoPickoDriverGenerator extends DriverGenerator{
	
	public WackoPickoDriverGenerator() throws JsonParseException, JsonMappingException, IOException{			
		super("wackopicko.json");
		initConnection();
	}
	
	@Override
	public void reset() {
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
	}
}
