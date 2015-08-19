package main.simpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import drivers.Driver;
import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.ScanDriver;
import learner.Learner;
import main.simpa.Options.LogLevel;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import tools.GraphViz;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;

abstract class Option<T> {
	protected String consoleName;
	protected String description;
	protected T defaultValue;
	protected T value;
	protected boolean needed = true;

	public Option(String consoleName, String description, T defaultValue){
		assert consoleName.startsWith("-");
		this.consoleName = consoleName;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public void parse(String[] args, ArrayList<Boolean> used){
		preCheck(args,used);
		parseInternal(args,used);
		postCheck(args);
	}

	protected void preCheck(String[] args, ArrayList<Boolean> used){
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)){
				if (used.get(i))
					LogManager.logError("argument '" + consoleName + "' already used. Are you sur of your syntax for " + args[i-1] + " ?");
			}
	}

	protected void postCheck(String[] args){
		int found=0;
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName))
				found ++;
		if (found > 1){
			LogManager.logError("argument '" + consoleName + "' appears at least two times. used value " + getValue());
		}

		if (needed && getValue() == null){
			LogManager.logError("argument '" + consoleName + "' missing");
		}
	}

	public abstract void parseInternal(String[] args, ArrayList<Boolean> used);

	public String getConsoleName(){
		return consoleName;
	}

	public String getDescription() {
		return description;
	}

	public T getValue() {
		return (value == null) ? defaultValue : value;
	}

	public T getDefaultValue() {
		return defaultValue;
	}
}

class IntegerOption extends Option<Integer> {
	public IntegerOption(String consoleName, String description, Integer defaultValue) {
		super(consoleName, description, defaultValue);
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)){
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				try{
					value = new Integer(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("Error parsing argument '" + args[i] + "' for " + consoleName);
					throw e;
				}
			}
	}
}

class StringOption extends Option<String> {
	public StringOption(String consoleName, String description, String defaultValue) {
		super(consoleName, description, defaultValue);
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)){
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				value = args[i];
			}
	}
}

/**
 * append strings to a list
 */
class StringListOption extends Option<ArrayList<String>> {
	public StringListOption(String consoleName, String description, List<String> URLS) {
		super(consoleName, description, (URLS == null) ? null : new ArrayList<String>(URLS));
		value = null;
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)){
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				if (value == null)
					value = new ArrayList<String>();
				for (String s : args[i].split(";"))
					value.add(s);
			}
	}

	protected void postCheck(String[] args, ArrayList<Boolean> used){
		if (needed && getValue() == null){
			System.err.println("argument '" + consoleName + "' missing");
		}
	}
}

class BooleanOption extends Option<Boolean> {
	public BooleanOption(String consoleName, String description) {
		super(consoleName, description, false);
		assert defaultValue != null;
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)){
				used.set(i, true);
				value = !defaultValue;
			}
	}

	public Boolean getDefaultValue() {
		return null;
	}
}

class HelpOption extends Option<Object>{
	public HelpOption() {
		super("--help | -h", "Show help", null);
		needed = false;
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-h") || args[i].equals("--help")){
				used.set(i, true);
				System.out.println("you don't know how it's work ?");
				SIMPA.usage();
			}
		//TODO add a more detailed help for each commands.
	}

	protected void postCheck(String[] args, ArrayList<Boolean> used){}
	protected void preCheck(String[] args, ArrayList<Boolean> used){}
}

public class SIMPA {
	public final static String name = SIMPA.class.getSimpleName();
	private static Driver driver;
	public static final boolean DEFENSIVE_CODE = true;
	
	//General Options
	private static HelpOption help = new HelpOption();
	private static IntegerOption SEED = new IntegerOption("--seed", "Use NN as seed for random generator", null);
	private static Option<?>[] generalOptions = new Option<?>[]{help,SEED};

	//output options
	private static BooleanOption LOG_HTML = new BooleanOption("--html", "Use HTML logger");
	private static BooleanOption LOG_TEXT = new BooleanOption("--text", "Use the text logger");
	private static BooleanOption AUTO_OPEN_HTML = new BooleanOption("--openhtml", "Open HTML log automatically");
	private static StringOption OUTDIR = new StringOption("--outdir", "Where to save arff and graph files",Options.OUTDIR);
	private static Option<?>[] outputOptions = new Option<?>[]{LOG_HTML,LOG_TEXT,AUTO_OPEN_HTML,OUTDIR};

