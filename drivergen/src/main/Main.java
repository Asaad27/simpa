package main;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import crawler.DriverGenerator;
import crawler.configuration.Configuration;
import crawler.driver.GenericDriver;
import crawler.driver.WGStoredXSSDriver;
import drivers.efsm.EFSMDriver.Types;


public class Main {
	
	public static String VERSION = "1.0";
	public static String NAME = "TIC - The Inference Crawler";
	
	public static void testCrawler(){
		//DriverGenerator g = DriverGenerator.getDriver("WGStoredXSS");
		///DriverGenerator g = DriverGenerator.getDriver("WackoPicko");
		DriverGenerator g = DriverGenerator.getDriver("BookStore");
		g.start();
		g.exportToDot();
		g.exportToXML();
	}
	
	public static void testDriver() throws IOException{
		GenericDriver d = new WGStoredXSSDriver();
		System.out.println("System name   : " + d.getSystemName());
		System.out.println("Input list: ");
		int i, n = d.getInputSymbols().size();
		for(i=0; i<n; i++){
			System.out.println(d.getInputSymbols().get(i) + " " + d.inputs.get(i).getMethod() + " " + d.inputs.get(i).getAddress() + " " + d.inputs.get(i).getParams());
		}
		n = d.getOutputSymbols().size();
		System.out.println("Output list: ");
		for(i=0; i<n; i++){
			System.out.println(d.getOutputSymbols().get(i) + " " + d.outputs.get(i).getParams());
		}
		d.reset();
		ParameterizedInput pi = new ParameterizedInput(d.getInputSymbols().get(1));
		pi.getParameters().add(new Parameter("Login", Types.STRING));
		pi.getParameters().add(new Parameter("111", Types.STRING));
		pi.getParameters().add(new Parameter("john", Types.STRING));
		System.out.println(d.execute(pi));
		pi = new ParameterizedInput(d.getInputSymbols().get(5));
		pi.getParameters().add(new Parameter("ViewProfile", Types.STRING));
		pi.getParameters().add(new Parameter("111", Types.STRING));
		System.out.println(d.execute(pi));
	}
	
	public static void main(String[] args) throws Exception{
		//testJSON();
		testCrawler();
		//testDriver();
	}

	public static void testJSON() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		m.writeValue(new File("test.json"), new Configuration());
	}
}
