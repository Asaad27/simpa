package main.simpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import automata.mealy.InputSequence;
import drivers.Driver;
import drivers.ExhaustiveGenerator;
import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.ScanDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.Learner;
import learner.mealy.localizerBased.SplittingTree;
import main.simpa.Options.LogLevel;
import stats.GlobalGraphGenerator;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import tools.DotAntlrListener;
import tools.GraphViz;
import tools.Utils;
import tools.antlr4.SplittingTree.SplittingTreeLexer;
import tools.antlr4.SplittingTree.SplittingTreeParser;
import tools.antlr4.SplittingTree.SplittingTreeVisitorImpl;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;

abstract class Option<T> {
	protected String consoleName;
	protected String description;
	protected T defaultValue;
	protected T value;
	protected boolean needed = true;
	protected boolean haveBeenParsed = false;

	public Option(String consoleName, String description, T defaultValue) {
		assert consoleName.startsWith("-");
		this.consoleName = consoleName;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public void parse(String[] args, ArrayList<Boolean> used) {
		preCheck(args, used);
		parseInternal(args, used);
		postCheck(args);
	}

	protected void preCheck(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)) {
				if (used.get(i))
					LogManager.logError("argument '" + consoleName + "' already used. Are you sur of your syntax for "
							+ args[i - 1] + " ?");
			}
	}

	protected void postCheck(String[] args) {
		int found = 0;
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName))
				found++;
		if (found > 1) {
			LogManager.logError("argument '" + consoleName + "' appears at least two times. used value " + getValue());
		}

		if (needed && getValue() == null) {
			LogManager.logError("argument '" + consoleName + "' missing");
		}
	}

	public abstract void parseInternal(String[] args, ArrayList<Boolean> used);

	public String getConsoleName() {
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

	public void setNeeded(boolean needed) {
		this.needed = needed;
	}

	public boolean haveBeenParsed() {
		return haveBeenParsed;
	}
}

class IntegerOption extends Option<Integer> {
	public IntegerOption(String consoleName, String description, Integer defaultValue) {
		super(consoleName, description, defaultValue);
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				try {
					value = new Integer(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("Error parsing argument '" + args[i] + "' for " + consoleName);
					throw e;
				}
			}
	}
}

class LongOption extends Option<Long> {
	public LongOption(String consoleName, String description, Long defaultValue) {
		super(consoleName, description, defaultValue);
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				try {
					value = new Long(args[i]);
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
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				value = args[i];
			}
	}

}

/** add SplittingTreeOption 12/27/2016 **/

class SplittingTreeOption extends Option<SplittingTree> {

	public SplittingTreeOption(String consoleName, String description, SplittingTree defaultValue) {
		super(consoleName, description, defaultValue);
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {

		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				String sTree = args[i];
				File file = new File(sTree);
				if (!file.exists()) {
					try {
						throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ANTLRInputStream stream = null;
				try {
					stream = new ANTLRInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SplittingTreeLexer lexer = new SplittingTreeLexer(stream);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				SplittingTreeParser parser = new SplittingTreeParser(tokens);
				// tell ANTLR to build a parse tree
				parser.setBuildParseTree(true);
				ParseTree tree = parser.splitting_tree();
				SplittingTreeVisitorImpl antlrVisitor = new SplittingTreeVisitorImpl();
				SplittingTree st = antlrVisitor.visit(tree);

				value = st;
			}

	}

}

/**
 * append T to a list
 */
abstract class ListOption<T> extends Option<ArrayList<T>> {
	protected String sampleValue1;
	protected String sampleValue2;

	public ListOption(String consoleName, String description, List<T> defaults, String sampleValue1,
			String sampleValue2) {
		super(consoleName, description, (defaults == null) ? null : new ArrayList<T>(defaults));
		value = null;
		this.sampleValue1 = sampleValue1;
		this.sampleValue2 = sampleValue2;
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				i++;
				assert !used.get(i) : "argument already parsed";
				used.set(i, true);
				if (value == null)
					value = new ArrayList<T>();
				for (String s : args[i].split(";"))
					value.add(valueFromString(s));
			}
	}

	protected void postCheck(String[] args, ArrayList<Boolean> used) {
		if (needed && getValue() == null) {
			System.err.println("argument '" + consoleName + "' missing");
		}
	}

	protected abstract T valueFromString(String s);

	public String getDescription() {
		return super.getDescription() + "\n\tusage : '" + getConsoleName() + " " + sampleValue1 + ";" + sampleValue2
				+ "' or '" + getConsoleName() + " " + sampleValue1 + " " + getConsoleName() + " " + sampleValue2 + "'";
	}
}

class StringListOption extends ListOption<String> {
	public StringListOption(String consoleName, String description, String sampleValue1, String sampleValue2,
			List<String> defaults) {
		super(consoleName, description, defaults, sampleValue1, sampleValue2);
	}

	@Override
	protected String valueFromString(String s) {
		return s;
	}
}

class InputSequenceListOption extends ListOption<InputSequence> {
	public InputSequenceListOption(String consoleName, String description, List<InputSequence> defaults) {
		super(consoleName, description, defaults, "a,b,c", "b,c,d");
	}

	@Override
	protected InputSequence valueFromString(String s) {
		InputSequence r = new InputSequence();
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens())
			r.addInput(st.nextToken());
		return r;
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
			if (args[i].equals(consoleName)) {
				haveBeenParsed = true;
				used.set(i, true);
				value = !defaultValue;
			}
	}

	public Boolean getDefaultValue() {
		return null;
	}
}

class HelpOption extends Option<Object> {
	public HelpOption() {
		super("--help | -h", "Show help", null);
		needed = false;
	}

