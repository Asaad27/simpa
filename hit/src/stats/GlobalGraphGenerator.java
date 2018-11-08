package stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.RandomOneOutputDiffMealyDriver;
import learner.mealy.combinatorial.CutterCombinatorialStatsEntry;
import learner.mealy.hW.HWGraphGenerator;
import learner.mealy.hW.HWStatsEntry;
import learner.mealy.localizerBased.LocalizerBasedStatsEntry;
import learner.mealy.rivestSchapire.RivestSchapireStatsEntry;
import learner.mealy.table.LmStatsEntry;
import learner.mealy.tree.ZStatsEntry;
import main.simpa.Options;
import stats.Graph.PlotStyle;
import stats.attribute.Attribute;
import stats.attribute.ComputedAttribute;
import stats.attribute.restriction.ClassRestriction;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.InSetRestriction;
import stats.attribute.restriction.RangeRestriction;
import stats.attribute.restriction.Restriction;

/**
 * This class aim to make graphs with data of multiple kind of inferences
 */
public class GlobalGraphGenerator extends GraphGenerator {
	private StatsSet locW = null;
	private StatsSet hW__ = null;
	private StatsSet combinatorialPruning = null;
	private StatsSet rivestSchapire = null;
	private StatsSet lm = null;
	private StatsSet zQ = null;

	public void add(StatsSet s) {
		if (s.size() == 0)
			return;
		StatsEntry s1 = s.get(0);
		if (s1 instanceof LocalizerBasedStatsEntry)
			locW = s;
		if (s1 instanceof HWStatsEntry)
			hW__ = s;
		if (s1 instanceof RivestSchapireStatsEntry)
			rivestSchapire = s;
		if (s1 instanceof CutterCombinatorialStatsEntry)
			combinatorialPruning = s;
		if (s1 instanceof LmStatsEntry) {
			lm = s;
		} else if (s1 instanceof ZStatsEntry)
			zQ = s;
	}

	public void generate() {
		String outdir = Options.OUTDIR;
		File outFile = new File(outdir + "/../RS_Comb_ICTSS");
		outFile.mkdir();
		Options.OUTDIR = outFile.getAbsolutePath();
		if (rivestSchapire != null && combinatorialPruning != null
				&& locW != null && hW__ != null) {
			makeRS_Comb_NoReset();
			generate_ICGI2018();
		}
		generate_JSS2018();
		Options.OUTDIR = outdir;
	}

	@Override
	public void generate(StatsSet s) {
		add(s);
		generate();
	}

