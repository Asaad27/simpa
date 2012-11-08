package drivergenerator;

import tools.loggers.LogManager;

public class Europe1DriverGenerator extends DriverGenerator{
	
	public Europe1DriverGenerator(Config c){
		super(c);
		initConnection();
		addUrl("/WackoPicko/");
	}
	
	@Override
	public void reset() {
	}
	
	private void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
	}
}
