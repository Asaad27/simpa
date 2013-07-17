package main;

import tools.GraphViz;

public class Options {
	public static final String VERSION = "1.0";
	public static final String NAME = "TIC";
	public static boolean CSS = false;
	public static boolean JS = false;
	public static int TIMEOUT = 10000;
	
	public static boolean GRAPHVIZ = GraphViz.check() == 0;
	public static String OUTDIR = System.getProperty("user.dir");
	public static boolean LOG = false;
	public static boolean OPEN_LOG = false;
	
	public static String INPUT = "";

}
