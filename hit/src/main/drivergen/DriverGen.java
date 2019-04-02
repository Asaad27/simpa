/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 ********************************************************************************/
package main.drivergen;

import crawler.DriverGenerator;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import crawler.DriverGeneratorBFS;


public class DriverGen {
	
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
				else if (args[i].equals("--css")) main.drivergen.Options.CSS = true;
				else if (args[i].equals("--js")) main.drivergen.Options.JS = true;
				else if (args[i].equals("--timeout")) main.drivergen.Options.TIMEOUT = Integer.parseInt(args[++i]);
				else if (args[i].equals("--limit")) main.drivergen.Options.LIMIT_TIME = Integer.parseInt(args[++i])*1000;
				else main.drivergen.Options.INPUT = args[i];
			}			
			if (main.drivergen.Options.INPUT.isEmpty()) usage();			
		} catch (NumberFormatException e) {
			System.err.println("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}		
	}
	
	public static void launch(){
		DriverGenerator g;
		try {
			g = new DriverGeneratorBFS(main.drivergen.Options.INPUT);
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
		System.out.println("Usage : "+DriverGen.class.getSimpleName()+" [options] config_file.json");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> Crawling");
		System.out.println("    --timeout 10000   : Request timeout in milliseconds");
		System.out.println("    --limit			  : Crawling limit time in seconds");
		System.out.println("    --css             : Enable CSS rendering (May slow down the crawler)");
		System.out.println("    --js              : Enable JS execution (May slow down the crawler)");
		System.out.println("> General");
		System.out.println("    --help | -h       : Show help");
		System.out.println();
		System.out.println("Example : "+DriverGen.class.getSimpleName()+" webgoat.json");
		System.out.println();
		System.exit(0);
	}
}