	@Override
	public void parseInternal(String[] args, ArrayList<Boolean> used) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-h") || args[i].equals("--help")) {
				haveBeenParsed = true;
				used.set(i, true);
				System.out.println("you don't know how it works ?");
				SIMPA.usage();
			}
		// TODO add a more detailed help for each commands.
	}

	protected void postCheck(String[] args, ArrayList<Boolean> used) {
	}

	protected void preCheck(String[] args, ArrayList<Boolean> used) {
	}
}

public class SIMPA {
	public final static String name = SIMPA.class.getSimpleName();
	private static Driver driver;
	public static final boolean DEFENSIVE_CODE = true;
	private static String[] launchArgs;

	// General Options
	private static HelpOption help = new HelpOption();
	private static LongOption SEED = new LongOption("--seed", "Use NN as seed for random generator", null);
	private static StringOption LOAD_DOT_FILE = new StringOption("--loadDotFile",
			"load the specified dot file\n use with drivers.mealy.FromDotMealyDriver", null);
	private static BooleanOption INTERACTIVE = new BooleanOption("--interactive",
			"algorithms may ask user to choose a sequence, a counter example or something else");
	private static Option<?>[] generalOptions = new Option<?>[] { help, SEED, LOAD_DOT_FILE, INTERACTIVE };

	// output options
	private static BooleanOption LOG_HTML = new BooleanOption("--html", "Use HTML logger");
	private static BooleanOption LOG_TEXT = new BooleanOption("--text", "Use the text logger");
	private static BooleanOption AUTO_OPEN_HTML = new BooleanOption("--openhtml", "Open HTML log automatically");
	private static StringOption OUTDIR = new StringOption("--outdir", "Where to save arff and graph files",
			Options.OUTDIR);
	private static Option<?>[] outputOptions = new Option<?>[] { LOG_HTML, LOG_TEXT, AUTO_OPEN_HTML, OUTDIR };

	// inference choice
	private static BooleanOption TREE_INFERENCE = new BooleanOption("--tree",
			"Use tree inference (if available) instead of table");
	private static BooleanOption LM_INFERENCE = new BooleanOption("--lm", "Use lm inference");
	private static BooleanOption LOCALIZER_BASED_INFERENCE = new BooleanOption("--localizerBased", "use localizer based Algorithm (also called 'DUBAI' and 'ICTSS2015' and previously 'noReset')");
	private static BooleanOption HW_INFERENCE = new BooleanOption("--hW", "use hW algorithm");
	private static BooleanOption COMBINATORIAL_INFERENCE = new BooleanOption("--combinatorial",
			"use the combinatorial inference");
	private static BooleanOption CUTTER_COMBINATORIAL_INFERENCE = new BooleanOption("--cutCombinatorial",
			"use the combinatorial inference with cutting");
	private static BooleanOption RIVETSCHAPIRE_INFERENCE = new BooleanOption("--rivestSchapire",
			"use the RivestSchapire inference (must be used with an other learner)\nThis option let you to run an inference algorithm with resets on a driver without reset.");
	private static Option<?>[] inferenceChoiceOptions = new Option<?>[] {
			TREE_INFERENCE, LM_INFERENCE, LOCALIZER_BASED_INFERENCE,
			HW_INFERENCE, CUTTER_COMBINATORIAL_INFERENCE,
			COMBINATORIAL_INFERENCE, RIVETSCHAPIRE_INFERENCE };

	// ZQ options
	private static BooleanOption STOP_AT_CE_SEARCH = new BooleanOption("--stopatce",
			"Stop inference when a counter exemple is asked");
	private static IntegerOption MAX_CE_LENGTH = new IntegerOption("--maxcelength",
			"Maximal length of random walk for counter example search", Options.MAX_CE_LENGTH);
	private static IntegerOption MAX_CE_RESETS = new IntegerOption("--maxceresets",
			"Maximal number of random walks for counter example search", Options.MAX_CE_RESETS);
	private static StringOption INITIAL_INPUT_SYMBOLS = new StringOption("-I", "Initial input symbols (a,b,c)",
			Options.INITIAL_INPUT_SYMBOLS);
	private static StringOption INITIAL_INPUT_SEQUENCES = new StringOption("-Z",
			"Initial distinguishing sequences (a-b,a-c,a-c-b))", Options.INITIAL_INPUT_SEQUENCES);
	private static BooleanOption INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = new BooleanOption("-I=X",
			"Initial input symbols set to X");
	private static Option<?>[] ZQOptions = new Option<?>[] { STOP_AT_CE_SEARCH, MAX_CE_LENGTH, MAX_CE_RESETS,
			INITIAL_INPUT_SYMBOLS, INITIAL_INPUT_SEQUENCES, INITIAL_INPUT_SYMBOLS_EQUALS_TO_X };

