package stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
import stats.attribute.restriction.HasAttributeRestriction;
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
		}
		if (hW__ != null && rivestSchapire != null && locW != null
				&& lm != null) {
			generate_ICGI2018();
		}
		// generate_JSS2018();
		generate_SANER19();
		Options.OUTDIR = outdir;
	}

	@Override
	public void generate(StatsSet s) {
		add(s);
		generate();
	}

	private void makeRS_Comb_NoReset() {
		assert rivestSchapire != null && combinatorialPruning != null
				&& locW != null;
		Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER,
				Attribute.TRACE_LENGTH);
		StatsSet NRw1 = new StatsSet(locW);
		NRw1.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 1));
		StatsSet NRw2 = new StatsSet(locW);
		NRw2.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 2));
		StatsSet RS = new StatsSet(rivestSchapire);
		StatsSet comb = new StatsSet(combinatorialPruning);

		Restriction automaRestriction = new EqualsRestriction<String>(
				Attribute.AUTOMATA, new RandomMealyDriver().getSystemName());
		Restriction inputsRestriction = new EqualsRestriction<>(
				Attribute.INPUT_SYMBOLS, 5);
		Restriction outputsRestriction = new EqualsRestriction<>(
				Attribute.OUTPUT_SYMBOLS, 5);

		Restriction[] restrictions = new Restriction[] { automaRestriction,
				inputsRestriction, outputsRestriction };

		NRw1.restrict(restrictions);
		NRw2.restrict(restrictions);
		RS.restrict(restrictions);
		comb.restrict(restrictions);

		g.setDataDescriptionFields(new Attribute[] { Attribute.INPUT_SYMBOLS,
				Attribute.OUTPUT_SYMBOLS, Attribute.AUTOMATA });
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
		StatsSet allStats = new StatsSet();
		allStats.add(zQ);
		allStats.add(hW__);
		allStats.add(rivestSchapire);
		allStats.add(locW);
		allStats.add(lm);

		allStats.restrict(new Restriction() {

			@Override
			public boolean contains(StatsEntry s) {
				if (s.get(Attribute.AUTOMATA)
						.startsWith("dot_file(BenchmarkCircuit"))
					return false;
				if (s.get(Attribute.AUTOMATA)
						.startsWith("dot_file(BenchmarkToyModels"))
					return false;
				if (s.get(Attribute.AUTOMATA)
						.startsWith("dot_file(BenchmarkQUICprotoco"))
					return false;
				if (s.get(Attribute.AUTOMATA)
						.startsWith("dot_file(BenchmarkCoffee"))
					return false;
				return true;
			}
		});

		String filePrefix = "JSS2018_";
		Restriction[] hWMrBeanRestrictions = new Restriction[] {
				new ClassRestriction<>(HWStatsEntry.class),
				// new EqualsRestriction<>(Attribute.ADD_H_IN_W, false),
				// new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, true),
				// new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, true),
				// new EqualsRestriction<>(HWStatsEntry.REUSE_HZXW, true),
				// new EqualsRestriction<>(HWStatsEntry.CHECK_3rd_INCONSISTENCY,
				// true),
				// new EqualsRestriction<>(HWStatsEntry.SEARCH_CE_IN_TRACE,
				// "simple"),
				// new EqualsRestriction<>(HWStatsEntry.PRECOMPUTED_W, false),
				new EqualsRestriction<>(HWStatsEntry.ORACLE_USED, "MrBean") };
		OneConfigPlotter hWMrBeanPlotter = new OneConfigPlotter(
				hWMrBeanRestrictions, Graph.PointShape.FILLED_TRIANGLE_DOWN,
				new Graph.Color(255, 200, 50), "hW MrBean");

		OneConfigPlotter hW_LY_plotter = new OneConfigPlotter(
				new Restriction[] { new ClassRestriction<>(HWStatsEntry.class),
						new EqualsRestriction<>(HWStatsEntry.ORACLE_USED,
								"distinctionTree + MrBean") },
				Graph.PointShape.EMPTY_TRIANGLE_DOWN,
				new Graph.Color(0, 200, 50), "LY");
		OneConfigPlotter RS_plotter = new OneConfigPlotter(new Restriction[] {
				new ClassRestriction<>(RivestSchapireStatsEntry.class),

		}, Graph.PointShape.EMPTY_SQUARE, new Graph.Color(255, 0, 0),
				"Rivest&schapire");

		PlotStyle defaultStyle = PlotStyle.CANDLESTICK;
		hWMrBeanPlotter.setPlotStyle(defaultStyle);
		hW_LY_plotter.setPlotStyle(defaultStyle);
		RS_plotter.setPlotStyle(defaultStyle);
		{
			Graph<String, Integer> g = new Graph<>(Attribute.AUTOMATA,
					Attribute.ORACLE_TRACE_LENGTH);
			g.setForArticle(true);
			// g.setSize(width, noKeyHeight);
			g.setTitle("");
			// g.getKeyParameters().disable();
			// g.setDataDescriptionFields(description);
			hWMrBeanPlotter.plotOn(g, allStats);
			hW_LY_plotter.plotOn(g, allStats);
			// g.forceAbsRange(0, 150);
			// g.setXTics(50);
			g.setForceOrdLogScale(true);
			// g.forceOrdRange(null, 200000);
			// g.setYTics(50000);
			g.setForceAbsLogScale(false);
			g.setFileName(filePrefix + "pouet");
			g.export();
		}
		{
			Graph<Integer, Integer> g = new Graph<>(
					ComputedAttribute.TRANSITION_COUNT, Attribute.TRACE_LENGTH);
			g.setForArticle(true);
			// g.setSize(width, noKeyHeight);
			g.setTitle("");
			// g.getKeyParameters().disable();
			g.setDataDescriptionFields(new Attribute<?>[] {});
			hWMrBeanPlotter.plotOn(g, allStats);
			hW_LY_plotter.plotOn(g, allStats);
			RS_plotter.plotOn(g, allStats);
			// g.forceAbsRange(0, 150);
			// g.setXTics(50);
			g.setForceOrdLogScale(true);
			// g.forceOrdRange(null, 200000);
			// g.setYTics(50000);
			g.setForceAbsLogScale(true);
			g.setFileName(filePrefix + "pouet2");
			g.export();
		}

		{

			StatsSet baseStats = new StatsSet(allStats);
			baseStats.restrict(new Restriction() {
				{
					setTitle("from dot file");
				}

				@Override
				public boolean contains(StatsEntry s) {
					return s.get(HWStatsEntry.AUTOMATA).startsWith("dot_file");
				}
			});
			List<TableRow> rows = new ArrayList<>();
			rows.add(new AutomataRow("MQTT ActiveMQ two client retain",
					"dot_file(BenchmarkMQTT_ActiveMQ__two_client_will_retain)"));
			rows.add(new AutomataRow("FromRhapsodyToDezyne 1",
					"dot_file(BenchmarkFromRhapsodyToDezyne_model1)"));
			rows.add(new AutomataRow("FromRhapsodyToDezyne 3",
					"dot_file(BenchmarkFromRhapsodyToDezyne_model3)"));
			SortedMap<Integer, List<String>> bySize = new TreeMap<>();
			Map<String, TableRow> automaticRows = new HashMap<>();
			for (StatsEntry e : new StatsSet(baseStats, new Restriction[] {
					new ClassRestriction<>(HWStatsEntry.class),
					new EqualsRestriction<>(Attribute.USE_RESET, false), })
							.getStats()) {
				String name = e.get(Attribute.AUTOMATA);
				if (!automaticRows.containsKey(name)) {
					String dispName = dot_to_short_name(name);

					automaticRows.put(name, new AutomataRow(dispName, name));

					Integer size = e.get(Attribute.STATE_NUMBER)
							* e.get(Attribute.INPUT_SYMBOLS);
					List<String> thisSize = bySize.get(size);
					if (thisSize == null) {
						thisSize = new ArrayList<>();
						bySize.put(size, thisSize);
					}
					thisSize.add(name);
				}
			}

			List<Restriction> hWBaseRestrictions = Arrays.asList(
					new ClassRestriction<>(HWStatsEntry.class),
					new EqualsRestriction<>(Attribute.USE_ADAPTIVE_W, false),
					new EqualsRestriction<>(Attribute.USE_ADAPTIVE_H, false),

					new EqualsRestriction<>(Attribute.REUSE_HZXW, true),
					new EqualsRestriction<>(Attribute.CHECK_3rd_INCONSISTENCY,
							true),
					new EqualsRestriction<>(Attribute.SEARCH_CE_IN_TRACE,
							"simple"));

			rows = new ArrayList<>(automaticRows.values());
			rows = new ArrayList<>();
			for (List<String> entry : bySize.values()) {
				Collections.sort(entry);
				for (String fileName : entry) {
					rows.add(automaticRows.get(fileName));
				}
			}
			List<TableColumn> columns = new ArrayList<>();
			{
				columns.add(new TransitionCol());
				columns.add(new MyCol("LocW", false, new Restriction[] {
						new ClassRestriction<>(LocalizerBasedStatsEntry.class),
						new EqualsRestriction<>(Attribute.W_SIZE, 2), }));
				List<Restriction> hWWithW = new ArrayList<>(hWBaseRestrictions);
				hWWithW.addAll(Arrays.asList(
						new EqualsRestriction<>(Attribute.MAX_W_SIZE, 2),
						new EqualsRestriction<>(Attribute.PRECOMPUTED_W,
								true)));

				columns.add(new MyCol("hW with known W", false,
						hWWithW.toArray(new Restriction[0])) {
					@Override
					String getTitle() {
						return super.title + "(#oracle)";
					}

					@Override
					String getData(StatsSet stats) {
						return super.getData(stats) + "(" + stats.attributeAVG(
								Attribute.ASKED_COUNTER_EXAMPLE) + ")";
					}
				});
				makeTable(
						"/home/nbremond/shared_articles/JSS2018/figures/benchmarkLocW.tex",
						baseStats, columns, rows);
			}
			columns.clear();
			// rows.clear();

			columns.add(new TransitionCol());
			List<Restriction> hWUnknownW = new ArrayList<>(hWBaseRestrictions);
			hWUnknownW.add(
					new EqualsRestriction<>(Attribute.PRECOMPUTED_W, false));
			ArrayList<Restriction> hWUnknownWNoReset = new ArrayList<>(
					hWUnknownW);
			hWUnknownWNoReset
					.add(new EqualsRestriction<>(Attribute.USE_RESET, false));

			MyCol hWCol = new MyCol("hW", false,
					hWUnknownWNoReset.toArray(new Restriction[0]));
			columns.add(hWCol);
			columns.add(new MyCol("RS", false, new Restriction[] {
					new ClassRestriction<>(RivestSchapireStatsEntry.class),
					new EqualsRestriction<>(Attribute.RS_WITH_GIVEN_H, false),

			}));
			MyCol lmCol = new MyCol("Lm", true, new Restriction[] {
					new ClassRestriction<>(LmStatsEntry.class), });
			columns.add(lmCol);

			columns.add(null);
			columns.add(new ComparisonCol(lmCol, hWCol));
			// columns.add(new TableColumn() {
			//
			// @Override
			// String getTitle() {
			// return "theorical complexity";
			// }
			//
			// @Override
			// StatsSet restrict(StatsSet set) {
			// return set;
			// }
			//
			// @Override
			// String getData(StatsSet stats) {
			// Integer inputs = stats
			// .attributeMax(Attribute.INPUT_SYMBOLS);
			// Float states = stats.attributeMax(Attribute.STATE_NUMBER)
			// .floatValue();
			// return "" + (int) (inputs * 75 * Math.pow(states, 1.28));
			// }
			// });

			StatsSet MrBean = new StatsSet(baseStats,
					new HasAttributeRestriction<>(Attribute.ORACLE_USED,
							"MrBean"));
			makeTable(
					"/home/nbremond/shared_articles/JSS2018/figures/benchmarkStrongMrBean.tex",
					MrBean, columns, rows);
			StatsSet DS = new StatsSet(baseStats, new HasAttributeRestriction<>(
					Attribute.ORACLE_USED, "distinctionTree + MrBean"));
			makeTable(
					"/home/nbremond/shared_articles/JSS2018/figures/benchmarkStrongDS.tex",
					DS, columns, rows);

			// now proceed not strongly connected

			Set<String> stonglyConnectd = new HashSet<>(automaticRows.keySet());

			bySize.clear();
			automaticRows.clear();
			for (StatsEntry e : new StatsSet(baseStats, new Restriction[] {
					new ClassRestriction<>(HWStatsEntry.class),
			})
							.getStats()) {
				String name = e.get(Attribute.AUTOMATA);
				if (stonglyConnectd.contains(name))
					continue;
				if (!automaticRows.containsKey(name)) {
					String dispName = dot_to_short_name(name);

					automaticRows.put(name, new AutomataRow(dispName, name));

					Integer size = e.get(Attribute.STATE_NUMBER)
							* e.get(Attribute.INPUT_SYMBOLS);
					List<String> thisSize = bySize.get(size);
					if (thisSize == null) {
						thisSize = new ArrayList<>();
						bySize.put(size, thisSize);
					}
					thisSize.add(name);
				}
			}
			rows.clear();
			for (List<String> entry : bySize.values()) {
				Collections.sort(entry);
				for (String fileName : entry) {
					rows.add(automaticRows.get(fileName));
				}
			}
			columns.clear();
			// columns.add(new MyCol("ZQuotient", true, new Restriction[] {
			// new ClassRestriction<>(ZStatsEntry.class),
			// }));
			columns.add(new TransitionCol());

			ArrayList<Restriction> hWUnknownWReset = new ArrayList<>(
					hWUnknownW);
			hWCol = new MyCol("hW", true,
					hWUnknownWReset.toArray(new Restriction[0]));
			columns.add(hWCol);
			// columns.add(new TableColumn() {
			//
			// @Override
			// String getTitle() {
			// return "theorical complexity";
			// }
			//
			// @Override
			// StatsSet restrict(StatsSet set) {
			// return set;
			// }
			//
			// @Override
			// String getData(StatsSet stats) {
			// Integer inputs = stats
			// .attributeMax(Attribute.INPUT_SYMBOLS);
			// Float states = stats.attributeMax(Attribute.STATE_NUMBER)
			// .floatValue();
			// return "" + (int) (inputs * 75 * Math.pow(states, 1.28));
			// }
			// });
			hWCol.dispOracle = true;
			lmCol.dispOracle = true;
			columns.add(lmCol);
			columns.add(new ComparisonCol(lmCol, hWCol));
			
			
			makeTable(
					"/home/nbremond/shared_articles/JSS2018/figures/benchmarkResetMrBean.tex",
					MrBean, columns, rows);
			makeTable(
					"/home/nbremond/shared_articles/JSS2018/figures/benchmarkResetDS.tex",
					DS, columns, rows);
		}
	}

	public static String dot_to_short_name(String name) {
		HashMap<String, String> filesNames = new HashMap<>();

		filesNames.put("dot_file(TCP_Windows8_Server__connected)",
				"TCP W8 server");
		filesNames.put("dot_file(bankcard_4_learnresult_SecureCode)",
				"4_learnresult_SecureCode");
		filesNames.put("dot_file(bankcard_ASN_learnresult_SecureCode)",
				"ASN_learnresult_SecureCode");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_new_device-simple_fix)",
				"Edentifier2 new device");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_old_device-simple_fix)",
				"Edentifier2 old device");
		filesNames.put("dot_file(BenchmarkMQTT_unknown)", "unknown");
		filesNames.put("dot_file(BenchmarkMQTT_unknown1)", "unknown1");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__invalid)",
				"ActiveMQ invalid");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__non_clean)",
				"ActiveMQ non_clean");
		filesNames.put(
				"dot_file(BenchmarkMQTT_mosquitto__two_client_will_retain)",
				"mqtt 2 client ret.");
		filesNames.put("dot_file(BenchmarkMQTT_analyse)", "analyse");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__two_client_same_id)",
				"mqtt 2 client 1 id");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__two_client)",
				"mqtt two_client");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__single_client)",
				"mqtt single_client");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__non_clean)",
				"mqtt non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__mosquitto)",
				"mqtt mqtt");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__invalid)",
				"mqtt invalid");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__two_client_same_id)",
				"emqtt 2 client 1 id");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__two_client)",
				"emqtt 2 client");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__single_client)",
				"emqtt 1 client");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__simple)", "emqtt simple");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__non_clean)",
				"emqtt non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__invalid)",
				"emqtt invalid");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__two_client_same_id)",
				"VerneMQ 2 client 1 id");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__two_client)",
				"VerneMQ 2 client");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__single_client)",
				"VerneMQ 1 client");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__simple)",
				"VerneMQ simple");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__non_clean)",
				"VerneMQ non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__invalid)",
				"VerneMQ invalid");
		filesNames.put(
				"dot_file(BenchmarkMQTT_ActiveMQ__two_client_will_retain)",
				"ActiveMQ 2 client ret.");
		filesNames.put(
				"dot_file(BenchmarkMQTT_VerneMQ__two_client_will_retain)",
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
		String dispName = name.substring(18, name.length() - 1);
		dispName = dispName.replaceAll("FromRhapsodyToDezyne_",
				"Rhapsody-Dezyne ");
		if (dispName.startsWith("Bankcard"))
			dispName = dispName.replaceAll("learnresult", "");
		dispName = dispName.replaceAll("TLS_GnuTLS", "GnuTLS");
		dispName = dispName.replaceAll("TLS_OpenSSL", "OpenSSL");
		dispName = dispName.replaceAll("TLS_RSA", "RSA");
		dispName = dispName.replaceAll("MQTT_VerneMQ__", "VerneMQ ");
		dispName = dispName.replaceAll("MQTT_ActiveMQ__", "ActiveMQ ");
		dispName = dispName.replaceAll("MQTT_emqtt__", "emqtt ");
		dispName = dispName.replaceAll("MQTT_mosquitto__", "Mosquitto ");
		dispName = dispName.replaceAll("Edentifier2_learnresult_",
				"Edentifier2 ");
		dispName = dispName.replaceAll("-simple_fix", "");
		dispName = dispName.replaceAll("will_retain", "ret.");
		dispName = dispName.replaceAll("same_id", "1 id");
		dispName = dispName.replaceAll("two_client", "2 client");
		dispName = dispName.replaceAll("model3", "3");
		dispName = dispName.replaceAll("_", " ");
		return dispName;
	}

	String stringToLatex(String str) {
		str = str.replaceAll("_", "\\\\_").replaceAll("#", "\\\\#")
				.replaceAll("≥", "\\$\\\\geq\\$");
		if (str.contains("\n"))
			str = "\\shortstack{" + str.replaceAll("\n", "\\\\\\\\ \\\\relax")
					+ "}";
		return str;
	}

	abstract class TableRow {
		abstract String getHeader();

		String getLaTeXHeader() {
			return stringToLatex(getHeader());
		}

		String getHTMLHeader() {
			return getHeader();
		}

		abstract StatsSet getData(StatsSet s);
	}

	class ATableRow<T extends Comparable<T>> extends TableRow {
		public ATableRow(String header, Attribute<T> attribute,
				T attributeValue) {
			super();
			this.header = header;
			this.attribute = attribute;
			this.attributeValue = attributeValue;
		}

		final String header;
		final Attribute<T> attribute;
		final T attributeValue;

		@Override
		String getHeader() {
			return header;
		}

		@Override
		StatsSet getData(StatsSet s) {
			return new StatsSet(s,
					new EqualsRestriction<T>(attribute, attributeValue));
		}

	}

	class AutomataRow extends ATableRow<String> {

		public AutomataRow(String header, String attributeValue) {
			super(header, Attribute.AUTOMATA, attributeValue);
		}

	}

	abstract class TableColumn {
		abstract String getTitle();

		String getHTMLTitle() {
			return getTitle();
		}

		String getLaTeXTitle() {
			return stringToLatex(getTitle());
		}

		abstract StatsSet restrict(StatsSet set);

		abstract String getData(StatsSet stats);

		String getHTMLData(StatsSet stats) {
			return getData(stats);
		}

		String getLaTeXData(StatsSet stats) {
			return stringToLatex(getData(stats));
		}
	}

	class MyCol extends TableColumn {

		final String title;
		final boolean dispReset;
		public boolean dispOracle = false;
		final Restriction[] restrictions;

		@Override
		String getTitle() {
			String r = title;
			if (dispReset)
				r = r + " (#resets)";
			if (dispReset && dispOracle)
				r = r + "\n";
			if (dispOracle)
				r = r + "[#oracle]";
			return r;
		}

		@Override
		String getData(StatsSet stats) {
			assert stats.size() != 0;
			String tl = "" + (int) stats.attributeAVG(Attribute.TRACE_LENGTH);
			// tl = tl + " " + stats.attributeAVG(Attribute.DURATION)
			// + Attribute.DURATION.getUnits() + " ";
			if (dispReset)
				tl = tl + " ("
					+ ((int) stats.attributeAVG(Attribute.RESET_CALL_NB)) + ")";
			if (dispOracle)
				tl = tl + " [" + ((int) stats
						.attributeAVG(Attribute.ASKED_COUNTER_EXAMPLE)) + "]";
			return tl;
		}

		@Override
		String getLaTeXData(StatsSet stats) {
			assert stats.size() != 0;
			String len = "";
			for (StatsEntry s : stats.getStats()) {
				len = len + s.get(Attribute.TRACE_LENGTH) + "\n";
			}
			String tl = "" + (int) stats.attributeAVG(Attribute.TRACE_LENGTH);
			// tl = tl + " " + stats.attributeAVG(Attribute.DURATION)
			// + Attribute.DURATION.getUnits() + " ";
			tl = stringToLatex(len);
			if (dispReset)
				tl = tl + " ("
						+ ((int) stats.attributeAVG(Attribute.RESET_CALL_NB))
						+ ")";
			if (dispOracle)
				tl = tl + " ["
						+ ((int) stats
								.attributeAVG(Attribute.ASKED_COUNTER_EXAMPLE))
						+ "]";
			return tl;
		}

		@Override
		StatsSet restrict(StatsSet set) {
			return new StatsSet(set, restrictions);
		}

		public MyCol(String title, boolean dispReset,
				Restriction[] restrictions) {
			this.title = title;
			this.dispReset = dispReset;
			this.restrictions = restrictions;
		}

	}

	class TransitionCol extends TableColumn {

		@Override
		String getTitle() {
			return "number of transitions";
		}

		@Override
		String getLaTeXTitle() {
			return "$|Q|\\times|I|$";
		}

		@Override
		StatsSet restrict(StatsSet set) {
			return set;
		}

		@Override
		String getData(StatsSet stats) {
			int states = stats.attributeMax(Attribute.STATE_NUMBER);
			int inputs = stats.attributeMax(Attribute.INPUT_SYMBOLS);
			return "" + (states * inputs);
		}

	}

	class ComparisonCol extends TableColumn {
		final TableColumn ref;
		final TableColumn test;

		public ComparisonCol(TableColumn ref, TableColumn test) {
			super();
			this.ref = ref;
			this.test = test;
		}

		@Override
		String getTitle() {
			return "reset cost \n #input";
			// return "cost of reset for wich " + test.getTitle()
			// + " is cheaper than " + ref.getTitle();
		}

		@Override
		StatsSet restrict(StatsSet set) {
			StatsSet s = new StatsSet();
			s.add(ref.restrict(set));
			s.add(test.restrict(set));
			return s;
		}

		@Override
		String getData(StatsSet stats) {
			StatsSet refStats = ref.restrict(stats);
			StatsSet testStats = test.restrict(stats);
			if (refStats.size() == 0 || testStats.size() == 0)
				return "";
			float refReset = refStats.attributeAVG(Attribute.RESET_CALL_NB);
			float testLength = testStats.attributeAVG(Attribute.TRACE_LENGTH);
			float refLength = refStats.attributeAVG(Attribute.TRACE_LENGTH);
			float testReset = testStats.attributeAVG(Attribute.RESET_CALL_NB);

			float resetRatio = (testLength - refLength)
					/ (refReset - testReset);
			if (testLength > refLength) {
				if (resetRatio < 0) {
					return "-";
				} else {
					String ratioString = String.format("%.2g", resetRatio);
					if (resetRatio > 1) {
						return " ≥ " + ratioString;
					} else {

						return " ≥ " + ratioString;
						// return "win if reset is not cheaper than an input";
					}
				}
			} else {
				if (testReset > refReset)
					throw new RuntimeException();
				return "0";
			}
		}

	}

	void makeTable(String file, StatsSet stats, List<TableColumn> columns,
			List<TableRow> rows) {
		PrintStream out = null;
		try {
			out = new PrintStream(
					file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = System.out;
		}
		out.println();
		out.print("\\begin{tabular}{|l|");
		for (TableColumn s : columns) {
			if (s != null)
				out.print("l|");
			else
				out.print('|');
		}
		out.println("}");
		out.println("\\hline");

		out.print("automata ");

		for (TableColumn s : columns) {
			if (s != null)
				// out.print(" & \\rotatebox{75}{" + s.getLaTeXTitle() + "}");
				out.print(" & " + s.getLaTeXTitle());
		}
		out.println("\\\\");
		out.println("\\hline");

		for (TableRow row : rows) {
			StatsSet automaton = row.getData(stats);
			if (automaton.size() == 0)
				continue;
			out.print(row.getLaTeXHeader());
			for (TableColumn c : columns) {
				if (c == null)
					continue;
				StatsSet cell = c.restrict(automaton);
				int nb = cell.size();
				if (nb == 0)
					out.print("& -");
				else {
					out.print(" & " + c.getLaTeXData(cell));
					// if (nb < 150)
					// out.print(" \\tiny{(draft " + nb + ")}");
				}
			}
			out.println("\\\\");
		}
		out.println("\\hline");
		out.println("\\end{tabular}");

		out.close();

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
			if (lm != null)
				set.add(new StatsSet(lm, statesRestriction));
			if (zQ != null)
				set.add(new StatsSet(zQ, new RangeRestriction<>(
						Attribute.STATE_NUMBER, 0, 130)));

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
			if (lm != null)
				set.add(new StatsSet(lm, statesRestriction));
			if (zQ != null)
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
			if (lm != null)
				set.add(new StatsSet(lm, statesRestriction));
			if (zQ != null)
				set.add(new StatsSet(zQ, new RangeRestriction<>(
						Attribute.STATE_NUMBER, 0, 130)));

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
			if (lm != null)
				set.add(new StatsSet(lm));
			if (zQ != null)
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
		if (lm != null)
			all.getStats().addAll(lm.getStats());
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

			public LearnerSet(StatsSet set, String title,
					LearnerSet reference) {
				super();
				this.reference = reference;
				this.set = set;
				this.title = title;

			}

			LearnerSet reference = null;
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

		HashMap<String, String> filesNames = new HashMap<String,String>() {
			private static final long serialVersionUID = 1L;
			
			public String put(String dot,String name) {
				String shortName = name;
				if (shortName.length()>40)
					shortName=name.substring(0, 35);
			return super.put (dot,shortName);
			}
			
		};

		filesNames.put("dot_file(BenchmarkFromRhapsodyToDezyne_model3)",
				"model3");
		filesNames.put("dot_file(BenchmarkFromRhapsodyToDezyne_model1)",
				"model1");
		filesNames.put("dot_file(BenchmarkFromRhapsodyToDezyne_model2)",
				"model2");
		filesNames.put("dot_file(BenchmarkFromRhapsodyToDezyne_model4)",
				"model4");
		filesNames.put(
				"dot_file(BenchmarkMQTT_VerneMQ__two_client_will_retain)",
				"VerneMQ__two_client_will_retain");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__simple)",
				"hbmqtt__simple");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__single_client)",
				"VerneMQ__single_client");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__invalid)",
				"hbmqtt__invalid");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__non_clean)",
				"VerneMQ__non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__non_clean)",
				"ActiveMQ__non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__invalid)",
				"ActiveMQ__invalid");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__simple)",
				"VerneMQ__simple");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__invalid)",
				"VerneMQ__invalid");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__two_client_same_id)",
				"VerneMQ__two_client_same_id");
		filesNames.put(
				"dot_file(BenchmarkMQTT_ActiveMQ__two_client_will_retain)",
				"ActiveMQ__two_client_will_retain");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__simple)",
				"emqtt__simple");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__invalid)",
				"mosquitto__invalid");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__invalid)",
				"emqtt__invalid");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__mosquitto)",
				"mosquitto__mosquitto");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__two_client)",
				"hbmqtt__two_client");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__two_client_will_retain)",
				"hbmqtt__two_client_will_retain");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__single_client)",
				"emqtt__single_client");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__single_client)",
				"mosquitto__single_client");
		filesNames.put("dot_file(BenchmarkMQTT_VerneMQ__two_client)",
				"VerneMQ__two_client");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__two_client)",
				"emqtt__two_client");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__single_client)",
				"ActiveMQ__single_client");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__two_client_will_retain)",
				"emqtt__two_client_will_retain");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__two_client_same_id)",
				"emqtt__two_client_same_id");
		filesNames.put("dot_file(BenchmarkMQTT_ActiveMQ__simple)",
				"ActiveMQ__simple");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__two_client_same_id)",
				"mosquitto__two_client_same_id");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__single_client)",
				"hbmqtt__single_client");
		filesNames.put(
				"dot_file(BenchmarkMQTT_mosquitto__two_client_will_retain)",
				"mosquitto__two_client_will_retain");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__non_clean)",
				"mosquitto__non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_hbmqtt__non_clean)",
				"hbmqtt__non_clean");
		filesNames.put("dot_file(BenchmarkMQTT_mosquitto__two_client)",
				"mosquitto__two_client");
		filesNames.put("dot_file(BenchmarkMQTT_emqtt__non_clean)",
				"emqtt__non_clean");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_new_W-method_fix)",
				"learnresult_new_W-method_fix");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_new_device-simple_fix)",
				"learnresult_new_device-simple_fix");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_new_Rand_500_10-15_MC_fix)",
				"learnresult_new_Rand_500_10-15_MC_fix");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_old_device-simple_fix)",
				"learnresult_old_device-simple_fix");
		filesNames.put(
				"dot_file(BenchmarkEdentifier2_learnresult_old_500_10-15_fix)",
				"learnresult_old_500_10-15_fix");
		filesNames.put("dot_file(BenchmarkCoffeeMachine_coffeemachine)",
				"coffeemachine");
		filesNames.put("dot_file(BenchmarkSSH_DropBear)", "DropBear");
		filesNames.put("dot_file(BenchmarkSSH_BitVise)", "BitVise");
		filesNames.put("dot_file(BenchmarkSSH_OpenSSH)", "OpenSSH");
		filesNames.put(
				"dot_file(BenchmarkToyModels_lee_yannakakis_non_distinguishable)",
				"lee_yannakakis_non_distinguishable");
		filesNames.put("dot_file(BenchmarkToyModels_cacm)", "cacm");
		filesNames.put(
				"dot_file(BenchmarkToyModels_lee_yannakakis_distinguishable)",
				"lee_yannakakis_distinguishable");
		filesNames.put("dot_file(BenchmarkToyModels_naiks)", "naiks");
		filesNames.put(
				"dot_file(BenchmarkBankcard_ASN_learnresult_MAESTRO_fix)",
				"ASN_learnresult_MAESTRO_fix");
		filesNames.put("dot_file(BenchmarkBankcard_4_learnresult_PIN_fix)",
				"4_learnresult_PIN_fix");
		filesNames.put("dot_file(BenchmarkBankcard_4_learnresult_MAESTRO_fix)",
				"4_learnresult_MAESTRO_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_Rabo_learnresult_MAESTRO_fix)",
				"Rabo_learnresult_MAESTRO_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_Volksbank_learnresult_MAESTRO_fix)",
				"Volksbank_learnresult_MAESTRO_fix");
		filesNames.put("dot_file(BenchmarkBankcard_learnresult_fix)",
				"learnresult_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_4_learnresult_SecureCode_20Aut_fix)",
				"4_learnresult_SecureCode%20Aut_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_ASN_learnresult_SecureCode_20Aut_fix)",
				"ASN_learnresult_SecureCode%20Aut_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_Rabo_learnresult_SecureCode_Aut_fix)",
				"Rabo_learnresult_SecureCode_Aut_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_10_learnresult_MasterCard_fix)",
				"10_learnresult_MasterCard_fix");
		filesNames.put(
				"dot_file(BenchmarkBankcard_1_learnresult_MasterCard_fix)",
				"1_learnresult_MasterCard_fix");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.8_server_regular)",
				"GnuTLS_3.3.8_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1g_client_regular)",
				"OpenSSL_1.0.1g_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.8_client_full)",
				"GnuTLS_3.3.8_client_full");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.8_client_regular)",
				"GnuTLS_3.3.8_client_regular");
		filesNames.put(
				"dot_file(BenchmarkTLS_RSA_BSAFE_C_4.0.4_server_regular)",
				"RSA_BSAFE_C_4.0.4_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.12_server_full)",
				"GnuTLS_3.3.12_server_full");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.8_server_full)",
				"GnuTLS_3.3.8_server_full");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1l_server_regular)",
				"OpenSSL_1.0.1l_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_JSSE_1.8.0_25_server_regular)",
				"JSSE_1.8.0_25_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1l_client_regular)",
				"OpenSSL_1.0.1l_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1j_client_regular)",
				"OpenSSL_1.0.1j_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.12_client_full)",
				"GnuTLS_3.3.12_client_full");
		filesNames.put("dot_file(BenchmarkTLS_miTLS_0.1.3_server_regular)",
				"miTLS_0.1.3_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_NSS_3.17.4_client_regular)",
				"NSS_3.17.4_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1j_server_regular)",
				"OpenSSL_1.0.1j_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.12_server_regular)",
				"GnuTLS_3.3.12_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.2_client_full)",
				"OpenSSL_1.0.2_client_full");
		filesNames.put("dot_file(BenchmarkTLS_JSSE_1.8.0_31_server_regular)",
				"JSSE_1.8.0_31_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_GnuTLS_3.3.12_client_regular)",
				"GnuTLS_3.3.12_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.1g_server_regular)",
				"OpenSSL_1.0.1g_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_NSS_3.17.4_server_regular)",
				"NSS_3.17.4_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.2_client_regular)",
				"OpenSSL_1.0.2_client_regular");
		filesNames.put("dot_file(BenchmarkTLS_NSS_3.17.4_client_full)",
				"NSS_3.17.4_client_full");
		filesNames.put(
				"dot_file(BenchmarkTLS_RSA_BSAFE_Java_6.1.1_server_regular)",
				"RSA_BSAFE_Java_6.1.1_server_regular");
		filesNames.put("dot_file(BenchmarkTLS_OpenSSL_1.0.2_server_regular)",
				"OpenSSL_1.0.2_server_regular");
		filesNames.put(
				"dot_file(BenchmarkCircuits_lion_with_hidden_states_with_sink_minimized)",
				"lion_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dvram_minimized)",
				"dvram_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_tav_minimized)",
				"tav_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1_with_sink_minimized)",
				"s1_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet1_with_sink_minimized)",
				"planet1_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk16_minimized)",
				"dk16_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_styr_with_loops_minimized)",
				"styr_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_lion9_with_sink_minimized)",
				"lion9_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_shiftreg_minimized)",
				"shiftreg_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet1_with_hidden_states_with_sink_minimized)",
				"planet1_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_styr_with_loops_with_hidden_states_minimized)",
				"styr_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train4_with_loops_minimized)",
				"train4_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet1_with_loops_minimized)",
				"planet1_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk15_minimized)",
				"dk15_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_lion9_with_loops_minimized)",
				"lion9_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex5_with_sink_minimized)",
				"ex5_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_styr_with_sink_minimized)",
				"styr_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet_with_loops_minimized)",
				"planet_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s386_with_sink_minimized)",
				"s386_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex2_with_loops_with_hidden_states_minimized)",
				"ex2_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_train4_with_sink_minimized)",
				"train4_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_pma_with_sink_minimized)",
				"pma_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_bbtas_minimized)",
				"bbtas_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_keyb_minimized)",
				"keyb_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex3_with_loops_minimized)",
				"ex3_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train11_with_loops_with_hidden_states_minimized)",
				"train11_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1a_with_sink_minimized)",
				"s1a_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex5_with_loops_with_hidden_states_minimized)",
				"ex5_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_rie_minimized)",
				"rie_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_planet_with_sink_minimized)",
				"planet_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1494_with_sink_minimized)",
				"s1494_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_bbsse_with_hidden_states_with_sink_minimized)",
				"bbsse_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_modulo12_minimized)",
				"modulo12_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_beecount_with_sink_minimized)",
				"beecount_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex7_with_loops_with_hidden_states_minimized)",
				"ex7_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_pma_with_loops_minimized)",
				"pma_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_mark1_with_loops_minimized)",
				"mark1_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex3_with_sink_minimized)",
				"ex3_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk17_minimized)",
				"dk17_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s420_with_sink_minimized)",
				"s420_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex7_with_hidden_states_with_sink_minimized)",
				"ex7_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_bbsse_minimized)",
				"bbsse_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s820_with_loops_minimized)",
				"s820_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_beecount_with_loops_minimized)",
				"beecount_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1_with_loops_minimized)",
				"s1_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk27_minimized)",
				"dk27_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk14_minimized)",
				"dk14_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex7_with_loops_minimized)",
				"ex7_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_donfile_minimized)",
				"donfile_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s420_with_loops_minimized)",
				"s420_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_bbara_minimized)",
				"bbara_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet_with_loops_with_hidden_states_minimized)",
				"planet_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex3_with_hidden_states_with_sink_minimized)",
				"ex3_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_lion_with_loops_with_hidden_states_minimized)",
				"lion_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex1_with_sink_minimized)",
				"ex1_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_keyb_with_loops_with_hidden_states_minimized)",
				"keyb_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_mark1_with_loops_with_hidden_states_minimized)",
				"mark1_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s27_with_sink_minimized)",
				"s27_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex7_with_sink_minimized)",
				"ex7_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1488_with_loops_minimized)",
				"s1488_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_bbsse_with_loops_with_hidden_states_minimized)",
				"bbsse_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_cse_with_loops_minimized)",
				"cse_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train4_with_hidden_states_with_sink_minimized)",
				"train4_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex5_with_hidden_states_with_sink_minimized)",
				"ex5_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_lion_with_loops_minimized)",
				"lion_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex6_with_sink_minimized)",
				"ex6_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train4_with_loops_with_hidden_states_minimized)",
				"train4_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_cse_with_loops_with_hidden_states_minimized)",
				"cse_with_loops_with_hidden_states_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_mc_minimized)",
				"mc_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_opus_with_loops_minimized)",
				"opus_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex2_with_loops_minimized)",
				"ex2_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_lion_with_sink_minimized)",
				"lion_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_tma_with_sink_minimized)",
				"tma_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_opus_with_sink_minimized)",
				"opus_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet_with_hidden_states_with_sink_minimized)",
				"planet_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s27_with_loops_minimized)",
				"s27_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1488_with_sink_minimized)",
				"s1488_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s298_minimized)",
				"s298_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_tma_with_loops_minimized)",
				"tma_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex2_with_sink_minimized)",
				"ex2_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex1_with_loops_minimized)",
				"ex1_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s820_with_sink_minimized)",
				"s820_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex2_with_hidden_states_with_sink_minimized)",
				"ex2_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_ex3_with_loops_with_hidden_states_minimized)",
				"ex3_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_keyb_with_hidden_states_with_sink_minimized)",
				"keyb_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train11_with_loops_minimized)",
				"train11_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1494_with_loops_minimized)",
				"s1494_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_log_minimized)",
				"log_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex5_with_loops_minimized)",
				"ex5_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s8_minimized)",
				"s8_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_dk512_minimized)",
				"dk512_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s208_minimized)",
				"s208_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s1a_with_loops_minimized)",
				"s1a_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train11_with_hidden_states_with_sink_minimized)",
				"train11_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_s386_with_loops_minimized)",
				"s386_with_loops_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_mark1_with_sink_minimized)",
				"mark1_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex4_minimized)",
				"ex4_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_fetch_minimized)",
				"fetch_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_planet1_with_loops_with_hidden_states_mi//					if (filesNames.containsKey(name))\r\n"
						+ "//						dispName = filesNames.get(name);\r\n"
						+ "nimized)",
				"planet1_with_loops_with_hidden_states_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_mark1_with_hidden_states_with_sink_minimized)",
				"mark1_with_hidden_states_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_cse_with_hidden_states_with_sink_minimized)",
				"cse_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_tbk_minimized)",
				"tbk_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_train11_with_sink_minimized)",
				"train11_with_sink_minimized");
		filesNames.put(
				"dot_file(BenchmarkCircuits_styr_with_hidden_states_with_sink_minimized)",
				"styr_with_hidden_states_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_cse_with_sink_minimized)",
				"cse_with_sink_minimized");
		filesNames.put("dot_file(BenchmarkCircuits_ex6_with_loops_minimized)",
				"ex6_with_loops_minimized");
		filesNames.put(
				"dot_file(BenchmarkQUICprotocol_QUICprotocolwithout0RTT)",
				"QUICprotocolwithout0RTT");
		filesNames.put("dot_file(BenchmarkQUICprotocol_QUICprotocolwith0RTT)",
				"QUICprotocolwith0RTT");
		filesNames.put("dot_file(BenchmarkTCP_TCP_Windows8_Server)",
				"TCP_Windows8_Server");
		filesNames.put("dot_file(BenchmarkTCP_TCP_FreeBSD_Client)",
				"TCP_FreeBSD_Client");
		filesNames.put("dot_file(BenchmarkTCP_TCP_Linux_Server)",
				"TCP_Linux_Server");
		filesNames.put("dot_file(BenchmarkTCP_TCP_Windows8_Client)",
				"TCP_Windows8_Client");
		filesNames.put("dot_file(BenchmarkTCP_TCP_Linux_Client)",
				"TCP_Linux_Client");
		filesNames.put("dot_file(BenchmarkTCP_TCP_FreeBSD_Server)",
				"TCP_FreeBSD_Server");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult5)",
				"learnresult5");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult6)",
				"learnresult6");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult2)",
				"learnresult2");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult4)",
				"learnresult4");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult1)",
				"learnresult1");
		filesNames.put("dot_file(BenchmarkX-ray-system-PCS_learnresult3)",
				"learnresult3");

		Set<String> toIgnore = new HashSet<>();
		toIgnore.add("dot_file(g)");
		toIgnore.add("dot_file(unamed dot graph)");
		toIgnore.add("dot_file(out_dot_file(benchmark_heating_system)_inf");
		toIgnore.add("dot_file(benchmark_heating_system)");
		toIgnore.add("dot_file(benchmark_heating_system)");
		toIgnore.add("dot_file(toyModels_naiks)");
		toIgnore.add("dot_file(toyModels_lee_yannakakis_distinguishable)");
		toIgnore.add("dot_file(toyModels_lee_yannakakis_non_distinguishable)");
		//toIgnore.add("dot_file(BenchmarkFromRhapsodyToDezyne_model3)");

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
		
		// JSS traceLength table
		
		StatsSet hW_Old = new StatsSet(hW__);
		hW_Old.restrict(
				new EqualsRestriction<Boolean>(Attribute.REUSE_HZXW, true));
		hW_Old.restrict(new EqualsRestriction<Boolean>(
				Attribute.CHECK_3rd_INCONSISTENCY, true));
		hW_Old.restrict(new EqualsRestriction<String>(
				Attribute.SEARCH_CE_IN_TRACE, "simple"));
		hW_Old.restrict(
				new EqualsRestriction<Boolean>(Attribute.ADD_H_IN_W, true));

		
		hW_Old.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_H, false));
		hW_Old.restrict(
				new EqualsRestriction<>(HWStatsEntry.USE_ADAPTIVE_W, false));
		StatsSet	hWWithGivenW=new StatsSet(hW_Old);
		hW_Old.restrict(new EqualsRestriction<>(HWStatsEntry.PRECOMPUTED_W,false));
		hW_Old.restrict(new EqualsRestriction<>(HWStatsEntry.ORACLE_USED,"distinctionTree + MrBean"));
		StatsSet	hWReset=new StatsSet(hW_Old);
		StatsSet	hWNoReset=new StatsSet(hW_Old);
		hWReset.restrict(new EqualsRestriction<>(HWStatsEntry.USE_RESET,true));
		hWNoReset.restrict(new EqualsRestriction<>(HWStatsEntry.USE_RESET,false));
		hWWithGivenW.restrict(new EqualsRestriction<>(HWStatsEntry.PRECOMPUTED_W,true));
		hWReset.restrict(new EqualsRestriction<>(HWStatsEntry.ORACLE_USED,"distinctionTree + MrBean"));
		hWNoReset.restrict(new EqualsRestriction<>(HWStatsEntry.ORACLE_USED,"distinctionTree + MrBean"));
		
 sets = new ArrayList<>();

		

		// sets.add(new LearnerSet(hW_adaptive, "\\parbox[t]{2cm}{hW
		// with\\\\adaptive}"));
