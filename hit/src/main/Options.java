package main;

public class Options {
	
	// Test or not
	
	public static boolean TEST = false;
	public static boolean STAT = false;
	
	// Unicode characters
	
	public static final String SYMBOL_AND = "\u2227";
	public static final String SYMBOL_OR = "\u2228";
	public static final String SYMBOL_OMEGA_UP = "\u03a9";
	public static final String SYMBOL_OMEGA_LOW = "\u03c9";
	public static final String SYMBOL_EPSILON = "\u03b5";
	public static final String SYMBOL_SIGMA = "\u03a3";
	public static final String SYMBOL_NOT_EQUAL = "\u2260";

	public static final String DIRGRAPH = "out";
	public static final String DIRARFF = "arff";
	public static final String DIRTEST = "test";
	public static final String DIRLOG = "log";
	public static final String DIRASLAN = "model";
	
	// Tools available
	
	public static boolean WEKA = true;
	public static boolean GRAPHVIZ = true;
	
	// Algorithm's options
	
	public static boolean TREEINFERENCE = false;
	public static int SUPPORT_MIN = 20;
	public static boolean REUSE_OP_IFNEEDED = false;
	public static boolean FORCE_J48 = false;
	
	// Output's options 
		
	public static boolean LOG_TEXT = false;
	public static boolean LOG_HTML = false;
	public static boolean AUTO_OPEN_HTML = false;
	
	public static String SYSTEM = "";
	public static String OUTDIR = System.getProperty("user.dir");
	
	// Kitest's options
	public static int RETEST = -1;
	
	public static int NBTEST = 50;
	public static int MINSTATES = 10;
	public static int MAXSTATES = 10;
	public static int TRANSITIONPERCENT = 25;
	public static int MININPUTSYM = 3;
	public static int MAXINPUTSYM = 3;
	public static int MINOUTPUTSYM = 3;
	public static int MAXOUTPUTSYM = 3;
	public static int MINPARAMETER = 1;
	public static int MAXPARAMETER = 1;
	public static int DOMAINSIZE = 10;
	public static int SIMPLEGUARDPERCENT = 25;
	public static int NDVGUARDPERCENT = 25;
	public static int NDVMINTRANSTOCHECK = 1;
	public static int NDVMAXTRANSTOCHECK = 1;
}
