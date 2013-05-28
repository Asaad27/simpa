package crawler.driver;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;

import tools.loggers.LogManager;

public class BookStoreDriver extends GenericDriver {
	
	public BookStoreDriver() throws IOException {
		
		super("..//drivergen//abs//GotoCode.xml");
	}

	@Override
	public void reset() {
		super.reset();
		client = new WebClient();		
	}
		
	public void initConnection() {
		LogManager.logInfo("Initializing connection to the system");		
	}

	@Override
	protected void updateParameters() {
		// TODO Auto-generated method stub
		
	}
}