	private void makeRS_Comb_NoReset() {
		assert rivestSchapire != null && combinatorialPruning != null && locW != null;
		Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER, Attribute.TRACE_LENGTH);
		StatsSet NRw1 = new StatsSet(locW);
		NRw1.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 1));
		StatsSet NRw2 = new StatsSet(locW);
		NRw2.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 2));
		StatsSet RS = new StatsSet(rivestSchapire);
		StatsSet comb = new StatsSet(combinatorialPruning);

		Restriction automaRestriction = new EqualsRestriction<String>(Attribute.AUTOMATA,
				new RandomMealyDriver().getSystemName());
		Restriction inputsRestriction = new EqualsRestriction<>(Attribute.INPUT_SYMBOLS, 5);
		Restriction outputsRestriction = new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 5);

		Restriction[] restrictions = new Restriction[] { automaRestriction, inputsRestriction, outputsRestriction };

		NRw1.restrict(restrictions);
		NRw2.restrict(restrictions);
		RS.restrict(restrictions);
		comb.restrict(restrictions);

		g.setDataDescriptionFields(
				new Attribute[] { Attribute.INPUT_SYMBOLS, Attribute.OUTPUT_SYMBOLS, Attribute.AUTOMATA });
		g.plot(NRw1, PlotStyle.MEDIAN, "ICTSS2015, p=1");
		g.plot(NRw2, PlotStyle.MEDIAN, "ICTSS2015, p=2");
		g.plot(RS, PlotStyle.MEDIAN, "\\\\\\\\algRS*");
		g.plot(comb, PlotStyle.MEDIAN, "combinatorial with pruning");

		g.forceOrdRange(null, 100000);
		g.setFileName("influence_of_states_number_length");
		g.export();
	}
	
	private void generate_JSS2018() {
		if (hW__==null)return;
	}

	/**
	 * generate figures for ICGI2018 paper.
	 */
	private void generate_ICGI2018() {
		String filePrefix = "ICGI2018_";
		Restriction[] hWAdaptiveRestrictions = new Restriction[] {
				new ClassRestriction<>(HWStatsEntry.class),
				new EqualsRestriction<>(Attribute.ADD_H_IN_W, false),
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, true),
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, true),
				new EqualsRestriction<>(HWStatsEntry.REUSE_HZXW, true),
				new EqualsRestriction<>(HWStatsEntry.CHECK_3rd_INCONSISTENCY,
						true),
				new EqualsRestriction<>(HWStatsEntry.SEARCH_CE_IN_TRACE,
						"simple"),
				new EqualsRestriction<>(HWStatsEntry.PRECOMPUTED_W, false),
				new EqualsRestriction<>(HWStatsEntry.ORACLE_USED, "MrBean") };
		OneConfigPlotter hW_adaptive_ploter = new OneConfigPlotter(
				hWAdaptiveRestrictions, Graph.PointShape.FILLED_TRIANGLE_DOWN,
				new Graph.Color(255, 200, 50), "adaptive hW");
		Restriction[] hWPresetRestrictions = new Restriction[] {
				new ClassRestriction<>(HWStatsEntry.class),
				new EqualsRestriction<>(Attribute.ADD_H_IN_W, false),
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, false),
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, false),
				new EqualsRestriction<>(HWStatsEntry.REUSE_HZXW, true),
				new EqualsRestriction<>(HWStatsEntry.CHECK_3rd_INCONSISTENCY,
						true),
				new EqualsRestriction<>(HWStatsEntry.SEARCH_CE_IN_TRACE,
						"simple"),
				new EqualsRestriction<>(HWStatsEntry.PRECOMPUTED_W, false),
				new EqualsRestriction<>(HWStatsEntry.ORACLE_USED, "MrBean") };
		OneConfigPlotter hW_preset_ploter = new OneConfigPlotter(
				hWPresetRestrictions, Graph.PointShape.FILLED_TRIANGLE_UP,
				new Graph.Color(50, 255, 255), "preset hW");
		Restriction[] locWRestrictions = new Restriction[] {
				new ClassRestriction<>(LocalizerBasedStatsEntry.class),
				new EqualsRestriction<Integer>(Attribute.W_SIZE, 2),
				new EqualsRestriction<Boolean>(Attribute.WITH_SPEEDUP, false) };
		OneConfigPlotter locW_ploter = new OneConfigPlotter(locWRestrictions,
				Graph.PointShape.EMPTY_CIRCLE, new Graph.Color(0, 0, 255),
				"locW");
		Restriction[] RS_Restrictions = new Restriction[] {
				new ClassRestriction<>(RivestSchapireStatsEntry.class),
				new EqualsRestriction<>(RivestSchapireStatsEntry.H_IS_GIVEN,
						false), };
		OneConfigPlotter RS_ploter = new OneConfigPlotter(RS_Restrictions,
				Graph.PointShape.EMPTY_DIAMOND, new Graph.Color(255, 0, 255),
				"Rivest and Schapire");
		Restriction[] ZQ_Restrictions = new Restriction[] {
				new ClassRestriction<>(ZStatsEntry.class), };
		OneConfigPlotter ZQ_ploter = new OneConfigPlotter(ZQ_Restrictions,
				Graph.PointShape.EMPTY_SQUARE, new Graph.Color(255, 0, 0),
				"Z-Quotient");
		Restriction[] Lm_Restrictions = new Restriction[] {
				new ClassRestriction<>(LmStatsEntry.class), };
		OneConfigPlotter Lm_ploter = new OneConfigPlotter(Lm_Restrictions,
				Graph.PointShape.TIMES_CROSS, new Graph.Color(0, 150, 0), "Lm");
		
		PlotStyle style = PlotStyle.AVERAGE;
		hW_adaptive_ploter.setPlotStyle(style);
		hW_preset_ploter.setPlotStyle(style);
		locW_ploter.setPlotStyle(style);
		RS_ploter.setPlotStyle(style);
		ZQ_ploter.setPlotStyle(style);
		Lm_ploter.setPlotStyle(style);

		int height = 7;
		int noKeyHeight = 5;
		int width = 8;
		Attribute<?>[] description = new Attribute[] {};
		{
			StatsSet set = new StatsSet();
			Integer[] keptStates = new Integer[] { 5, 10, 15, 20, 25, 30, 40,
					55, 75, 100, 130, 170, 220, 290, 375, 500, 625, 800, 1000,
					1300, 1700, 2200, 2900, 3000 };
			keptStates = new Integer[] { 5, 15, 25, 40, 55, 75, 100, 130, 170,
					220, 290, 375, 500, 625, 800, 1000, 1300, 1700, 2200, 2900,
					3000 };
			Restriction statesRestriction = new InSetRestriction<>(
					Attribute.STATE_NUMBER, keptStates);
			set.add(new StatsSet(locW, statesRestriction));
			set.add(new StatsSet(hW__, statesRestriction));
			set.add(new StatsSet(rivestSchapire, statesRestriction));
			set.add(new StatsSet(lm, statesRestriction));
			set.add(new StatsSet(zQ,
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 130)));

			set.restrict(new EqualsRestriction<String>(Attribute.AUTOMATA,
					new RandomMealyDriver().getSystemName()));
			set.restrict(new EqualsRestriction<>(Attribute.INPUT_SYMBOLS, 2));
			set.restrict(new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 2));
			set.restrict(statesRestriction);
			set.restrict(
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 220));
			Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER,
					Attribute.TRACE_LENGTH);
			g.setForArticle(true);
			g.setSize(width, noKeyHeight);
			g.setTitle("");
			g.getKeyParameters().disable();
			g.setDataDescriptionFields(description);
			hW_adaptive_ploter.plotOn(g, set);
			hW_preset_ploter.plotOn(g, set);
			RS_ploter.plotOn(g, set);
			locW_ploter.plotOn(g, set);
			Lm_ploter.plotOn(g, set);
			ZQ_ploter.plotOn(g, set);
			g.forceAbsRange(0, 200);
			g.setXTics(50);
			g.setForceOrdLogScale(false);
			g.forceOrdRange(null, 100000);
			g.setYTics(25000);
			g.setForceAbsLogScale(false);
			g.setFileName(filePrefix + "trace_length-comparaison_no_reset");
			g.export();
		}

		{
			// hardRandom
			StatsSet set = new StatsSet();
			Integer[] keptStates = new Integer[] { 5, 10, 20, 30, 40, 55, 75,
					100, 130, 170, };
			Restriction statesRestriction = new InSetRestriction<>(
					Attribute.STATE_NUMBER, keptStates);
			set.add(new StatsSet(locW,
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 70)));
			set.add(new StatsSet(hW__, statesRestriction));
			set.add(new StatsSet(rivestSchapire,
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 40)));
			set.add(new StatsSet(lm, statesRestriction));
			set.add(new StatsSet(zQ,
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 55)));

			set.restrict(new EqualsRestriction<String>(Attribute.AUTOMATA,
					new RandomOneOutputDiffMealyDriver().getSystemName()));
			set.restrict(new EqualsRestriction<>(Attribute.INPUT_SYMBOLS, 5));
			set.restrict(new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 2));
			set.restrict(
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 200));
			set.restrict(statesRestriction);
			Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER,
					Attribute.TRACE_LENGTH);
			g.setForArticle(true);
			g.setSize(width, noKeyHeight);
			g.setTitle("");
			g.getKeyParameters().disable();
			g.setDataDescriptionFields(description);
			hW_adaptive_ploter.plotOn(g, set);
			hW_preset_ploter.plotOn(g, set);
			RS_ploter.plotOn(g, set);
			locW_ploter.plotOn(g, set);
			Lm_ploter.plotOn(g, set);
			ZQ_ploter.plotOn(g, set);
			g.forceAbsRange(0, 150);
			g.setXTics(50);
			g.setForceOrdLogScale(false);
			g.forceOrdRange(null, 200000);
			g.setYTics(50000);
			g.setForceAbsLogScale(false);
			g.setFileName(filePrefix
					+ "trace_length-comparaison_no_reset_hardRandom");
			g.export();
		}
		{
			// CE comparison
			StatsSet set = new StatsSet();
			Integer[] keptStates = new Integer[] { 5, 10, 15, 20, 25, 30, 40,
					55, 75, 100, 130, 170, 220, 290, 375, 500, 625, 800, 1000,
					1300, 1700, 2200, 2900, 3000 };
			Restriction statesRestriction = new InSetRestriction<>(
					Attribute.STATE_NUMBER, keptStates);
			set.add(new StatsSet(hW__, statesRestriction));
			set.add(new StatsSet(rivestSchapire, statesRestriction));
			set.add(new StatsSet(lm, statesRestriction));
			set.add(new StatsSet(zQ,
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 130)));

			set.restrict(new EqualsRestriction<String>(Attribute.AUTOMATA,
					new RandomMealyDriver().getSystemName()));
			set.restrict(new EqualsRestriction<>(Attribute.INPUT_SYMBOLS, 2));
			set.restrict(new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 2));
			set.restrict(
					new RangeRestriction<>(Attribute.STATE_NUMBER, 0, 220));
			set.restrict(statesRestriction);
			Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER,
					Attribute.ASKED_COUNTER_EXAMPLE);
			g.setForArticle(true);
			g.setSize(width, height);
			g.setTitle("");
			g.getKeyParameters().setOutside(true);
			g.setDataDescriptionFields(description);
			hW_adaptive_ploter.plotOn(g, set);
			hW_preset_ploter.plotOn(g, set);
			RS_ploter.plotOn(g, set);
			locW_ploter.plotOn(g, set);
			Lm_ploter.plotOn(g, set);
			ZQ_ploter.plotOn(g, set);
			g.forceOrdRange(2, 100);
			g.setYTics(new Integer[] { 2, 10, 100 });
			g.setYTics(10);
			g.setXTics(50);
			g.forceAbsRange(null, 200);
			g.setForceOrdLogScale(true);
			g.setForceAbsLogScale(false);
			g.setFileName(filePrefix + "counter_exemple_comparaison");
			g.export();
		}
		{
			// comparison by inputs
			StatsSet set = new StatsSet();
			set.add(new StatsSet(hW__));
			set.add(combinatorialPruning);
			set.add(new StatsSet(rivestSchapire));
			set.add(locW);
			set.add(new StatsSet(lm));
			set.add(new StatsSet(zQ));

			set.restrict(new EqualsRestriction<String>(Attribute.AUTOMATA,
					new RandomMealyDriver().getSystemName()));
			set.restrict(new EqualsRestriction<>(Attribute.STATE_NUMBER, 30));
			set.restrict(new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 2));
			set.restrict(new InSetRestriction<>(Attribute.INPUT_SYMBOLS,
					new Integer[] { 2, 10, 20, 30, 40, 50, 60, 70, 80 }));
			Graph<Integer, Integer> g = new Graph<>(Attribute.INPUT_SYMBOLS,
					Attribute.TRACE_LENGTH);
			g.setForArticle(true);
			g.setSize(width, height);
			g.setTitle("");
			g.getKeyParameters().setOutside(true);
			g.setDataDescriptionFields(description);
			hW_adaptive_ploter.plotOn(g, set);
			hW_preset_ploter.plotOn(g, set);
			RS_ploter.plotOn(g, set);
			locW_ploter.plotOn(g, set);
			Lm_ploter.plotOn(g, set);
			ZQ_ploter.plotOn(g, set);
			g.forceOrdRange(0, 250000);
			g.setForceOrdLogScale(false);
			g.setForceAbsLogScale(false);
			g.setFileName(filePrefix + "inputs_comparaison");
			g.export();
		}
		createTable();
	}

	/**
	 * create a latex table to compare algorithms. Used for paper ICGI 2018
	 */
	public void createTable() {
		StatsSet all = new StatsSet();

		all.getStats().addAll(locW.getStats());
		all.getStats().addAll(hW__.getStats());
		all.getStats().addAll(rivestSchapire.getStats());
		all.restrict(new Restriction() {
			{
				setTitle("from dot file");
			}

			@Override
			public boolean contains(StatsEntry s) {
				return s.get(HWStatsEntry.AUTOMATA).startsWith("dot_file");
			}
		});
		Set<String> names = all.sortByAtribute(Attribute.AUTOMATA).keySet();

		class LearnerSet {
			public LearnerSet(StatsSet set, String title) {
				super();
				this.set = set;
				this.title = title;
			}

			StatsSet set;
			String title;
		}
		List<LearnerSet> sets = new ArrayList<>();
		StatsSet hW_Base = new StatsSet(hW__);
		hW_Base.restrict(
				new EqualsRestriction<Boolean>(Attribute.REUSE_HZXW, true));
		hW_Base.restrict(new EqualsRestriction<Boolean>(
				Attribute.CHECK_3rd_INCONSISTENCY, true));
		hW_Base.restrict(new EqualsRestriction<String>(
				Attribute.SEARCH_CE_IN_TRACE, "simple"));
		hW_Base.restrict(
				new EqualsRestriction<Boolean>(Attribute.ADD_H_IN_W, false));
		hW_Base.restrict(HWGraphGenerator.withUnknownWRestriction);

		StatsSet hW_preset = new StatsSet(hW_Base);
		StatsSet hW_adaptive = new StatsSet(hW_Base);
		hW_preset.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, false));
		hW_preset.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, false));
		hW_adaptive.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, true));
		hW_adaptive.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, true));
		StatsSet hW_knownW = new StatsSet(hW__);
		hW_knownW.restrict(
				new EqualsRestriction<Boolean>(Attribute.REUSE_HZXW, true));
		hW_knownW.restrict(new EqualsRestriction<Boolean>(
				Attribute.CHECK_3rd_INCONSISTENCY, true));
		hW_knownW.restrict(new EqualsRestriction<String>(
				Attribute.SEARCH_CE_IN_TRACE, "simple"));
		hW_knownW.restrict(
				new EqualsRestriction<Boolean>(Attribute.ADD_H_IN_W, false));
		hW_knownW.restrict(HWGraphGenerator.withKnownWRestriction);
		hW_knownW.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, false));
		hW_knownW.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, true));

		// sets.add(new LearnerSet(hW_adaptive, "\\parbox[t]{2cm}{hW
		// with\\\\adaptive}"));
		sets.add(new LearnerSet(rivestSchapire, "Rivest\\&Schapire"));
		sets.add(new LearnerSet(hW_preset, "preset hW"));
		sets.add(new LearnerSet(hW_adaptive, "adaptive hW"));

		if (lm != null)
			sets.add(new LearnerSet(lm, "Lm (\\#resets)"));
		// if (zQ != null)
		// sets.add(new LearnerSet(zQ, "Z-Quotient (\\#resets)"));

		sets.add(null);
		sets.add(new LearnerSet(locW, "LocW"));
		sets.add(new LearnerSet(hW_knownW,
				"\\parbox[t]{2cm}{hW with a\\\\provided $W$-set}"));

		HashMap<String, String> filesNames = new HashMap<>();

		filesNames.put("dot_file(TCP_Windows8_Server__connected)",
				"TCP W8 server");
		filesNames.put("dot_file(bankcard_4_learnresult_SecureCode)",
				"4_learnresult_SecureCode");
		filesNames.put("dot_file(bankcard_ASN_learnresult_SecureCode)",
				"ASN_learnresult_SecureCode");
		filesNames.put(
				"dot_file(edentifier2_learnresult_new_device-simple_fix)",
				"Edentifier2 new device");
		filesNames.put(
				"dot_file(edentifier2_learnresult_old_device-simple_fix)",
				"Edentifier2 old device");
		filesNames.put("dot_file(mqtt_unknown)", "unknown");
		filesNames.put("dot_file(mqtt_unknown1)", "unknown1");
		filesNames.put("dot_file(mqtt_ActiveMQ__invalid)", "ActiveMQ invalid");
		filesNames.put("dot_file(mqtt_ActiveMQ__non_clean)",
				"ActiveMQ non_clean");
		filesNames.put("dot_file(mqtt_mosquitto__two_client_will_retain)",
				"mqtt 2 client ret.");
		filesNames.put("dot_file(mqtt_analyse)", "analyse");
		filesNames.put("dot_file(mqtt_mosquitto__two_client_same_id)",
				"mqtt 2 client 1 id");
		filesNames.put("dot_file(mqtt_mosquitto__two_client)",
				"mqtt two_client");
		filesNames.put("dot_file(mqtt_mosquitto__single_client)",
				"mqtt single_client");
		filesNames.put("dot_file(mqtt_mosquitto__non_clean)", "mqtt non_clean");
		filesNames.put("dot_file(mqtt_mosquitto__mosquitto)", "mqtt mqtt");
		filesNames.put("dot_file(mqtt_mosquitto__invalid)", "mqtt invalid");
		filesNames.put("dot_file(mqtt_emqtt__two_client_same_id)",
				"emqtt 2 client 1 id");
		filesNames.put("dot_file(mqtt_emqtt__two_client)", "emqtt 2 client");
		filesNames.put("dot_file(mqtt_emqtt__single_client)", "emqtt 1 client");
		filesNames.put("dot_file(mqtt_emqtt__simple)", "emqtt simple");
		filesNames.put("dot_file(mqtt_emqtt__non_clean)", "emqtt non_clean");
		filesNames.put("dot_file(mqtt_emqtt__invalid)", "emqtt invalid");
		filesNames.put("dot_file(mqtt_VerneMQ__two_client_same_id)",
				"VerneMQ 2 client 1 id");
		filesNames.put("dot_file(mqtt_VerneMQ__two_client)",
				"VerneMQ 2 client");
		filesNames.put("dot_file(mqtt_VerneMQ__single_client)",
				"VerneMQ 1 client");
		filesNames.put("dot_file(mqtt_VerneMQ__simple)", "VerneMQ simple");
		filesNames.put("dot_file(mqtt_VerneMQ__non_clean)",
				"VerneMQ non_clean");
		filesNames.put("dot_file(mqtt_VerneMQ__invalid)", "VerneMQ invalid");
		filesNames.put("dot_file(mqtt_ActiveMQ__two_client_will_retain)",
				"ActiveMQ 2 client ret.");
		filesNames.put("dot_file(mqtt_VerneMQ__two_client_will_retain)",
				"VerneMQ 2 client ret.");
		filesNames.put("dot_file(tcp_TCP_FreeBSD_Server)",
				"TCP_FreeBSD_Server");
		filesNames.put("dot_file(tcp_TCP_Linux_Client)", "TCP_Linux_Client");
		filesNames.put("dot_file(tcp_TCP_Linux_Server)", "TCP_Linux_Server");
		filesNames.put("dot_file(tcp_TCP_Windows8_Client)",
				"TCP_Windows8_Client");
		filesNames.put("dot_file(tcp_TCP_Windows8_Server)",
				"TCP_Windows8_Server");
		filesNames.put("dot_file(tls_GnuTLS_3.3.8_server_regular)",
				"GnuTLS_3.3.8_server_regular");
		filesNames.put("dot_file(tls_miTLS_0.1.3_server_regular)",
				"miTLS_0.1.3_server_regular");
		filesNames.put("dot_file(tls_NSS_3.17.4_client_full)",
				"NSS_3.17.4_client_full");
		filesNames.put("dot_file(tls_NSS_3.17.4_client_regular)",
				"NSS_3.17.4_client_regular");
		filesNames.put("dot_file(tls_NSS_3.17.4_server_regular)",
				"NSS_3.17.4_server_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1g_client_regular)",
				"OpenSSL_1.0.1g_client_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1g_server_regular)",
				"OpenSSL_1.0.1g_server_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1j_client_regular)",
				"OpenSSL_1.0.1j_client_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1j_server_regular)",
				"OpenSSL_1.0.1j_server_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1l_client_regular)",
				"OpenSSL_1.0.1l_client_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.1l_server_regular)",
				"OpenSSL_1.0.1l_server_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.2_client_full)",
				"OpenSSL_1.0.2_client_full");
		filesNames.put("dot_file(tls_OpenSSL_1.0.2_client_regular)",
				"OpenSSL_1.0.2_client_regular");
		filesNames.put("dot_file(tls_OpenSSL_1.0.2_server_regular)",
				"OpenSSL_1.0.2_server_regular");
		filesNames.put("dot_file(tls_RSA_BSAFE_C_4.0.4_server_regular)",
				"RSA_BSAFE_C_4.0.4_server_regular");
		filesNames.put("dot_file(tls_RSA_BSAFE_Java_6.1.1_server_regular)",
				"RSA_BSAFE_Java_6.1.1_server_regular");
		filesNames.put("dot_file(tls_JSSE_1.8.0_25_server_regular)",
				"JSSE_1.8.0_25_server_regular");
		filesNames.put("dot_file(tls_JSSE_1.8.0_31_server_regular)",
				"JSSE_1.8.0_31_server_regular");
		filesNames.put("dot_file(tls_GnuTLS_3.3.12_client_full)",
				"GnuTLS_3.3.12_client_full");
		filesNames.put("dot_file(tls_GnuTLS_3.3.12_client_regular)",
				"GnuTLS_3.3.12_client_regular");
		filesNames.put("dot_file(tls_GnuTLS_3.3.12_server_full)",
				"GnuTLS_3.3.12_server_full");
		filesNames.put("dot_file(tls_GnuTLS_3.3.12_server_regular)",
				"GnuTLS_3.3.12_server_regular");
		filesNames.put("dot_file(tls_GnuTLS_3.3.8_client_full)",
				"GnuTLS_3.3.8_client_full");
		filesNames.put("dot_file(tls_GnuTLS_3.3.8_client_regular)",
				"GnuTLS_3.3.8_client_regular");
		filesNames.put("dot_file(tls_GnuTLS_3.3.8_server_full)",
				"GnuTLS_3.3.8_server_full");
		filesNames.put("dot_file(toyModels_naiks)", "naiks");
		filesNames.put("dot_file(toyModels_lee_yannakakis_distinguishable)",
				"L.Yannakakis 6 states");
		filesNames.put("dot_file(toyModels_lee_yannakakis_non_distinguishable)",
				"L.Yannakakis 3 states");
		filesNames.put(
				"dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)",
				"dot_file(edentifier2_learnresult_old_device-simple_fix)_inf");
		filesNames.put(
				"dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)",
				"dot_file(edentifier2_learnresult_old_device-simple_fix)_inf");
		filesNames.put(
				"dot_file(out_dot_file(edentifier2_learnresult_old_device-simple_fix)_inf)",
				"dot_file(edentifier2_learnresult_old_device-simple_fix)_inf");

		Set<String> toIgnore = new HashSet<>();
		toIgnore.add("dot_file(out_dot_file(benchmark_heating_system)_inf");
		toIgnore.add("dot_file(benchmark_heating_system)");
		toIgnore.add("dot_file(benchmark_heating_system)");
		toIgnore.add("dot_file(toyModels_naiks)");
		toIgnore.add("dot_file(toyModels_lee_yannakakis_distinguishable)");
		toIgnore.add("dot_file(toyModels_lee_yannakakis_non_distinguishable)");

		PrintStream out = System.out;
		

		List<String> sortedNames = new ArrayList<>();
		List<Integer> sizes = new ArrayList<>();
		for (String name : names) {
			StatsSet automaton = new StatsSet(all);
			automaton.restrict(
					new EqualsRestriction<String>(Attribute.AUTOMATA, name));
			int size = getSortForTable(automaton);
			int i;
			for (i = 0; i < sizes.size() && sizes.get(i) <= size; i++) {
			}
			sizes.add(i, size);
			sortedNames.add(i, name);
		}
		
		// traceLength table

		try {
			out = new PrintStream(
					"/tmp/benchmarkTableTraceLength.tex");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = System.out;
		}
		out.println();
		out.print("\\begin{tabular}{|l|l|");
		for (LearnerSet s : sets) {
			if (s != null)
				out.print("l|");
			else
				out.print('|');
		}
		out.println("}");
		out.println("\\hline");

		out.print("automata & \\rotatebox{75}{$|Q|\\times |I|$ }");

		for (LearnerSet s : sets) {
			if (s != null)
				out.print(" & \\rotatebox{75}{" + s.title + "}");
		}
		out.println("\\\\");
		out.println("\\hline");

		for (String name : sortedNames) {
			if (toIgnore.contains(name)) {
				System.out.println("ignoring " + name);
				continue;
			}
			String title = filesNames.get(name);
			if (title == null) {
				System.err.println("name unknown : " + name);
				continue;
			}
			out.print(title.replaceAll("_", "\\\\_"));
			StatsSet automaton = new StatsSet(all);
			automaton.restrict(
					new EqualsRestriction<String>(Attribute.AUTOMATA, name));
			out.print(" & " + (automaton.get(0).get(Attribute.STATE_NUMBER)
					* automaton.get(0).get(Attribute.INPUT_SYMBOLS)));
			for (LearnerSet s : sets) {
				if (s == null)
					continue;
				automaton = new StatsSet(s.set);
				automaton.restrict(new EqualsRestriction<String>(
						Attribute.AUTOMATA, name));
				int nb = automaton.size();
				if (nb == 0)
					out.print("& -");
				else {
					float traceLength = automaton
							.attributeAVG(Attribute.TRACE_LENGTH);
					out.print(" & " + (int) traceLength);
					if (automaton.get(0) instanceof ZStatsEntry) {

						float resets = automaton
								.attributeAVG(Attribute.RESET_CALL_NB);

						out.print(" (" + (int) resets + ")");
					}
					if (nb < 150)
						out.print(" (draft " + nb + ")");
				}
			}
			out.println("\\\\");
		}
		out.println("\\hline");
		out.println("\\end{tabular}");

	}

	private int getSortForTable(StatsSet s) {
		if (s.size() == 0)
			return -1;
		int size = s.get(0).get(Attribute.STATE_NUMBER)
				* s.get(0).get(Attribute.INPUT_SYMBOLS);
		return size;
	}

}
