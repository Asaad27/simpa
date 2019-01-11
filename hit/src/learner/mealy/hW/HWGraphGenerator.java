package learner.mealy.hW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import drivers.mealy.transparent.RandomAndCounterMealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.RandomOneOutputDiffMealyDriver;
import stats.Graph;
import stats.GraphGenerator;
import stats.LineStyle;
import stats.StatsEntry;
import stats.StatsSet;
import stats.Graph.PlotStyle;
import stats.Graph.PointShape;
import stats.Graph.Color;
import stats.Graph.KeyParameters.HorizontalPosition;
import stats.Graph.KeyParameters.VerticalPosition;
import stats.OneConfigPlotter;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.InSetRestriction;
import stats.attribute.restriction.Restriction;

public class HWGraphGenerator extends GraphGenerator {

	public static Integer[] keptStates = { 5, 10, 15, 20, 25, 30, 40, 55, 75,
			100, 130, 170, 220, 290, 375, 500, 625, 800, 1000, 1300, 1700, 2200,
			3000 };// *1.3

	Map<Integer, Map<Integer, Map<Integer, Collection<HWStatsEntry>>>> groups = new HashMap<>();

	Graph.PlotStyle defaultStyle = Graph.PlotStyle.AVERAGE;

	static Restriction randomCounterRestriction = new EqualsRestriction<String>(
			HWStatsEntry.AUTOMATA,
			new RandomAndCounterMealyDriver().getSystemName());
	static Restriction fileRestriction = new EqualsRestriction<String>(
			HWStatsEntry.AUTOMATA, "G");
	static Restriction randomMealyRestriction = new EqualsRestriction<String>(
			HWStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName());
	static Restriction fromDotFileRestriction = new Restriction() {
		{
			setTitle("from dot file");
		}

		@Override
		public boolean contains(StatsEntry s) {
			return s.get(HWStatsEntry.AUTOMATA).startsWith("dot_file");
		}
	};
	static Restriction input2 = new EqualsRestriction<Integer>(
			HWStatsEntry.INPUT_SYMBOLS, 2);
	static Restriction input5 = new EqualsRestriction<Integer>(
			HWStatsEntry.INPUT_SYMBOLS, 5);
	static Restriction output2 = new EqualsRestriction<Integer>(
			HWStatsEntry.OUTPUT_SYMBOLS, 2);
	static Restriction state10 = new EqualsRestriction<Integer>(
			HWStatsEntry.STATE_NUMBER, 10);
	static Restriction state30 = new EqualsRestriction<Integer>(
			HWStatsEntry.STATE_NUMBER, 30);
	static Restriction state60 = new EqualsRestriction<Integer>(
			HWStatsEntry.STATE_NUMBER, 60);

	static public final Restriction withKnownWRestriction = new EqualsRestriction<Boolean>(
			HWStatsEntry.PRECOMPUTED_W, true);
	static public final Restriction withUnknownWRestriction = new EqualsRestriction<Boolean>(
			HWStatsEntry.PRECOMPUTED_W, false);
	static Restriction MrBeanRestriction = new EqualsRestriction<>(
			HWStatsEntry.ORACLE_USED, "MrBean");
	static Restriction shortestOracleRestriction = new EqualsRestriction<>(
			HWStatsEntry.ORACLE_USED, "shortest");

	static Restriction hInWRestriction = new EqualsRestriction<>(
			HWStatsEntry.ADD_H_IN_W, true);
	static Restriction hNotInWRestriction = new EqualsRestriction<>(
			HWStatsEntry.ADD_H_IN_W, false);

	static Restriction simpleTraceSearchRestriction = new EqualsRestriction<>(
			HWStatsEntry.SEARCH_CE_IN_TRACE, "simple");;
	static Restriction noTraceSearchRestriction = new EqualsRestriction<>(
			HWStatsEntry.SEARCH_CE_IN_TRACE, "none");

	static Restriction check3rdRestriction = new EqualsRestriction<>(
			HWStatsEntry.CHECK_3rd_INCONSISTENCY, true);
	static Restriction noCheck3rdRestriction = new EqualsRestriction<>(
			HWStatsEntry.CHECK_3rd_INCONSISTENCY, false);

	static Restriction withHZXWRestriction = new EqualsRestriction<>(
			HWStatsEntry.REUSE_HZXW, true);
	static Restriction withoutHZXWRestriction = new EqualsRestriction<>(
			HWStatsEntry.REUSE_HZXW, false);

	static Restriction fixedHomingSequenceRestriction = new EqualsRestriction<>(
			HWStatsEntry.USE_ADAPTIVE_H, false);
	static Restriction adaptiveHomingSequenceRestriction = new EqualsRestriction<>(
			HWStatsEntry.USE_ADAPTIVE_H, true);

	static Restriction fixedWRestriction = new EqualsRestriction<>(
			HWStatsEntry.USE_ADAPTIVE_W, false);
	static Restriction adaptiveWRestriction = new EqualsRestriction<>(
			HWStatsEntry.USE_ADAPTIVE_W, true);

	{
		randomCounterRestriction.setTitle("counter automata");
		fileRestriction.setTitle("from file");
		randomMealyRestriction.setTitle("random automata");
		input2.setTitle("2 input symbols");
		input5.setTitle("5 input symbols");
		output2.setTitle("2 output symbols");
		state10.setTitle("10 states");
		state30.setTitle("30 states");
		state60.setTitle("60 states");

		MrBeanRestriction.setTitle("MrBean");
		shortestOracleRestriction.setTitle("shortest CE");

		hInWRestriction.setTitle("adding h in W");
		hNotInWRestriction.setTitle("not adding h in W");

		noTraceSearchRestriction.setTitle("not using trace for CE");

		check3rdRestriction.setTitle("checking 3rd inconsistencies");
		noCheck3rdRestriction.setTitle("not checking 3rd inconsistencies");

		fixedHomingSequenceRestriction
				.setTitle("using non-adaptive homing sequence");
		adaptiveHomingSequenceRestriction
				.setTitle("using adaptive homing sequence");

		fixedWRestriction.setTitle("using non-adaptive W-set");
		adaptiveWRestriction.setTitle("using adaptive W-set");

	}