	// LocalizerBased options
	private static IntegerOption STATE_NUMBER_BOUND = new IntegerOption("--stateBound",
			"a bound of states number in the infered automaton\n" + "\tn  → use n as bound of state number\n"
					+ "\t0  → use exact states number (need to know the automaton)\n"
					+ "\t-n → use a random bound between the number of states and the number of states plus n (need to know the automaton)",
			0);
	private static InputSequenceListOption CHARACTERIZATION_SET = new InputSequenceListOption("--characterizationSeq",
			"use the given charcacterization sequences", null);
	/**
	 * use Splitting Tree input sequence 11/14/2016 LX private static
	 * InputSequenceListOption SPLITTING_TREE = new
	 * InputSequenceListOption("--splittingTree","use the given splitting tree
	 * file", null);
	 **/
	private static SplittingTreeOption SPLITTING_TREE = new SplittingTreeOption("--splittingTree",
			"load the specified splitting tree file", null);

	private static BooleanOption WITHOUT_SPEEDUP = new BooleanOption("--noSpeedUp",
			"Don't use speedUp (deduction from trace based on state incompatibilities)\nthis is usefull if you don't know the real state number but only the bound.");
	private static Option<?>[] localizerBasedOptions = new Option<?>[] { STATE_NUMBER_BOUND, CHARACTERIZATION_SET,
			WITHOUT_SPEEDUP, SPLITTING_TREE };

	// hW options
	private static BooleanOption ADD_H_IN_W = new BooleanOption("--addHInW",
			"add homing sequence in W-set.");
	private static BooleanOption CHECK_INCONSISTENCY_H_MAPPING = new BooleanOption(
			"--3rd-inconsistency",
			"search inconsistencies between mapping and conjecture");
	private static BooleanOption TRY_CE_FROM_TRACE = new BooleanOption(
			"--tryTraceCE", "Try to use trace as a counter example.");
	private static Option<?>[] hWOptions = new Option<?>[] { ADD_H_IN_W,
			TRY_CE_FROM_TRACE ,CHECK_INCONSISTENCY_H_MAPPING};

	// RS Options
	private static BooleanOption RS_WITH_UNKNOWN_H = new BooleanOption(
			"--RS-probabilistic",
			"do not compute a homming sequence to give to RS algorithme. h will be computed by RS itself.");
	private static Option<?>[] RSOptions = new Option<?>[] {RS_WITH_UNKNOWN_H};
	// EFSM options
	private static BooleanOption GENERIC_DRIVER = new BooleanOption("--generic", "Use generic driver");
	private static BooleanOption SCAN = new BooleanOption("--scan", "Use scan driver");
	private static BooleanOption REUSE_OP_IFNEEDED = new BooleanOption("--reuseop",
			"Reuse output parameter for non closed row");
	private static BooleanOption FORCE_J48 = new BooleanOption("--forcej48",
			"Force the use of J48 algorithm instead of M5P for numeric classes");
	private static BooleanOption WEKA = new BooleanOption("--weka", "Force the use of weka");
	private static IntegerOption SUPPORT_MIN = new IntegerOption("--supportmin", "Minimal support for relation (1-100)",
			Options.SUPPORT_MIN);
	private static Option<?>[] EFSMOptions = new Option<?>[] { GENERIC_DRIVER, SCAN, REUSE_OP_IFNEEDED, FORCE_J48, WEKA,
			SUPPORT_MIN };

	// TestEFSM options //TODO group with Random generator ?
	private static IntegerOption MIN_PARAMETER = new IntegerOption("--minparameter",
			"Minimal number of parameter by symbol", Options.MINPARAMETER);
	private static IntegerOption MAX_PARAMETER = new IntegerOption("--maxparameter",
			"Maximal number of parameter by symbol", Options.MAXPARAMETER);
	private static IntegerOption DOMAIN_SIZE = new IntegerOption("--domainsize", "Size of the parameter's domain",
			Options.DOMAINSIZE);
	private static IntegerOption SIMPLE_GUARD_PERCENT = new IntegerOption("--simpleguard",
			"% of simple guard by transitions", Options.SIMPLEGUARDPERCENT);
	private static IntegerOption NDV_GUARD_PERCENT = new IntegerOption("--ndvguard",
			"% of generating NDV by transitions", Options.NDVGUARDPERCENT);
	private static IntegerOption NDV_MIN_TRANS = new IntegerOption("--ndvmintrans",
			"Minimum number of states before checking NDV value", Options.NDVMINTRANSTOCHECK);
	private static IntegerOption NDV_MAX_TRANS = new IntegerOption("--ndvmaxtrans",
			"Maximum number of states before checking NDV value", Options.NDVMAXTRANSTOCHECK);
	private static Option<?>[] testEFSMOptions = new Option<?>[] { MIN_PARAMETER, MAX_PARAMETER, DOMAIN_SIZE,
			SIMPLE_GUARD_PERCENT, NDV_GUARD_PERCENT, NDV_MIN_TRANS, NDV_MAX_TRANS };

