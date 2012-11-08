package main;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;

import drivergenerator.Config;
import drivergenerator.WGStoredXSSDriverGenerator;


public class Main {
	
	public static void main(String[] args) throws Exception{
		ObjectMapper mapper = new ObjectMapper();	
		Config config = mapper.readValue(new File("conf//webgoat_stored_xss"), Config.class);
		WGStoredXSSDriverGenerator gen = new WGStoredXSSDriverGenerator(config);
		gen.start();
	}
}
