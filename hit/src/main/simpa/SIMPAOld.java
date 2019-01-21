package main.simpa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import drivers.Driver;
import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.ScanDriver;
import learner.Learner;
import main.simpa.Options.LogLevel;
import tools.GraphViz;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;

public class SIMPAOld {
	public final static String name = SIMPAOld.class.getSimpleName();
	private static Driver driver;

	private static void init(String[] args) {
		LogManager.logConsole("Checking environment and options");

		int i = 0;
		try {
			Options.LOG_LEVEL = LogLevel.LOW;
			for (i = 0; i < args.length; i++) {
				if (args[i].equals("--reuseop"))
					Options.REUSE_OP_IFNEEDED = true;
				else if (args[i].equals("--text"))
					Options.LOG_TEXT = true;
				else if (args[i].equals("-I"))
					Options.INITIAL_INPUT_SYMBOLS = args[++i];
				else if (args[i].equals("-Z"))
					Options.INITIAL_INPUT_SEQUENCES = args[++i];
				else if (args[i].equals("--I=X"))
					Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = true;
				else if (args[i].equals("--stopatce"))
					Options.STOP_ON_CE_SEARCH = true;
				else if (args[i].equals("--maxcelength"))
					Options.MAX_CE_LENGTH = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxceresets"))
					Options.MAX_CE_RESETS = Integer.parseInt(args[++i]);
				else if (args[i].equals("--html"))
					Options.LOG_HTML = true;
				else if (args[i].equals("--openhtml"))
					Options.AUTO_OPEN_HTML = true;
				else if (args[i].equals("--forcej48"))
					Options.FORCE_J48 = true;
				else if (args[i].equals("--scan"))
					Options.SCAN = true;
				else if (args[i].equals("--minstates"))
					Options.MINSTATES = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxstates"))
					Options.MAXSTATES = Integer.parseInt(args[++i]);
				else if (args[i].equals("--transitions"))
					Options.TRANSITIONPERCENT = Integer.parseInt(args[++i]);
				else if (args[i].equals("--mininputsym"))
					Options.MININPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxinputsym"))
					Options.MAXINPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--minoutputsym"))
					Options.MINOUTPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxoutputsym"))
					Options.MAXOUTPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--urls")){
					Options.URLS = new ArrayList<String>();
					for(String url : args[++i].split(";")) Options.URLS.add(url);
				}
				else if (args[i].equals("--generic"))
					Options.GENERICDRIVER = true;
				else if (args[i].equals("--tree"))
					Options.TREEINFERENCE = true;
				else if (args[i].equals("--noReset"))
					Options.LOCALIZER_BASED_INFERENCE = true;
				else if (args[i].equals("--combinatorial"))
					Options.COMBINATORIALINFERENCE = true;
				else if (args[i].equals("--rivestSchapire"))
					Options.RIVESTSCHAPIREINFERENCE = true;
				else if (args[i].equals("--weka"))
					Options.WEKA = true;
				else if (args[i].startsWith("--supportmin"))
					Options.SUPPORT_MIN = Integer.parseInt(args[++i]);
				else if (args[i].startsWith("--outdir"))
					Options.OUTDIR = args[++i];
				else if (args[i].equals("--seed"))
					Options.SEED = Long.parseLong(args[++i]);

				else if (args[i].equals("--help") || args[i].equals("-h"))
					usage();
				else
					Options.SYSTEM = args[i];
			}

			Utils.setSeed(Options.SEED);
			if (Options.SYSTEM.isEmpty())
				usage();

			Utils.deleteDir(new File(Options.OUTDIR + Options.DIRGRAPH));
			Utils.deleteDir(new File(Options.OUTDIR + Options.DIRARFF));

		} catch (NumberFormatException e) {
			System.err.println("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}
	}

	private static void check() throws Exception {
		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".") + 1));
		if (major < 1 || minor < 5)
			throw new Exception("Java >=1.5 needed");

		if (Options.SUPPORT_MIN < 1 || Options.SUPPORT_MIN > 100)
			throw new Exception("Minimal between 1 and 100 include needed");

		if (Options.WEKA){
			try {
				Options.WEKA = weka.core.Version.MAJOR >= 3;
				if (!Options.WEKA)
					LogManager
							.logError("Warning : Weka version >= 3 needed. Please update Weka.");
			} catch (Exception e) {
				LogManager
						.logError("Warning : Unable to use Weka. Check the buildpath.");
			}
		}

		File f = new File(Options.OUTDIR);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new Exception("Unable to create/write " + f.getName());
		Options.OUTDIR = Utils.makePath(f.getAbsolutePath());

		f = new File(Options.OUTDIR + Options.DIRGRAPH);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new Exception("Unable to create/write " + f.getName());

		f = new File(Options.OUTDIR + Options.DIRARFF);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new Exception("Unable to create/write " + f.getName());

		if (Options.LOG_TEXT)
			LogManager.addLogger(new TextLogger());
		if (Options.LOG_HTML)
			LogManager.addLogger(new HTMLLogger(Options.AUTO_OPEN_HTML));
	}

	public static Driver loadDriver(String system) throws Exception {
		Driver driver;
		try {
			if (Options.GENERICDRIVER) {
				driver = new GenericDriver(system);
			}else if (Options.SCAN){
				driver = new ScanDriver(system); 
			}else {
				driver = (Driver) Class.forName(system).newInstance();
			}
			LogManager.logConsole("System : " + driver.getSystemName());
			return driver;
		} catch (InstantiationException e) {
			throw new Exception("Unable to instantiate the driver : " + system);
		} catch (IllegalAccessException e) {
			throw new Exception("Unable to access the driver : " + system);
		} catch (ClassNotFoundException e) {
			throw new Exception(
					"Unable to find the driver. Please check the system name ("
							+ system + ")");
		}
	}

	public static void launch() throws Exception {
		check();
		LogManager.start();
		Options.LogOptions();
		driver = loadDriver(Options.SYSTEM);
		Learner learner = Learner.getLearnerFor(driver);
		learner.learn();
		driver.logStats();
		LogManager.end();
	}

	public static void main(String[] args) {
		welcome();
		init(args);
		try {
			launch();
		} catch (Exception e) {
			System.err.println("Unexpected error");
			e.printStackTrace(System.err);
		}
	}

	private static void welcome() {
		System.out.println(name + " - "
				+ new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();
	}

	public static void usage() {
		System.out.println("Usage : SIMPA driverClass [Options]");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> General");
		System.out.println("    --help | -h       : Show help");
		System.out.println("    --seed NN       : Use NN as seed for random generator");
		System.out.println("> Algorithm EFSM");
		System.out
				.println("    --generic         : Use generic driver");
		System.out
				.println("    --reuseop         : Reuse output parameter for non closed row");
		System.out
				.println("    --forcej48        : Force the use of J48 algorithm instead of M5P for numeric classes");
		System.out
				.println("    --weka            : Force the use of Weka");
		System.out
		.println("    --supportmin (20) : Minimal support for relation (1-100)");
		System.out.println("> Algorithm NoReset");
		System.out
		.println("    --noReset					: Use noReset Algorithm");
		System.out.println("> Algorithm ZQ");
		System.out
				.println("    --tree            : Use tree inference (if available) instead of table");
		System.out
				.println("    -I                : Initial input symbols (a,b,c)");
		System.out
				.println("    -Z                : Initial distinguishing sequences (a-b,a-c,a-c-b)");
		System.out
				.println("    --I=X             : Initial input symbols set to X");
		System.out
				.println("    --stopatce        : Stop at counter example search");
		System.out.println("    --maxcelength"
				+ String.format("%5s", "(" + Options.MAX_CE_LENGTH + ")")
				+ ": Maximal length of random walk for counter example search");
		System.out.println("    --maxceresets"
				+ String.format("%5s", "(" + Options.MAX_CE_RESETS + ")")
				+ ": Maximal number of random walks for counter example search");
		System.out.println("> Output");
		System.out
				.println("    --outdir (.)      : Where to save arff and graph files");
		System.out.println("    --text            : Use the text logger");
		System.out.println("    --html            : Use HTML logger");
		System.out
				.println("    --openhtml        : Open HTML log automatically");
		System.out.println();
		System.out
				.println("Ex: SIMPA drivers.efsm.NSPKDriver --outdir mydir --text");
		System.out.println();
		System.exit(0);
	}
}