	protected <K, E> E getOrCreate(Map<K, E> map, K key, E defaultValue) {
		if (!map.containsKey(key))
			map.put(key, defaultValue);
		return map.get(key);
	}

	protected Collection<HWStatsEntry> getGroup(HWStatsEntry entry) {
		Integer cStates = entry.get(HWStatsEntry.STATE_NUMBER);
		Integer rStates = entry.get(HWStatsEntry.MAX_RECKONED_STATES);
		Integer fStates = entry.get(HWStatsEntry.MAX_FAKE_STATES);
		Collection<HWStatsEntry> r = getOrCreate(getOrCreate(getOrCreate(groups,
				cStates,
				new HashMap<Integer, Map<Integer, Collection<HWStatsEntry>>>()),
				rStates, new HashMap<Integer, Collection<HWStatsEntry>>()),
				fStates, new ArrayList<HWStatsEntry>());
		return r;
	}

	void printStats(StatsSet set) {
		Map<Integer, StatsSet> byCState = set
				.sortByAtribute(HWStatsEntry.STATE_NUMBER);
		for (Integer cStates : new TreeSet<Integer>(byCState.keySet())) {
			StatsSet cStats = byCState.get(cStates);
			Map<Integer, StatsSet> byRStates = cStats
					.sortByAtribute(HWStatsEntry.MAX_RECKONED_STATES);
			for (Integer rStates : new TreeSet<Integer>(byRStates.keySet())) {
				StatsSet rStats = byRStates.get(rStates);

				Map<Integer, StatsSet> byFStates = rStats
						.sortByAtribute(HWStatsEntry.MAX_FAKE_STATES);
				for (Entry<Integer, StatsSet> ef : byFStates.entrySet()) {
					Integer fStates = ef.getKey();
					StatsSet fStats = ef.getValue();

					System.out.println("" + fStats.size() + "\t" + cStates
							+ "\t" + rStates + "\t" + fStates + "\t"
							+ fStats.attributeMax(HWStatsEntry.TRACE_LENGTH)
							+ "\t" + fStats.attributeMedian(
									HWStatsEntry.TRACE_LENGTH));

				}
			}
		}
	}

	void printStats2(StatsSet set) {
		Attribute<Integer> sort = HWStatsEntry.STATE_NUMBER;
		List<Attribute<?>> attributes = new ArrayList<>();
		attributes.add(HWStatsEntry.MAX_RECKONED_STATES);
		attributes.add(HWStatsEntry.MAX_FAKE_STATES);
		attributes.add(HWStatsEntry.H_MAX_LENGTH);
		attributes.add(HWStatsEntry.MAX_W_SIZE);
		attributes.add(HWStatsEntry.MAX_W_TOTAL_LENGTH);
		attributes.add(HWStatsEntry.TRACE_LENGTH);

		System.out.print("occurences\t" + sort.getName());
		for (Attribute<?> a : attributes) {
			System.out.print('\t');
			System.out.print('\t');
			System.out.print(a.getName() + "(median and max)");
			System.out.print('\t');
		}
		System.out.print('\n');

		Map<Integer, StatsSet> byCState = set.sortByAtribute(sort);
		for (Integer cStates : new TreeSet<Integer>(byCState.keySet())) {
			StatsSet cStats = byCState.get(cStates);
			System.out.print(cStats.size() + "\t" + cStates);
			for (Attribute<?> a : attributes) {
				System.out.print("\t\t");
				System.out.print(cStats.attributeMedian(a));
				System.out.print('\t');
				System.out.print(cStats.attributeMax(a));
			}
			System.out.print('\n');
		}
	}

	/**
	 * a class to change settings of a graph created by another function
	 *
	 */
	private interface GraphPersonalizer {
		void personalize(Graph<?, ?> g);
	}

	public HWGraphGenerator() {
	}

