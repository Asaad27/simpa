package main;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import crawler.DriverGenerator;


public class Main {
	
	public static Options Options;		
	
	public static void hello(){
		System.out.println("************************************************************************");
		System.out.println("******************       Test Driver Generator       *******************");
		System.out.println("************************************************************************");
		System.out.println("");		
	}
	
	public static void check(String[] args){	
		int i=0;
		try {			
			for(i = 0; i < args.length; i++){
				if (args[i].equals("--help") || args[i].equals("-h")) usage();
				else if (args[i].equals("--css")) main.Options.CSS = true;
				else if (args[i].equals("--js")) main.Options.JS = true;
				else if (args[i].equals("--timeout")) main.Options.TIMEOUT = Integer.parseInt(args[++i]);
				else main.Options.INPUT = args[i];
			}			
			if (main.Options.INPUT.isEmpty()) usage();			
		} catch (NumberFormatException e) {
			System.err.println("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}		
	}
	
	public static void launch(){
		DriverGenerator g;
		try {
			g = new DriverGenerator(main.Options.INPUT);
			g.start();
			g.exportToDot();
			g.exportToXML();
		} catch (JsonParseException e) {
			System.err.println("Error : Unable to parse JSON file");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			System.err.println("Error : Unable to map JSON data to options");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error : File is not readable");
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) throws Exception{
		hello();
		check(args);
		try {
			launch();
		}catch (Exception e) {
			System.err.println("Unexpected error");
			e.printStackTrace(System.err);
		}
	}

	public static void usage(){
		System.out.println("Usage : TIC [options] config_file.json");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> Crawling");
		System.out.println("    --timeout 10000   : Timeout in milliseconds");
		System.out.println("    --css             : Enable CSS rendering (May slow down the crawler)");
		System.out.println("    --js              : Enable JS execution (May slow down the crawler)");
		System.out.println("> General");
		System.out.println("    --help | -h       : Show help");
		System.out.println();
		System.out.println("Example : TIC --log webgoat.json");
		System.out.println();
		System.exit(0);
	}
}