	// Random driver options
	private static IntegerOption MIN_STATE = new IntegerOption("--minstates",
			"Minimal number of states for random automatas", Options.MINSTATES);
	private static IntegerOption MAX_STATE = new IntegerOption("--maxstates",
			"Maximal number of states for random automatas", Options.MAXSTATES);
	private static IntegerOption MIN_INPUT_SYM = new IntegerOption("--mininputsym",
			"Minimal number of input symbols for random automatas", Options.MININPUTSYM);
	private static IntegerOption MAX_INPUT_SYM = new IntegerOption("--maxinputsym",
			"Maximal number of input symbols for random automatas", Options.MAXINPUTSYM);
	private static IntegerOption MIN_OUTPUT_SYM = new IntegerOption("--minoutputsym",
			"Minimal number of output symbols for random automatas\nThat is the minimal number used for output symbol genration but it is possible that less symbols are used",
			Options.MINOUTPUTSYM);
	private static IntegerOption MAX_OUTPUT_SYM = new IntegerOption("--maxoutputsym",
			"Maximal number of output symbols for random automatas", Options.MAXOUTPUTSYM);
	private static IntegerOption TRANSITION_PERCENT = new IntegerOption("--transitions",
			"percentage of loop in random automatas\nSome other loop may be generated randomly so it's a minimal percentage",
			Options.TRANSITIONPERCENT);
	private static Option<?>[] randomAutomataOptions = new Option<?>[] { MIN_STATE, MAX_STATE, MIN_INPUT_SYM,
			MAX_INPUT_SYM, MIN_OUTPUT_SYM, MAX_OUTPUT_SYM, TRANSITION_PERCENT };

	// stats options
	private static IntegerOption NB_TEST = new IntegerOption("--nbtest", "number of execution of the algorithm",
			Options.NBTEST);
	private static BooleanOption MAKE_GRAPH = new BooleanOption("--makeGraph", "create graph based on csv files");
	private static BooleanOption STATS_MODE = new BooleanOption("--stats",
			"enable stats mode\n - save results to csv\n - disable some feature");
	private static BooleanOption ENUMERATE_MODE = new BooleanOption("--enumerate",
			"generate automata with an interval of seeds");
	private static Option<?>[] statsOptions = new Option<?>[] { NB_TEST, MAKE_GRAPH, STATS_MODE, ENUMERATE_MODE };

	// Other options undocumented //TODO sort and explain them.
	private static StringListOption URLS = new StringListOption("--urls", "??? TODO", "url1", "url2", Options.URLS);
	private static BooleanOption XSS_DETECTION = new BooleanOption("--xss", "Detect XSS vulnerability");
	private static BooleanOption SHORTEST_CE = new BooleanOption(
			"--shortestCE",
			"use transparent driver to compute the shortest counter example possible");
	private static Option<?>[] otherOptions = new Option<?>[] { URLS,
			XSS_DETECTION, SHORTEST_CE };

	private static void parse(String[] args, ArrayList<Boolean> used, Option<?>[] options) {
		for (Option<?> o : options)
			o.parse(args, used);
	}

