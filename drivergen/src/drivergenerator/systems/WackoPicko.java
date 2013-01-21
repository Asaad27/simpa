package drivergenerator.systems;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import tools.loggers.LogManager;
import drivergenerator.DriverGenerator;
import drivergenerator.Input;

public class WackoPicko extends DriverGenerator{
	
	public WackoPicko() throws JsonParseException, JsonMappingException, IOException{			
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
