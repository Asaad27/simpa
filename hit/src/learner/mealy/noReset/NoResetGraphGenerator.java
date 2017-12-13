package learner.mealy.noReset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import drivers.mealy.transparent.RandomAndCounterMealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import stats.Graph;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.InSetRestriction;

public class NoResetGraphGenerator extends GraphGenerator {

	Map<Integer, Map<Integer, Map<Integer, Collection<NoResetStatsEntry>>>> groups = new HashMap<>();

	protected <K, E> E getOrCreate(Map<K, E> map, K key, E defaultValue) {
		if (!map.containsKey(key))
			map.put(key, defaultValue);
		return map.get(key);
	}

	protected Collection<NoResetStatsEntry> getGroup(NoResetStatsEntry entry) {
		Integer cStates = entry.get(NoResetStatsEntry.STATE_NUMBER);
		Integer rStates = entry.get(NoResetStatsEntry.MAX_RECKONED_STATES);
		Integer fStates = entry.get(NoResetStatsEntry.MAX_FAKE_STATES);
		Collection<NoResetStatsEntry> r = getOrCreate(
				getOrCreate(
						getOrCreate(
								groups,
								cStates,
								new HashMap<Integer, Map<Integer, Collection<NoResetStatsEntry>>>()),
						rStates,
						new HashMap<Integer, Collection<NoResetStatsEntry>>()),
				fStates, new ArrayList<NoResetStatsEntry>());
		return r;
	}

	void printStats(StatsSet set) {
		Map<Integer, StatsSet> byCState = set
				.sortByAtribute(NoResetStatsEntry.STATE_NUMBER);
		for (Integer cStates : new TreeSet<Integer>(byCState.keySet())) {
			StatsSet cStats = byCState.get(cStates);
			Map<Integer, StatsSet> byRStates = cStats
					.sortByAtribute(NoResetStatsEntry.MAX_RECKONED_STATES);
			for (Integer rStates : new TreeSet<Integer>(byRStates.keySet())) {
				StatsSet rStats = byRStates.get(rStates);

				Map<Integer, StatsSet> byFStates = rStats
						.sortByAtribute(NoResetStatsEntry.MAX_FAKE_STATES);
				for (Entry<Integer, StatsSet> ef : byFStates.entrySet()) {
					Integer fStates = ef.getKey();
					StatsSet fStats = ef.getValue();

					System.out
							.println(""
									+ fStats.size()
									+ "\t"
									+ cStates
									+ "\t"
									+ rStates
									+ "\t"
									+ fStates
									+ "\t"
									+ fStats.attributeMax(NoResetStatsEntry.TRACE_LENGTH)
									+ "\t"
									+ fStats.attributeMedian(NoResetStatsEntry.TRACE_LENGTH));

				}
			}
		}
	}
	
	void printStats2(StatsSet set){
		Attribute<Integer>sort=NoResetStatsEntry.STATE_NUMBER;
		List<Attribute<?>>attributes=new ArrayList<>();
		attributes.add(NoResetStatsEntry.MAX_RECKONED_STATES);
		attributes.add(NoResetStatsEntry.MAX_FAKE_STATES);
		attributes.add(NoResetStatsEntry.H_LENGTH);
		attributes.add(NoResetStatsEntry.W_SIZE);
		attributes.add(NoResetStatsEntry.MAX_W_LENGTH);
		attributes.add(NoResetStatsEntry.W_TOTAL_LENGTH);
		attributes.add(NoResetStatsEntry.TRACE_LENGTH);

		
		
		System.out.print("occurences\t"+sort.getName());
		for (Attribute<?>a:attributes){
			System.out.print('\t');
			System.out.print('\t');
			System.out.print(a.getName()+"(median and max)");
			System.out.print('\t');
		}
		System.out.print('\n');
		
		Map<Integer, StatsSet> byCState = set
				.sortByAtribute(sort);
		for (Integer cStates : new TreeSet<Integer>(byCState.keySet())) {
			StatsSet cStats = byCState.get(cStates);
			System.out.print(cStats.size()+"\t"+cStates);
			for (Attribute<?>a:attributes){
				System.out.print("\t\t");
				System.out.print(cStats.attributeMedian(a));
				System.out.print('\t');
				System.out.print(cStats.attributeMax(a));
			}
			System.out.print('\n');
		}
	}

	public NoResetGraphGenerator() {
	}

