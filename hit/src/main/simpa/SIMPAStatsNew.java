package main.simpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import learner.Learner;
import learner.mealy.noReset.NoResetLearner;
import main.simpa.Options.LogLevel;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import tools.GraphViz;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;
import drivers.Driver;

public class SIMPAStatsNew {
	public final static String name = "SIMPA Test Mealy";

	private static boolean graphGeneration = false;

	private static void init(String[] args) {
		if (!Options.STAT)
			System.out.println("[+] Reading arguments");

		int i = 0;
		try {
			for (i = 0; i < args.length; i++) {
				if (args[i].equals("--nbtest"))
					Options.NBTEST = Integer.parseInt(args[++i]);
				else if (args[i].equals("--tree"))
					Options.TREEINFERENCE = true;
				else if (args[i].equals("--combinatorial"))
					Options.COMBINATORIALINFERENCE = true;
				else if (args[i].equals("--noReset"))
					Options.NORESETINFERENCE = true;
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
				else if (args[i].equals("-I"))
					Options.INITIAL_INPUT_SYMBOLS = args[++i];
				else if (args[i].equals("-Z"))
					Options.INITIAL_INPUT_SEQUENCES = args[++i];
				else if (args[i].equals("-I=X"))
					Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = true;

				else if (args[i].equals("--retest"))
					Options.RETEST = Integer.parseInt(args[++i]);

				else if (args[i].equals("--text"))
					Options.LOG_TEXT = true;
				else if (args[i].equals("--html"))
					Options.LOG_HTML = true;
				else if (args[i].equals("--openhtml"))
					Options.AUTO_OPEN_HTML = true;
				else if (args[i].startsWith("--outdir"))
					Options.OUTDIR = args[++i];

				else if (args[i].startsWith("--makeGraph"))
					graphGeneration = true;

				else if (args[i].equals("--help") || args[i].equals("-h"))
					usage();
				else
					Options.SYSTEM = args[i];
			}

		} catch (NumberFormatException e) {
			LogManager.logError("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		int i = 0;
		Driver driver = null;
		welcome();
		Options.TEST = false;
		init(args);
		try {
			check();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		String baseDir = Options.OUTDIR;
		File f = new File(baseDir + File.separator + Options.DIRSTATS);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		String statsDir = Utils.makePath(f.getAbsolutePath());

		f = new File(baseDir + File.separator + Options.DIRTEST);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		String logDir = Utils.makePath(f.getAbsolutePath());

		if (Options.NBTEST > 0)
			Utils.cleanDir(new File(logDir));

		Options.OUTDIR = logDir;
		System.out.println("[+] Testing " + Options.NBTEST
				+ " automaton");
		StatsSet stats = new StatsSet();
		Options.LOG_LEVEL = LogLevel.LOW;
		
		for (i = 1; i <= Options.NBTEST; i++) {
			Utils.createDir(new File(Options.OUTDIR));
			System.out.println("\t" + i + "/" + Options.NBTEST);
			try {
				if (Options.LOG_HTML)
					LogManager.addLogger(new HTMLLogger());
				if (Options.LOG_TEXT)
					LogManager.addLogger(new TextLogger());
				LogManager.start();
				driver = SIMPA.loadDriver(Options.SYSTEM);
				Learner gl = Learner.getLearnerFor(driver);
				assert gl instanceof NoResetLearner;
				NoResetLearner l = (NoResetLearner) gl;
				l.learn();
				driver.logStats();

				StatsEntry learnerStats = l.getStats();
				stats.add(learnerStats);

				File globalStats = new File(statsDir+File.separator+learnerStats.getClass().getName()+".csv");
				Writer globalStatsWriter;
				if (!globalStats.exists()){
					globalStats.createNewFile();
					globalStatsWriter = new BufferedWriter(new FileWriter(globalStats));
					globalStatsWriter.append(learnerStats.getCSVHeader() + "\n");
				}else{
					globalStatsWriter = new BufferedWriter(new FileWriter(globalStats,true));
				}


				globalStatsWriter.append(learnerStats.toCSV() + "\n");
				globalStatsWriter.close();
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				LogManager.end();
			}
			
		}

		if (graphGeneration){
			System.out.println("[+] Make Graph");
			for (File statFile : new File(statsDir).listFiles()){
				System.out.println("\tmaking graph for "+statFile.getName());
				Options.OUTDIR = baseDir + File.separator + "out" + File.separator + statFile.getName();
				Utils.cleanDir(new File(Options.OUTDIR));
				stats = new StatsSet(statFile);
				GraphGenerator gen = stats.get(0).getDefaultsGraphGenerator();
				gen.generate(stats);
			}
		}
		System.out.println("[+] End");
	}

	private static void welcome() {
		System.out.println(name + " - "
				+ new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();
	}

	private static void check() throws Exception {
		if (!Options.STAT)
			System.out.println("[+] Checking environment and options");

		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".") + 1));
		if (major < 1 || minor < 5)
			throw new Exception("Java >=1.5 needed");

		Options.GRAPHVIZ = true;
		if (GraphViz.check() != 0 && !Options.TEST) {
			Options.GRAPHVIZ = false;
			LogManager
			.logError("Warning: Unable to find GraphViz and converting dot to image files");
		}

		if (Options.NBTEST < 0)
			throw new Exception("Number of test >= 0 needed");
		if (Options.MINSTATES < 1)
			throw new Exception("Minimal number of states >= 1 needed");
		if (Options.MAXSTATES < Options.MINSTATES)
			throw new Exception(
					"Maximal number of states > Minimal number of states needed");
		if (Options.TRANSITIONPERCENT < 0 || Options.TRANSITIONPERCENT > 100)
			throw new Exception(
					"Percent of transition between 0 and 100 needed");
		if (Options.MININPUTSYM < 1)
			throw new Exception("Minimal number of input symbols >= 1 needed");
		if (Options.MININPUTSYM > Options.MAXINPUTSYM)
			throw new Exception(
					"Minimal number of input symbols <= Maximal number of input symbols needed");
		if (Options.MINOUTPUTSYM < 1)
			throw new Exception("Minimal number of output symbols >= 1 needed");
		if (Options.MINOUTPUTSYM > Options.MAXOUTPUTSYM)
			throw new Exception(
					"Minimal number of output symbols < Maximal number of output symbols needed");

	}

	public static void usage() {
		System.out.println("Usage : KITestMealy [Options]");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> General");
		System.out.println("    --help | -h            : Show help");
		System.out
		.println("    --retest X             : Load and test the random EFSM numner X");
		System.out.println("> Algorithm");
		System.out
		.println("    --tree                 : Use tree inference instead of table");
		System.out.println("    -I                "
				+ String.format("%4s", "(" + Options.SYMBOL_EPSILON + ")")
				+ " : Initial inputs symbols");
		System.out.println("    -Z                "
				+ String.format("%4s", "(" + Options.SYMBOL_EPSILON + ")")
				+ " : Initial Z");
		System.out
		.println("    --I=X                  : Initial input symbols set to X");
		System.out.println("> Test");
		System.out.println("    --nbtest          "
				+ String.format("%4s", "(" + Options.NBTEST + ")")
				+ " : Number of tests");
		System.out.println("    --minstates       "
				+ String.format("%4s", "(" + Options.MINSTATES + ")")
				+ " : Minimal number of states");
		System.out.println("    --maxstates       "
				+ String.format("%4s", "(" + Options.MAXSTATES + ")")
				+ " : Maximal number of states");
		System.out.println("    --mininputsym     "
				+ String.format("%4s", "(" + Options.MININPUTSYM + ")")
				+ " : Minimal number of input symbols");
		System.out.println("    --maxinputsym     "
				+ String.format("%4s", "(" + Options.MAXINPUTSYM + ")")
				+ " : Maximal number of input symbols");
		System.out.println("    --minoutputsym    "
				+ String.format("%4s", "(" + Options.MINOUTPUTSYM + ")")
				+ " : Minimal number of output symbols");
		System.out.println("    --maxoutputsym    "
				+ String.format("%4s", "(" + Options.MAXOUTPUTSYM + ")")
				+ " : Maximal number of output symbols");

		System.exit(0);
	}
}
