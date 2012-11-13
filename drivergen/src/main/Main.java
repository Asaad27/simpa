package main;

import drivergenerator.DriverGenerator;

public class Main {
	
	public static void main(String[] args) throws Exception{
		DriverGenerator g = DriverGenerator.getDriver("WackoPicko");
		g.start();
		g.exportToDot();
	}
}