	//inference choice
	private static BooleanOption TREE_INFERENCE = new BooleanOption("--tree", "Use tree inference (if available) instead of table");
	private static BooleanOption LM_INFERENCE = new BooleanOption("--lm", "Use lm inference");
	private static BooleanOption NORESET_INFERENCE = new BooleanOption("--noReset", "use noReset Algorithm");
	private static BooleanOption COMBINATORIAL_INFERENCE = new BooleanOption("--combinatorial", "use the combinatorial inference");
	private static BooleanOption RIVETSCHAPIRE_INFERENCE = new BooleanOption("--rivestSchapire", "use the RivestSchapire inference (must be used with an other learner)\nThis option let you to run an inference algorithm with resets on a driver without reset.");
	private static Option<?>[] inferenceChoiceOptions = new Option<?>[]{TREE_INFERENCE,LM_INFERENCE,NORESET_INFERENCE,COMBINATORIAL_INFERENCE,RIVETSCHAPIRE_INFERENCE};

	//ZQ options
	private static BooleanOption STOP_AT_CE_SEARCH = new BooleanOption("--stopatce", "Stop inference when a counter exemple is asked");
	private static IntegerOption MAX_CE_LENGTH = new IntegerOption("--maxcelength", "Maximal length of random walk for counter example search", Options.MAX_CE_LENGTH);
	private static IntegerOption MAX_CE_RESETS = new IntegerOption("--maxceresets", "Maximal number of random walks for counter example search", Options.MAX_CE_RESETS);
	private static StringOption INITIAL_INPUT_SYMBOLS = new StringOption("-I","Initial input symbols (a,b,c)",Options.INITIAL_INPUT_SYMBOLS);
	private static StringOption INITIAL_INPUT_SEQUENCES = new StringOption("-Z","Initial distinguishing sequences (a-b,a-c,a-c-b))",Options.INITIAL_INPUT_SEQUENCES);
	private static BooleanOption INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = new BooleanOption("-I=X", "Initial input symbols set to X");
	private static Option<?>[] ZQOptions = new Option<?>[]{MAX_CE_LENGTH,MAX_CE_RESETS,INITIAL_INPUT_SYMBOLS,INITIAL_INPUT_SEQUENCES,INITIAL_INPUT_SYMBOLS_EQUALS_TO_X};

	//EFSM options
	private static BooleanOption GENERIC_DRIVER = new BooleanOption("--generic", "Use generic driver");
	private static BooleanOption SCAN = new BooleanOption("--scan", "Use scan driver");
	private static BooleanOption REUSE_OP_IFNEEDED = new BooleanOption("--reuseop", "Reuse output parameter for non closed row");
	private static BooleanOption FORCE_J48 = new BooleanOption("--forcej48", "Force the use of J48 algorithm instead of M5P for numeric classes");
	private static BooleanOption WEKA = new BooleanOption("--weka", "Force the use of weka");
	private static IntegerOption SUPPORT_MIN = new IntegerOption("--supportmin", "Minimal support for relation (1-100)", Options.SUPPORT_MIN);
	private static Option<?>[] EFSMOptions = new Option<?>[]{GENERIC_DRIVER,SCAN,REUSE_OP_IFNEEDED,FORCE_J48,WEKA,SUPPORT_MIN};
	
