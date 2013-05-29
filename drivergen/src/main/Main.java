package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import crawler.DriverGenerator;
import crawler.configuration.Configuration;
import crawler.driver.BookStoreDriver;
import crawler.driver.GenericDriver;
import crawler.driver.WGBypassBLDriver;
import crawler.driver.WGStoredXSSDriver;


public class Main {
	
	public static String VERSION = "1.0";
	public static String NAME = "TIC - The Inference Crawler";
	
	public static void testCrawler(){
		//DriverGenerator g = DriverGenerator.getDriver("WGStoredXSS");
		///DriverGenerator g = DriverGenerator.getDriver("WackoPicko");
		//DriverGenerator g = DriverGenerator.getDriver("BookStore");
		DriverGenerator g = DriverGenerator.getDriver("WGBypassBL");
		g.start();
		g.exportToDot();
		g.exportToXML();
	}
	
	public static void testDriver() throws IOException{
		GenericDriver d = new WGBypassBLDriver();
		System.out.println("System name   : " + d.getSystemName());
		System.out.println("Input list: ");
		int i, n = d.getInputSymbols().size();
		for(i=0; i<n; i++){
			System.out.println(d.getInputSymbols().get(i) + " " + d.inputs.get(i).getMethod() + " " + d.inputs.get(i).getAddress() + " " + d.inputs.get(i).getParams());
		}
	}
	
	public static void main(String[] args) throws Exception{
		//testJSON();
		//testCrawler();
		testDriver();
	}

	public static void testJSON() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		m.writeValue(new File("test.json"), new Configuration());
	}
}
