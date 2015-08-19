package main.simpa;

import java.io.File;
import java.util.List;
import tools.Utils;
import tools.loggers.LogManager;

public class Options {

	// General seed for SIMPA (generation of automata and algo)
	public static long SEED = Utils.randLong();

	// Test or not

	public static boolean TEST = false;

	// Unicode characters

	public static final String SYMBOL_AND = "\u2227";
	public static final String SYMBOL_OR = "\u2228";
	public static final String SYMBOL_OMEGA_UP = "\u03a9";
	public static final String SYMBOL_OMEGA_LOW = "\u03c9";
	public static final String SYMBOL_EPSILON = "\u03b5";
	public static final String SYMBOL_SIGMA = "\u03a3";
	public static final String SYMBOL_NOT_EQUAL = "\u2260";

	public static String DIRGRAPH = "out";
	public static String DIRGRAPHSTATS = "stats"+File.separator+"graphs";
	public static String DIRARFF = "arff";
	public static String DIRTEST = "test";
	public static String DIRSTATSCSV = "stats"+File.separator+"CSV";
	public static String DIRLOG = "log";
	public static String DIRASLAN = "model";
	public static String DIRFAIL = "fails";//where to store logs of fails during stats computation 

	// Tools available

	public static boolean WEKA = false;
	public static boolean GRAPHVIZ = true;

	// Algorithm's options

	public static boolean SCAN = false;
	public static boolean TREEINFERENCE = false;
	public static boolean LMINFERENCE = false;
	public static boolean NORESETINFERENCE = false;
	public static boolean COMBINATORIALINFERENCE = false;
	public static boolean RIVESTSCHAPIREINFERENCE = false;
	public static boolean GENERICDRIVER = false;
	public static String INITIAL_INPUT_SYMBOLS = "";
	public static String INITIAL_INPUT_SEQUENCES = "";
	public static boolean INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = true;
	public static int SUPPORT_MIN = 20;
	public static boolean REUSE_OP_IFNEEDED = false;
	public static boolean FORCE_J48 = false;
	
	// Counterexample options
	
	public static boolean STOP_ON_CE_SEARCH = false;

	//Web application analysis
	public static boolean XSS_DETECTION = false;

	public static int MAX_CE_LENGTH = 20;
	public static int MAX_CE_RESETS = 10;
	
	// Output's options

	public static LogLevel LOG_LEVEL = LogLevel.ALL;
	public static boolean LOG_TEXT = false;
	public static boolean LOG_HTML = false;
	public static boolean AUTO_OPEN_HTML = false;

	public static String SYSTEM = "";
	public static List<String> URLS = null;
	public static String OUTDIR = System.getProperty("user.dir");

	// SIMPATest's options
	public static int RETEST = -1;

	public static int NBTEST = 1;
	public static int MINSTATES = 5;
	public static int MAXSTATES = 5;
	public static int TRANSITIONPERCENT = 90;
	public static int MININPUTSYM = 5;
	public static int MAXINPUTSYM = 5;
	public static int MINOUTPUTSYM = 5;
	public static int MAXOUTPUTSYM = 5;
	
	// EFSM
	public static int MINPARAMETER = 1;
	public static int MAXPARAMETER = 1;
	public static int DOMAINSIZE = 10;
	public static int SIMPLEGUARDPERCENT = 25;
	public static int NDVGUARDPERCENT = 25;
	public static int NDVMINTRANSTOCHECK = 1;
	public static int NDVMAXTRANSTOCHECK = 1;
	
	public enum LogLevel{
		ALL,
		DO_NOT_COMPLEXIFY,//don't log things that will take a non constant-time
		LOW;
	}
	public static void LogOptions (){
		LogManager.logInfo("Seed used: " + SEED);
		LogManager.logInfo("Applicable options:");
		
		LogManager.logInfo("STOP_ON_CE_SEARCH = " + STOP_ON_CE_SEARCH);
		if (!STOP_ON_CE_SEARCH) {
			LogManager.logInfo("MAX_CE_LENGTH = " + MAX_CE_LENGTH);
			LogManager.logInfo("MAX_CE_RESETS = " + MAX_CE_RESETS);
		}
		if(TEST) {
			LogManager.logInfo("NBTEST = " + NBTEST);
			LogManager.logInfo("MINSTATES = " + MINSTATES);
			LogManager.logInfo("MAXSTATES = " + MAXSTATES);
			LogManager.logInfo("TRANSITIONPERCENT = " + TRANSITIONPERCENT);
			LogManager.logInfo("MININPUTSYM = " + MININPUTSYM);
			LogManager.logInfo("MAXINPUTSYM = " + MAXINPUTSYM);
			LogManager.logInfo("MINOUTPUTSYM = " + MINOUTPUTSYM);
			LogManager.logInfo("MAXOUTPUTSYM = " + MAXOUTPUTSYM);
			LogManager.logInfo("TEST = " + TEST);
			LogManager.logInfo("TEST = " + TEST);
		}
		// TODO: To be completed with options on choice of algo and EFSM
		LogManager.logLine();
		}
	}

