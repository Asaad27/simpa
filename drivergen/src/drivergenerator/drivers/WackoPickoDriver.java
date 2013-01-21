package drivergenerator.drivers;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import drivergenerator.DriverGenerator;
import drivergenerator.Input;

import tools.loggers.LogManager;

public class WackoPickoDriver extends DriverGenerator{
	
	public WackoPickoDriver() throws JsonParseException, JsonMappingException, IOException{			
		super("wackopicko.json");
		initConnection();
	}
	
	@Override
	public void reset() {
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
	}

	@Override
	protected String prettyprint(Input in) {
		return in.toString();
	}
}
