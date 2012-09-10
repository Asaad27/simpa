package main;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import learner.Learner;
import tools.GraphViz;
import tools.Stats;
import tools.Utils;
import tools.loggers.HTMLLogger;
import tools.loggers.LogManager;
import tools.loggers.TextLogger;
import drivers.Driver;
import drivers.mealy.RandomMealyDriver;
import examples.mealy.RandomMealy;

public class KITestMealy {
	public final static String name = "KITestMealy";
	
	private static void init(String[] args) {
		if (!Options.STAT) System.out.println("[+] Reading arguments");
		
		Options.TREEINFERENCE = true;
		Options.LOG_HTML = true;
		
		int i=0;
		try {			
			for(i = 0; i < args.length; i++){
				if (args[i].equals("--nbtest")) Options.NBTEST = Integer.parseInt(args[++i]);
				else if (args[i].equals("--minstates")) Options.MINSTATES = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxstates")) Options.MAXSTATES = Integer.parseInt(args[++i]);
				else if (args[i].equals("--transitions")) Options.TRANSITIONPERCENT = Integer.parseInt(args[++i]);
				else if (args[i].equals("--mininputsym")) Options.MININPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxinputsym")) Options.MAXINPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--minoutputsym")) Options.MININPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("--maxoutputsym")) Options.MAXOUTPUTSYM = Integer.parseInt(args[++i]);
				else if (args[i].equals("-I")) Options.INITIAL_INPUT_SYMBOLS = args[++i];
				else if (args[i].equals("-Z")) Options.INITIAL_INPUT_SEQUENCES = args[++i];
				
				else if (args[i].equals("--retest")) Options.RETEST = Integer.parseInt(args[++i]);
				
				else if (args[i].equals("--text"))  Options.LOG_TEXT = true;
				else if (args[i].equals("--html")) Options.LOG_HTML = true;
				else if (args[i].equals("--openhtml")) Options.AUTO_OPEN_HTML = true;
				
