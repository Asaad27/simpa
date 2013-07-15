package crawler.init;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import crawler.DriverGenerator;
import crawler.Input;

public class BookStore extends DriverGenerator{
	
	public BookStore() throws JsonParseException, JsonMappingException, IOException{
		super("BookStore.json");
		initConnection();
	}
	
	@Override
	public void reset() {
	}
	
	@Override
	protected void initConnection() {
	}

	@Override
	protected String prettyprint(Input in) {
		return in.getAddress();
	}
}
