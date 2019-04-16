package main.simpa;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import automata.mealy.Mealy;
import drivers.Driver;
import drivers.mealy.MealyDriver;
import drivers.mealy.MealyDriverChoice;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.hW.HWOptions;
import learner.mealy.localizerBased.LocalizerBasedLearner;
import learner.mealy.localizerBased.LocalizerBasedOptions;
import learner.mealy.rivestSchapire.RivestSchapireOptions;
import options.OptionTree;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;
import options.learnerOptions.MealyLearnerChoice;
import options.learnerOptions.OracleOption;

public class JSS_figures extends SIMPA{
	
	static MealyLearnerChoice learnerChoice=automataChoice.mealyLearnerChoice;

	static void resetInferenceOption() {
		setUseDT(false);
		modeOption.selectChoice(modeOption.stats);
		modeOption.stats.inferenceNb.getValueHolder().setValue(1);;
		modeOption.stats.makeGraphs.getValueHolder().setValue(true);
	}

	static abstract class Config {
		abstract void set_up();

		abstract String name();
	}

	static final Config hWWithoutHeuristic = new Config() {
		@Override
		void set_up() {
			resetInferenceOption();
			HWOptions hW = learnerChoice.hW;
			learnerChoice.selectChoice(hW);
			hW.useReset.getValueHolder().setValue(false);
			hW.setUseAdaptiveH(false);
			hW.setUseAdaptiveW(false);
			hW.addHInW.getValueHolder().setValue(false);
			hW.useDictionary.getValueHolder().setValue(false);
			hW.checkInconsistenciesHMapping.getValueHolder().setValue(false);
			hW.searchCeInTrace.getValueHolder().setValue(false);
			hW.setUsePrecomputedW(false);
		}

		@Override
		String name() {
			return "hW Without Heuristic";
		}
	};
	static final Config hW_addHInW = new Config() {
		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			learnerChoice.hW.addHInW.getValueHolder().setValue(true);
		}

