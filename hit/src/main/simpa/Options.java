package main.simpa;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import automata.mealy.InputSequence;
import tools.loggers.LogManager;

public class Options {

	// General seed for SIMPA (generation of automata and algo)
	@Deprecated
	public static long SEED = new Random().nextLong();

	// Test or not

	@Deprecated
	public static boolean TEST = false;

	// Unicode characters

	public static final String SYMBOL_AND = "\u2227";
	public static final String SYMBOL_OR = "\u2228";
	public static final String SYMBOL_OMEGA_UP = "\u03a9";
	public static final String SYMBOL_OMEGA_LOW = "\u03c9";
	public static final String SYMBOL_EPSILON = "\u03b5";
	public static final String SYMBOL_SIGMA = "\u03a3";
	public static final String SYMBOL_NOT_EQUAL = "\u2260";

	@Deprecated
	public static String DIRGRAPH = "out";
	@Deprecated
	public static String DIRGRAPHSTATS = "stats" + File.separator + "graphs";
	@Deprecated
	public static String DIRARFF = "arff";
	@Deprecated
	public static String DIRTEST = "test";
	@Deprecated
	public static String DIRSTATSCSV = "stats" + File.separator + "CSV";
	@Deprecated
	public static String DIRLOG = "log";
	@Deprecated
	public static String DIRASLAN = "model";
	@Deprecated
	public static String DIRFAIL = "fails";// where to store logs of fails
											// during stats computation

	// Tools available

	public static boolean WEKA = false;
	public static boolean GRAPHVIZ = true;

	// Algorithm's options

	public static boolean SCAN = false;
	@Deprecated
	public static boolean TREEINFERENCE = false;
	public static boolean LOCALIZER_BASED_INFERENCE = false;
	@Deprecated
	public static boolean COMBINATORIALINFERENCE = false;
	@Deprecated
	public static boolean RIVESTSCHAPIREINFERENCE = false;
	public static boolean GENERICDRIVER = false;
	public static String INITIAL_INPUT_SYMBOLS = "";
	public static String INITIAL_INPUT_SEQUENCES = "";
	public static boolean INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = true;
	public static int SUPPORT_MIN = 20;
	public static boolean REUSE_OP_IFNEEDED = false;
	public static boolean FORCE_J48 = false;
	public static int STATE_NUMBER_BOUND;
	public static List<InputSequence> CHARACTERIZATION_SET;

	public static boolean RS_WITH_UNKNOWN_H = false;

	// public static boolean CHECK_INCONSISTENCY_H_NOT_HOMING = false;
	// public static boolean REUSE_HZXW = false;
	// public static boolean HW_WITH_KNOWN_W = false;
	// public static boolean ADAPTIVE_H = false;
	// public static boolean ADAPTIVE_W_SEQUENCES = false;
	// public static boolean TRY_TRACE_AS_CE = true;

	public static boolean INTERACTIVE = false; //For algorithms which can prompt user for counter example or chosen sequences
	
	// Counterexample options
	
	@Deprecated
	public static boolean STOP_ON_CE_SEARCH = false;
	@Deprecated
	public static boolean USE_SHORTEST_CE;

	//Web application analysis
	public static boolean XSS_DETECTION = false;

	@Deprecated
	public static int MAX_CE_LENGTH = 20;
	@Deprecated
	public static int MAX_CE_RESETS = 10;
	
	// Output's options

	public static LogLevel getLogLevel() {
		return SIMPA.getLogLevel();
	}
	@Deprecated
	public static LogLevel LOG_LEVEL = LogLevel.ALL;
	@Deprecated
	public static boolean LOG_TEXT = false;
	@Deprecated
	public static boolean LOG_HTML = false;
	@Deprecated
	public static boolean AUTO_OPEN_HTML = false;
	/**
	 * if greater than 0, only start and end of sequences are displayed in order
	 * to be at most this size
	 */
	public static int REDUCE_DISPLAYED_TRACES = 25;

	@Deprecated
	public static String SYSTEM = "";
	public static List<String> URLS = null;
	@Deprecated
	public static String OUTDIR = System.getProperty("user.dir");

	public static boolean useTmpLog = false;

	public static void useTmpLogDir() {
		useTmpLog = true;
	}

	public static void useNormalLogDir() {
		useTmpLog = false;
	}

	private static File getOutDir() {
		return SIMPA.getOutputsOptions().outputDir.getcompletePath();
	}

