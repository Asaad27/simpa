package main;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;

import drivergenerator.Config;
import drivergenerator.Europe1DriverGenerator;


public class Main {
	
	public static void main(String[] args) throws Exception{
		ObjectMapper mapper = new ObjectMapper();	
		Config config = mapper.readValue(new File("conf//europe1.json"), Config.class);
		Europe1DriverGenerator gen = new Europe1DriverGenerator(config);
		gen.start();
	}
}