	@Override
	public void generate(StatsSet allStats) {
		if (allStats.size() == 0)
			return;
		// printStats2(allStats);

		int maxStates = allStats.attributeMax(HWStatsEntry.STATE_NUMBER);
		Integer[] mult5 = new Integer[maxStates / 5];
		for (int i = 1; i <= maxStates / 5; i += 1) {
			mult5[i - 1] = Integer.valueOf(5 * i);
		}
		// keptStates = mult5;

		// Stats sets

		StatsSet files = new StatsSet(allStats);
		files.setTitle("");
		files.restrict(fileRestriction);

		StatsSet s4Counter = new StatsSet(allStats);
		s4Counter.restrict(randomCounterRestriction);
		s4Counter.restrict(new InSetRestriction<Integer>(
				HWStatsEntry.STATE_NUMBER, keptStates));
		s4Counter.restrict(input2);

		StatsSet random = new StatsSet(allStats);
		random.restrict(new InSetRestriction<Integer>(HWStatsEntry.STATE_NUMBER,
				keptStates));
		random.restrict(randomMealyRestriction);
		random.restrict(output2);
		random.restrict(input2);

		StatsSet randomBean = new StatsSet(random);
		randomBean.restrict(MrBeanRestriction);

		StatsSet randomShortest = new StatsSet(random);
		randomShortest.restrict(shortestOracleRestriction);
		// randomShortest.setTitle("random automata, shortest CE");
		randomShortest.setTitle("");

		StatsSet counterBean = new StatsSet(s4Counter);
		counterBean.restrict(MrBeanRestriction);
		// counterBean.setTitle("counter automata, MrBean");
		StatsSet counterShortest = new StatsSet(s4Counter);
		counterShortest.restrict(shortestOracleRestriction);
		// counterShortest.setTitle("counter automata, shortest CE");

		StatsSet oneOutputDiff = new StatsSet(allStats);
		oneOutputDiff
				.restrict(new EqualsRestriction<String>(HWStatsEntry.AUTOMATA,
						new RandomOneOutputDiffMealyDriver().getSystemName()));
		oneOutputDiff.restrict(
				new EqualsRestriction<Integer>(HWStatsEntry.INPUT_SYMBOLS, 5));
		oneOutputDiff.restrict(new InSetRestriction<Integer>(
				HWStatsEntry.STATE_NUMBER, mult5));
		oneOutputDiff.setTitle("automata with one output changing");

		StatsSet noHeuristic = new StatsSet(randomShortest);
		noHeuristic.restrict(noTraceSearchRestriction);
		noHeuristic.restrict(hNotInWRestriction);
		noHeuristic.setTitle("base algo (no heuristic)");

		StatsSet hInW = new StatsSet(randomShortest);
		hInW.restrict(noTraceSearchRestriction);
		StatsSet notHInW = new StatsSet(hInW);
		hInW.restrict(hInWRestriction);
		notHInW.restrict(hNotInWRestriction);

		StatsSet noTraceSearch = new StatsSet(randomShortest);
		noTraceSearch.restrict(noTraceSearchRestriction);

		StatsSet oneOutputDiffBean = new StatsSet(oneOutputDiff);
		oneOutputDiffBean.restrict(MrBeanRestriction);

		StatsSet oneOutputDiffShortest = new StatsSet(oneOutputDiff);
		oneOutputDiffShortest.restrict(shortestOracleRestriction);

		List<StatsSet> statsSets = new ArrayList<>();
		statsSets.add(noHeuristic);
		// statsSets.add(randomBean);
		// statsSets.add(randomShortest);
		// statsSets.add(counterBean);
		// statsSets.add(counterShortest);
		// statsSets.add(oneOutputDiffBean);
		// statsSets.add(oneOutputDiffShortest);
		statsSets.add(hInW);
		// statsSets.add(notHInW);
		// statsSets.add(noTraceSearch);
		// statsSets.add(files);

		Graph.PlotStyle style = Graph.PlotStyle.AVERAGE_WITH_EXTREMA;
		// style=Graph.PlotStyle.MEDIAN;
		{
			Graph<Integer, Float> g4bw2 = new Graph<Integer, Float>(
					HWStatsEntry.STATE_NUMBER, HWStatsEntry.DURATION);

			for (StatsSet statSet : statsSets) {
				g4bw2.plot(statSet, style);
			}
			g4bw2.plotEstimation(noHeuristic, Graph.EstimationMode.POWER);
			// g4bw2.setForceOrdLogScale(true);
			// g4bw2.setForceAbsLogScale(false);
			g4bw2.setFileName("influence_of_state_number_on_duration");
			// g4bw2.export();
		}

		Graph<Integer, Integer> gWt = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.MAX_W_TOTAL_LENGTH);
		for (StatsSet statSet : statsSets) {
			gWt.plot(statSet, style);
		}
		gWt.setFileName("influence_of_state_number_on_W_total_length");
		// gWt.export();