	public static File getDotDir() {
		if (useTmpLog)
			return new File(
					getOutDir().getAbsoluteFile() + File.separator + "tmpOut");
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "out");
		return dir;
	}

	public static File getStatsGraphDir() {
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "stats"
						+ File.separator + "graphs");
		return dir;
	}

	public static File getStatsCSVDir() {
		File dir = new File(getOutDir().getAbsoluteFile() + File.separator
				+ "stats" + File.separator + "CSV");
		return dir;
	}

	public static File getAslanDir() {
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "model");
		return dir;
	}

	public static File getLogDir() {
		if (useTmpLog)
			return new File(
					getOutDir().getAbsoluteFile() + File.separator + "tmpOut");
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "log");
		return dir;
	}

	public static File getXSSLogDir() {
		File dir = new File(getLogDir() + File.separator + "xss");
		return dir;
	}

	public static File getArffDir() {
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "arff");
		return dir;
	}

	public static File getSerializedObjectsDir() {
		return getDotDir();
	}

	public static File getFailDir() {
		File dir = new File(
				getOutDir().getAbsoluteFile() + File.separator + "fail");
		return dir;
	}

	/**
	 * Get the directory where articles are stored. This is used to directly
	 * export result in article working directory. Current implementation is a
	 * really simple one it should be improved to let users personalize their
	 * location of articles
	 * 
	 * @param articleID
	 *            an identifier for the article to look at.
	 * @return a directory in home called "shared_articles" if it exists or a
	 *         sub-directory in the Graph output directory.
	 * @TODO let this directory be configurable through a configuration user
	 *       file.
	 */
	public static Path getArticleDir(String articleID) {
		// fall-back directory :
		Path baseDir = getStatsGraphDir().toPath().resolve("articles");

		// try to find a directory called "shared_articles" in home
		String home = System.getProperty("user.home");
		if (home != null) {
			Path inHome = new File(home + File.separator + "shared_articles")
					.toPath();
			if (inHome.toFile().exists())
				baseDir = inHome;
		}

		// articleID is only a sub-directory in baseDir at this time. A
		// database-like might be implemented later to enable specific directory
		// per article
		Path dir = baseDir.resolve(articleID);
		dir.toFile().mkdirs();
		return dir;
	}

	// SIMPATest's options
	public static int RETEST = -1;

	@Deprecated
	public static int NBTEST = 1;
	public static int MINSTATES = 5;
	public static int MAXSTATES = 5;
	public static int TRANSITIONPERCENT = 90;
	public static int MININPUTSYM = 5;
	public static int MAXINPUTSYM = 5;
	public static int MINOUTPUTSYM = 5;
	public static int MAXOUTPUTSYM = 5;

	// EFSM
	@Deprecated
	public static int MINPARAMETER = 1;
	@Deprecated
	public static int MAXPARAMETER = 1;
	@Deprecated
	public static int DOMAINSIZE = 10;
	@Deprecated
	public static int SIMPLEGUARDPERCENT = 25;
	@Deprecated
	public static int NDVGUARDPERCENT = 25;
	@Deprecated
	public static int NDVMINTRANSTOCHECK = 1;
	@Deprecated
	public static int NDVMAXTRANSTOCHECK = 1;

	public enum LogLevel {
		ALL, DO_NOT_COMPLEXIFY, // don't log things that will take a non
								// constant-time
		LOW;
	}

	@Deprecated
	public static void LogOptions() {
		LogManager.logInfo("Seed used: " + SEED);
		LogManager.logInfo("Applicable options:");

		LogManager.logInfo("STOP_ON_CE_SEARCH = " + STOP_ON_CE_SEARCH);
		if (!STOP_ON_CE_SEARCH) {
			LogManager.logInfo("MAX_CE_LENGTH = " + MAX_CE_LENGTH);
			LogManager.logInfo("MAX_CE_RESETS = " + MAX_CE_RESETS);
		}
			LogManager.logInfo("NBTEST = " + NBTEST);
			LogManager.logInfo("MINSTATES = " + MINSTATES);
			LogManager.logInfo("MAXSTATES = " + MAXSTATES);
			LogManager.logInfo("TRANSITIONPERCENT = " + TRANSITIONPERCENT);
			LogManager.logInfo("MININPUTSYM = " + MININPUTSYM);
			LogManager.logInfo("MAXINPUTSYM = " + MAXINPUTSYM);
			LogManager.logInfo("MINOUTPUTSYM = " + MINOUTPUTSYM);
			LogManager.logInfo("MAXOUTPUTSYM = " + MAXOUTPUTSYM);
		// TODO: To be completed with options on choice of algo and EFSM
		LogManager.logLine();
	}
}