	//TestEFSM options //TODO group with Random generrator ?
	private static IntegerOption MIN_PARAMETER = new IntegerOption("--minparameter", "Minimal number of parameter by symbol", Options.MINPARAMETER);
	private static IntegerOption MAX_PARAMETER = new IntegerOption("--maxparameter", "Maximal number of parameter by symbol", Options.MAXPARAMETER);
	private static IntegerOption DOMAIN_SIZE = new IntegerOption("--domainsize", "Size of the parameter's domain", Options.DOMAINSIZE);
	private static IntegerOption SIMPLE_GUARD_PERCENT = new IntegerOption("--simpleguard", "% of simple guard by transitions", Options.SIMPLEGUARDPERCENT);
	private static IntegerOption NDV_GUARD_PERCENT = new IntegerOption("--ndvguard", "% of generating NDV by transitions", Options.NDVGUARDPERCENT);
	private static IntegerOption NDV_MIN_TRANS = new IntegerOption("--ndvmintrans", "Minimum number of states before checking NDV value", Options.NDVMINTRANSTOCHECK);
	private static IntegerOption NDV_MAX_TRANS = new IntegerOption("--ndvmaxtrans", "Maximum number of states before checking NDV value", Options.NDVMAXTRANSTOCHECK);
	private static Option<?>[] testEFSMOptions = new Option<?>[]{MIN_PARAMETER,MAX_PARAMETER,DOMAIN_SIZE,SIMPLE_GUARD_PERCENT,NDV_GUARD_PERCENT,NDV_MIN_TRANS,NDV_MAX_TRANS};


	//Random driver options
	private static IntegerOption MIN_STATE = new IntegerOption("--minstates", "Minimal number of states for random automatas", Options.MINSTATES);
	private static IntegerOption MAX_STATE = new IntegerOption("--maxstates", "Maximal number of states for random automatas", Options.MAXSTATES);
	private static IntegerOption MIN_INPUT_SYM = new IntegerOption("--mininputsym", "Minimal number of input symbols for random automatas", Options.MININPUTSYM);
	private static IntegerOption MAX_INPUT_SYM = new IntegerOption("--maxinputsym", "Maximal number of input symbols for random automatas", Options.MAXINPUTSYM);
	private static IntegerOption MIN_OUTPUT_SYM = new IntegerOption("--minoutputsym", "Minimal number of output symbols for random automatas\nThat is the minimal number used for output symbol genration but it is possible that less symbols are used", Options.MINOUTPUTSYM);
	private static IntegerOption MAX_OUTPUT_SYM = new IntegerOption("--maxoutputsym", "Maximal number of output symbols for random automatas", Options.MAXOUTPUTSYM);
	private static IntegerOption TRANSITION_PERCENT = new IntegerOption("--transitions", "percentage of loop in random automatas\nSome other loop may be generated randomly so it's a minimal percentage", Options.TRANSITIONPERCENT);
	private static BooleanOption XSS_DETECTION = new BooleanOption("--xss", "Detect XSS vulnerability");
	private static Option<?>[] randomAutomataOptions = new Option<?>[]{MIN_STATE,MAX_STATE,MIN_INPUT_SYM,MAX_INPUT_SYM,MIN_OUTPUT_SYM,MAX_OUTPUT_SYM,TRANSITION_PERCENT,XSS_DETECTION};

	//stats options
	private static IntegerOption NB_TEST = new IntegerOption("--nbtest","number of execution of the algorithm",Options.NBTEST);
	private static BooleanOption MAKE_GRAPH = new BooleanOption("--makeGraph","create graph based on csv files");
	private static BooleanOption STATS_MODE = new BooleanOption("--stats","enable stats mode\n - save results to csv\n - disable some feature");
	private static Option<?>[] statsOptions = new Option<?>[]{NB_TEST,MAKE_GRAPH,STATS_MODE};

	//Other options undocumented //TODO sort and explain them.
	private static StringListOption URLS = new StringListOption("--urls", "??? TODO\n  (format '--urls url1;url2' or '--urls url1 --urls url2')", Options.URLS);
	private static Option<?>[] otherOptions = new Option<?>[]{URLS};



	private static void parse(String[] args, ArrayList<Boolean> used, Option<?>[] options){
		for (Option<?> o : options)
			o.parse(args, used);
	}


