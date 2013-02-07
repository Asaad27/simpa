package drivergenerator.systems;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import drivergenerator.DriverGenerator;
import drivergenerator.Input;

public class BookStore extends DriverGenerator{
	
	public BookStore() throws JsonParseException, JsonMappingException, IOException{
		super("bookstore.json");
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
		return in.getAddress().substring(32);
	}
}
