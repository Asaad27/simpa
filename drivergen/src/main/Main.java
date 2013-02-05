package main;

import java.io.IOException;

import drivergenerator.DriverGenerator;
import drivergenerator.driver.GenericDriver;
import drivergenerator.driver.WGStoredXSSDriver;

public class Main {
	
	public static String VERSION = "1.0";
	public static String NAME = "TIC - The Inference Crawler";
	
	public static void testCrawler(){
		DriverGenerator g = DriverGenerator.getDriver("BookStore");
		g.start();
		g.exportToDot();
		g.exportToXML();
	}
	
	public static void testDriver() throws IOException{
		GenericDriver d = new WGStoredXSSDriver("abs//WebGoat_Stored_XSS.xml");
		System.out.println("System name   : " + d.getSystemName());
		System.out.println("Input symbols : " + d.getInputSymbols().size());
		System.out.println("Output symbols : " + d.getOutputSymbols().size());
	}
	
	public static void main(String[] args) throws Exception{
		testCrawler();
		//testDriver();
	}
}