	private static void parseArguments(String[] args) {
		LogManager.logConsole("Checking environment and options");

		ArrayList<Boolean> used = new ArrayList<>();
		for (int j = 0; j < args.length; j++)
			used.add(false);

		parse(args,used,generalOptions);
		Options.SEED = (SEED.getValue() != null) ? SEED.getValue() : Utils.randLong();
		Utils.setSeed(Options.SEED);


		parse(args,used,new Option<?>[]{LOG_HTML,LOG_TEXT,AUTO_OPEN_HTML,OUTDIR,NORESET_INFERENCE,TREE_INFERENCE,COMBINATORIAL_INFERENCE,RIVETSCHAPIRE_INFERENCE});
		if (TREE_INFERENCE.getValue() && NORESET_INFERENCE.getValue()){
			System.out.println("You cannot choose two inference system");
			usage();
		}
		if (TREE_INFERENCE.getValue() || LM_INFERENCE.getValue()){
			parse(args,used,ZQOptions);
		}

		parse(args,used,EFSMOptions);
		parse(args,used,randomAutomataOptions);

		parse(args,used,statsOptions);


		//TODO check those options and put them in the right place
		parse(args,used,otherOptions);

		//check for unused arguments and select the driver
		int unusedArgs = 0;
		for (int j=0; j < args.length; j++)
			if (!used.get(j)){
				if (args[j].startsWith("-")){
					System.err.println("the argument '" + args[j] + "' is not interpreted");
				}
				unusedArgs++;
				Options.SYSTEM = args[j];
			}
		if (unusedArgs < 1 && NB_TEST.getValue() > 0){
			System.err.println("please specify the driverClass");
			usage();
		}
		if (unusedArgs > 1){
			System.err.println("please specify only one driverClass");
			usage();
		}






		Options.LOG_HTML = LOG_HTML.getValue();
		Options.LOG_TEXT = LOG_TEXT.getValue();
		Options.AUTO_OPEN_HTML = AUTO_OPEN_HTML.getValue();
		Options.OUTDIR = OUTDIR.getValue();

		Options.LMINFERENCE = LM_INFERENCE.getValue();
		Options.TREEINFERENCE = TREE_INFERENCE.getValue();
		Options.NORESETINFERENCE = NORESET_INFERENCE.getValue();
		Options.COMBINATORIALINFERENCE = COMBINATORIAL_INFERENCE.getValue();
		Options.RIVESTSCHAPIREINFERENCE = RIVETSCHAPIRE_INFERENCE.getValue();

		Options.STOP_ON_CE_SEARCH = STOP_AT_CE_SEARCH.getValue();
		Options.MAX_CE_LENGTH = MAX_CE_LENGTH.getValue();
		Options.MAX_CE_RESETS = MAX_CE_RESETS.getValue();
		Options.INITIAL_INPUT_SYMBOLS = INITIAL_INPUT_SYMBOLS.getValue();
		Options.INITIAL_INPUT_SEQUENCES = INITIAL_INPUT_SEQUENCES.getValue();
		Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = INITIAL_INPUT_SYMBOLS_EQUALS_TO_X.getValue();

		Options.GENERICDRIVER = GENERIC_DRIVER.getValue();
		Options.REUSE_OP_IFNEEDED = REUSE_OP_IFNEEDED.getValue();
		Options.FORCE_J48 = FORCE_J48.getValue();
		Options.WEKA = WEKA.getValue();
		Options.SUPPORT_MIN = SUPPORT_MIN.getValue();

		Options.MINPARAMETER = MIN_PARAMETER.getValue();
		Options.MAXPARAMETER = MAX_PARAMETER.getValue();
		Options.DOMAINSIZE = DOMAIN_SIZE.getValue();
		Options.SIMPLEGUARDPERCENT = SIMPLE_GUARD_PERCENT.getValue();
		Options.NDVGUARDPERCENT = NDV_GUARD_PERCENT.getValue();
		Options.NDVMINTRANSTOCHECK = NDV_MIN_TRANS.getValue();
		Options.NDVMAXTRANSTOCHECK = NDV_MAX_TRANS.getValue();

		Options.MINSTATES = MIN_STATE.getValue();
		Options.MAXSTATES = MAX_STATE.getValue();
		Options.MININPUTSYM = MIN_INPUT_SYM.getValue();
		Options.MAXINPUTSYM = MAX_INPUT_SYM.getValue();
		Options.MINOUTPUTSYM = MIN_OUTPUT_SYM.getValue();
		Options.MAXOUTPUTSYM = MAX_OUTPUT_SYM.getValue();
		Options.TRANSITIONPERCENT = TRANSITION_PERCENT.getValue();
		Options.XSS_DETECTION = XSS_DETECTION.getValue();

		Options.NBTEST = NB_TEST.getValue();

		Options.URLS = URLS.getValue();
		Options.SCAN = SCAN.getValue();
	}

