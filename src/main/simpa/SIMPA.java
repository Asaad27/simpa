/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Emmanuel PERRIER
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 *     Roland GROZ
 *     Lingxiao WANG
 ********************************************************************************/
package main.simpa;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneLayout;

import automata.mealy.InputSequence;
import drivers.Driver;
import drivers.ExhaustiveGeneratorOption;
//EFSM//import drivers.efsm.EFSMDriver;
import drivers.efsm.real.GenericDriver;
import drivers.efsm.real.ScanDriver;
import drivers.mealy.MealyDriver;
import learner.Learner;
import learner.mealy.LmConjecture;
import main.simpa.Options.LogLevel;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;
import options.OptionValidator;
import options.OptionsGroup;
import options.OptionValidator.CriticalityLevel;
import options.automataOptions.AutomataChoice;
import options.automataOptions.PostDriverValidator;
import options.modeOptions.ModeOption;
import options.outputOptions.OutputOptions;
import options.valueHolders.SeedHolder;
import options.valueHolders.ValueHolder;
import stats.GlobalGraphGenerator;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import tools.DotAntlrListener;
import tools.NullStream;
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
	protected boolean haveBeenParsed = false;

	public Option(String consoleName, String description, T defaultValue) {
		assert consoleName.startsWith("-");
		this.consoleName = consoleName;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public void parse(String[] args, ArrayList<Boolean> used) {
		if (haveBeenParsed())
			return;
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
					value = Integer.parseInt(args[i]);
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

	@Override
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

	@Override
	public Boolean getDefaultValue() {
		return null;
	}
}

public class SIMPA {
	public final static String name = SIMPA.class.getSimpleName();
	public static final boolean DEFENSIVE_CODE = true;


	// ZQ options
	private static BooleanOption STOP_AT_CE_SEARCH = new BooleanOption("--stopatce",
			"Stop inference when a counter exemple is asked");
	private static StringOption INITIAL_INPUT_SYMBOLS = new StringOption("-I", "Initial input symbols (a,b,c)",
			Options.INITIAL_INPUT_SYMBOLS);
	private static StringOption INITIAL_INPUT_SEQUENCES = new StringOption("-Z",
			"Initial distinguishing sequences (a-b,a-c,a-c-b))", Options.INITIAL_INPUT_SEQUENCES);
	private static BooleanOption INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = new BooleanOption("-I=X",
			"Initial input symbols set to X");
	private static Option<?>[] ZQOptions = new Option<?>[] { STOP_AT_CE_SEARCH,
			INITIAL_INPUT_SYMBOLS,
			INITIAL_INPUT_SEQUENCES, INITIAL_INPUT_SYMBOLS_EQUALS_TO_X };



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

	private static void parse(String[] args, ArrayList<Boolean> used, Option<?>[] options) {
		for (Option<?> o : options)
			o.parse(args, used);
	}

	@Deprecated
	private static void parseArguments(String[] args) {
		LogManager.logConsole("Checking environment and options");


		ArrayList<Boolean> used = new ArrayList<>();
		for (int j = 0; j < args.length; j++)
			used.add(false);

		parse(args, used, randomAutomataOptions);


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
		if (unusedArgs > 1) {
			System.err.println("please specify only one driverClass");
			usage();
		}

		Options.INITIAL_INPUT_SYMBOLS = INITIAL_INPUT_SYMBOLS.getValue();
		Options.INITIAL_INPUT_SEQUENCES = INITIAL_INPUT_SEQUENCES.getValue();
		Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X = INITIAL_INPUT_SYMBOLS_EQUALS_TO_X.getValue();


		Options.MINSTATES = MIN_STATE.getValue();
		Options.MAXSTATES = MAX_STATE.getValue();
		Options.MININPUTSYM = MIN_INPUT_SYM.getValue();
		Options.MAXINPUTSYM = MAX_INPUT_SYM.getValue();
		Options.MINOUTPUTSYM = MIN_OUTPUT_SYM.getValue();
		Options.MAXOUTPUTSYM = MAX_OUTPUT_SYM.getValue();
		Options.TRANSITIONPERCENT = TRANSITION_PERCENT.getValue();

	}

	private static void check() {
		String v = System.getProperty("java.specification.version");
		int major = Integer.parseInt(v.substring(0, v.indexOf(".")));
		int minor = Integer.parseInt(v.substring(v.indexOf(".") + 1));
		if (major < 1 || minor < 5)
			throw new RuntimeException("Java >=1.5 needed");


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

	@Deprecated
	public static Driver<?, ?> loadDriver(String system) throws Exception {
		Driver<?, ?> driver;
		try {
			try {
				if (Options.GENERICDRIVER) {
					driver = new GenericDriver(system);
				} else if (Options.SCAN) {
					driver = new ScanDriver(system);
				} else {
					driver = (Driver<?, ?>) Class.forName(system).newInstance();
				}
				if (getLogLevel() != LogLevel.LOW)
					LogManager.logConsole("System : " + driver.getSystemName());
				return driver;
			} catch (InstantiationException e) {
				throw new Exception("Unable to instantiate the driver : " + system);
			} catch (IllegalAccessException e) {
				throw new Exception("Unable to access the driver : " + system);
			} catch (ClassNotFoundException e) {
				throw new Exception("Unable to find the driver. Please check the system name (" + system + ")");
			}
		} catch (DotAntlrListener.ParseException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			usage();
			throw e;
		}
	}

	/**
	 * a file to store options used during last launch of SIMPA
	 */
	static File lastOptionsFile;


	private static boolean runWithOptions() {
		MultiArgChoiceOptionItem selectedMode = modeOption.getSelectedItem();
		if (selectedMode == modeOption.simple) {
			try {
				inferOneTime();
			} catch (Exception e) {
				LogManager.end();
				System.err.println("Unexpected error");
				e.printStackTrace(System.err);
				return false;
			}
		} else if (selectedMode == modeOption.stats) {
			if (!run_stats())
				return false;
		}
		return true;
	}

	/**
	 * run one inference. This method do not catch RuntimeException which may
	 * occur during inference.
	 * 
	 * @return {@code false} if an error occurred during one inference,
	 *         {@code true} if everything went fine.
	 */
	private static Learner inferOneTime() {
		Predicate<OptionValidator> postDriverPredicate = new Predicate<OptionValidator>() {
					@Override
					public boolean test(OptionValidator t) {
						if (t instanceof PostDriverValidator)
					return true;
				return false;
					}
		};
		if (allOptions.validateSelectedTree(true, postDriverPredicate.negate())
				.compareTo(CriticalityLevel.WARNING) > 0)
			return null;
		Utils.deleteDir(Options.getArffDir());
		Utils.deleteDir(Options.getDotDir());
		if (getOutputsOptions().textLoggerOption.isEnabled())
			LogManager.addLogger(new TextLogger());
		if (getOutputsOptions().htmlLoggerOption.isEnabled())
			LogManager.addLogger(new HTMLLogger());
		LogManager.start();
		LogManager.logInfo("starting inference with options "
				+ allOptions.buildBackCLILine(false));
		for (OptionTree option : allOptions.getAllSelectedChildren()) {
			ValueHolder<?, ?> value = option.getValueHolder();
			if (value != null && value instanceof SeedHolder)
				((SeedHolder) value).initRandom();
		}
		if (argLabel != null) {
			argLabel.setText(allOptions.buildBackCLILine(true));
		}
		System.out.println("you can start inference again using "
				+ allOptions.buildBackCLILine(true));
		Driver<?, ?> d = null;
		Learner l = null;
		if (automataChoice.getSelectedItem() == automataChoice.mealy) {
			d = automataChoice.mealyDriverChoice.createDriver();
			l = Learner.getLearnerFor(d);
//EFSM//		} else if (automataChoice.getSelectedItem() == automataChoice.efsm) {
//EFSM//			EFSMDriver eDriver = automataChoice.efsmDriverChoice.createDriver();
//EFSM//			d = eDriver;
//EFSM//			l = automataChoice.efsmLearnerChoice.getSelectedItem()
//EFSM//					.getLearner(eDriver);
		} else {
			throw new RuntimeException("this must be implemented");
		}
		if (allOptions.validateSelectedTree(true, postDriverPredicate)
				.compareTo(CriticalityLevel.WARNING) > 0)
			return null;
		assert (allOptions.validateSelectedTree(false, null).compareTo(
				CriticalityLevel.WARNING) <= 0) : "all validators were not checked or some validators changed their values";
		boolean conjectureIsFalse = false;
		try {
			l.learn();
			if (d instanceof MealyDriver) {
				conjectureIsFalse |= !((MealyDriver) d).searchConjectureError(
						(LmConjecture) l.createConjecture());
			}
			l.logStats();
		} finally {
			LogManager.end();
			LogManager.clearsLoggers();
		}
		if (conjectureIsFalse) {
			LogManager.logError(
					"Post checking of conjecture failed. The conjecture is false.");
			return null;
		}
		return l;
	}


	/**
	 * launch several inferences and record results in CSV files
	 * 
	 * @return {@code false} if an error occurred during one inference,
	 *         {@code true} if everything went fine.
	 */
	private static boolean run_stats() {
		assert modeOption.getSelectedItem() == modeOption.stats;
		int nbTests = modeOption.stats.inferenceNb.getValue();
		System.out.println("[+] Testing " + nbTests + " automaton");
		if (getLogLevel() != LogLevel.LOW)
			throw new RuntimeException();

		for (int i = 1; i <= nbTests; i++) {
			for (OptionTree option : allOptions.getAllSelectedChildren())
			{
				ValueHolder<?, ?> value = option.getValueHolder();
				if (value != null && value instanceof SeedHolder
						&& !((SeedHolder) value).useAutoValue()) {
					System.err.println(
							"running stats with forced seeds can produce invalid results.");
					System.err.println(
							"If you're sure to want to do this, you must disable this warning in code…");
					return false;
				}
			}

			Runtime.getRuntime().gc();
			System.out.println("\t" + i + "/" + nbTests);
			if (!learnAndSaveOneTime())
				return false;
		}
		boolean makeGraphs = modeOption.stats.makeGraphs.isEnabled();
		if (makeGraphs) {
			System.out.println("[+] Make Graph");
			GlobalGraphGenerator globalGraph = new GlobalGraphGenerator();
			for (File statFile : Options.getStatsCSVDir().listFiles()) {
				String statName = statFile.getName().substring(0,
						statFile.getName().length() - 4);
				statName = statName.substring(statName.lastIndexOf(".") + 1,
						statName.length());
				System.out.println("\tmaking graph for " + statName);
				Utils.cleanDir(Options.getStatsGraphDir());
				StatsSet stats = new StatsSet(statFile);
				GraphGenerator gen = stats.get(0).getDefaultsGraphGenerator();
				gen.generate(stats);
				globalGraph.add(stats);
			}
			globalGraph.generate();
		}
		return true;
	}

	/**
	 * Launch one inference and record result in CSV files.
	 * 
	 * The normal outputs produced by inference are not stored.
	 * 
	 * If an exception is thrown during inference, it will be saved in the
	 * {@link main.simpa.Options#getFailDir()} directory.
	 * 
	 * @return {@code false} if an error occurred during one inference,
	 *         {@code true} if everything went fine.
	 */
	private static boolean learnAndSaveOneTime() {

		try {
			Options.useTmpLogDir();
			Learner l = inferOneTime();

			StatsEntry learnerStats = l.getStats();

			File globalStats = new File(
					Options.getStatsCSVDir() + File.separator
							+ learnerStats.getClass().getName() + ".csv");
			Writer globalStatsWriter;
			if (!globalStats.exists()) {
				globalStats.getParentFile().mkdirs();
				globalStats.createNewFile();
				globalStatsWriter = new BufferedWriter(
						new FileWriter(globalStats));
				globalStatsWriter.append(learnerStats.getCSVHeader() + "\n");
			} else {
				globalStatsWriter = new BufferedWriter(
						new FileWriter(globalStats, true));
			}

			globalStatsWriter.append(learnerStats.toCSV() + "\n");
			globalStatsWriter.close();
		} catch (Exception e) {
			LogManager.end();
			File failDir = new File(Options.getFailDir() + File.separator
					+ e.getClass().getSimpleName() + "-" + e.getMessage());
			if (!Utils.createDir(failDir))
				failDir = new File(Options.getFailDir() + File.separator
						+ e.getClass().getSimpleName());
			Utils.createDir(failDir);
			failDir = new File(failDir + File.separator
					+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")
							.format(new Date()));
			System.err.println("An error occurred durring inference :");
			e.printStackTrace();
			System.err.println("Saving data in " + failDir);
			try {
				Utils.copyDir(Options.getLogDir().toPath(), failDir.toPath());
				File readMe = new File(failDir + File.separator + "ReadMe.txt");
				Writer readMeWriter = new BufferedWriter(
						new FileWriter(readMe));
				readMeWriter.write(
						name + " " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
								.format(new Date()));
				readMeWriter
						.write("\nOne learner during stats throw an exception");
				readMeWriter.write("\n");
				e.printStackTrace(new PrintWriter(readMeWriter));
				readMeWriter.write("\n");
				readMeWriter.write("\n");
				readMeWriter.write(
						"\nyou can try to do this learning again by running something like '"
								+ allOptions.buildBackCLILine(true) + "'");

				readMeWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		} finally {
			LogManager.clearsLoggers();
			try {
				Options.getDotDir().delete();
			} catch (Exception e) {
			}
			Options.useNormalLogDir();
		}
		return true;
	}

	protected static boolean run_enum() {
		ExhaustiveGeneratorOption<? extends Driver<?, ?>> option;

		if (automataChoice.getSelectedItem() == automataChoice.mealy) {
			option = automataChoice.mealyDriverChoice.exhaustiveDriver;
		} else {
			throw new RuntimeException("not implemented");
		}

		// System.out.println("[+] Testing " + Options.NBTEST + " automaton");
		if (getLogLevel() != LogLevel.LOW)
			throw new RuntimeException();

		int testedAutomata = 0;
		while (option.hasNext()) {
			Runtime.getRuntime().gc();
			System.out.println("\t" + testedAutomata);
				learnAndSaveOneTime();
				testedAutomata++;


		}
		System.out.println(testedAutomata + " automata tested");
		return true;
	}


	public static AutomataChoice automataChoice = new AutomataChoice();
	private static ModeOption modeOption = new ModeOption(automataChoice);

	static OutputOptions getOutputsOptions() {
		if (modeOption.getSelectedItem() == modeOption.simple)
			return modeOption.simple.outputOptions;
		if (modeOption.getSelectedItem() == modeOption.stats)
			return modeOption.stats.outputOptions;
		assert false;
		return null;
	}

	protected static LogLevel getLogLevel() {
		return getOutputsOptions().logLevel.getSelectedItem().level;
	}

	private static OptionsGroup allOptions = new OptionsGroup("all") {
		{
			addSubOption(automataChoice);
			addSubOption(modeOption);
			validateSubOptions();
		}
	};

	protected static OptionTree getOptions() {
		return allOptions;
	}
	static Thread inferThread = null;
	/**
	 * The label to display the CLI line. Must be filled after computing the
	 * seeds options.
	 */
	static JTextArea argLabel = null;

	private static void createAndShowGUI() {
		final JFrame frame = new JFrame(
				"SIMPA - Simpa Infers Models Pretty Automatically");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container pane = frame.getContentPane();

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.weightx = 1;
		pane.setLayout(new GridBagLayout());
		JScrollPane scroll = new JScrollPane();
		scroll.setLayout(new ScrollPaneLayout());
		JPanel optionsPanel = new JPanel();
		scroll.getViewport().add(optionsPanel);
		{
			optionsPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			optionsPanel.add(automataChoice.getComponent(), c);
			optionsPanel.add(modeOption.getComponent(), c);
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			optionsPanel.add(Box.createGlue(), c);
		}
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		pane.add(scroll, constraints);
		constraints.weighty = 0;
		GridBagConstraints CLIconstraints = new GridBagConstraints();
		CLIconstraints.gridy = GridBagConstraints.RELATIVE;
		CLIconstraints.gridx = 0;
		CLIconstraints.fill = GridBagConstraints.NONE;
		CLIconstraints.weightx = 0;
		CLIconstraints.weighty = 0;
		CLIconstraints.anchor = GridBagConstraints.LINE_START;
		pane.add(new JLabel("arguments to launch this in CLI : "),
				CLIconstraints);
		argLabel = new JTextArea();
		argLabel.setFont(Font.getFont(Font.MONOSPACED));
		argLabel.setLineWrap(true);
		argLabel.setWrapStyleWord(true);
		argLabel.setEditable(false);
		CLIconstraints.gridx = 1;
		CLIconstraints.weightx = 1;
		CLIconstraints.fill = GridBagConstraints.HORIZONTAL;
		pane.add(argLabel, CLIconstraints);

		final JButton startButton = new JButton("start inference");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (lastOptionsFile != null)
					Utils.setFileContent(lastOptionsFile,
							allOptions.buildBackCLILine(false));
				frame.setEnabled(false);
				startButton.setEnabled(false);
				assert (inferThread == null || !inferThread.isAlive());
				inferThread = new Thread() {
					@Override
					public void run() {
						runWithOptions();
					}
				};
				inferThread.start();
				try {
					inferThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				startButton.setEnabled(true);
				frame.setEnabled(true);

			}

		});
		pane.add(startButton, constraints);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private static final String GUI_ARGUMENT = "--gui";
	private static final String HELP_ARGUMENT = "--help";
	private static final String SHORT_HELP_ARGUMENT = "-h";

	public static void main(String[] args) {
		if (args.length == 0) {
			lastOptionsFile = new File(
					Utils.getSIMPACacheDirectory().getAbsolutePath()
							+ File.separator + "lastOptions");
			String lastOptions = "";
			if (lastOptionsFile.exists())
				lastOptions = Utils
						.fileContentOf(lastOptionsFile);
			allOptions.parseArguments(lastOptions, new NullStream());
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						createAndShowGUI();
					} catch (HeadlessException e) {
						System.err.println("Cannot open gui");
						System.out
								.println("To run SIMPA in console, try option "
										+ HELP_ARGUMENT
										+ " to see complete list of arguments");
					}
				}
			});
		} else {
			List<String> arrayArgs = new ArrayList<String>(Arrays.asList(args));
			if (arrayArgs.contains(HELP_ARGUMENT)
					|| arrayArgs.contains(SHORT_HELP_ARGUMENT)) {
				usage();
				return;
			}
			boolean openGui = arrayArgs.contains(GUI_ARGUMENT);
			while (arrayArgs.remove(GUI_ARGUMENT))
				;
			@SuppressWarnings("resource")
			PrintStream parsingErrorStream = (openGui ? System.out
					: System.err);
			boolean parsedSuccessfully = allOptions
					.parseArguments(arrayArgs, parsingErrorStream);
			if (!parsedSuccessfully)
				parsingErrorStream
						.println("there was errors while parsing arguments");

			welcome();
			// parseArguments(args);
			// check();

			if (openGui) {

				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							createAndShowGUI();
						} catch (HeadlessException e) {
							System.err.println("Cannot open gui");
							System.out
									.println("Please remove the " + GUI_ARGUMENT
											+ " and try to run SIMPA in CLI.");
						}
					}
				});
			} else {
				if (!parsedSuccessfully) {
					System.err.println("Cannot parse options. Use argument "
							+ HELP_ARGUMENT + " to display usage.");
					System.exit(1);
				}
				runWithOptions();
				System.out.println("[+] End");
			}
		}
	}

	private static void welcome() {
		System.out.println(name + " - " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();
	}

	static protected class Sample {
		public final String cmd, description;

		public Sample(String cmd, String description) {
			super();
			this.cmd = cmd;
			this.description = description;
		}
	}

	protected final static Sample[] samples = {
			new Sample(
					"--TDDotFile=automaton.dot --algo=MLm --html --OT_shortest",
					"Infer the Mealy system represented in file 'automaton.dot' with Lm algorithm. As we use a transparent (glass-box) driver, we can compare conjecture and real automata to compute the shortest counter-example."),
			new Sample(
					"--algo=MhW --MhW_whitout_reset --Driver=mqttDriver --Dtimeout=500 --DMQTT_client=client2:connect|disconnect|publish(topic,hello,false,)|: --DMQTT_client=client1:connect|disconnect|subscribe(topic,)|: --ORnd --ORnd_seed=auto --ORnd_maxlength=200 --text",
					"Infer a non-resetable MQTT System with hW algorithm."
							+ System.lineSeparator()
							+ "The configuration of MQTT client (i.e. the choice of actions sent to the broker) can be made in graphical interface with argument "
							+ GUI_ARGUMENT + "."),
			new Sample(
					"--Stats --Sgraph --Snbtest=10 --Driver=drivers.mealy.RandomMealyDriver --algo=MZQ --ORnd --ORnd_seed=auto --ORnd_maxlength=200 --ORnd_maxresets=1 --outdir=stats",
					"Run 10 inferences of a random automata with ZQ algorithm."
							+ System.lineSeparator()
							+ "The graph plotted at end of execution is configured through the java class "
							+ learner.mealy.tree.ZStatsEntry.class + "."),

	};

	public static void usage() {
		PrintStream out = System.out;
		out.println("Help for " + name);
		out.println();

		out.println(SHORT_HELP_ARGUMENT + " | " + HELP_ARGUMENT
				+ " \tdisplay this help");
		out.println(GUI_ARGUMENT
				+ "       \topen GUI (other arguments can be specified to preset GUI options)");
		out.println();

		out.println(Utils.prefixString("", "Keys for prefixes in options:"
				+ System.lineSeparator()
				+ "T : option applies only to Transparent (glass-box) Drivers"
				+ System.lineSeparator() + "M: option specific for Mealy models"
				+ System.lineSeparator() + "E: option specific for EFSM models"
				+ System.lineSeparator()
				+ "Other prefix letters indicate which component the option modifies D(river), O(racle), S(tatistics), and algorithm-specific options are prefixed with the name of the algorithm.",
				Utils.terminalWidth()));
		out.println();
		allOptions.printHelp(out);
		out.println();
		out.println();
		out.println("EXAMPLES :");
		for (Sample sample : samples) {
			out.println(sample.cmd);
			out.print(Utils.prefixString("  ", sample.description,
					Utils.terminalWidth()));
			out.println();
		}

	}

}