	@Override
	public void generate(StatsSet s) {
		printStats2(s);

		StatsSet withoutSpeedUp = new StatsSet(s);
		withoutSpeedUp.restrict(new EqualsRestriction<Boolean>(NoResetStatsEntry.WITH_SPEEDUP, false));
		
		StatsSet randomWithout = new StatsSet(withoutSpeedUp);
		randomWithout.restrict(
				new EqualsRestriction<String>(NoResetStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));

//		Graph<Integer, Integer> g1 = new Graph<Integer, Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS,
//				NoResetStatsEntry.TRACE_LENGTH);
//		StatsSet s1 = new StatsSet(random);
		
		// s1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		// s1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 10));
		// s1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W1_LENGTH, 1));
		// g1.plotGroup(s1,NoResetStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		// g1.setFileName("influence_of_output_symbols");
		// g1.export();


		// StatsSet s2w3 = new StatsSet(s2);
		// s2w3.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 3));
		// Graph<Integer, Integer> g2w3 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.INPUT_SYMBOLS,
		// NoResetStatsEntry.TRACE_LENGTH);
		// g2w3.plotGroup(s2w3,NoResetStatsEntry.W_SIZE,
		// Graph.PlotStyle.MEDIAN);
		// g2w3.setForceOrdLogScale(false);
		// g2w3.forceOrdRange(0, null);
		// g2w3.forceAbsRange(0, null);
		// g2w3.setFileName("influence_of_input_symbols_w3");
		// g2w3.plotFunc("0.04*"+makeMaxTheoricalFunction(s2w3,
		// NoResetStatsEntry.INPUT_SYMBOLS), "shape of complexity bound
		// (1/25)");
		// if (s2w3.size() != 0)
		// g2w3.forceOrdRange(null,
		// s2w3.attributeMax(NoResetStatsEntry.TRACE_LENGTH));
		// g2w3.export();

		// Graph<Integer, Integer> g3 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.W_SIZE,
		// NoResetStatsEntry.TRACE_LENGTH);
		// StatsSet s3 = new StatsSet(random);
		// s3 .restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// s3 .restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s3.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 12));
		// s3.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		// if (s3.size() > 0){
		// g3.plot(s3, Graph.PlotStyle.POINTS);
		// g3.setFileName("influence_of_W_size");
		// g3.plotFunc("0.006*"+makeMaxTheoricalFunction(s2,
		// NoResetStatsEntry.W_SIZE), "shape of complexity bound");
		// g3.export();
		// }
		
		StatsSet withCounter = new StatsSet(withoutSpeedUp);
		withCounter.restrict(
				new EqualsRestriction<String>(NoResetStatsEntry.AUTOMATA, new RandomAndCounterMealyDriver().getSystemName()));


		StatsSet s4Counter = new StatsSet(withCounter);
		s4Counter.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 2));

		StatsSet s4Without = new StatsSet(randomWithout);
		s4Without.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 2));
		s4Without.restrict(new EqualsRestriction<Integer>(
				NoResetStatsEntry.INPUT_SYMBOLS, 2));
		int maxStates = 200;
		Integer[] mult5 = new Integer[maxStates / 5];
		for (int i = 0; i < maxStates / 5; i++) {
			mult5[i] = new Integer(5 * i);
		}
		s4Without.restrict(new InSetRestriction<Integer>(
				NoResetStatsEntry.STATE_NUMBER, mult5));

		Graph<Integer, Float> g4bw2 = new Graph<Integer, Float>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.DURATION);
		//g4bw2.plot(s4bw2W, Graph.PlotStyle.MEDIAN);
		StatsSet s4bw2WO = new StatsSet(s4Without);
		
		g4bw2.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		//g4bw2.plot(s4bw2WO, Graph.PlotStyle.MEDIAN);
		StatsSet s4bw2C = new StatsSet(s4Counter);

		g4bw2.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		
		g4bw2.setForceOrdLogScale(true);
		g4bw2.setFileName("influence_of_state_number_on_duration");
		g4bw2.export();

		
		Graph<Integer, Integer> gWt = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.W_TOTAL_LENGTH);
		gWt.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		gWt.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		gWt.setFileName("influence_of_state_number_on_W_total_length");
		gWt.export();
		
		

		Graph<Integer, Integer> gW = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.W_SIZE);
		gW.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		gW.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		gW.setFileName("influence_of_state_number_on_W_size");
		gW.export();
		

		Graph<Integer,Float> gw = new Graph<Integer, Float>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.AVERAGE_W_LENGTH);
		gw.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		gw.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		gw.setFileName("influence_of_state_number_on_W_sequences_length");
		gw.export();
		

		
		Graph<Integer, Integer> go = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.ASKED_COUNTER_EXAMPLE);
		go.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		go.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		go.setFileName("influence_of_state_number_on_number_of_call_to_oracle");
		go.export();

		
		Graph<Integer, Integer> gs = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.SUB_INFERANCE_NB);
		gs.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		gs.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		gs.setFileName("influence_of_state_number_on_number_of_sub-inference");
		gs.export();

		
		Graph<Integer, Integer> ghr = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.H_ANSWERS_NB);
		ghr.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		ghr.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		ghr.setFileName("influence_of_state_number_on_homing_sequence_responses");
		ghr.export();
		

		
		Graph<Integer, Integer> gh = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.H_LENGTH);
		gh.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		gh.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		gh.setFileName("influence_of_state_number_on_homing_sequence_length");
		gh.export();
		
		
		
		
		Graph<Integer, Integer> g4bw3 = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.TRACE_LENGTH);
		g4bw3.plot(s4bw2WO, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random automata");
		g4bw3.plot(s4bw2C, Graph.PlotStyle.AVERAGE_WITH_EXTREMA,"random and counter automata");
		//g4bw3.plot(s4bw3O, Graph.PlotStyle.MEDIAN);
		g4bw3.setForceOrdLogScale(false);
//		g4bw3.setForceAbsLogScale(true);
		g4bw3.plotEstimation(s4bw2C, Graph.EstimationMode.POWER);
		g4bw3.plotEstimation(s4bw2WO, Graph.EstimationMode.POWER);
		g4bw3.setFileName("influence_of_state_number_on_trace_length");
		g4bw3.export();
		
		
		
		
		
		StatsSet s5Without = new StatsSet(randomWithout);
		s5Without.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s5Without.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
//		s5Without.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));

		
		
		
		Graph<Integer, Integer> g5 = new Graph<Integer, Integer>(
				NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.TRACE_LENGTH);
		g5.plot(s5Without, Graph.PlotStyle.AVERAGE_WITH_EXTREMA);
		//g5.setForceOrdLogScale(false);
		//g4bw3.setForceAbsLogScale(true);
		//g5.plotFunc("6*x**2","O(n^2)",LineStyle.APPROXIMATION);
		//g5.plotFunc("2*x**2.5","O(n^{2.5})",LineStyle.APPROXIMATION);
		g5.setFileName("influence_of_state_number_on_trace_length_5");
		g5.export();

		
		
		
		
		
		
		// //RandomCounter
		// s4 = new StatsSet(withoutSpeedUp);
		// s4.restrict(new EqualsRestriction<String>(NoResetStatsEntry.AUTOMATA,
		// new RandomAndCounterMealyDriver().getSystemName()));
		// s4.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// s4.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s4.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		//
		// g4w1 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER,
		// NoResetStatsEntry.TRACE_LENGTH);
		// s4w1 = new StatsSet(s4);
		// s4w1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 1));
		// g4w1.plot(s4w1, Graph.PlotStyle.MEDIAN);
		// g4w1.setForceOrdLogScale(false);
		// g4w1.setFileName("influence_of_state_number_w1_RC");
		// g4w1.export();
		//
		// g4w2 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER,
		// NoResetStatsEntry.TRACE_LENGTH);
		// s4w2 = new StatsSet(s4);
		// s4w2.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		// g4w2.plot(s4w2, Graph.PlotStyle.MEDIAN);
		// g4w2.setForceOrdLogScale(false);
		// g4w2.setFileName("influence_of_state_number_w2_RC");
		// g4w2.export();
		//
		// g4w3 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER,
		// NoResetStatsEntry.TRACE_LENGTH);
		// s4w3 = new StatsSet(s4);
		// s4w3.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 3));
		// g4w3.plot(s4w3, Graph.PlotStyle.MEDIAN);
		// g4w3.setForceOrdLogScale(false);
		// g4w3.setFileName("influence_of_state_number_w3_RC");
		// g4w3.export();

		// Graph<Integer, Integer> g5 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER,
		// NoResetStatsEntry.LOCALIZER_CALL_NB);
		// StatsSet s5 = new StatsSet(random);
		// //s5.restrict(new
		// RangeRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0,
		// 30));
		// s5.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// s5.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE,
		// 2));
		// //s5.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W1_LENGTH, 2));
		// s5.restrict(new
		// InSetRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER_BOUND, new
		// Integer[]{6,9,12,15,18}));
		// g5.plotGroup(s5,NoResetStatsEntry.STATE_NUMBER_BOUND,
		// Graph.PlotStyle.MEDIAN);
		// g5.setFileName("influence_of_state_number_on_localizer_call");
		// g5.export();
		//
		// Graph<Integer, Integer> g5b = new
		// Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER_BOUND,
		// NoResetStatsEntry.LOCALIZER_SEQUENCE_LENGTH);
		// StatsSet s5b = new StatsSet(random);
		// s5b.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE,
		// 2));
		// g5b.plotGroup(s5b,NoResetStatsEntry.W1_LENGTH,
		// Graph.PlotStyle.MEDIAN);
		// s5b = new StatsSet(random);
		// s5b.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE,
		// 3));
		// g5b.plotGroup(s5b,NoResetStatsEntry.W1_LENGTH,
		// Graph.PlotStyle.MEDIAN);
		// g5b.setFileName("influence_of_state_number_bound_on_localizer_sequence_length");
		// g5b.export();
		//
		// Graph<Integer, Integer> g6 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER_BOUND,
		// NoResetStatsEntry.TRACE_LENGTH);
		// StatsSet s6 = new StatsSet(random);
		// s6.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// s6.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s6.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE,
		// 2));
		// s6.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 6));
		// s6.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W1_LENGTH, 1));
		// g6.plot(s6, Graph.PlotStyle.POINTS);
		// g6.setForceOrdLogScale(false);
		// g6.setFileName("influence_of_state_number_bound");
		// g6.export();
		//
		// Graph<Integer, Integer> g7 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.TRACE_LENGTH,
		// NoResetStatsEntry.MEMORY);
		// StatsSet s7 = new StatsSet(random);
		// //s7.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// //s7.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// // s7.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		// // s7.restrict(new
		// InSetRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, new
		// Integer[]{0,5,10,15}));
		// g7.plot(s7, Graph.PlotStyle.POINTS);
		// //g7.setForceOrdLogScale(true);
		// g7.setFileName("memory");
		// g7.export();
		//
		// Graph<Integer, Float> g7bis = new
		// Graph<Integer,Float>(NoResetStatsEntry.INPUT_SYMBOLS,
		// NoResetStatsEntry.DURATION);
		// g7bis.plotGroup(s7,NoResetStatsEntry.STATE_BOUND_OFFSET,
		// Graph.PlotStyle.POINTS);
		// //g7.setForceOrdLogScale(true);
		// g7bis.setFileName("duration");
		// g7bis.export();

		// StatsSet s8w3 = new StatsSet(s8);
		// s8w3.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 3));
		// Graph<Integer, Float> g8w3 = new
		// Graph<Integer,Float>(NoResetStatsEntry.TRACE_LENGTH,
		// NoResetStatsEntry.DURATION);
		// g8w3.plotGroup(s8w3,NoResetStatsEntry.W_SIZE,
		// Graph.PlotStyle.POINTS);
		// g8w3.setFileName("similarity_between_duration_and_trace_length_w3");
		// g8w3.export();

		// Graph<Integer, Integer> g_locker = new
		// Graph<>(NoResetStatsEntry.INPUT_SYMBOLS,
		// NoResetStatsEntry.TRACE_LENGTH);
		// StatsSet s_locker = new StatsSet(withoutSpeedUp);
		// s_locker.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		// s_locker.restrict(new
		// RangeRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 5, 5));
		// s_locker.restrict(new
		// RangeRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0,
		// 0));
		// g_locker.plotGroup(s_locker, NoResetStatsEntry.AUTOMATA,
		// PlotStyle.MEDIAN);
		// g_locker.setFileName("lockers");
		// g_locker.export();

		// {StatsSet s_speedUp = new StatsSet(s);
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		// s_speedUp.restrict(new
		// EqualsRestriction<String>(NoResetStatsEntry.AUTOMATA, new
		// RandomMealyDriver().getSystemName()));
		//
		// {StatsSet s_speedUp_w1 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_w1 = new
		// Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER,
		// NoResetStatsEntry.TRACE_LENGTH);
		// s_speedUp_w1.restrict(new
		// EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 1));
		// g_speedUp_w1.plotGroup(s_speedUp_w1, NoResetStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w1.setForceOrdLogScale(false);
		// g_speedUp_w1.setFileName("influence_of_state_number_speedUp_w1");
		// g_speedUp_w1.export();}
		//


	}

