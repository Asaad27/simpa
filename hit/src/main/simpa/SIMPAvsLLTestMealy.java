package main.simpa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import automata.mealy.InputSequence;
import LLTest.LLRunner;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;
import learner.Learner;
import learner.mealy.tree.SigmaLearner;
import learner.mealy.tree.ZLearner;
import tools.GraphViz;
import tools.Stats;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;
import drivers.Driver;
import drivers.mealy.MealyDriver;
import drivers.mealy.RandomMealyDriver;
import examples.mealy.CrashLL;
import examples.mealy.RandomMealy;

public class SIMPAvsLLTestMealy {
	public final static String name = "KITestMealy";

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

				else if (args[i].equals("--help") || args[i].equals("-h"))
					usage();
			}

		} catch (NumberFormatException e) {
			LogManager.logError("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		int i = 0;
		Driver driver = null;
		Driver driver2 =null;
		ZLearner l = null;

		if (!Options.STAT)
			welcome();
		Options.TEST = true;
		init(args);
		Options.OUTDIR = Options.OUTDIR + "/" + Options.MINSTATES;
		try {
			check();
			String dir = Options.OUTDIR;
			if (Options.RETEST == -1) {
				Utils.cleanDir(new File(Options.OUTDIR));

				Stats stats = new Stats(Options.OUTDIR + "stats.csv");
				stats.setHeaders(RandomMealyDriver.getStatHeaders());

				if (!Options.STAT)
					System.out.println("[+] Testing " + Options.NBTEST
							+ " automaton");

				// create LL learning alphabet
				// create learning alphabet
				Alphabet<String> inputs = new SimpleAlphabet<String>();
				String s = "a";
				for (int j = 0; j < Options.MININPUTSYM; j++) {
					inputs.add(s);
					s = Utils.nextSymbols(s);
				}

				String cs = "CS";
				List<LLRunner> runnerList = new ArrayList<>();
				/*
				LLRunner runner1 = new LLRunner(Options.OUTDIR
						+ "/statsLL_DHC.csv", inputs, Options.NBTEST, "DHC",
						"", cs, true);
				runnerList.add(runner1);

				LLRunner runner2 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_C.csv", inputs, Options.NBTEST, "L*",
						"C", cs, true);
				runnerList.add(runner2);

				LLRunner runner3 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FL.csv", inputs, Options.NBTEST, "L*",
						"FL", cs, true);
				runnerList.add(runner3);

				LLRunner runner4 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FLA.csv", inputs, Options.NBTEST, "L*",
						"FLA", cs, true);
				runnerList.add(runner4);
*/
				LLRunner runner5 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FLR_WITHOUT.csv", inputs, Options.NBTEST, "L*",
						"FLR", cs, "WITHOUT");
				runnerList.add(runner5);
				
				/*
				LLRunner runner5b = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FLR_LLDIC.csv", inputs, Options.NBTEST, "L*",
						"FLR", cs, "LLDIC");
				runnerList.add(runner5b);
				*/
				LLRunner runner5c = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FLR_NEWDIC.csv", inputs, Options.NBTEST, "L*",
						"FLR", cs, "NEWDIC");
				runnerList.add(runner5c);
/*
				LLRunner runner6 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_FLRA.csv", inputs, Options.NBTEST, "L*",
						"FLRA", cs, true);
				runnerList.add(runner6);

				LLRunner runner7 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_MP.csv", inputs, Options.NBTEST, "L*",
						"MP", cs, true);
				runnerList.add(runner7);

				LLRunner runner8 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_RS.csv", inputs, Options.NBTEST, "L*",
						"RS", cs, true);
				runnerList.add(runner8);
			
				LLRunner runner9 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_RSA.csv", inputs, Options.NBTEST, "L*",
						"RSA", cs, true);
				runnerList.add(runner9);

				LLRunner runner10 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_SH.csv", inputs, Options.NBTEST, "L*",
						"SH", cs, true);
				runnerList.add(runner10);

				LLRunner runner11 = new LLRunner(Options.OUTDIR
						+ "/statsLL_L_1B1.csv", inputs, Options.NBTEST, "L*",
						"1B1", cs, true);
				runnerList.add(runner11);
*/List<InputSequence> halfZ = new ArrayList<>();
				for (i = 1; i <= Options.NBTEST; i++) {
					long seed = System.currentTimeMillis();
					Utils.setSeed(seed);
					Options.SYSTEM = "Random " + i;
					if (!Options.STAT)
						System.out.println("    " + i + "/" + Options.NBTEST);
					try {
						if (Options.LOG_HTML)
							LogManager.addLogger(new HTMLLogger());
						if (Options.LOG_TEXT)
							LogManager.addLogger(new TextLogger());
						LogManager.start();

						driver = new RandomMealyDriver();
						l = new ZLearner(driver);
						l.learn();
						halfZ = new ArrayList<>();
						List<InputSequence> auxZ = new ArrayList<>(l.getZ());
						System.out.println("Z :"+l.getZ());
						System.out.println("auxZ :"+auxZ);
						for(int j=0;j<(l.getZ().size()/2);j++){
							
							halfZ.add(auxZ.remove(Utils.randIntBetween(0, l.getZ().size()-1-j)));
							System.out.println("halfZ :"+halfZ);
						}
						
						System.out.println("halfZ :"+halfZ);
						auxZ = new ArrayList<>(l.getZ());
						Utils.setSeed(seed);
						driver2 = new RandomMealyDriver();
						SigmaLearner sl = new SigmaLearner(driver2,auxZ);
						
						sl.learn();
						
						
						
						driver2.logStats();

						List<String> statlist = ((RandomMealyDriver) driver2)
								.getStats();
						statlist.add(String.valueOf(l.getRounds()));
						statlist.add(String.valueOf(seed));
						statlist.add(String.valueOf(l.getZ().size()));
						statlist.add(String.valueOf(sl.conjStates()));
						stats.addRecord(statlist);
					} finally {
						
						LogManager.end();
						System.gc();
						
					}

					// testing with LL
					LogManager.end();
					for(LLRunner runner : runnerList){
						 System.out.println("--------------------------------------------------");
						 System.out.println("");
						 List<InputSequence> auxZ = new ArrayList<>(l.getZ());
						 runner.run(((MealyDriver) driver).getAutomaton(),auxZ);
					}
					
				}
				stats.close();

			} 
			if (!Options.STAT)
				System.out.println("[+] End");
		} catch (Exception e) {
			LogManager.logException("Unexpected error at test "
					+ (i == 0 ? Options.RETEST : i), e);
		}
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

		try {
			Options.WEKA = weka.core.Version.MAJOR >= 3;
			if (!Options.WEKA)
				throw new Exception();
		} catch (Exception e) {
			LogManager
					.logError("Warning : Unable to find Weka and make the final conjecture");
		}

		if (GraphViz.check() != 0) {
			Options.GRAPHVIZ = false;
			LogManager
					.logError("Warning: Unable to find GraphViz and converting dot to image files");
		}

		File f = new File(Options.OUTDIR + File.separator + Options.DIRTEST);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new Exception("Unable to create/write " + f.getName());
		Options.OUTDIR = Utils.makePath(f.getAbsolutePath());

		if (Options.NBTEST < 1)
			throw new Exception("Number of test >= 1 needed");
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
					"Minimal number of output symbols <= Maximal number of output symbols needed");

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

