package main;

import drivergenerator.DriverGenerator;

public class Main {
	
	public static String VERSION = "1.0";
	public static String NAME = "TIC - The Inference Crawler";
	
	public static void main(String[] args) throws Exception{
		DriverGenerator g = DriverGenerator.getDriver("WGStoredXSS");
		g.start();
		g.exportToDot();
		//g.exportToXML();
	}
}