	private static void parseArguments(String[] args) {
		LogManager.logConsole("Checking environment and options");

		SEED.setNeeded(false);
		URLS.setNeeded(false);
		LOAD_DOT_FILE.setNeeded(false);
		CHARACTERIZATION_SET.setNeeded(false);
		// use Splitting Tree input sequence 11/14/2016
		SPLITTING_TREE.setNeeded(false);

		ArrayList<Boolean> used = new ArrayList<>();
		for (int j = 0; j < args.length; j++)
			used.add(false);

		parse(args, used, generalOptions);
		Options.SEED = (SEED.getValue() != null) ? SEED.getValue() : Utils.randLong();
		Utils.setSeed(Options.SEED);

		parse(args, used, outputOptions);
		parse(args, used, inferenceChoiceOptions);
		if (TREE_INFERENCE.getValue() && LOCALIZER_BASED_INFERENCE.getValue()) {
			System.out.println("You cannot choose two inference system");
			usage();
		}
		if (TREE_INFERENCE.getValue() || LM_INFERENCE.getValue()) {
			parse(args, used, ZQOptions);
		}
		if (LOCALIZER_BASED_INFERENCE.getValue()) {
			parse(args, used, localizerBasedOptions);
		}
		if (HW_INFERENCE.getValue()) {
			parse(args, used, hWOptions);
		}
		if (RIVETSCHAPIRE_INFERENCE.getValue()) {
			parse(args, used, RSOptions);
		}

		parse(args, used, EFSMOptions);
		parse(args, used, randomAutomataOptions);

		parse(args, used, statsOptions);

		// TODO check those options and put them in the right place
		parse(args, used, otherOptions);

		// check for unused arguments and select the driver
		int unusedArgs = 0;
		for (int j = 0; j < args.length; j++)
			if (!used.get(j)) {
				if (args[j].startsWith("-")) {
					System.err.println("the argument '" + args[j] + "' is not interpreted");
				}
				unusedArgs++;
				Options.SYSTEM = args[j];
			}
		if (unusedArgs < 1 && NB_TEST.getValue() > 0) {
			System.err.println("please specify the driverClass");
			usage();
		}
		if (unusedArgs > 1) {
			System.err.println("please specify only one driverClass");
			usage();
		}

		Options.INTERACTIVE = INTERACTIVE.getValue();

		Options.LOG_HTML = LOG_HTML.getValue();
		Options.LOG_TEXT = LOG_TEXT.getValue();
		Options.AUTO_OPEN_HTML = AUTO_OPEN_HTML.getValue();
		Options.OUTDIR = OUTDIR.getValue();

		Options.LMINFERENCE = LM_INFERENCE.getValue();
		Options.TREEINFERENCE = TREE_INFERENCE.getValue();
		Options.LOCALIZER_BASED_INFERENCE = LOCALIZER_BASED_INFERENCE.getValue();
		Options.HW_INFERENCE = HW_INFERENCE.getValue();
		Options.COMBINATORIALINFERENCE = COMBINATORIAL_INFERENCE.getValue();
		Options.CUTTERCOMBINATORIALINFERENCE = CUTTER_COMBINATORIAL_INFERENCE.getValue();
		Options.RIVESTSCHAPIREINFERENCE = RIVETSCHAPIRE_INFERENCE.getValue();

		Options.STOP_ON_CE_SEARCH = STOP_AT_CE_SEARCH.getValue();
		if (!RIVETSCHAPIRE_INFERENCE.getValue()) {
			Options.MAX_CE_RESETS = MAX_CE_RESETS.getValue();
			Options.MAX_CE_LENGTH = MAX_CE_LENGTH.getValue();
		} else {
			Options.MAX_CE_RESETS = 1;
			Options.MAX_CE_LENGTH = MAX_CE_LENGTH.getValue()
					* MAX_CE_RESETS.getValue();
		}
		Options.INITIAL_INPUT_SYMBOLS = INITIAL_INPUT_SYMBOLS.getValue();
		Options.INITIAL_INPUT_SEQUENCES = INITIAL_INPUT_SEQUENCES.getValue();
		Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = INITIAL_INPUT_SYMBOLS_EQUALS_TO_X.getValue();

		Options.CHARACTERIZATION_SET = CHARACTERIZATION_SET.getValue();
		// add splitting tree option
		Options.SPLITTING_TREE = SPLITTING_TREE.getValue();

		Options.ICTSS2015_WITHOUT_SPEEDUP = WITHOUT_SPEEDUP.getValue();
		Options.ADD_H_IN_W = ADD_H_IN_W.getValue();
		Options.CHECK_INCONSISTENCY_H_MAPPING = CHECK_INCONSISTENCY_H_MAPPING
				.getValue();
		Options.TRY_TRACE_AS_CE = TRY_CE_FROM_TRACE.getValue();

		Options.RS_WITH_UNKNOWN_H = RS_WITH_UNKNOWN_H.getValue();

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

		Options.NBTEST = NB_TEST.getValue();

		Options.XSS_DETECTION = XSS_DETECTION.getValue();
		Options.URLS = URLS.getValue();
		Options.SCAN = SCAN.getValue();
		Options.USE_SHORTEST_CE = SHORTEST_CE.getValue();
	}

