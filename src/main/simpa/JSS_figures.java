/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package main.simpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import automata.mealy.Mealy;
import drivers.Driver;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.Learner;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import main.simpa.Options.LogLevel;
import stats.GlobalGraphGenerator;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import tools.Utils;

public class JSS_figures {

	static void resetInferenceOption() {
		Options.HW_INFERENCE = false;
		Options.RIVESTSCHAPIREINFERENCE = false;
		Options.TREEINFERENCE = false;
		Options.COMBINATORIALINFERENCE = false;
		Options.CUTTERCOMBINATORIALINFERENCE = false;
		Options.LMINFERENCE = false;
		Options.LOCALIZER_BASED_INFERENCE = false;
		Options.USE_SHORTEST_CE = false;
		Options.USE_DT_CE = false;
		Options.LOG_LEVEL = LogLevel.LOW;
	}

	static abstract class Config {
		abstract void set_up();
	}

	static final Config hWWithoutHeuristic = new Config() {
		@Override
		void set_up() {
			resetInferenceOption();
			Options.HW_INFERENCE = true;
			Options.HW_WITH_RESET = false;
			Options.ADAPTIVE_H = false;
			Options.ADAPTIVE_W_SEQUENCES = false;
			Options.ADD_H_IN_W = false;
			Options.REUSE_HZXW = false;
			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = false;
			Options.TRY_TRACE_AS_CE = false;
			Options.HW_WITH_KNOWN_W = false;
		}
	};
	static final Config hW_addHInW = new Config() {
		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			Options.ADD_H_IN_W = true;
		}
	};
	static final Config hw_hzxw = new Config() {

		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			Options.REUSE_HZXW = true;
		}
	};
	static final Config hw_3rd = new Config() {

		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = true;
		}
	};
	static final Config hW_heuristicsNoTrace = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			Options.HW_INFERENCE = true;
			Options.HW_WITH_RESET = false;
			Options.ADAPTIVE_H = false;
			Options.ADAPTIVE_W_SEQUENCES = false;
			Options.ADD_H_IN_W = true;
			Options.REUSE_HZXW = true;
			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = true;
			Options.TRY_TRACE_AS_CE = false;
			Options.HW_WITH_KNOWN_W = false;
		}
	};
	static final Config hWWithAllHeuristics = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			Options.HW_INFERENCE = true;
			Options.HW_WITH_RESET = false;
			Options.ADAPTIVE_H = false;
			Options.ADAPTIVE_W_SEQUENCES = false;
			Options.ADD_H_IN_W = true;
			Options.REUSE_HZXW = true;
			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = true;
			Options.TRY_TRACE_AS_CE = true;
			Options.HW_WITH_KNOWN_W = false;
		}

	};
	static final Config hWWithKnownW = new Config() {

		@Override
		void set_up() {
			hWWithAllHeuristics.set_up();
			Options.HW_WITH_KNOWN_W = true;
		}

	};
	static final Config hWWithReset = new Config() {

		@Override
		void set_up() {
			hWWithAllHeuristics.set_up();
			Options.HW_WITH_RESET = true;
		}

	};
	static final Config RS = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			Options.RIVESTSCHAPIREINFERENCE = true;
			Options.RS_WITH_UNKNOWN_H = true;
		}
	};
	static final Config locW = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			Options.LOCALIZER_BASED_INFERENCE = true;
			Options.ICTSS2015_WITHOUT_SPEEDUP = true;
		}
	};
	static final Config Lm = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			Options.LMINFERENCE = true;
		}
	};

	static boolean random;
	static URL url;

	public static Driver loadDriver(String system) throws Exception {
		Driver driver = null;
		if (random) {
			RandomMealyDriver d = null;
			do {
				d = new RandomMealyDriver();
			} while (d.getAutomata().getStateCount() != Options.MAXSTATES);
			driver = d;
			LocalizerBasedLearner.findShortestWSet = false;
		} else {
			driver = new TransparentMealyDriver(Mealy.importFromUrl(url, true));
			LocalizerBasedLearner.findShortestWSet = true;
		}
		return driver;
	}

	protected static Driver createAndUpdateDriver() throws Exception {

		Driver driver = null;
		driver = loadDriver(Options.SYSTEM);
		if (driver == null)
			System.exit(1);
		if (driver instanceof TransparentMealyDriver) {
			Mealy automaton = ((TransparentMealyDriver) driver).getAutomata();
			Options.MAX_CE_RESETS = 1;
			Options.MAX_CE_LENGTH = automaton.getStateCount()
					* driver.getInputSymbols().size() * 1500;
			Options.MAX_CE_LENGTH = 50000;
			if (random)
				Options.MAX_CE_LENGTH = (int) (Math
						.pow(automaton.getStateCount()
								* driver.getInputSymbols().size(), 0.5)
						* 100);
			if (Options.LMINFERENCE || Options.TREEINFERENCE
					|| (Options.HW_INFERENCE && Options.HW_WITH_RESET)) {
				Options.MAX_CE_RESETS = Options.MAX_CE_LENGTH;
				Options.MAX_CE_LENGTH = automaton.getStateCount() * 10;
			}
			System.out.println("Maximum counter example length set to "
					+ Options.MAX_CE_LENGTH
					+ " and maximum counter example reset set to "
					+ Options.MAX_CE_RESETS + " from topology of driver ("
					+ automaton.getStateCount() + " states and "
					+ driver.getInputSymbols().size() + " inputs).");
		}
		if (Options.LOCALIZER_BASED_INFERENCE
				|| (Options.RIVESTSCHAPIREINFERENCE
						&& Options.RS_WITH_UNKNOWN_H)) {
			int nb_states = ((TransparentMealyDriver) driver).getAutomata()
					.getStateCount();
			Options.STATE_NUMBER_BOUND = nb_states;
		}
		return driver;
	}

	protected static Learner learnOneTime() throws Exception {
		Learner learner = null;
		Driver driver = null;
		boolean error = false;
		int errorNb = 0;
		do {
			error = false;
			try {
				System.out.println(new Date());
				driver = createAndUpdateDriver();
				learner = Learner.getLearnerFor(driver);
				learner.learn();
			} catch (Exception e) {
				if (++errorNb > 100) {
					System.err.println("too many errors occured");
					throw e;
				}
				System.out.println("retring because of error " + e);
				error = true;
			}
		} while (error);
		// System.err.println(learner.toString());
		/** LX add commentaire **/
		// learner.createConjecture();
		learner.logStats();
		driver.logStats();
		// TODO check conjecture
		return learner;
	}

	protected static void run_stats() {
		if (random)
			System.out.println("states " + Options.MAXSTATES);
		else
			System.out.println(url);
		if (Options.HW_INFERENCE)
			System.out.println("hW");
		if (Options.RIVESTSCHAPIREINFERENCE)
			System.out.println("RS");
		if (Options.LOCALIZER_BASED_INFERENCE)
			System.out.println("locW");
		if (Options.USE_DT_CE)
			System.out.println("distinction tree CE");
		else if (Options.USE_SHORTEST_CE)
			System.out.println("shortest CE");
		else
			System.out.println("Mr Bean");
		String baseDir = System.getProperty("user.dir");
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

				File globalStats = new File(statsDir + File.separator
						+ learnerStats.getClass().getName() + ".csv");
				Writer globalStatsWriter;
				if (!globalStats.exists()) {
					globalStats.createNewFile();
					globalStatsWriter = new BufferedWriter(
							new FileWriter(globalStats));
					globalStatsWriter
							.append(learnerStats.getCSVHeader() + "\n");
				} else {
					globalStatsWriter = new BufferedWriter(
							new FileWriter(globalStats, true));
				}

				globalStatsWriter.append(learnerStats.toCSV() + "\n");
				globalStatsWriter.close();
			} catch (Exception e) {
				String failDir = baseDir + File.separator + Options.DIRFAIL;
				Utils.createDir(new File(failDir));
				failDir = failDir + File.separator
						+ e.getClass().getSimpleName() + "-" + e.getMessage();
				if (!Utils.createDir(new File(failDir)))
					failDir = failDir + File.separator
							+ e.getClass().getSimpleName();
				Utils.createDir(new File(failDir));
				failDir = failDir + File.separator
						+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")
								.format(new Date());
				try {
					Utils.copyDir(Paths.get(Options.OUTDIR),
							Paths.get(failDir));
					File readMe = new File(
							failDir + File.separator + "ReadMe.txt");
					Writer readMeWriter = new BufferedWriter(
							new FileWriter(readMe));
					readMeWriter.write(
							"\nOne learner during stats throw an exception");
					readMeWriter.write("\n");
					e.printStackTrace(new PrintWriter(readMeWriter));
					readMeWriter.write("\n");
					readMeWriter.write("\n");
					readMeWriter.write("\nthe driver was " + Options.SYSTEM);
					readMeWriter.write("\nthe seed was " + Options.SEED);

					readMeWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				e.printStackTrace();
				System.err.println("data saved in " + failDir);
				System.exit(1);
			}

		}

	}

	static void makeGraph() {
		System.out.println("[+] Make Graph");
		String baseDir = System.getProperty("user.dir");
		File f = new File(baseDir + File.separator + Options.DIRSTATSCSV);
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());
		String statsDir = Utils.makePath(f.getAbsolutePath());
		String baseDirGraph = baseDir + File.separator + Options.DIRGRAPHSTATS
				+ File.separator;
		new File(baseDirGraph).mkdir();
		GlobalGraphGenerator globalGraph = new GlobalGraphGenerator();
		for (File statFile : new File(statsDir).listFiles()) {
			String statName = statFile.getName().substring(0,
					statFile.getName().length() - 4);
			statName = statName.substring(statName.lastIndexOf(".") + 1,
					statName.length());
			System.out.println("\tmaking graph for " + statName);
			Options.OUTDIR = baseDirGraph + File.separator + statName;
			new File(Options.OUTDIR).mkdir();
			Utils.cleanDir(new File(Options.OUTDIR));
			StatsSet stats = new StatsSet(statFile);
			GraphGenerator gen = stats.get(0).getDefaultsGraphGenerator();
			gen.generate(stats);
			globalGraph.add(stats);
		}
		globalGraph.generate();
	}

	public static void main(String[] args) throws MalformedURLException {
		Options.NBTEST = 2;
		Options.LOG_LEVEL = LogLevel.LOW;

		makeGraph();
		// System.exit(0);

		// heuristic comparison on states
		random = true;
		resetInferenceOption();
		Options.HW_INFERENCE = true;
		Options.ADAPTIVE_H = false;
		Options.ADAPTIVE_W_SEQUENCES = false;
		Options.USE_SHORTEST_CE = false;
		Options.MININPUTSYM = Options.MAXINPUTSYM = 2;
		Options.MAXOUTPUTSYM = Options.MINOUTPUTSYM = 2;
		Options.TRY_TRACE_AS_CE = false;
//		for (int config = 0; config < 6; config++) {
//			Options.REUSE_HZXW = config == 3 || config >= 4;
//			Options.ADD_H_IN_W = config == 1 || config >= 4;
//			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = config == 2
//					|| config >= 4;
//			Options.TRY_TRACE_AS_CE = config == 5;
		for (Config config : new Config[] { hWWithoutHeuristic, hW_addHInW,
				hw_3rd, hw_hzxw, hW_heuristicsNoTrace, hWWithAllHeuristics }) {
			config.set_up();
			for (Integer s : new Integer[] { 40, 60, 80, 100, 120, 140, 160,
					180, 200, 5, 15, 30, 70, 150, 300, 700, 1500, 3000 }) {
				Options.MAXSTATES = s;
				Options.MINSTATES = s;
				run_stats();
			}
		}

		// heuristic comparison on inputs
		random = true;
		Options.MAXSTATES = Options.MINSTATES = 30;
//	for (int config = 0; config < 2; config++) {
//			Options.REUSE_HZXW = config == 1;
//			Options.ADD_H_IN_W = config == 1;
//			Options.CHECK_INCONSISTENCY_H_NOT_HOMING = config == 1;
//			Options.TRY_TRACE_AS_CE = config == 1;
		for (Config config : new Config[] { hWWithoutHeuristic,
				hWWithAllHeuristics }) {
			config.set_up();
			for (Integer i : new Integer[] { 2, 5, 10, 15, 20, 30, 40, 50,
					60 }) {
				Options.MININPUTSYM = Options.MAXINPUTSYM = i;
				run_stats();
			}
		}

		Options.MININPUTSYM = Options.MAXINPUTSYM = 2;
		for (Integer s : new Integer[] { 5, 10, 15, 20, 25, 30, 40, 55, 75, 100,
				130, 170, 220, 290, 375, 500, 625, 800, 1000, 1300, 1700, 2200,
				2900, }) {
//			for (int config = 0; config < 3; config++) {
//				Options.HW_INFERENCE = config == 0;
//				Options.RIVESTSCHAPIREINFERENCE = config == 1;
//				Options.LOCALIZER_BASED_INFERENCE = config == 2;
//				if (config == 0) {
//					Options.REUSE_HZXW = true;
//					Options.ADD_H_IN_W = true;
//					Options.CHECK_INCONSISTENCY_H_NOT_HOMING = true;
//					Options.TRY_TRACE_AS_CE = true;
//				} else if (config == 1) {
//					Options.RS_WITH_UNKNOWN_H = true;
//				} else if (config == 2) {
//					Options.ICTSS2015_WITHOUT_SPEEDUP = true;
//				}
//				if (config == 1 && s > 170)
//					continue;
//				if (config == 2 && s > 100)
//					continue;
			for (Config config : new Config[] { hWWithAllHeuristics, RS,
					locW, }) {
				config.set_up();
				if (config == RS && s > 170)
					continue;
				if (config == locW && s > 100)
					continue;
				Options.MAXSTATES = Options.MINSTATES = s;
				Options.STATE_NUMBER_BOUND = s;
				run_stats();
			}
		}

		String[] connected = new String[] {
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkEdentifier2/learnresult_new_device-simple_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkEdentifier2/learnresult_old_device-simple_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkFromRhapsodyToDezyne/model3.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/ActiveMQ__invalid.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/ActiveMQ__non_clean.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/ActiveMQ__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__invalid.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__non_clean.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__simple.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__two_client_same_id.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__invalid.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__non_clean.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__simple.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__two_client_same_id.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/mosquitto__invalid.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/mosquitto__mosquitto.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/mosquitto__non_clean.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/mosquitto__two_client_same_id.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/mosquitto__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkToyModels/lee_yannakakis_distinguishable.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkToyModels/lee_yannakakis_non_distinguishable.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkToyModels/cacm.dot",

		};

		String[] reset = new String[] {
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/4_learnresult_MAESTRO_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/ASN_learnresult_MAESTRO_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/Rabo_learnresult_MAESTRO_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/Volksbank_learnresult_MAESTRO_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/10_learnresult_MasterCard_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/1_learnresult_MasterCard_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/4_learnresult_PIN_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/4_learnresult_SecureCode%20Aut_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/ASN_learnresult_SecureCode%20Aut_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/Rabo_learnresult_SecureCode_Aut_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkBankcard/learnresult_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkEdentifier2/learnresult_new_Rand_500_10-15_MC_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkEdentifier2/learnresult_new_W-method_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkEdentifier2/learnresult_old_500_10-15_fix.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/ActiveMQ__simple.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/ActiveMQ__single_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__single_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__two_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__single_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/emqtt__two_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__invalid.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__non_clean.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__simple.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__single_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__two_client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/hbmqtt__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkSSH/DropBear.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTCP/TCP_FreeBSD_Client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTCP/TCP_Linux_Client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTCP/TCP_Windows8_Client.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.12_client_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.12_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.12_server_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.12_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.8_client_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.8_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.8_server_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/GnuTLS_3.3.8_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/miTLS_0.1.3_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/NSS_3.17.4_client_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/NSS_3.17.4_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/NSS_3.17.4_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1g_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1g_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1j_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1j_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1l_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.1l_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.2_client_full.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.2_client_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/OpenSSL_1.0.2_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/RSA_BSAFE_C_4.0.4_server_regular.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkTLS/RSA_BSAFE_Java_6.1.1_server_regular.dot",

		};

		List<String> wSetTooLarge = Arrays.asList(
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkMQTT/VerneMQ__two_client_will_retain.dot",
				"http://automata.cs.ru.nl/automata_pmwiki/uploads/BenchmarkFromRhapsodyToDezyne/model3.dot");

		for (String automata : connected) {
			random = false;
			url = new URL(automata);
			for (Config config : new Config[] { hWWithAllHeuristics,
					hWWithKnownW, RS, locW, Lm }) {
				config.set_up();
				if ((Options.LOCALIZER_BASED_INFERENCE
						|| (Options.HW_INFERENCE && Options.HW_WITH_KNOWN_W))
						&& wSetTooLarge.contains(automata)) {
					System.out.println("skiped");
					continue;
				}
				for (Boolean useDT : new Boolean[] { true, false }) {
					Options.USE_DT_CE = useDT;
					run_stats();
				}
			}
		}

		for (String automata : reset) {
			random = false;
			url = new URL(automata);
			for (Config config : new Config[] { hWWithReset, Lm }) {
				config.set_up();
				for (Boolean useDT : new Boolean[] { true, false }) {
					Options.USE_DT_CE = useDT;
					run_stats();
				}
			}
		}
		makeGraph();
		System.out.println("[+] End");
	}


}
