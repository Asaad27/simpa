package main.simpa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import drivers.Driver;
import drivers.efsm.RandomEFSMDriver;
import examples.efsm.RandomEFSM;
import learner.efsm.table.LiLearner;
import tools.GraphViz;
import tools.Stats;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;

public class SIMPATestEFSM {
	public final static String name = "KITestEFSM";

	private static void init(String[] args) {
		//if (!Options.STAT)
			System.out.println("[+] Reading arguments");

		int i = 0;
		try {
			for (i = 0; i < args.length; i++) {
				if (args[i].equals("--nbtest"))
					Options.NBTEST = Integer.parseInt(args[++i]);
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
					Options.MININPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxoutputsym"))
					Options.MAXOUTPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--minparameter"))
					Options.MINPARAMETER = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxarameter"))
					Options.MAXPARAMETER = Integer.parseInt(args[++i]);
				else if (args[i].equals("--domainsize"))
					Options.DOMAINSIZE = Integer.parseInt(args[++i]);
				else if (args[i].equals("--simpleguard"))
					Options.SIMPLEGUARDPERCENT = Integer.parseInt(args[++i]);
				else if (args[i].equals("--ndvguard"))
					Options.NDVGUARDPERCENT = Integer.parseInt(args[++i]);
				else if (args[i].equals("--ndvmintrans"))
					Options.NDVMINTRANSTOCHECK = Integer.parseInt(args[++i]);
				else if (args[i].equals("--ndvmaxtrans"))
					Options.NDVMAXTRANSTOCHECK = Integer.parseInt(args[++i]);
				else if (args[i].equals("--retest"))
					Options.RETEST = Integer.parseInt(args[++i]);

				else if (args[i].equals("--reuseop"))
					Options.REUSE_OP_IFNEEDED = true;
				else if (args[i].equals("--forcej48"))
					Options.FORCE_J48 = true;
				else if (args[i].equals("--supportmin"))
					Options.SUPPORT_MIN = Integer.parseInt(args[++i]);

				else if (args[i].equals("--text"))
					Options.LOG_TEXT = true;
				else if (args[i].equals("--html"))
					Options.LOG_HTML = true;
				else if (args[i].equals("--openhtml"))
					Options.AUTO_OPEN_HTML = true;
				else if (args[i].equals("--seed"))
					Options.SEED = Long.parseLong(args[++i]);

				else if (args[i].equals("--help") || args[i].equals("-h"))
					usage();
			}
			Utils.setSeed(Options.SEED);

		} catch (NumberFormatException e) {
			LogManager.logError("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		int i = 0;
		Driver driver = null;
		//if (!Options.STAT)
			welcome();
		Options.TEST = true;
		init(args);
		try {
			check();
			String dir = Options.OUTDIR;
			if (Options.RETEST == -1) {
				Utils.cleanDir(new File(Options.OUTDIR));

				Stats stats = new Stats(Options.OUTDIR + "stats.csv");
				stats.setHeaders(RandomEFSMDriver.getStatHeaders());

				//if (!Options.STAT)
					System.out.println("[+] Testing " + Options.NBTEST
							+ " automaton");

				for (i = 1; i <= Options.NBTEST; i++) {
					Options.OUTDIR = Utils.makePath(dir + i);
					Utils.createDir(new File(Options.OUTDIR));
					Options.SYSTEM = "Random " + i;
					//if (!Options.STAT)
						System.out.print("    " + i + "/" + Options.NBTEST);
					try {
						if (Options.LOG_HTML)
							LogManager.addLogger(
									new HTMLLogger(Options.AUTO_OPEN_HTML));
						if (Options.LOG_TEXT)
							LogManager.addLogger(new TextLogger());
						LogManager.start();
						Options.LogOptions();

						RandomEFSM rEFSM = new RandomEFSM();
						driver = new RandomEFSMDriver(rEFSM);
						LiLearner lilearner = new LiLearner(driver);
						lilearner.learn();
						driver.logStats();
						learner.efsm.LiConjecture c = lilearner
								.createConjecture();
						c.exportToRawDot();
						LogManager.logLine();
						c.exportToDot();

						stats.addRecord(((RandomEFSMDriver) driver).getStats());
					} finally {
						LogManager.end();
						System.gc();
					}
				}
				stats.close();
			} else {
				System.out.println("[+] Retesting automaton " + Options.RETEST);
				Options.OUTDIR = Utils.makePath(dir + Options.RETEST);
				Options.SYSTEM = "Random " + Options.RETEST;
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRGRAPH));
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRARFF));
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRLOG));
				try {
					if (Options.LOG_HTML)
						LogManager.addLogger(
								new HTMLLogger(Options.AUTO_OPEN_HTML));
					if (Options.LOG_TEXT)
						LogManager.addLogger(new TextLogger());
					LogManager.start();
					RandomEFSM randEFSM = RandomEFSM.deserialize(Options.OUTDIR
							+ "Random.serialized");
					randEFSM.exportToDot();
					driver = new RandomEFSMDriver(randEFSM);
					LiLearner lilearner = new LiLearner(driver);
					lilearner.learn();
					driver.logStats();
					learner.efsm.LiConjecture c = lilearner.createConjecture();
					c.exportToRawDot();
					LogManager.logLine();
					c.exportToDot();
				} finally {
					LogManager.end();
				}
			}
			//if (!Options.STAT)
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
		//if (!Options.STAT)
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
		if (Options.MINPARAMETER < 1)
			throw new Exception("Minimal number of parameters >= 1 needed");
		if (Options.MINPARAMETER > Options.MAXPARAMETER)
			throw new Exception(
					"Minimal number of parameters <= Maximal number of parameters needed");
		if (Options.DOMAINSIZE < 10)
			throw new Exception("Size of domain >= 10 needed");
		if (Options.SIMPLEGUARDPERCENT < 0 || Options.SIMPLEGUARDPERCENT > 100)
			throw new Exception(
					"Percent of simple guards between 0 and 100 needed");
		if (Options.NDVGUARDPERCENT < 0 || Options.NDVGUARDPERCENT > 100)
			throw new Exception("Percent of ndv guard between 0 and 100 needed");
		if (Options.NDVMINTRANSTOCHECK < 1)
			throw new Exception(
					"Minimal number of transitions to check ndv >= 1 needed");
		if (Options.NDVMINTRANSTOCHECK > Options.NDVMAXTRANSTOCHECK)
			throw new Exception(
					"Minimal number of transitions to check ndv <= Minimal number of transitions to check ndv needed");
		if (Options.SUPPORT_MIN < 1 || Options.SUPPORT_MIN > 100)
			throw new Exception("Minimal between 1 and 100 include needed");
	}

	public static void usage() {
		System.out.println("Usage : KITest [Options]");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> General");
		System.out.println("    --help | -h            : Show help");
		System.out
				.println("    --retest X             : Load and test the random EFSM number X");
		System.out.println("    --seed NN       : Use NN as seed for random generator");
		System.out.println("> Algorithm");
		System.out
				.println("    --reuseop              : Reuse output parameter for non closed row");
		System.out
				.println("    --forcej48             : Force the use of J48 algorithm instead of M5P for numeric classes");
		System.out.println("    --supportmin      "
				+ String.format("%4s", "(" + Options.SUPPORT_MIN + ")")
				+ " : Minimal support for relation (1-100)");
		System.out.println("> Test");
		System.out.println("    --nbtest          "
				+ String.format("%4s", "(" + Options.NBTEST + ")")
				+ " : Number of tests");
		System.out.println("    --minstates       "
				+ String.format("%4s", "(" + Options.MINSTATES + ")")
				+ " : Minimal number of states");
		System.out.println("    --maxstates       "
				+ String.format("%4s", "(" + Options.MAXSTATES + ")")
				+ " :  Maximal number of states");
		System.out.println("    --transitions     "
				+ String.format("%4s", "(" + Options.TRANSITIONPERCENT + ")")
				+ " :  % of transitions for each states");
		System.out.println("    --mininputsym     "
				+ String.format("%4s", "(" + Options.MININPUTSYM + ")")
				+ " :  Minimal number of input symbols");
		System.out.println("    --maxinputsym     "
				+ String.format("%4s", "(" + Options.MAXINPUTSYM + ")")
				+ " :  Maximal number of input symbols");
		System.out.println("    --minoutputsym    "
				+ String.format("%4s", "(" + Options.MINOUTPUTSYM + ")")
				+ " :  Minimal number of output symbols");
		System.out.println("    --maxoutputsym    "
				+ String.format("%4s", "(" + Options.MAXOUTPUTSYM + ")")
				+ " :  Maximal number of output symbols");
		System.out.println("    --minparameter    "
				+ String.format("%4s", "(" + Options.MINPARAMETER + ")")
				+ " :  Minimal number of parameters by symbol");
		System.out.println("    --maxparameter    "
				+ String.format("%4s", "(" + Options.MAXPARAMETER + ")")
				+ " :  Maximal number of parameters by symbol");
		System.out.println("    --domainsize      "
				+ String.format("%4s", "(" + Options.DOMAINSIZE + ")")
				+ " :  Size of the parameter's domain");
		System.out.println("    --simpleguard     "
				+ String.format("%4s", "(" + Options.SIMPLEGUARDPERCENT + ")")
				+ " :  % of simple guard by transitions");
		System.out.println("    --ndvguard        "
				+ String.format("%4s", "(" + Options.NDVGUARDPERCENT + ")")
				+ " :  % of generating NDV by transitions");
		System.out.println("    --ndvmintrans     "
				+ String.format("%4s", "(" + Options.NDVMINTRANSTOCHECK + ")")
				+ " :  Minimum number of states before checking NDV value");
		System.out.println("    --ndvmaxtrans     "
				+ String.format("%4s", "(" + Options.NDVMAXTRANSTOCHECK + ")")
				+ " :  Maximum number of states before checking NDV value");
		System.exit(0);
	}
}