	private static void check() {
		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".") + 1));
		if (major < 1 || minor < 5)
			throw new RuntimeException("Java >=1.5 needed");

		if (Options.SUPPORT_MIN < 1 || Options.SUPPORT_MIN > 100)
			throw new RuntimeException("Minimal between 1 and 100 include needed");

		if (Options.WEKA) {
			try {
				Options.WEKA = weka.core.Version.MAJOR >= 3;
				if (!Options.WEKA)
					LogManager.logError("Warning : Weka version >= 3 needed. Please update Weka.");
			} catch (Exception e) {
				LogManager.logError("Warning : Unable to use Weka. Check the buildpath.");
			}
		}

		if (GraphViz.check() != 0) {
			Options.GRAPHVIZ = false;
			LogManager.logError("Warning : Unable to find GraphViz dot. Check your environment.");
		}

		File f = new File(Options.OUTDIR);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		Options.OUTDIR = Utils.makePath(f.getAbsolutePath());

		f = new File(Options.OUTDIR + Options.DIRGRAPH);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());

		f = new File(Options.OUTDIR + Options.DIRARFF);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());

		if (STATS_MODE.getValue()) {
			boolean assert_test = false;
			assert (assert_test = true);
			if (assert_test) {
				System.out.println("you're about to make stats with active assertions."
						+ "This can make wrong results for duration stats because some assertions may have a big computation duration.\n"
						+ "Do you want to continue ? [y/n]");
				while (true) {
					Scanner input = new Scanner(System.in);
					String answer = input.next();
					if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
						input.close();
						break;
					}
					if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) {
						input.close();
						System.exit(0);
					}
					System.out.println("what did you say ?");
				}
			}
			if (INTERACTIVE.getValue()) {
				System.err.println(
						"you cannot use interactive mode for stats (that may induce wrong values for duration when user wait)");
				System.exit(1);
			}
			if (SEED.getValue() != null) {
				System.err.println(
						"you cannot impose seed for stats (that may duplicate results and make wrong average)");
				System.exit(1);
			}
			if (ENUMERATE_MODE.getValue()){
				System.err.println("stats are not compatible with enumerate mode yet.");
				System.exit(1);
			}
		}

		if (Options.NBTEST < 0)
			throw new RuntimeException("Number of test >= 0 needed");

		if (Options.MINSTATES < 1)
			throw new RuntimeException("Minimal number of states >= 1 needed");
		if (Options.MAXSTATES < Options.MINSTATES)
			throw new RuntimeException("Maximal number of states > Minimal number of states needed");
		if (Options.TRANSITIONPERCENT < 0 || Options.TRANSITIONPERCENT > 100)
			throw new RuntimeException("Percent of transition between 0 and 100 needed");
		if (Options.MININPUTSYM < 1)
			throw new RuntimeException("Minimal number of input symbols >= 1 needed");
		if (Options.MININPUTSYM > Options.MAXINPUTSYM)
			throw new RuntimeException("Minimal number of input symbols <= Maximal number of input symbols needed");
		if (Options.MINOUTPUTSYM < 1)
			throw new RuntimeException("Minimal number of output symbols >= 1 needed");
		if (Options.MINOUTPUTSYM > Options.MAXOUTPUTSYM)
			throw new RuntimeException("Minimal number of output symbols < Maximal number of output symbols needed");

	}

	public static Driver loadDriver(String system) throws Exception {
		Driver driver;
		try {
			try {
				if (Options.GENERICDRIVER) {
					driver = new GenericDriver(system);
				} else if (Options.SCAN) {
					driver = new ScanDriver(system);
				} else if (LOAD_DOT_FILE.getValue() != null) {
					try {
						driver = (Driver) Class.forName(system).getConstructor(File.class)
								.newInstance(new File(LOAD_DOT_FILE.getValue()));
					} catch (InvocationTargetException e) {
						throw new Exception(e.getTargetException());
					}
				} else {
					driver = (Driver) Class.forName(system).newInstance();
				}
				if (Options.LOG_LEVEL!=LogLevel.LOW)
					LogManager.logConsole("System : " + driver.getSystemName());
				return driver;
			} catch (InstantiationException e) {
				throw new Exception("Unable to instantiate the driver : " + system);
			} catch (IllegalAccessException e) {
				throw new Exception("Unable to access the driver : " + system);
			} catch (ClassNotFoundException e) {
				throw new Exception("Unable to find the driver. Please check the system name (" + system + ")");
			}
		} catch (ExhaustiveGenerator.TooBigSeedException e) {
			throw e;
		} catch (ExhaustiveGenerator.EndOfLoopException e) {
			throw e;
		} catch (DotAntlrListener.ParseException e) {
			throw e;
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

	private static String makeLaunchLine() {
		StringBuilder r = new StringBuilder();
		r.append("java ");
		r.append(SIMPA.class.getName() + " ");
		for (int i = 0; i < launchArgs.length; i++) {
			String arg = launchArgs[i];
			boolean keepArg = true;
			for (Option<?> statArg : statsOptions) {
				if (arg.equals(statArg.getConsoleName())) {
					keepArg = false;
					if (!(statArg instanceof BooleanOption))
						i++;
				}
			}
			if (arg.equals(SEED.getConsoleName())) {
				i++;
				keepArg = false;
			}
			if (arg.equals(LOG_HTML.getConsoleName()))
				keepArg = false;
			if (arg.equals(LOG_TEXT.getConsoleName()))
				keepArg = false;

			if (keepArg) {
				if (arg.contains(" "))
					r.append("\"" + arg + "\"");
				else
					r.append(arg);
				r.append(" ");
			}
		}
		r.append(LOG_HTML.getConsoleName() + " ");
		r.append(LOG_TEXT.getConsoleName() + " ");
		r.append(SEED.getConsoleName() + " ");
		r.append(Options.SEED + " ");
		return r.toString();
	}

	protected static Learner learnOneTime() throws Exception {
		if (Options.LOG_TEXT)
			LogManager.addLogger(new TextLogger());
		if (Options.LOG_HTML)
			LogManager.addLogger(new HTMLLogger());
		LogManager.start();
		Options.LogOptions();
		LogManager
				.logInfo("you can try to do this learning again by running something like '" + makeLaunchLine() + "'");
		LogManager.logConsole(
				"you can try to do this learning again by running something like '" + makeLaunchLine() + "'");
		driver = null;
		driver = loadDriver(Options.SYSTEM);
		if (driver == null)
			System.exit(1);
		if (Options.LOCALIZER_BASED_INFERENCE
				|| (Options.RIVESTSCHAPIREINFERENCE && Options.RS_WITH_UNKNOWN_H)) {
			if (STATE_NUMBER_BOUND.getValue() > 0)
				Options.STATE_NUMBER_BOUND = STATE_NUMBER_BOUND.getValue();
			else {
				if (driver instanceof TransparentMealyDriver) {
					int nb_states = ((TransparentMealyDriver) driver).getAutomata().getStateCount();
					if (STATE_NUMBER_BOUND.getValue() == 0)
						Options.STATE_NUMBER_BOUND = nb_states;
					else
						Options.STATE_NUMBER_BOUND = Utils.randIntBetween(nb_states,
								nb_states - STATE_NUMBER_BOUND.getValue());
				} else {
					if (STATE_NUMBER_BOUND.haveBeenParsed())
						throw new IllegalArgumentException(
								"You must provide a positive integer for state number bound ("
										+ STATE_NUMBER_BOUND.getConsoleName()
										+ ") because the number of states in the driver is unspecified");
					else
						throw new IllegalArgumentException("You must specify " + STATE_NUMBER_BOUND
								+ " because the number of states in driver is unknown");
				}
			}
		}
		Learner learner = Learner.getLearnerFor(driver);
		learner.learn();
		// System.err.println(learner.toString());
		/** LX add commentaire **/
		// learner.createConjecture();
		learner.logStats();
		driver.logStats();
		// TODO check conjecture
		LogManager.end();
		LogManager.clearsLoggers();
		return learner;
	}

	protected static void run_stats() {
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
		System.out.println("[+] Testing " + Options.NBTEST + " automaton");
		Options.LOG_LEVEL = LogLevel.LOW;

		for (int i = 1; i <= Options.NBTEST; i++) {
			Runtime.getRuntime().gc();
			System.out.println("\t" + i + "/" + Options.NBTEST);
			Options.SEED = Utils.randLong();
			Utils.setSeed(Options.SEED);
			Options.OUTDIR = logDir + File.separator + i + File.separator;
			Utils.createDir(new File(Options.OUTDIR));
			try {
				Learner l = learnOneTime();

				StatsEntry learnerStats = l.getStats();

				File globalStats = new File(statsDir + File.separator + learnerStats.getClass().getName() + ".csv");
				Writer globalStatsWriter;
				if (!globalStats.exists()) {
					globalStats.createNewFile();
					globalStatsWriter = new BufferedWriter(new FileWriter(globalStats));
					globalStatsWriter.append(learnerStats.getCSVHeader() + "\n");
				} else {
					globalStatsWriter = new BufferedWriter(new FileWriter(globalStats, true));
				}

				globalStatsWriter.append(learnerStats.toCSV() + "\n");
				globalStatsWriter.close();
			} catch (Exception e) {
				LogManager.end();
				String failDir = baseDir + File.separator + Options.DIRFAIL;
				Utils.createDir(new File(failDir));
				failDir = failDir + File.separator + e.getClass().getSimpleName() + "-" + e.getMessage();
				if (!Utils.createDir(new File(failDir)))
					failDir = failDir + File.separator + e.getClass().getSimpleName();
				Utils.createDir(new File(failDir));
				failDir = failDir + File.separator + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
				try {
					Utils.copyDir(Paths.get(Options.OUTDIR), Paths.get(failDir));
					File readMe = new File(failDir + File.separator + "ReadMe.txt");
					Writer readMeWriter = new BufferedWriter(new FileWriter(readMe));
					readMeWriter.write(name + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
					readMeWriter.write("\nOne learner during stats throw an exception");
					readMeWriter.write("\n");
					e.printStackTrace(new PrintWriter(readMeWriter));
					readMeWriter.write("\n");
					readMeWriter.write("\n");
					readMeWriter.write("\nthe driver was " + Options.SYSTEM);
					readMeWriter.write("\nthe seed was " + Options.SEED);
					readMeWriter.write("\nyou can try to do this learning again by running something like '"
							+ makeLaunchLine() + "'");

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

		if (MAKE_GRAPH.getValue()) {
			System.out.println("[+] Make Graph");
			String baseDirGraph = baseDir + File.separator + Options.DIRGRAPHSTATS + File.separator;
			new File(baseDirGraph).mkdir();
			GlobalGraphGenerator globalGraph = new GlobalGraphGenerator();
			for (File statFile : new File(statsDir).listFiles()) {
				String statName = statFile.getName().substring(0, statFile.getName().length() - 4);
				statName = statName.substring(statName.lastIndexOf(".") + 1, statName.length());
				System.out.println("\tmaking graph for " + statName);
				Options.OUTDIR = baseDirGraph + File.separator + statName;
				new File(Options.OUTDIR).mkdir();
				Utils.cleanDir(new File(Options.OUTDIR));
				StatsSet stats = new StatsSet(statFile);
				GraphGenerator gen = stats.get(0).getDefaultsGraphGenerator();
				gen.generate(stats);
				globalGraph.generate(stats);
			}
		}

	}

	protected static void run_enum() {
		Class<?> driverClass = null;
		try {
			driverClass = Class.forName(Options.SYSTEM);
		} catch (ClassNotFoundException e2) {
			System.err.println(e2);
			System.exit(1);
		}

		if (!(ExhaustiveGenerator.class.isAssignableFrom(driverClass))) {
			System.err.println("The driver " + driverClass.getCanonicalName()
					+ " does not support enumeration of automaton"
					+ " (must implements "
					+ ExhaustiveGenerator.class.getCanonicalName() + ")");
			System.exit(1);
		}
		
		String baseDir = Options.OUTDIR+File.separator+"enum"+File.separator;
		File f = new File(baseDir + File.separator + Options.DIRSTATSCSV);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		String statsDir = Utils.makePath(f.getAbsolutePath());
		
		f = new File(baseDir + File.separator + Options.DIRTEST);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		String logDir = Utils.makePath(f.getAbsolutePath());
		Utils.cleanDir(new File(logDir));
		Options.OUTDIR = logDir;
		
		System.out.println("[+] Testing " + Options.NBTEST + " automaton");
		 Options.LOG_LEVEL = LogLevel.LOW;
		int testedAutomata = 0;
		boolean seedTooBig = false;
		for (Options.SEED = 0; !seedTooBig
				&& (!NB_TEST.haveBeenParsed() || Options.SEED < Options.NBTEST); Options.SEED++) {
			Runtime.getRuntime().gc();
			System.out
					.println("\t" + testedAutomata + " // " + Options.SEED);
			Utils.setSeed(Options.SEED);
			try {
				Learner l = learnOneTime();
				testedAutomata++;
				StatsEntry learnerStats = l.getStats();

				File globalStats = new File(statsDir + File.separator
						+ learnerStats.getClass().getName() + ".csv");
				Writer globalStatsWriter;
				if (!globalStats.exists()) {
					globalStats.createNewFile();
					globalStatsWriter = new BufferedWriter(new FileWriter(
							globalStats));
					globalStatsWriter
							.append(learnerStats.getCSVHeader() + "\n");
				} else {
					globalStatsWriter = new BufferedWriter(new FileWriter(
							globalStats, true));
				}

				 globalStatsWriter.append(learnerStats.toCSV() + "\n");
				 globalStatsWriter.close();
			} catch (ExhaustiveGenerator.TooBigSeedException e) {
				System.out.println("\nSeed is too big for this driver");
				seedTooBig = true;
			} catch (ExhaustiveGenerator.EndOfLoopException e) {
				System.out
						.println("\nAll loops of generating automata are done");
				seedTooBig = true;
			} catch (Exception e) {
				LogManager.end();
				String failDir = baseDir + File.separator + Options.DIRFAIL
						+ File.separator + "enum";
				Utils.createDir(new File(failDir));
				failDir = failDir + File.separator
						+ e.getClass().getSimpleName() + "-" + e.getMessage();
				if (!Utils.createDir(new File(failDir)))
					failDir = failDir + File.separator
							+ e.getClass().getSimpleName();
				Utils.createDir(new File(failDir));
				failDir = failDir
						+ File.separator
						+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")
								.format(new Date());
				try {
					Utils.copyDir(Paths.get(Options.OUTDIR), Paths.get(failDir));
					File readMe = new File(failDir + File.separator
							+ "ReadMe.txt");
					Writer readMeWriter = new BufferedWriter(new FileWriter(
							readMe));
					readMeWriter.write(name
							+ " "
							+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
									.format(new Date()));
					readMeWriter
							.write("\nOne learner during stats throw an exception");
					readMeWriter.write("\n");
					e.printStackTrace(new PrintWriter(readMeWriter));
					readMeWriter.write("\n");
					readMeWriter.write("\n");
					readMeWriter.write("\nthe driver was " + Options.SYSTEM);
					readMeWriter.write("\nthe seed was " + Options.SEED);
					readMeWriter
							.write("\nyou can try to do this learning again by running something like '"
									+ makeLaunchLine() + "'");

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
		System.out.println(testedAutomata + " automata tested");

	}
	
	
	public static void main(String[] args) {
		launchArgs = args;
		welcome();
		parseArguments(args);
		check();

		if (STATS_MODE.getValue()) {
			run_stats();
		} else if (ENUMERATE_MODE.getValue()){
			run_enum();
		}else {
			try {
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRGRAPH));
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRARFF));
				learnOneTime();
			} catch (Exception e) {
				LogManager.end();
				System.err.println("Unexpected error");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		System.out.println("[+] End");
	}

	private static void welcome() {
		System.out.println(name + " - " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
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

		System.out.println("> Algorithm based on localizer (called ICTSS2015/Dubai/'noReset')");
		printUsage(localizerBasedOptions);

		System.out.println("> Algorithm hW");
		printUsage(hWOptions);

		System.out.println("> Algorithm Rivest&Schapire");
		printUsage(RSOptions);

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
		System.out.println("Ex: SIMPA drivers.efsm.NSPKDriver --outdir mydir --text");
		System.out.println();
		System.exit(0);
	}

	protected static void printUsage(Option<?>[] options) {
		int firstColumnLength = 0;
		for (Option<?> o : options) {
			int length = o.consoleName.length()
					+ ((o.getDefaultValue() == null) ? 0 : o.getDefaultValue().toString().length() + 3);
			if (length > firstColumnLength && length < 25)
				firstColumnLength = length;
		}

		StringBuilder newLine = new StringBuilder("\n\t");
		for (int j = 0; j <= firstColumnLength; j++)
			newLine.append(" ");
		newLine.append("  ");
		for (Option<?> o : options) {
			StringBuilder s = new StringBuilder("\t");
			s.append(o.consoleName);
			if (o.getDefaultValue() == null) {
				for (int i = s.length(); i <= firstColumnLength; i++)
					s.append(" ");
			} else {
				for (int i = s.length(); i <= firstColumnLength - 3 - o.getDefaultValue().toString().length(); i++)
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