	private static void check() {
		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".") + 1));
		if (major < 1 || minor < 5)
			throw new RuntimeException("Java >=1.5 needed");

		if (Options.SUPPORT_MIN < 1 || Options.SUPPORT_MIN > 100)
			throw new RuntimeException("Minimal between 1 and 100 include needed");

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

		if (GraphViz.check() != 0) {
			Options.GRAPHVIZ = false;
			LogManager
			.logError("Warning : Unable to find GraphViz dot. Check your environment.");
		}

		File f = new File(Options.OUTDIR);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		Options.OUTDIR = Utils.makePath(f.getAbsolutePath());
		Utils.deleteDir(new File(Options.OUTDIR + Options.DIRGRAPH));
		Utils.deleteDir(new File(Options.OUTDIR + Options.DIRARFF));

		f = new File(Options.OUTDIR + Options.DIRGRAPH);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());

		f = new File(Options.OUTDIR + Options.DIRARFF);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());

		if (STATS_MODE.getValue()){
			boolean assert_test=false;
			assert (assert_test = true);
			if (assert_test){
				System.out.println("you're about to make stats with active assertions."+
						"This can make wrong results for duration stats because some assertions may have a big computation duration.\n"+
						"Do you want to continue ? [y/n]");
				while (true){
					Scanner input = new Scanner(System.in);
					String answer = input.next();
					if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")){
						input.close();
						break;
					}
					if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")){
						input.close();
						System.exit(0);
					}
					System.out.println("what did you say ?");
				}
			}
		}

		if (Options.NBTEST < 0)
			throw new RuntimeException("Number of test >= 0 needed");


		if (Options.MINSTATES < 1)
			throw new RuntimeException("Minimal number of states >= 1 needed");
		if (Options.MAXSTATES < Options.MINSTATES)
			throw new RuntimeException(
					"Maximal number of states > Minimal number of states needed");
		if (Options.TRANSITIONPERCENT < 0 || Options.TRANSITIONPERCENT > 100)
			throw new RuntimeException(
					"Percent of transition between 0 and 100 needed");
		if (Options.MININPUTSYM < 1)
			throw new RuntimeException("Minimal number of input symbols >= 1 needed");
		if (Options.MININPUTSYM > Options.MAXINPUTSYM)
			throw new RuntimeException(
					"Minimal number of input symbols <= Maximal number of input symbols needed");
		if (Options.MINOUTPUTSYM < 1)
			throw new RuntimeException("Minimal number of output symbols >= 1 needed");
		if (Options.MINOUTPUTSYM > Options.MAXOUTPUTSYM)
			throw new RuntimeException(
					"Minimal number of output symbols < Maximal number of output symbols needed");



	}

	public static Driver loadDriver(String system) throws Exception {
		Driver driver;
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
			usage();
			throw e;
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

	protected static Learner learnOneTime() throws Exception{
		if (Options.LOG_TEXT)
			LogManager.addLogger(new TextLogger());
		if (Options.LOG_HTML)
			LogManager.addLogger(new HTMLLogger());
		LogManager.start();
		Options.LogOptions();
		driver = loadDriver(Options.SYSTEM);
		Learner learner = Learner.getLearnerFor(driver);
		learner.learn();
		learner.createConjecture();
		driver.logStats();
		//TODO check conjecture
		LogManager.end();
		LogManager.clearsLoggers();
		return learner;
	}

	protected static void run_stats(){
		String baseDir = Options.OUTDIR;
		File f = new File(baseDir + File.separator + Options.DIRSTATSCSV);
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
		Options.LOG_LEVEL = LogLevel.LOW;

		for (int i = 1; i <= Options.NBTEST; i++) {
			System.out.println("\t" + i + "/" + Options.NBTEST);
			Options.OUTDIR = logDir+File.separator+i+File.separator;
			Utils.createDir(new File(Options.OUTDIR));
			try {
				Learner l = learnOneTime();

				StatsEntry learnerStats = l.getStats();

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
				LogManager.end();
				String failDir = baseDir + File.separator + 
						Options.DIRFAIL + File.separator + 
						new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
				try {
					Utils.copyDir(Paths.get(Options.OUTDIR),Paths.get(failDir));
					File readMe = new File(failDir+File.separator+"ReadMe.txt");
					Writer readMeWriter = new BufferedWriter(new FileWriter(readMe));
					readMeWriter.write(name+ " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
					readMeWriter.write("One learner during stats throw an exception");
					readMeWriter.write("\n");
					e.printStackTrace(new PrintWriter(readMeWriter));
					readMeWriter.write("\n");
					readMeWriter.write("\n");
					readMeWriter.write("\nthe driver was "+Options.SYSTEM);
					readMeWriter.write("\nthe seed was "+Options.SEED);
					readMeWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				e.printStackTrace();
				System.err.println("data saved in " + failDir);
			} finally {
				LogManager.clearsLoggers();
			}

		}

		if (MAKE_GRAPH.getValue()){
			System.out.println("[+] Make Graph");
			String baseDirGraph = baseDir + File.separator + Options.DIRGRAPHSTATS + File.separator;
			new File(baseDirGraph).mkdir();
			for (File statFile : new File(statsDir).listFiles()){
				String statName = statFile.getName().substring(0, statFile.getName().length()-4);
				statName = statName.substring(statName.lastIndexOf(".")+1,statName.length());
				System.out.println("\tmaking graph for "+statName);
				Options.OUTDIR = baseDirGraph + File.separator + statName;
				new File(Options.OUTDIR).mkdir();
				Utils.cleanDir(new File(Options.OUTDIR));
				StatsSet stats = new StatsSet(statFile);
				GraphGenerator gen = stats.get(0).getDefaultsGraphGenerator();
				gen.generate(stats);
			}
		}

	}

	public static void main(String[] args) {
		welcome();
		parseArguments(args);
		check();

		if (STATS_MODE.getValue()){
			run_stats();
		} else {
			try {
				learnOneTime();
			} catch (Exception e) {
				System.err.println("Unexpected error");
				e.printStackTrace(System.err);
			}
		}
		System.out.println("[+] End");
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
		printUsage(generalOptions);

		System.out.println("> Inference Algorithm");
		printUsage(inferenceChoiceOptions);

		System.out.println("> Algorithm ZQ");
		printUsage(ZQOptions);

		System.out.println("> Algorithm EFSM");
		printUsage(EFSMOptions);

		System.out.println("> Random Mealy Generator");
		printUsage(randomAutomataOptions);

		System.out.println("> Output");
		printUsage(outputOptions);

		System.out.println("> Stats");
		printUsage(statsOptions);
		
		System.out.println("> Test EFSM (should be group with random Generator ?)");
		printUsage(testEFSMOptions);

		System.out.println("> Others...");
		printUsage(otherOptions);

		System.out.println();
		System.out
		.println("Ex: SIMPA drivers.efsm.NSPKDriver --outdir mydir --text");
		System.out.println();
		System.exit(0);
	}

	protected static void printUsage(Option<?>[] options){
		int firstColumnLength = 0;
		for (Option<?> o : options){
			int length = o.consoleName.length()+((o.getDefaultValue() == null) ? 0 : o.getDefaultValue().toString().length()+3);
			if (length > firstColumnLength && length < 25)
				firstColumnLength = length;
		}

		StringBuilder newLine = new StringBuilder("\n\t");
		for (int j = 0; j <= firstColumnLength; j ++)
			newLine.append(" ");
		newLine.append("  ");
		for (Option<?> o : options){
			StringBuilder s = new StringBuilder("\t");
			s.append(o.consoleName);
			if (o.getDefaultValue() == null){
				for (int i = s.length(); i <= firstColumnLength; i++)
					s.append(" ");
			}else{
				for (int i = s.length(); i <= firstColumnLength-3-o.getDefaultValue().toString().length(); i++)
					s.append(" ");
				s.append(" (");
				s.append(o.getDefaultValue().toString());
				s.append(")");
			}
			s.append(" : ");
			s.append(o.getDescription().replaceAll("\n", newLine.toString()));
			System.out.println(s.toString());
		}
	}
}