//		sets.add(new LearnerSet(rivestSchapire, "Rivest&Schapire"));
		//sets.add(new LearnerSet(hW_Old, "hW"));
		LearnerSet hWNoResetSet = new LearnerSet(hWNoReset, "hW without reset");
		sets.add(hWNoResetSet);
		LearnerSet hWWithResetSet = new LearnerSet(hWReset, "hW with reset (#resets if used)");
		sets.add(hWWithResetSet);

		if (lm != null)
			sets.add(new LearnerSet(lm, "Lm (#resets)"));
		if (zQ != null)
			sets.add(new LearnerSet(zQ, "Z-Quotient (\\#resets)"));

		sets.add(null);
		
		LearnerSet locWSet=new LearnerSet(locW, "LocW");
		sets.add(locWSet);
		LearnerSet hWWithGivenWSet=new LearnerSet(hWWithGivenW, "hW with precomputed W");
		sets.add(hWWithGivenWSet);
//		sets.add(new LearnerSet(hW_knownW,
//				"\\parbox[t]{2cm}{hW with a\\\\provided $W$-set}"));
		try {
			out = new PrintStream(
					Options.OUTDIR+"/ICGIbenchmarkTableTraceLength.tex");
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
					if (automaton.get(0) instanceof ZStatsEntry
							|| automaton.get(0) instanceof HWStatsEntry) {

						float resets = automaton
								.attributeAVG(Attribute.RESET_CALL_NB);

						out.print(" (" + (int) resets + ")");
					}
					if (nb < 150)
						out.print(" \tiny{(draft " + nb + ")}");
				}
			}
			out.println("\\\\");
		}
		out.println("\\hline");
		out.println("\\end{tabular}");
		
		
	
		//JSS table
		try {
			out = new PrintStream(
					Options.OUTDIR+"/benchmarkTableTraceLength.tex");
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
					if (automaton.get(0) instanceof ZStatsEntry
							|| automaton.get(0) instanceof HWStatsEntry
									&& automaton.attributeMax(
											Attribute.RESET_CALL_NB) != 0) {

						float resets = automaton
								.attributeAVG(Attribute.RESET_CALL_NB);

						out.print(" (" + (int) resets + ")");
					}
					if (nb < 40)
						out.print("{\\tiny (" + nb + " tried)}");
				}
			}
			out.println("\\\\");
		}
		out.println("\\hline");
		out.println("\\end{tabular}");

		//JSS table HTML
		try {
			out = new PrintStream(
					Options.OUTDIR+"/benchmarkTableTraceLength.html");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = System.out;
		}
		out.println("<!DOCTYPE html>");
		out.println("<head>");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
		out.println("<style>");
		out.println("table, th, td {\r\n" + 
				"    border: 1px solid black;\r\n" + 
				"    border-collapse: collapse;\r\n" + 
				"}");
		out.println("table tr:nth-child(even) {\r\n" + 
				"    background-color: #eee;\r\n" + 
				"}");
		out.println("table tr:nth-child(odd) {\r\n" + 
				"    background-color: #fff;\r\n" + 
				"}");
		out.println("table tr:hover{"
				+ "background-color:#ddd;"
				+ "}");
		out.println(".spacer{"
				+ "border-style:none;"
				+ "width:1px;"
				+ "}");
		out.println("</style>");
		out.println("</head>");
		out.print("<table style='border: 1px solid black; border-collapse: collapse;'>");
		
		out.println("<tr>");
		out.print("<th>automata</th><th>|Q| × |I|</th>");
		for (LearnerSet s : sets) {
			if (s != null)
				out.print(" <th>" + s.title + "</th>");
			else
				out.print("<th class='spacer'></th>");
		}
		out.println("</tr>");

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
			StatsSet lmSet = new StatsSet(lm);
			lmSet.restrict(new EqualsRestriction<>(Attribute.AUTOMATA,name));
			int lmResetNoOracle = (int) (lmSet
					.attributeAVG(Attribute.RESET_CALL_NB)
					- lmSet.attributeAVG(Attribute.ORACLE_RESET_NB));
			int lmTraceNoOracle = (int) (lmSet
					.attributeAVG(Attribute.TRACE_LENGTH)
					- lmSet.attributeAVG(Attribute.ORACLE_TRACE_LENGTH));
			int lmOracleCall = (int) lmSet
					.attributeAVG(Attribute.ASKED_COUNTER_EXAMPLE);
			out.println("<tr>");
			out.print("<td>" + title.replaceAll("_", "_") + "</td>");
			StatsSet automaton = new StatsSet(all);
			automaton.restrict(
					new EqualsRestriction<String>(Attribute.AUTOMATA, name));
			int stateNb=automaton.get(0).get(Attribute.STATE_NUMBER);
			int inputNb=automaton.get(0).get(Attribute.INPUT_SYMBOLS);
			out.print("<td>");
			if (!automaton.attributeMax(Attribute.STATE_NUMBER)
					.equals(automaton.attributeMin(Attribute.STATE_NUMBER))) {
				out.print("invalid state");
				int min = automaton.attributeMin(Attribute.STATE_NUMBER);
				out.print(automaton.attributeMin(Attribute.STATE_NUMBER) + "-"
						+ automaton.attributeMax(Attribute.STATE_NUMBER));
				StatsSet s = new StatsSet(automaton);
				s.restrict(
						new EqualsRestriction<>(Attribute.STATE_NUMBER, min));
//				 out.print("<br/>"+s.get(0).getClass());
//				 out.print("<br/>"+s.get(0).toCSV());
				out.print("<br/>");
			}
					out.print(stateNb*inputNb);
					out.print("</td>");
			for (LearnerSet s : sets) {
				if (s == null) {
					out.print("<td class='spacer'></td>");
					continue;
				}
				out.print("<td>");
				automaton = new StatsSet(s.set);
				automaton.restrict(new EqualsRestriction<String>(
						Attribute.AUTOMATA, name));
				int nb = automaton.size();
				if (nb == 0)
					out.print("-");
				else {
					float oracleTraceLength;
					if (s != locWSet)
						oracleTraceLength = automaton
								.attributeAVG(Attribute.ORACLE_TRACE_LENGTH);
					else
						oracleTraceLength = 0;

					float traceLengthNoOracle = automaton.attributeAVG(
							Attribute.TRACE_LENGTH) - oracleTraceLength;
					float resetsNoOracle = (float) -100000000000000000000000000.;
					if (automaton.get(0) instanceof ZStatsEntry
							|| automaton.get(0) instanceof HWStatsEntry) {
						resetsNoOracle = automaton
								.attributeAVG(Attribute.RESET_CALL_NB)
								- automaton.attributeAVG(
										Attribute.ORACLE_RESET_NB);
					}

					String styleTraceLength = "";
					String resetRatioString="";
					if (automaton.get(0) instanceof HWStatsEntry
							&& s != hWWithGivenWSet) {
						float resetRatio = (traceLengthNoOracle
								- lmTraceNoOracle)
								/ (lmResetNoOracle - resetsNoOracle);
						if (traceLengthNoOracle > lmTraceNoOracle) {
							styleTraceLength = "background-color:orange;";

							StatsSet RS = new StatsSet(rivestSchapire);
							RS.restrict(new EqualsRestriction<String>(
									Attribute.AUTOMATA, name));
							if (RS.size() > 0) {
//								float rsLength = RS
//										.attributeAVG(Attribute.TRACE_LENGTH);
//								if (traceLengthNoOracle < rsLength)
//									styleTraceLength = "background-color:orange";
							}
							if (resetRatio < 0) {
								styleTraceLength = "background-color:red;";
								resetRatioString = " (can not win even with a costly reset)";
							} else if (resetRatio > 1) {
								String ratioString = String.format("%.2g",
										resetRatio);
								resetRatioString = " (win if reset ≥ "
										+ ratioString + " input)";
							} else {
								resetRatioString = " (win if reset is not cheaper than an input)";
								styleTraceLength = "background-color:lightgreen;";
							}
						} else
							styleTraceLength = "background-color:lightgreen;";
					}
					out.print("<span style='" + styleTraceLength + "'>"
							+ (int) traceLengthNoOracle);
					out.print(resetRatioString);
					out.print("</span>");
					int oracleNumber;
					if (s!=locWSet)oracleNumber=(int) automaton.attributeAVG(Attribute.ASKED_COUNTER_EXAMPLE);
					else oracleNumber=0;
					String oracleNumberStyle="";
					if (oracleNumber>lmOracleCall)
						oracleNumberStyle="border:solid 2px red;";
					if (oracleNumber<lmOracleCall)
						oracleNumberStyle="border:solid 2px green;";
					if ((s != hWWithGivenWSet || oracleTraceLength != 0
							|| oracleNumber != 1)&&s!=locWSet)
						out.print(" [+" + (int) oracleTraceLength
								+ " by <span style='" + oracleNumberStyle + "'>"
								+ oracleNumber + " oracle</span>]");

					if (automaton.get(0) instanceof ZStatsEntry
							|| automaton.get(0) instanceof HWStatsEntry
									&& automaton.attributeMax(
											Attribute.RESET_CALL_NB) != 0) {

						resetsNoOracle = automaton
								.attributeAVG(Attribute.RESET_CALL_NB)
								- automaton.attributeAVG(
										Attribute.ORACLE_RESET_NB);
						String styleReset = "";
//						if (resetsNoOracle < lmResetNoOracle * 2 / 10
	//							&& automaton.get(0) instanceof HWStatsEntry) {
		//					styleReset = "background-color:lightgreen;";
			//			}
						// if (resets > lmReset *8/10 && automaton.get(0)
						// instanceof HWStatsEntry) {
						// styleReset = "background-color:orange;";
						// }
						if (resetsNoOracle > lmResetNoOracle + 1) {
							styleReset = "background-color:red;";
						}

						out.print(" (<span style='" + styleReset + "'>"
								+ (int) resetsNoOracle);
						out.print("</span>");
						float oracleReset = automaton
								.attributeAVG(Attribute.ORACLE_RESET_NB);
						out.print(" [+" + (int) oracleReset + "])");
					}

					if (nb < 40)
						out.print("<span style='color:grey;'>(" + nb + " tried)</span>");
				}
				out.println("</td>");
			}
			out.println("</tr>");
		}

		out.println("<tr>");
		out.print("<th>automata</th><th>|Q| × |I|</th>");
		for (LearnerSet s : sets) {
			if (s != null)
				out.print(" <th>" + s.title + "</th>");
			else
				out.print("<th class='spacer'></th>");
		}
		out.println("</tr>");
	}

	private int getSortForTable(StatsSet s) {
		if (s.size() == 0)
			return -1;
		int size = s.get(0).get(Attribute.STATE_NUMBER)
				* s.get(0).get(Attribute.INPUT_SYMBOLS);
		return size;
	}

	private void generate_SANER19() {
		if (hW__==null)return;
		final StatsSet allStats = new StatsSet();
		allStats.add(zQ);
		allStats.add(hW__);
		allStats.add(rivestSchapire);
		allStats.add(locW);
		allStats.add(lm);
	
		allStats.restrict(new Restriction() {
			String kept = "dot_file(BenchmarkBankcard_Volksbank_learnresult_MAESTRO_fix)\r\n"
					+ "dot_file(BenchmarkTCP_TCP_Windows8_Client)\r\n"
					+ "dot_file(BenchmarkBankcard_1_learnresult_MasterCard_fix)\r\n"
					+ "dot_file(BenchmarkMQTT_mosquitto__two_client_will_retain)\r\n"
					+ "dot_file(BenchmarkTLS_OpenSSL_1.0.1g_client_regular)\r\n"
					+ "dot_file(BenchmarkTLS_OpenSSL_1.0.1g_server_regular)\r\n"
					+ "dot_file(BenchmarkSSH_DropBear)\r\n"
					+ "dot_file(BenchmarkCircuits_bbara_minimized)\r\n"
					+ "dot_file(BenchmarkTLS_OpenSSL_1.0.2_client_full)\r\n"
					+ "dot_file(BenchmarkMQTT_emqtt__two_client_will_retain)\r\n"
					+ "";
			@Override
			public boolean contains(StatsEntry s) {
				String automata = s.get(Attribute.AUTOMATA);

				return kept.contains(automata);
			}
		});
	
	
		{
			StatsSet baseStats = new StatsSet(allStats);
			baseStats.restrict(new Restriction() {
				
				@Override
				public boolean contains(StatsEntry s) {
					if (!s.hasAttribute(Attribute.ORACLE_USED))
					return true;
					return s.get(Attribute.ORACLE_USED).equals("MrBean");
				}
			});
			List<TableRow> rows = new ArrayList<>();
			rows.add(new AutomataRow("MQTT ActiveMQ two client retain",
					"dot_file(BenchmarkMQTT_ActiveMQ__two_client_will_retain)"));
			rows.add(new AutomataRow("FromRhapsodyToDezyne 1",
					"dot_file(BenchmarkFromRhapsodyToDezyne_model1)"));
			rows.add(new AutomataRow("FromRhapsodyToDezyne 3",
					"dot_file(BenchmarkFromRhapsodyToDezyne_model3)"));
			SortedMap<Integer, List<String>> bySize = new TreeMap<>();
			Map<String, TableRow> automaticRows = new HashMap<>();
			for (StatsEntry e : new StatsSet(baseStats, new Restriction[] {
					new ClassRestriction<>(HWStatsEntry.class),
					// new EqualsRestriction<>(Attribute.USE_RESET, false),
			})
							.getStats()) {
				String name = e.get(Attribute.AUTOMATA);
				if (!automaticRows.containsKey(name)) {
					String dispName = dot_to_short_name(name);
	
					automaticRows.put(name, new AutomataRow(dispName, name));
	
					Integer size = e.get(Attribute.STATE_NUMBER)
							* e.get(Attribute.INPUT_SYMBOLS);
					List<String> thisSize = bySize.get(size);
					if (thisSize == null) {
						thisSize = new ArrayList<>();
						bySize.put(size, thisSize);
					}
					thisSize.add(name);
				}
			}
	
			List<Restriction> hWBaseRestrictions = Arrays.asList(
					new ClassRestriction<>(HWStatsEntry.class),
					new EqualsRestriction<>(Attribute.USE_ADAPTIVE_W, false),
					new EqualsRestriction<>(Attribute.USE_ADAPTIVE_H, false),
	
					new EqualsRestriction<>(Attribute.REUSE_HZXW, true),
					new EqualsRestriction<>(Attribute.CHECK_3rd_INCONSISTENCY,
							true),
					new EqualsRestriction<>(Attribute.SEARCH_CE_IN_TRACE,
							"simple"));
	
			rows = new ArrayList<>(automaticRows.values());
			rows = new ArrayList<>();
			for (List<String> entry : bySize.values()) {
				Collections.sort(entry);
				for (String fileName : entry) {
					rows.add(automaticRows.get(fileName));
				}
			}
			List<TableColumn> columns = new ArrayList<>();
			{
				columns.add(new MyCol("LocW", false, new Restriction[] {
						new ClassRestriction<>(LocalizerBasedStatsEntry.class),
						new EqualsRestriction<>(Attribute.W_SIZE, 2), }));
				List<Restriction> hWWithW = new ArrayList<>(hWBaseRestrictions);
				hWWithW.addAll(Arrays.asList(
						new EqualsRestriction<>(Attribute.MAX_W_SIZE, 2),
						new EqualsRestriction<>(Attribute.PRECOMPUTED_W,
								true)));
	
				// columns.add(new MyCol("hW with known W", false,
				// hWWithW.toArray(new Restriction[0])) {
				// @Override
				// String getTitle() {
				// return super.title + "(#oracle)";
				// }
				//
				// @Override
				// String getData(StatsSet stats) {
				// return super.getData(stats) + "(" + stats.attributeAVG(
				// Attribute.ASKED_COUNTER_EXAMPLE) + ")";
				// }
				// });
			}
			// columns.clear();
			// rows.clear();
	
			List<Restriction> hWUnknownW = new ArrayList<>(hWBaseRestrictions);
			hWUnknownW.add(
					new EqualsRestriction<>(Attribute.PRECOMPUTED_W, false));
			ArrayList<Restriction> hWUnknownWNoReset = new ArrayList<>(
					hWUnknownW);
			hWUnknownWNoReset
					.add(new EqualsRestriction<>(Attribute.USE_RESET, false));
	
			MyCol hWCol = new MyCol("hW", false,
					hWUnknownWNoReset.toArray(new Restriction[0]));
			columns.add(new MyCol("RS", false, new Restriction[] {
					new ClassRestriction<>(RivestSchapireStatsEntry.class),
					new EqualsRestriction<>(Attribute.RS_WITH_GIVEN_H, false),
					new Restriction() {
						// tmp, RS was launched on non-connex automata
						@Override
						public boolean contains(StatsEntry s) {
							StatsSet set = new StatsSet(allStats);
							set.restrict(
									new EqualsRestriction<>(Attribute.AUTOMATA,
											s.get(Attribute.AUTOMATA)));

							set.restrict(new HasAttributeRestriction<>(
									Attribute.USE_RESET, false));
							return set.size() != 0;
						}

					}, }));
			columns.add(hWCol);
			MyCol lmCol = new MyCol("Lm", true, new Restriction[] {
					new ClassRestriction<>(LmStatsEntry.class), });
			// columns.add(lmCol);

			MyCol zqCol = new MyCol("ZQ", true, new Restriction[] {
					new ClassRestriction<>(ZStatsEntry.class), });
	
			// columns.add(null);
			// columns.add(new ComparisonCol(lmCol, hWCol));
	
	
			ArrayList<Restriction> hWUnknownWReset = new ArrayList<>(
					hWUnknownW);
			hWUnknownWReset
					.add(new EqualsRestriction<>(Attribute.USE_RESET, true));
			hWUnknownWReset.add(new Restriction() {
				@Override
				public boolean contains(StatsEntry s) {
					return !s.get(Attribute.AUTOMATA).contains("bbara");
				}

			});
			hWCol = new MyCol("hW", true,
					hWUnknownWReset.toArray(new Restriction[0]));
			columns.add(hWCol);
			hWCol.dispOracle = true;
			lmCol.dispOracle = true;
			zqCol.dispOracle = true;
			columns.add(lmCol);
			columns.add(zqCol);
			// columns.add(new ComparisonCol(lmCol, hWCol));
			columns.add(new TransitionCol() {
				@Override
				String getTitle() {
					return "#transitions";
				}

				@Override
				String getLaTeXTitle() {
					return "\\#transitions";
				}
			});
			
			
			makeTable(
					"/home/nbremond/shared_articles/SANER19/figures/benchmark.tex",
					baseStats, columns, rows);
		}
	}

}