				else if (args[i].equals("--help") || args[i].equals("-h")) usage();
			}	
			
		} catch (NumberFormatException e) {
			LogManager.logError("Error parsing argument (number) : " + args[i]);
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		int i = 0;
		Driver driver = null;
		if (!Options.STAT) welcome();
		Options.TEST = true;
		init(args);
		try {
			check();
			String dir = Options.OUTDIR;
			if (Options.RETEST == -1){
				Utils.cleanDir(new File(Options.OUTDIR));
				
				Stats stats = new Stats(Options.OUTDIR + "stats.csv");
				stats.setHeaders(RandomMealyDriver.getStatHeaders());
				
				if (!Options.STAT) System.out.println("[+] Testing " + Options.NBTEST + " automaton");
				
				for (i=1; i<=Options.NBTEST; i++){
					Options.OUTDIR = Utils.makePath(dir + i);
					Utils.createDir(new File(Options.OUTDIR));
					Options.SYSTEM = "Random " + i;
					if (!Options.STAT) System.out.println("    " + i + "/" + Options.NBTEST);
					try {
						if (Options.LOG_HTML) LogManager.addLogger(new HTMLLogger());
						if (Options.LOG_TEXT) LogManager.addLogger(new TextLogger());
						LogManager.start();
						
						RandomMealy rMealy = new RandomMealy();
						driver = new RandomMealyDriver(rMealy);
						Learner l = Learner.getLearnerFor(driver);
						l.learn();
						driver.logStats();
						
						stats.addRecord(((RandomMealyDriver)driver).getStats());
					}finally {
						LogManager.end();
						LogManager.clear();
						System.gc();
					}					
				}
				stats.close();
			}
			else{
				System.out.println("[+] Retesting automaton " + Options.RETEST);
				Options.OUTDIR = Utils.makePath(dir + Options.RETEST);
				Options.SYSTEM = "Random " + Options.RETEST;
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRGRAPH));
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRARFF));
				Utils.deleteDir(new File(Options.OUTDIR + Options.DIRLOG));
				try {
					if (Options.LOG_HTML) LogManager.addLogger(new HTMLLogger());
					if (Options.LOG_TEXT) LogManager.addLogger(new TextLogger());
					LogManager.start();
					RandomMealy randMealy = RandomMealy.deserialize(Options.OUTDIR + "Random.serialized");
					randMealy.exportToDot();
					driver = new RandomMealyDriver(randMealy);
					Learner l = Learner.getLearnerFor(driver);
					l.learn();
					driver.logStats();
				}finally {
					LogManager.end();
					LogManager.clear();
					System.gc();
				}
			}
			if (!Options.STAT) System.out.println("[+] End");
		}catch (Exception e) {
			LogManager.logException("Unexpected error at test " + (i==0?Options.RETEST:i), e);
		}
	}
	
	private static void welcome() {
		System.out.println(name + " - " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();		
	}

	private static void check() throws Exception {
		if (!Options.STAT) System.out.println("[+] Checking environment and options");
		
		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".")+1));
		if (major < 1 || minor < 5) throw new Exception("Java >=1.5 needed");
		
		try{
			Options.WEKA = weka.core.Version.MAJOR >= 3;
			if (!Options.WEKA) throw new Exception();
		}catch (Exception e){
			LogManager.logError("Warning : Unable to find Weka and make the final conjecture");
		}
		
		if (GraphViz.check() != 0){
			Options.GRAPHVIZ = false;
			LogManager.logError("Warning: Unable to find GraphViz and converting dot to image files");
		}
			
		File f = new File(Options.OUTDIR + File.separator + Options.DIRTEST);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite()) throw new Exception("Unable to create/write " + f.getName());
		Options.OUTDIR = Utils.makePath(f.getAbsolutePath());
	
		if (Options.NBTEST < 1) throw new Exception("Number of test >= 1 needed");
		if (Options.MINSTATES < 1) throw new Exception("Minimal number of states >= 1 needed");
		if (Options.MAXSTATES < Options.MINSTATES) throw new Exception("Maximal number of states > Minimal number of states needed");
		if (Options.TRANSITIONPERCENT < 0 || Options.TRANSITIONPERCENT > 100) throw new Exception("Percent of transition between 0 and 100 needed");
		if (Options.MININPUTSYM < 1) throw new Exception("Minimal number of input symbols >= 1 needed");
		if (Options.MININPUTSYM > Options.MAXINPUTSYM) throw new Exception("Minimal number of input symbols <= Maximal number of input symbols needed");
		if (Options.MINOUTPUTSYM < 1) throw new Exception("Minimal number of output symbols >= 1 needed");
		if (Options.MINOUTPUTSYM > Options.MAXOUTPUTSYM) throw new Exception("Minimal number of output symbols <= Maximal number of output symbols needed");

	}
	
	public static void usage(){
		System.out.println("Usage : KITestMealy [Options]");
		System.out.println("");
		System.out.println("Options");
		System.out.println("> General");
		System.out.println("    --help | -h            : Show help");
		System.out.println("    --retest X             : Load and test the random EFSM numner X");
		System.out.println("> Algorithm");
		System.out.println("    -I                " + String.format("%4s", "("+ Options.SYMBOL_EPSILON + ")") + " : Initial inputs symbols");
		System.out.println("    -Z                " + String.format("%4s", "("+ Options.SYMBOL_EPSILON + ")") + " : Initial Z");
		System.out.println("> Test");
		System.out.println("    --nbtest          " + String.format("%4s", "("+ Options.NBTEST + ")") + " : Number of tests");
		System.out.println("    --minstates       " + String.format("%4s", "("+ Options.MINSTATES + ")") + " : Minimal number of states");
		System.out.println("    --maxstates       " + String.format("%4s", "("+ Options.MAXSTATES + ")") + " : Maximal number of states");
		System.out.println("    --mininputsym     " + String.format("%4s", "("+ Options.MININPUTSYM + ")") + " : Minimal number of input symbols");
		System.out.println("    --maxinputsym     " + String.format("%4s", "("+ Options.MAXINPUTSYM + ")") + " : Maximal number of input symbols");
		System.out.println("    --minoutputsym    " + String.format("%4s", "("+ Options.MINOUTPUTSYM + ")") + " : Minimal number of output symbols");
		System.out.println("    --maxoutputsym    " + String.format("%4s", "("+ Options.MAXOUTPUTSYM + ")") + " : Maximal number of output symbols");

		System.exit(0);
	}
}