		Graph<Integer, Integer> gW = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.MAX_W_SIZE);
		for (StatsSet statSet : statsSets) {
			gW.plot(statSet, style);
		}
		gW.setFileName("influence_of_state_number_on_W_size");
		// gW.export();

		Graph<Integer, Float> gw = new Graph<Integer, Float>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.AVERAGE_W_SIZE);
		for (StatsSet statSet : statsSets) {
			gw.plot(statSet, style);
		}
		gw.setFileName("influence_of_state_number_on_W_sequences_length");
		// gw.export();

		Graph<Integer, Float> gol = new Graph<Integer, Float>(
				HWStatsEntry.STATE_NUMBER,
				HWStatsEntry.ORACLE_TRACE_PERCENTAGE);
		for (StatsSet statSet : statsSets) {
			gol.plot(statSet, style);
		}
		// gol.forceOrdRange(0, null);
		// gol.setForceOrdLogScale(false);
		gol.plot(randomBean, style);
		gol.setFileName("influence_of_state_number_on_oracle_length");
		// gol.export();

		Graph<Integer, Integer> go = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.ASKED_COUNTER_EXAMPLE);
		for (StatsSet statSet : statsSets) {
			go.plot(statSet, style);
		}
		go.forceOrdRange(0, null);
		go.setFileName("influence_of_state_number_on_number_of_call_to_oracle");
		// go.export();

		Graph<Integer, Integer> giw = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.W_INCONSISTENCY_FOUND);
		for (StatsSet statSet : statsSets) {
			giw.plot(statSet, style);
		}
		// StatsSet tmpSet=new StatsSet(allStats);
		// tmpSet.restrict(new
		// RangeRestriction<Integer>(NoResetStatsEntry.MAX_FAKE_STATES, 50,
		// 10000));
		// tmpSet.restrict(shortestOracleRestriction);
		// gfs.plot(tmpSet, style);
		giw.forceOrdRange(0, 10);
		giw.forceAbsRange(0, 40);
		giw.plotFunc("x", "real states number", LineStyle.APPROXIMATION);
		giw.setFileName("influence_of_state_number_on_number_of_inc_2");
		// giw.export();

		Graph<Integer, Integer> gfs = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.MAX_FAKE_STATES);
		for (StatsSet statSet : statsSets) {
			gfs.plot(statSet, style);
		}
		// StatsSet tmpSet=new StatsSet(allStats);
		// tmpSet.restrict(new
		// RangeRestriction<Integer>(NoResetStatsEntry.MAX_FAKE_STATES, 50,
		// 10000));
		// tmpSet.restrict(shortestOracleRestriction);
		// gfs.plot(tmpSet, style);
		gfs.forceOrdRange(0, 150);
		gfs.plotFunc("x", "real states number", LineStyle.APPROXIMATION);
		gfs.setFileName("influence_of_state_number_on_number_of_fake-states");
		// gfs.export();

		Graph<Integer, Integer> gs = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.SUB_INFERANCE_NB);
		for (StatsSet statSet : statsSets) {
			gs.plot(statSet, style);
		}
		gs.setFileName("influence_of_state_number_on_number_of_sub-inference");
		// gs.export();

		Graph<Integer, Integer> ghr = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.H_ANSWERS_NB);
		for (StatsSet statSet : statsSets) {
			ghr.plot(statSet, style);
		}
		ghr.setFileName(
				"influence_of_state_number_on_homing_sequence_responses");
		// ghr.export();

		Graph<Integer, Integer> gh = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.H_MAX_LENGTH);
		for (StatsSet statSet : statsSets) {
			gh.plot(statSet, style);
		}
		gh.setFileName("influence_of_state_number_on_homing_sequence_length");
		// gh.export();
		{
			Graph<Integer, Integer> g4bw3 = new Graph<Integer, Integer>(
					HWStatsEntry.STATE_NUMBER, HWStatsEntry.TRACE_LENGTH);
			for (StatsSet statSet : statsSets) {
				g4bw3.plot(statSet, style);
			}
			g4bw3.plot(randomBean, style);
			// g4bw3.forceOrdRange(0, 500000);
			g4bw3.setForceOrdLogScale(false);
			// g4bw3.setForceAbsLogScale(true);
			// g4bw3.plotEstimation(counterShortest,
			// Graph.EstimationMode.POWER);
			// g4bw3.plotEstimation(counterBean, Graph.EstimationMode.POWER);
			g4bw3.plotEstimation(noHeuristic, Graph.EstimationMode.POWER);
			g4bw3.plotEstimation(hInW, Graph.EstimationMode.POWER);
			// g4bw3.plotEstimation(notHInW, Graph.EstimationMode.POWER);
			// g4bw3.plotEstimation(oneOutputDiffShortest,
			// Graph.EstimationMode.POWER);
			g4bw3.setFileName("influence_of_state_number_on_trace_length");
			// g4bw3.export();
		}
		{
			Graph<Integer, Integer> inputs_trace = new Graph<Integer, Integer>(
					HWStatsEntry.INPUT_SYMBOLS, HWStatsEntry.TRACE_LENGTH);
			StatsSet s = new StatsSet(allStats);
			s.restrict(MrBeanRestriction);
			s.restrict(randomMealyRestriction);
			s.restrict(new EqualsRestriction<Integer>(HWStatsEntry.STATE_NUMBER,
					30));
			StatsSet noHeuristics = new StatsSet(s);
			s.restrict(simpleTraceSearchRestriction);
			s.restrict(check3rdRestriction);
			s.restrict(hInWRestriction);
			s.restrict(withHZXWRestriction);
			s.setTitle("with heuristics");
			noHeuristics.restrict(noCheck3rdRestriction);
			noHeuristics.restrict(noTraceSearchRestriction);
			noHeuristics.restrict(hNotInWRestriction);
			noHeuristics.restrict(withoutHZXWRestriction);
			noHeuristics.setTitle("without heuristics");
			inputs_trace.plot(s, PlotStyle.AVERAGE, s.getTitle());
			inputs_trace.plot(noHeuristics, PlotStyle.AVERAGE,
					noHeuristic.getTitle());
			// inputs_trace.plotEstimation(s, Graph.EstimationMode.POWER);
			// inputs_trace.plotEstimation(noHeuristics,
			// Graph.EstimationMode.POWER);
			inputs_trace.setDataDescriptionFields(new Attribute<?>[] {});
			inputs_trace
					.setFileName("influence_of_inputs_number_on_trace_length");
			inputs_trace.setForceOrdLogScale(false);
			// inputs_trace.setSize(600, 400);
			inputs_trace.getKeyParameters()
					.sethPosition(HorizontalPosition.LEFT);
			inputs_trace.export();
		}
		StatsSet s5Without = new StatsSet(random);
		s5Without.restrict(
				new EqualsRestriction<Integer>(HWStatsEntry.OUTPUT_SYMBOLS, 5));
		s5Without.restrict(
				new EqualsRestriction<Integer>(HWStatsEntry.INPUT_SYMBOLS, 5));
		// s5Without.restrict(new
		// EqualsRestriction<Integer>(HWStatsEntry.STATE_BOUND_OFFSET, 0));

		Graph<Integer, Integer> g5 = new Graph<Integer, Integer>(
				HWStatsEntry.STATE_NUMBER, HWStatsEntry.TRACE_LENGTH);
		g5.plot(s5Without, Graph.PlotStyle.AVERAGE_WITH_EXTREMA);
		// g5.setForceOrdLogScale(false);
		// g4bw3.setForceAbsLogScale(true);
		// g5.plotFunc("6*x**2","O(n^2)",LineStyle.APPROXIMATION);
		// g5.plotFunc("2*x**2.5","O(n^{2.5})",LineStyle.APPROXIMATION);
		g5.setFileName("influence_of_state_number_on_trace_length_5");
		// g5.export();

		GraphPersonalizer statesPersonalizer = new GraphPersonalizer() {
			@Override
			public void personalize(Graph<?, ?> g) {
				g.forceAbsRange(10, null);
			}
		};
		List<StatsSet> baseSets = new ArrayList<>();
		{
			StatsSet random_ = new StatsSet(allStats);
			random_.restrict(new InSetRestriction<Integer>(
					HWStatsEntry.STATE_NUMBER, keptStates));
			random_.restrict(randomMealyRestriction);
			random_.restrict(withUnknownWRestriction);
			random_.restrict(output2);
			random_.restrict(input2);
			random_.setTitle("random automata");
			baseSets.add(random_);
			generateHeuristicComparison(baseSets, HWStatsEntry.STATE_NUMBER,
					" random ", statesPersonalizer);
			generateOracleComparison(baseSets, HWStatsEntry.STATE_NUMBER,
					" random ", statesPersonalizer);

			baseSets = new ArrayList<>();
			StatsSet oneOutputDiff_ = new StatsSet(allStats);
			oneOutputDiff_.restrict(new InSetRestriction<Integer>(
					HWStatsEntry.STATE_NUMBER, mult5));
			oneOutputDiff_.restrict(withUnknownWRestriction);
			oneOutputDiff_.restrict(new EqualsRestriction<String>(
					HWStatsEntry.AUTOMATA,
					new RandomOneOutputDiffMealyDriver().getSystemName()));
			oneOutputDiff_.restrict(input5);
			oneOutputDiff_.setTitle("one output diff");
			baseSets.add(oneOutputDiff_);
			// generateHeuristicComparison(baseSets, HWStatsEntry.STATE_NUMBER,
			// " one output diff ", statesPersonalizer);

			baseSets = new ArrayList<>();
			StatsSet randomAndCounter_ = new StatsSet(allStats);
			randomAndCounter_.restrict(new InSetRestriction<Integer>(
					HWStatsEntry.STATE_NUMBER, keptStates));
			randomAndCounter_.restrict(
					new EqualsRestriction<String>(HWStatsEntry.AUTOMATA,
							new RandomAndCounterMealyDriver().getSystemName()));
			randomAndCounter_.restrict(withUnknownWRestriction);
			randomAndCounter_.setTitle("random and counter");
			baseSets.add(randomAndCounter_);
			// generateHeuristicComparison(baseSets, HWStatsEntry.STATE_NUMBER,
			// " counter ", statesPersonalizer);

			baseSets = new ArrayList<>();
			baseSets.add(oneOutputDiff_);
			baseSets.add(randomAndCounter_);
			baseSets.add(random_);
			generateOracleComparison(baseSets, HWStatsEntry.STATE_NUMBER,
					"generated automata", statesPersonalizer);
		}

		baseSets = new ArrayList<>();
		{
			StatsSet s = new StatsSet(allStats);
			s.restrict(randomMealyRestriction);
			// s.restrict(new
			// RangeRestriction<Integer>(HWStatsEntry.STATE_NUMBER, 5, 20));
			s.restrict(output2);
			StatsSet s_ = new StatsSet(s);
			s_.restrict(MrBeanRestriction);
			s_.restrict(state10);
			s_.restrict(withUnknownWRestriction);
			baseSets.add(s_);
			s_ = new StatsSet(s);
			s_.restrict(state60);
			// baseSets.add(s_);
		}
		generateHeuristicComparison(baseSets, HWStatsEntry.INPUT_SYMBOLS);

		baseSets = new ArrayList<>();
		{
			StatsSet dotFiles = new StatsSet(allStats);
			dotFiles.restrict(fromDotFileRestriction);
			dotFiles.restrict(withUnknownWRestriction);
			baseSets.add(dotFiles);
		}
		generateHeuristicComparison(baseSets, HWStatsEntry.AUTOMATA);
		generateOracleComparison(baseSets, HWStatsEntry.AUTOMATA, "dot_files",
				null);

		generateJSS_2018(allStats);

	}

	<T_ABS extends Comparable<T_ABS>> void generateHeuristicComparison(
			List<StatsSet> baseSets, Attribute<T_ABS> abs) {
		generateHeuristicComparison(baseSets, abs, "", null);
	}

	<T_ABS extends Comparable<T_ABS>> void generateHeuristicComparison(
			List<StatsSet> baseSets, Attribute<T_ABS> abs, String setName,
			final GraphPersonalizer upperPersonalizer) {
		String filePrefix = "(1) heuristics " + setName + "-- " + abs.getName();
		GraphPersonalizer heuristicPersonalize = new GraphPersonalizer() {
			@Override
			public void personalize(Graph<?, ?> g) {
				if (upperPersonalizer != null)
					upperPersonalizer.personalize(g);
				g.setDataDescriptionFields(new Attribute<?>[] {
						Attribute.INPUT_SYMBOLS, Attribute.OUTPUT_SYMBOLS,
						Attribute.AUTOMATA, Attribute.ORACLE_USED });
				g.setDataDescriptionFields(new Attribute<?>[] {});

			}

		};

		class PlotItem {
			public PlotItem(StatsSet set, PointShape pointType) {
				super();
				this.set = set;
				this.pointType = pointType;
			}

			StatsSet set;
			PointShape pointType;

			public void plotOn(Graph<?, ?> g) {
				g.plot(set, defaultStyle, set.getTitle(), pointType, null);
			}
		}
		List<PlotItem> heuristics = new ArrayList<>();
		for (StatsSet s_ : baseSets) {
			StatsSet s = new StatsSet(s_);
			s.setTitle(s_.getTitle());
			if (baseSets.size() == 1)
				s.setTitle("");
			StatsSet _noHeuristic = new StatsSet(s);
			_noHeuristic.restrict(hNotInWRestriction);
			_noHeuristic.restrict(noTraceSearchRestriction);
			_noHeuristic.restrict(noCheck3rdRestriction);
			_noHeuristic.restrict(withoutHZXWRestriction);
			_noHeuristic.setTitle(s.getTitle() + "without heuristic");
			heuristics.add(
					new PlotItem(_noHeuristic, Graph.PointShape.PLUS_CROSS));

			StatsSet _hInW = new StatsSet(s);
			_hInW.restrict(hInWRestriction);
			_hInW.restrict(noTraceSearchRestriction);
			_hInW.restrict(noCheck3rdRestriction);
			_hInW.restrict(withoutHZXWRestriction);
			_hInW.setTitle(s.getTitle() + "adding h in W");
			heuristics.add(new PlotItem(_hInW, Graph.PointShape.TIMES_CROSS));

			StatsSet _check3rd = new StatsSet(s);
			_check3rd.restrict(hNotInWRestriction);
			_check3rd.restrict(noTraceSearchRestriction);
			_check3rd.restrict(check3rdRestriction);
			_check3rd.restrict(withoutHZXWRestriction);
			_check3rd.setTitle(s.getTitle() + "inconsistencies on h");
			heuristics.add(new PlotItem(_check3rd,
					Graph.PointShape.FILLED_TRIANGLE_UP));

			StatsSet _simpleTrace = new StatsSet(s);
			_simpleTrace.restrict(hNotInWRestriction);
			_simpleTrace.restrict(simpleTraceSearchRestriction);
			_simpleTrace.restrict(check3rdRestriction);
			_simpleTrace.restrict(withoutHZXWRestriction);
			_simpleTrace.setTitle(
					s.getTitle() + "using trace and third inconsistencies");
			heuristics.add(
					new PlotItem(_simpleTrace, Graph.PointShape.EMPTY_SQUARE));

			StatsSet _reuseHZXW = new StatsSet(s);
			_reuseHZXW.restrict(hNotInWRestriction);
			_reuseHZXW.restrict(noTraceSearchRestriction);
			_reuseHZXW.restrict(noCheck3rdRestriction);
			_reuseHZXW.restrict(withHZXWRestriction);
			_reuseHZXW.setTitle(s.getTitle() + "use dictionary");
			heuristics.add(new PlotItem(_reuseHZXW,
					Graph.PointShape.EMPTY_TRIANGLE_UP));

			StatsSet _allTogetherNotTrace = new StatsSet(s);
			_allTogetherNotTrace.restrict(hInWRestriction);
			_allTogetherNotTrace.restrict(noTraceSearchRestriction);
			_allTogetherNotTrace.restrict(check3rdRestriction);
			_allTogetherNotTrace.restrict(withHZXWRestriction);
			_allTogetherNotTrace.setTitle(
					s.getTitle() + "the three heuristics together, no trace");
			heuristics.add(new PlotItem(_allTogetherNotTrace,
					Graph.PointShape.FILLED_CIRCLE));

			StatsSet _allTogether = new StatsSet(s);
			_allTogether.restrict(hInWRestriction);
			_allTogether.restrict(simpleTraceSearchRestriction);
			_allTogether.restrict(check3rdRestriction);
			_allTogether.restrict(withHZXWRestriction);
			_allTogether.setTitle(
					s.getTitle() + "all heuristics together, using trace");
			heuristics.add(
					new PlotItem(_allTogether, Graph.PointShape.FILLED_CIRCLE));
		}

		Graph<T_ABS, Integer> wInconsistenciesFound = new Graph<T_ABS, Integer>(
				abs, HWStatsEntry.W_INCONSISTENCY_FOUND);
		for (PlotItem item : heuristics) {
			item.plotOn(wInconsistenciesFound);
		}
		wInconsistenciesFound.setForceOrdLogScale(false);
		heuristicPersonalize.personalize(wInconsistenciesFound);
		wInconsistenciesFound.setFileName(filePrefix + " on_w_inc_found");
		wInconsistenciesFound.export();

		Graph<T_ABS, Integer> hInconsistenciesFound = new Graph<T_ABS, Integer>(
				abs, HWStatsEntry.H_INCONSISTENCY_FOUND);
		for (PlotItem item : heuristics) {
			item.plotOn(hInconsistenciesFound);
		}
		hInconsistenciesFound.setForceOrdLogScale(false);
		heuristicPersonalize.personalize(hInconsistenciesFound);
		hInconsistenciesFound.setFileName(filePrefix + " on_h_inc_found");
		hInconsistenciesFound.export();

		Graph<T_ABS, Integer> traceLength = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.TRACE_LENGTH);
		for (PlotItem item : heuristics) {
			item.plotOn(traceLength);
		}
		heuristicPersonalize.personalize(traceLength);
		traceLength.setFileName(filePrefix + " on_trace_length");
		traceLength.getKeyParameters().setvPosition(VerticalPosition.BOTTOM);
		traceLength.setForceOrdLogScale(false);
		traceLength.export();

		Graph<T_ABS, Integer> w = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.MAX_W_SIZE);
		for (PlotItem item : heuristics) {
			item.plotOn(w);
		}
		w.setFileName(filePrefix + " _w_size");
		w.export();

		Graph<T_ABS, Integer> oracle_call = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.ASKED_COUNTER_EXAMPLE);
		for (PlotItem item : heuristics) {
			item.plotOn(oracle_call);
		}
		heuristicPersonalize.personalize(oracle_call);
		oracle_call.setFileName(filePrefix + " _on_oracle_call");
		oracle_call.export();

		Graph<T_ABS, Float> duration = new Graph<T_ABS, Float>(abs,
				HWStatsEntry.DURATION);

		for (PlotItem item : heuristics) {
			item.plotOn(duration);
		}
		heuristicPersonalize.personalize(duration);
		duration.setFileName(filePrefix + " _on_duration");
		duration.export();

	}

	/**
	 * plot comparisons depending on which oracle is used
	 * 
	 * @param baseSets
	 *            a list of sets to compare
	 * @param abs
	 *            abscissa attribute
	 * @param setName
	 *            name used for filename
	 * @param upperPersonalizer
	 *            an object to personalize all graphs. Can be null
	 */
	<T_ABS extends Comparable<T_ABS>> void generateOracleComparison(
			List<StatsSet> baseSets, Attribute<T_ABS> abs, String setName,
			final GraphPersonalizer upperPersonalizer) {
		String filePrefix = "(2) oracles - " + setName + "-- " + abs.getName();
		GraphPersonalizer globalPersonalize = new GraphPersonalizer() {
			@Override
			public void personalize(Graph<?, ?> g) {

				g.setDataDescriptionFields(new Attribute<?>[] {
						Attribute.INPUT_SYMBOLS, Attribute.OUTPUT_SYMBOLS,
						// Attribute.AUTOMATA,
						Attribute.ADD_H_IN_W, Attribute.SEARCH_CE_IN_TRACE,
						Attribute.REUSE_HZXW, });
				g.setDataDescriptionFields(new Attribute<?>[] {});
				if (upperPersonalizer != null)
					upperPersonalizer.personalize(g);

			}

		};

		List<StatsSet> setsToPlot = new ArrayList<>();
		for (StatsSet s_ : baseSets) {
			StatsSet s = new StatsSet(s_);
			if (baseSets.size() == 1)
				s.setTitle("");
			else
				s.setTitle(s_.getTitle());
			StatsSet mrBean = new StatsSet(s);
			mrBean.restrict(MrBeanRestriction);
			mrBean.setTitle(s.getTitle() + "Mr Bean");
			setsToPlot.add(mrBean);

			StatsSet shortest = new StatsSet(s);
			shortest.restrict(shortestOracleRestriction);
			shortest.setTitle(s.getTitle() + "shortest");
			setsToPlot.add(shortest);
		}

		Graph<T_ABS, Integer> wInconsistenciesFound = new Graph<T_ABS, Integer>(
				abs, HWStatsEntry.W_INCONSISTENCY_FOUND);
		for (StatsSet statSet : setsToPlot) {
			wInconsistenciesFound.plot(statSet, defaultStyle);
		}
		wInconsistenciesFound.setForceOrdLogScale(false);
		globalPersonalize.personalize(wInconsistenciesFound);
		wInconsistenciesFound.setFileName(filePrefix + " on_w_inc_found");
		wInconsistenciesFound.export();

		Graph<T_ABS, Integer> hInconsistenciesFound = new Graph<T_ABS, Integer>(
				abs, HWStatsEntry.H_INCONSISTENCY_FOUND);
		for (StatsSet statSet : setsToPlot) {
			hInconsistenciesFound.plot(statSet, defaultStyle);
		}
		hInconsistenciesFound.setForceOrdLogScale(false);
		globalPersonalize.personalize(hInconsistenciesFound);
		hInconsistenciesFound.setFileName(filePrefix + " on_h_inc_found");
		hInconsistenciesFound.export();

		Graph<T_ABS, Integer> traceLength = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.TRACE_LENGTH);
		for (StatsSet statSet : setsToPlot) {
			traceLength.plot(statSet, defaultStyle, statSet.getTitle());
		}
		globalPersonalize.personalize(traceLength);
		traceLength.setForceOrdLogScale(true);
		traceLength.setFileName(filePrefix + " on_trace_length");
		traceLength.getKeyParameters().setvPosition(VerticalPosition.BOTTOM);
		traceLength.export();

		Graph<T_ABS, Integer> w = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.MAX_W_SIZE);
		for (StatsSet statSet : setsToPlot) {
			w.plot(statSet, defaultStyle);
		}
		w.setFileName(filePrefix + " _w_size");
		w.export();

		Graph<T_ABS, Integer> oracle_call = new Graph<T_ABS, Integer>(abs,
				HWStatsEntry.ASKED_COUNTER_EXAMPLE);
		for (StatsSet statSet : setsToPlot) {
			oracle_call.plot(statSet, defaultStyle);
		}
		globalPersonalize.personalize(oracle_call);
		oracle_call.setFileName(filePrefix + " _on_oracle_call");
		oracle_call.export();

		Graph<T_ABS, Float> duration = new Graph<T_ABS, Float>(abs,
				HWStatsEntry.DURATION);
		for (StatsSet statSet : setsToPlot) {
			duration.plot(statSet, defaultStyle);
		}
		globalPersonalize.personalize(duration);
		duration.setFileName(filePrefix + " _on_duration");
		duration.export();
	}

	static public final OneConfigPlotter jss2018_traceAndHeuristicsPlotter = new OneConfigPlotter(
			new Restriction[] { MrBeanRestriction, hInWRestriction,
					simpleTraceSearchRestriction, check3rdRestriction,
					withHZXWRestriction },
			PointShape.EMPTY_DIAMOND, new Color("#800000"),
			"the 3 heuristics together and trace");

	private void generateJSS_2018(StatsSet allStats_) {
		if (allStats_.size() == 0)
			return;

		PlotStyle plotStyle = PlotStyle.AVERAGE;
		StatsSet allStats = new StatsSet(allStats_);
		allStats.restrict(withUnknownWRestriction);
		Integer[] keptStates = new Integer[] { 5, 15, 30, 70, 150, 300, 700,
				1500, 3000 };

		OneConfigPlotter withoutHeuristics = new OneConfigPlotter(
				new Restriction[] { MrBeanRestriction, hNotInWRestriction,
						noTraceSearchRestriction, noCheck3rdRestriction,
						withoutHZXWRestriction },
				PointShape.FILLED_TRIANGLE_UP, new Color("#88EB88"),
				"without heuristics");

		OneConfigPlotter hInW = new OneConfigPlotter(
				new Restriction[] { MrBeanRestriction, hInWRestriction,
						noTraceSearchRestriction, noCheck3rdRestriction,
						withoutHZXWRestriction },
				PointShape.FILLED_CIRCLE, new Color("#E0E070"),
				"adding h in W");

		OneConfigPlotter check3rd = new OneConfigPlotter(
				new Restriction[] { MrBeanRestriction, hNotInWRestriction,
						noTraceSearchRestriction, check3rdRestriction,
						withoutHZXWRestriction },
				PointShape.EMPTY_SQUARE, new Color("#4B0082"),
				"inconsistencies on h");

		OneConfigPlotter dictionnary = new OneConfigPlotter(
				new Restriction[] { MrBeanRestriction, hNotInWRestriction,
						noTraceSearchRestriction, noCheck3rdRestriction,
						withHZXWRestriction },
				PointShape.EMPTY_TRIANGLE_DOWN, new Color("#800000"),
				"use dictionary");

		OneConfigPlotter heuristicsWithoutTrace = new OneConfigPlotter(
				new Restriction[] { MrBeanRestriction, hInWRestriction,
						noTraceSearchRestriction, check3rdRestriction,
						withHZXWRestriction },
				PointShape.TIMES_CROSS, new Color("#008000"),
				"the 3 heuristics together");

		StatsSet random_ = new StatsSet(allStats);

		random_.restrict(randomMealyRestriction);
		random_.restrict(output2);
		random_.restrict(input2);
		random_.setTitle("random automata");

		StatsSet randomSmall = new StatsSet(random_);
		StatsSet randomBig = new StatsSet(random_);
		randomBig.restrict(new InSetRestriction<Integer>(
				HWStatsEntry.STATE_NUMBER, keptStates));
		randomSmall.restrict(new Restriction() {
			@Override
			public boolean contains(StatsEntry s) {
				int states = s.get(HWStatsEntry.STATE_NUMBER);
				return (states % 20 == 0) && states <= 220 && states >= 40;
			}
		});

		int halfPageSize = 300;
		{
			// trace CE comparison
			Graph<Integer, Integer> traceLength = new Graph<Integer, Integer>(
					HWStatsEntry.STATE_NUMBER, HWStatsEntry.TRACE_LENGTH);
			traceLength.setForArticle(true);
			traceLength.setSize(halfPageSize, halfPageSize);
			traceLength.setTitle("");
			heuristicsWithoutTrace.plotOn(traceLength, randomSmall,
					"using only random walk", plotStyle);
			jss2018_traceAndHeuristicsPlotter.plotOn(traceLength, randomSmall,
					"using trace to find CE", plotStyle);
			traceLength.setFileName("traceCE_comparison_trace");
			traceLength.setForceAbsLogScale(false);
			traceLength.setForceOrdLogScale(false);
			traceLength.forceAbsRange(50, 200);
			traceLength.setDataDescriptionFields(new Attribute<?>[] {});
			traceLength.getKeyParameters().setOutside(true);
			traceLength.export();

			Graph<Integer, Integer> oracleCalls = new Graph<Integer, Integer>(
					HWStatsEntry.STATE_NUMBER,
					HWStatsEntry.ASKED_COUNTER_EXAMPLE);
			oracleCalls.setForArticle(true);
			oracleCalls.setSize(halfPageSize, halfPageSize);
			heuristicsWithoutTrace.plotOn(oracleCalls, randomBig,
					"using only random walk", plotStyle);
			jss2018_traceAndHeuristicsPlotter.plotOn(oracleCalls, randomBig,
					"using trace to find CE", plotStyle);
			oracleCalls.setFileName("traceCE_comparison_oracle");
			oracleCalls.setForceAbsLogScale(true);
			oracleCalls.setForceOrdLogScale(false);
			oracleCalls.forceAbsRange(5, 4000);
			oracleCalls.setTitle("");
			oracleCalls.setDataDescriptionFields(new Attribute<?>[] {});
			oracleCalls.getKeyParameters().setOutside(true);
			oracleCalls.export();
		}
		{
			// heuristics comparison
			List<OneConfigPlotter> plotters = new ArrayList<>();
			plotters.add(withoutHeuristics);
			plotters.add(hInW);
			plotters.add(check3rd);
			plotters.add(dictionnary);
			plotters.add(heuristicsWithoutTrace);

			Graph<Integer, Integer> traceLength = new Graph<Integer, Integer>(
					HWStatsEntry.STATE_NUMBER, HWStatsEntry.TRACE_LENGTH);
			traceLength.setForArticle(true);
			traceLength.setSize(halfPageSize, halfPageSize);
			traceLength.setTitle("");
			traceLength.setDefaultPlotStyle(plotStyle);
			for (OneConfigPlotter item : plotters) {
				item.plotOn(traceLength, randomSmall);
			}
			traceLength.setFileName("heuristics_comparison_trace");
			// traceLength.getKeyParameters().setvPosition(VerticalPosition.BOTTOM);
			// traceLength.getKeyParameters().sethPosition(HorizontalPosition.LEFT);
			traceLength.setForceAbsLogScale(false);
			traceLength.setForceOrdLogScale(false);
			traceLength.forceAbsRange(50, 200);
			traceLength.setDataDescriptionFields(new Attribute<?>[] {});
			traceLength.getKeyParameters().setOutside(true);
			traceLength.export();

			Graph<Integer, Integer> oracleCalls = new Graph<Integer, Integer>(
					HWStatsEntry.STATE_NUMBER,
					HWStatsEntry.ASKED_COUNTER_EXAMPLE);
			oracleCalls.setForArticle(true);
			oracleCalls.setSize(halfPageSize, halfPageSize);
			oracleCalls.setDefaultPlotStyle(plotStyle);
			for (OneConfigPlotter item : plotters) {
				item.plotOn(oracleCalls, randomBig);
			}
			oracleCalls.setFileName("heuristics_comparison_oracle");
			oracleCalls.setForceAbsLogScale(true);
			oracleCalls.setForceOrdLogScale(false);
			oracleCalls.forceAbsRange(5, 4000);
			oracleCalls.setTitle("");
			oracleCalls.setDataDescriptionFields(new Attribute<?>[] {});
			oracleCalls.getKeyParameters().setOutside(true);
			oracleCalls.export();
		}

		{
			// inputs comparison
			StatsSet s = new StatsSet(allStats);
			s.restrict(MrBeanRestriction);
			s.restrict(randomMealyRestriction);
			s.restrict(new EqualsRestriction<Integer>(HWStatsEntry.STATE_NUMBER,
					30));
			s.restrict(new InSetRestriction<>(HWStatsEntry.INPUT_SYMBOLS,
					new Integer[] { 2, 4, 6, 8, 10, 15, 20, 30, 40, 50, 60 }));
			Graph<Integer, Integer> inputs_trace = new Graph<Integer, Integer>(
					HWStatsEntry.INPUT_SYMBOLS, HWStatsEntry.TRACE_LENGTH);
			inputs_trace.setForArticle(true);
			inputs_trace.setDefaultPlotStyle(plotStyle);
			withoutHeuristics.plotOn(inputs_trace, s, "without heuristics");
			jss2018_traceAndHeuristicsPlotter.plotOn(inputs_trace, s,
					"with heuristics");

			inputs_trace.setDataDescriptionFields(new Attribute<?>[] {});
			inputs_trace
					.setFileName("influence_of_inputs_number_on_trace_length");
			inputs_trace.setForceOrdLogScale(false);
			inputs_trace.setSize(600, 400);
			inputs_trace.getKeyParameters()
					.sethPosition(HorizontalPosition.LEFT);
			inputs_trace.export();
		}

	}

}