//	private String makeMaxTheoricalFunction(StatsSet s, Attribute<?> a) {
//		if (s.size() == 0)
//			return "0";
//		String n = (a == NoResetStatsEntry.STATE_NUMBER_BOUND) ? "x"
//				: s.attributeMax(NoResetStatsEntry.STATE_NUMBER_BOUND).toString();
//		String f = (a == NoResetStatsEntry.INPUT_SYMBOLS) ? "x"
//				: s.attributeMax(NoResetStatsEntry.INPUT_SYMBOLS).toString();
//		String p = (a == NoResetStatsEntry.W_SIZE) ? "x" : s.attributeMax(NoResetStatsEntry.W_SIZE).toString();
//		String w1 = (a == NoResetStatsEntry.W1_LENGTH) ? "x" : s.attributeMax(NoResetStatsEntry.W1_LENGTH).toString();
//		String L = "(" + w1 + "*(2*" + n + "-1)**" + p + ")";
//		// String coarseBound = p + "*(" + f + "+" + p + ")*(2**"+ p + ") * (" +
//		// n + "**(" + p + "+" + 2 +"))";
//		return "(" + n + "*(" + f + "+" + p + ")*" + p + "+1" + ")" + "*" + "(" + L + "+" + n + "**2+" + n + "+1)";
//	}
}