		@Override
		String name() {
			return "hW add h in W";
		}
	};
	static final Config hw_hzxw = new Config() {

		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			learnerChoice.hW.useDictionary.getValueHolder().setValue(true);
		}

		@Override
		String name() {
			return "hW hzxw";
		}
	};
	static final Config hw_3rd = new Config() {

		@Override
		void set_up() {
			hWWithoutHeuristic.set_up();
			learnerChoice.hW.checkInconsistenciesHMapping.getValueHolder()
					.setValue(true);
		}

		@Override
		String name() {
			return "hW 3rd inconsitencies";
		}
	};
	static final Config hW_heuristicsNoTrace = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			HWOptions hW = learnerChoice.hW;
			learnerChoice.selectChoice(hW);
			hW.useReset.getValueHolder().setValue(false);
			hW.setUseAdaptiveH(false);
			hW.setUseAdaptiveW(false);
			hW.addHInW.getValueHolder().setValue(true);
			hW.useDictionary.getValueHolder().setValue(true);
			hW.checkInconsistenciesHMapping.getValueHolder().setValue(true);
			hW.searchCeInTrace.getValueHolder().setValue(false);
			hW.setUsePrecomputedW(false);
		}

		@Override
		String name() {
			return "hW_heuristicNoTrace";
		}
	};
	static final Config hWWithAllHeuristics = new Config() {

		@Override
		void set_up() {
			hW_heuristicsNoTrace.set_up();
			learnerChoice.hW.searchCeInTrace.getValueHolder().setValue(true);
		}

		@Override
		String name() {
			return "hW all heuristics";
		}

	};
	static final Config hWWithKnownW = new Config() {

		@Override
		void set_up() {
			hWWithAllHeuristics.set_up();
			learnerChoice.hW.setUsePrecomputedW(true);
		}

		@Override
		String name() {
			return "hW known W";
		}

	};
	static final Config hWWithReset = new Config() {

		@Override
		void set_up() {
			hWWithAllHeuristics.set_up();
			learnerChoice.hW.useReset.getValueHolder().setValue(true);
		}

		@Override
		String name() {
			return "hW with reset";
		}

	};
	static final Config RS = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			RivestSchapireOptions RSchoice = learnerChoice.rivestSchapire;
			learnerChoice.selectChoice(RSchoice);
			RSchoice.setProbabilisticRS(true);
		}

		@Override
		String name() {
			return "Rivest&Schapire";
		}
	};
	static final Config locW = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			LocalizerBasedOptions lw = learnerChoice.localizerBased;
			learnerChoice.selectChoice(lw);
			lw.setUseSpeedUp(false);
		}

		@Override
		String name() {
			return "LocW";
		}
	};
	static final Config Lm = new Config() {

		@Override
		void set_up() {
			resetInferenceOption();
			learnerChoice.selectChoice(learnerChoice.lm);
		}

		@Override
		String name() {
			return "LM";
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

	private static void updateWithDriver(TransparentMealyDriver driver) {
		int resets;
		int length;

			Mealy automaton = driver.getAutomata();
			resets = 1;
			length = automaton.getStateCount()
					* driver.getInputSymbols().size() * 4500;
			length = 50000000;
			length = (int) Math.pow(
					driver.getInputSymbols().size(), automaton.getStateCount())
					* 2;
			if (random)
				length = (int) (Math
						.pow(automaton.getStateCount()
								* driver.getInputSymbols().size(), 0.7)
						* 500.);
			if (learnerChoice.getSelectedItem() == learnerChoice.lm
					|| learnerChoice.getSelectedItem() == learnerChoice.tree
					|| (learnerChoice.getSelectedItem() == learnerChoice.hW
							&& learnerChoice.hW.useReset.isEnabled())) {
				resets = automaton.getStateCount()
						* driver.getInputSymbols().size() * 10000;
				resets = (int) Math.pow(
						driver.getInputSymbols().size() / 2 + 1,
						automaton.getStateCount() / 4 + 2) * 10;
				resets = 100000;
				if (driver.getInputSymbols().size() < 5
						&& automaton.getStateCount() < 10)
				resets = 1000;
				if (driver.getInputSymbols().size() == 12
						&& automaton.getStateCount() == 15)
					resets = 1400000;
				if (driver.getInputSymbols().size() == 13
						&& automaton.getStateCount() == 17)
				resets = 100000;// 600000 but memory
				if (driver.getInputSymbols().size() == 12
						&& automaton.getStateCount() == 9)
					resets = 500000;
				length = automaton.getStateCount() * 2;
			}
			// 22 states 8 inputs
			// 16 states 9 inputs
			// 17 states 13 inputs -> plus de 100000 reset
			// 15 states 12 inputs -> + de 500000 reset
			// 9 states 12 inputs -> + de 100000 reset
			System.out.println("Maximum counter example length set to "
					+ length
					+ " and maximum counter example reset set to "
					+ resets + " from topology of driver ("
					+ automaton.getStateCount() + " states and "
					+ driver.getInputSymbols().size() + " inputs).");
		OracleOption oracle = getOracleOptions();
		if (oracle != null) {
			oracle.mrBean.setMaxTraceLength(length);
			oracle.mrBean.setMaxTraceNumber(resets);
		}
		else {
			System.out.println("no oracle found");
		}

		if (learnerChoice.getSelectedItem() == learnerChoice.localizerBased
				|| (learnerChoice
						.getSelectedItem() == learnerChoice.rivestSchapire)
						&& learnerChoice.rivestSchapire.probabilisticRS()) {
			int nb_states = driver.getAutomata()
					.getStateCount();
			if (learnerChoice.getSelectedItem() == learnerChoice.localizerBased)
				learnerChoice.localizerBased.setStateNumberBound(nb_states);
			else
				learnerChoice.rivestSchapire.setStateNumberBound(nb_states);
		}
	}

	protected static void learnOneTime(Config config) throws Exception {
		config.set_up();
		System.out.println("Using config : " + config.name());
		boolean error = false;
		int errorNb = 0;
		do {
			error = false;
				System.out.println(new Date());
			error = !learnAndSaveOneTime();
				if (++errorNb > 100) {
					System.err.println("too many errors occured");
				throw new RuntimeException("cannot infer");
				}
		} while (error);
	}

	static int configNb = 0;
	protected static void run_stats(Config config) {
		config.set_up();
		configNb++;
		if (configNb < 106)
			return;
		if (random)
			System.out.println("states " + Options.MAXSTATES);
		else
			System.out.println(url);
		System.out.println(learnerChoice.getSelectedItem().displayName);
		if (getOracleOptions() != null)
			System.out
					.println(getOracleOptions().getSelectedItem().displayName);
		File f = Options.getStatsCSVDir();
		if (!f.isDirectory() && !f.mkdirs() && !f.canWrite())
			throw new RuntimeException("Unable to create/write " + f.getName());

		assert (modeOption.getSelectedItem() == modeOption.stats);
		System.out.println(
				"[+] Testing " + modeOption.stats.inferenceNb.getValue()
						+ " automaton for configuration number " + configNb);

		for (int i = 1; i <= modeOption.stats.inferenceNb.getValue(); i++) {
			Runtime.getRuntime().gc();
			System.out.println(
					"\t" + i + "/" + modeOption.stats.inferenceNb.getValue());
			setUpDriverOption();
			try {
				learnOneTime(config);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}


	public static void main(String[] args)
	{

		makeGraphs();
		// System.exit(0);

		// heuristic comparison on states
		random = true;
		resetInferenceOption();
		learnerChoice.selectChoice(learnerChoice.hW);
		learnerChoice.hW.setUseAdaptiveH(false);
		learnerChoice.hW.setUseAdaptiveW(false);

		getOracleOptions().selectChoice(getOracleOptions().mrBean);
		Options.MININPUTSYM = Options.MAXINPUTSYM = 2;
		Options.MAXOUTPUTSYM = Options.MINOUTPUTSYM = 2;
		learnerChoice.hW.searchCeInTrace.getValueHolder().setValue(false);
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
				run_stats(config);
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
				run_stats(config);
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
				run_stats(config);
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
			try {
				url = new URL(automata);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			for (Config config : new Config[] { hWWithAllHeuristics,
					hWWithKnownW, RS, locW, Lm }) {
				config.set_up();
				if ((learnerChoice
						.getSelectedItem() == learnerChoice.localizerBased
						|| (learnerChoice.getSelectedItem() == learnerChoice.hW
								&& learnerChoice.hW.usePrecomputedW()))
						&& wSetTooLarge.contains(automata)) {
					System.out.println("skiped");
					continue;
				}
				for (Boolean useDT : new Boolean[] { true, false }) {
					run_stats(config);
					setUseDT(useDT);
				}
			}
		}

		for (String automata : reset) {
			random = false;
			try {
				url = new URL(automata);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			for (Config config : new Config[] { hWWithReset, Lm }) {
				config.set_up();
				for (Boolean useDT : new Boolean[] { true, false }) {
					setUseDT(useDT);
					run_stats(config);
				}
			}
		}
		makeGraphs();
		System.out.println("[+] End");
	}

	static OracleOption getOracleOptions() {
		for (OptionTree o : learnerChoice.getAllSelectedChildren()) {
			if (o instanceof OracleOption) {
				return (OracleOption) o;
			}
		}
		return null;
	}

	static void setUseDT(boolean enable) {
		OracleOption oracle = getOracleOptions();
		if (oracle == null)
			return;
				if (enable) {
					oracle.selectChoice(oracle.distinctionTreeBased);
				} else {
					oracle.selectChoice(oracle.mrBean);
		}
	}


}
