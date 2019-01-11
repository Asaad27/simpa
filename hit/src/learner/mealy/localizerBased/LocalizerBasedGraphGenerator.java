package learner.mealy.localizerBased;

import drivers.mealy.transparent.RandomMealyDriver;
import stats.Graph;
import stats.GraphGenerator;
import stats.LineStyle;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.RangeRestriction;

public class LocalizerBasedGraphGenerator extends GraphGenerator {

	public LocalizerBasedGraphGenerator() {
	}

	@Override
	public void generate(StatsSet s) {

		StatsSet withoutSpeedUp = new StatsSet(s);
		withoutSpeedUp.restrict(new EqualsRestriction<Boolean>(LocalizerBasedStatsEntry.WITH_SPEEDUP, false));

		StatsSet random = new StatsSet(withoutSpeedUp);
		random.restrict(
				new EqualsRestriction<String>(LocalizerBasedStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));

		Graph<Integer, Integer> g1 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS,
				LocalizerBasedStatsEntry.TRACE_LENGTH);
		StatsSet s1 = new StatsSet(random);

		// s1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));
		// s1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 10));
		// s1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W1_LENGTH, 1));
		// g1.plotGroup(s1,LocalizerBasedStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		// g1.setFileName("influence_of_output_symbols");
		// g1.export();

		g1 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, LocalizerBasedStatsEntry.TRACE_LENGTH);
		s1 = new StatsSet(random);

		s1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));
		s1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 8));
		s1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		s1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W1_LENGTH, 1));
		g1.plotGroup(s1, LocalizerBasedStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		g1.setFileName("influence_of_output_symbols_8states");
		g1.setDataDescriptionFields(
				new Attribute<?>[] { LocalizerBasedStatsEntry.W1_LENGTH,
						LocalizerBasedStatsEntry.INPUT_SYMBOLS,
						LocalizerBasedStatsEntry.STATE_NUMBER,
						LocalizerBasedStatsEntry.AUTOMATA,
						LocalizerBasedStatsEntry.WITH_SPEEDUP });
		g1.export();

		StatsSet s2 = new StatsSet(random);
		s2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		s2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 15));
		s2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));

		StatsSet s2w1 = new StatsSet(s2);
		s2w1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		Graph<Integer, Integer> g2w1 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS,
				LocalizerBasedStatsEntry.TRACE_LENGTH);
		g2w1.plotGroup(s2w1, LocalizerBasedStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		g2w1.setForceOrdLogScale(false);
		g2w1.setFileName("influence_of_input_symbols_w1");
		g2w1.forceOrdRange(0, null);
		g2w1.forceAbsRange(0, null);
		g2w1.plotFunc("0.04*" + makeMaxTheoricalFunction(s2w1, LocalizerBasedStatsEntry.INPUT_SYMBOLS),
				"shape of complexity bound (1/25)", LineStyle.BOUND);
		if (s2w1.size() != 0)
			g2w1.forceOrdRange(null, s2w1.attributeMax(LocalizerBasedStatsEntry.TRACE_LENGTH));
		g2w1.export();

		StatsSet s2w2 = new StatsSet(s2);
		s2w2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		Graph<Integer, Integer> g2w2 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS,
				LocalizerBasedStatsEntry.TRACE_LENGTH);
		g2w2.plotGroup(s2w2, LocalizerBasedStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		g2w2.setForceOrdLogScale(false);
		g2w2.setFileName("influence_of_input_symbols_w2");
		g2w2.forceOrdRange(0, null);
		g2w2.forceAbsRange(0, null);
		g2w2.plotFunc("0.04*" + makeMaxTheoricalFunction(s2w2, LocalizerBasedStatsEntry.INPUT_SYMBOLS),
				"shape of complexity bound (1/25)", LineStyle.BOUND);
		g2w2.forceOrdRange(null, s2w2.attributeMax(LocalizerBasedStatsEntry.TRACE_LENGTH));
		g2w2.export();

		// StatsSet s2w3 = new StatsSet(s2);
		// s2w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// Graph<Integer, Integer> g2w3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// g2w3.plotGroup(s2w3,LocalizerBasedStatsEntry.W_SIZE,
		// Graph.PlotStyle.MEDIAN);
		// g2w3.setForceOrdLogScale(false);
		// g2w3.forceOrdRange(0, null);
		// g2w3.forceAbsRange(0, null);
		// g2w3.setFileName("influence_of_input_symbols_w3");
		// g2w3.plotFunc("0.04*"+makeMaxTheoricalFunction(s2w3,
		// LocalizerBasedStatsEntry.INPUT_SYMBOLS), "shape of complexity bound
		// (1/25)");
		// if (s2w3.size() != 0)
		// g2w3.forceOrdRange(null,
		// s2w3.attributeMax(LocalizerBasedStatsEntry.TRACE_LENGTH));
		// g2w3.export();

		// Graph<Integer, Integer> g3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.W_SIZE,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// StatsSet s3 = new StatsSet(random);
		// s3 .restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// s3 .restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 12));
		// s3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));
		// if (s3.size() > 0){
		// g3.plot(s3, Graph.PlotStyle.POINTS);
		// g3.setFileName("influence_of_W_size");
		// g3.plotFunc("0.006*"+makeMaxTheoricalFunction(s2,
		// LocalizerBasedStatsEntry.W_SIZE), "shape of complexity bound");
		// g3.export();
		// }

		StatsSet s4 = new StatsSet(random);
		s4.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));

		Graph<Integer, Integer> g4w1 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
				LocalizerBasedStatsEntry.TRACE_LENGTH);
		StatsSet s4w1 = new StatsSet(s4);
		s4w1.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		g4w1.plot(s4w1, Graph.PlotStyle.MEDIAN);
		g4w1.plotFunc("0.1*" + makeMaxTheoricalFunction(s4w1, LocalizerBasedStatsEntry.STATE_NUMBER_BOUND),
				"shape of complexity bound (1/10)", LineStyle.BOUND);
		g4w1.plotFunc("7*x**1.5", "$7x\\\\^\\\\{1.5\\\\}$", LineStyle.APPROXIMATION);
		g4w1.setForceOrdLogScale(false);
		if (s4w1.size() != 0)
			g4w1.forceOrdRange(null, s4w1.attributeMax(LocalizerBasedStatsEntry.TRACE_LENGTH));
		g4w1.setFileName("influence_of_state_number_w1");
		g4w1.export();

		Graph<Integer, Integer> g4w2 = new Graph<Integer, Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
				LocalizerBasedStatsEntry.TRACE_LENGTH);
		StatsSet s4w2 = new StatsSet(s4);
		s4w2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		g4w2.plot(s4w2, Graph.PlotStyle.MEDIAN);
		g4w2.plotFunc("0.2*" + makeMaxTheoricalFunction(s4w1, LocalizerBasedStatsEntry.STATE_NUMBER_BOUND),
				"shape of complexity bound (1/5)", LineStyle.BOUND);
		g4w2.plotFunc("20*x**1.9", "$20x\\\\^\\\\{1.9\\\\}$", LineStyle.APPROXIMATION);
		g4w2.setForceOrdLogScale(false);
		if (s4w2.size() != 0)
			g4w2.forceOrdRange(null, s4w2.attributeMax(LocalizerBasedStatsEntry.TRACE_LENGTH));
		g4w2.setFileName("influence_of_state_number_w2");
		g4w2.export();

		// Graph<Integer, Integer> g4w3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// StatsSet s4w3 = new StatsSet(s4);
		// s4w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g4w3.plot(s4w3, Graph.PlotStyle.MEDIAN);
		// g4w3.setForceOrdLogScale(false);
		// g4w3.setFileName("influence_of_state_number_w3");
		// g4w3.export();

		// //RandomCounter
		// s4 = new StatsSet(withoutSpeedUp);
		// s4.restrict(new EqualsRestriction<String>(LocalizerBasedStatsEntry.AUTOMATA,
		// new RandomAndCounterMealyDriver().getSystemName()));
		// s4.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// s4.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s4.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));
		//
		// g4w1 = new Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s4w1 = new StatsSet(s4);
		// s4w1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		// g4w1.plot(s4w1, Graph.PlotStyle.MEDIAN);
		// g4w1.setForceOrdLogScale(false);
		// g4w1.setFileName("influence_of_state_number_w1_RC");
		// g4w1.export();
		//
		// g4w2 = new Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s4w2 = new StatsSet(s4);
		// s4w2.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// g4w2.plot(s4w2, Graph.PlotStyle.MEDIAN);
		// g4w2.setForceOrdLogScale(false);
		// g4w2.setFileName("influence_of_state_number_w2_RC");
		// g4w2.export();
		//
		// g4w3 = new Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s4w3 = new StatsSet(s4);
		// s4w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g4w3.plot(s4w3, Graph.PlotStyle.MEDIAN);
		// g4w3.setForceOrdLogScale(false);
		// g4w3.setFileName("influence_of_state_number_w3_RC");
		// g4w3.export();

		// Graph<Integer, Integer> g5 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.LOCALIZER_CALL_NB);
		// StatsSet s5 = new StatsSet(random);
		// //s5.restrict(new
		// RangeRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0,
		// 30));
		// s5.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// s5.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s5.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE,
		// 2));
		// //s5.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W1_LENGTH, 2));
		// s5.restrict(new
		// InSetRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER_BOUND, new
		// Integer[]{6,9,12,15,18}));
		// g5.plotGroup(s5,LocalizerBasedStatsEntry.STATE_NUMBER_BOUND,
		// Graph.PlotStyle.MEDIAN);
		// g5.setFileName("influence_of_state_number_on_localizer_call");
		// g5.export();
		//
		// Graph<Integer, Integer> g5b = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER_BOUND,
		// LocalizerBasedStatsEntry.LOCALIZER_SEQUENCE_LENGTH);
		// StatsSet s5b = new StatsSet(random);
		// s5b.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE,
		// 2));
		// g5b.plotGroup(s5b,LocalizerBasedStatsEntry.W1_LENGTH,
		// Graph.PlotStyle.MEDIAN);
		// s5b = new StatsSet(random);
		// s5b.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE,
		// 3));
		// g5b.plotGroup(s5b,LocalizerBasedStatsEntry.W1_LENGTH,
		// Graph.PlotStyle.MEDIAN);
		// g5b.setFileName("influence_of_state_number_bound_on_localizer_sequence_length");
		// g5b.export();
		//
		// Graph<Integer, Integer> g6 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER_BOUND,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// StatsSet s6 = new StatsSet(random);
		// s6.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// s6.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s6.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE,
		// 2));
		// s6.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 6));
		// s6.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W1_LENGTH, 1));
		// g6.plot(s6, Graph.PlotStyle.POINTS);
		// g6.setForceOrdLogScale(false);
		// g6.setFileName("influence_of_state_number_bound");
		// g6.export();
		//
		// Graph<Integer, Integer> g7 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.TRACE_LENGTH,
		// LocalizerBasedStatsEntry.MEMORY);
		// StatsSet s7 = new StatsSet(random);
		// //s7.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// //s7.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// // s7.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// // s7.restrict(new
		// InSetRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, new
		// Integer[]{0,5,10,15}));
		// g7.plot(s7, Graph.PlotStyle.POINTS);
		// //g7.setForceOrdLogScale(true);
		// g7.setFileName("memory");
		// g7.export();
		//
		// Graph<Integer, Float> g7bis = new
		// Graph<Integer,Float>(LocalizerBasedStatsEntry.INPUT_SYMBOLS,
		// LocalizerBasedStatsEntry.DURATION);
		// g7bis.plotGroup(s7,LocalizerBasedStatsEntry.STATE_BOUND_OFFSET,
		// Graph.PlotStyle.POINTS);
		// //g7.setForceOrdLogScale(true);
		// g7bis.setFileName("duration");
		// g7bis.export();

		StatsSet s8 = new StatsSet(random);
		// s8.restrict(new RangeRestriction<Float>(LocalizerBasedStatsEntry.DURATION,
		// new Float(0), new Float(300)));
		s8.restrict(new RangeRestriction<Integer>(LocalizerBasedStatsEntry.TRACE_LENGTH, 0, 500000));
		StatsSet s8w2 = new StatsSet(s8);
		s8w2.restrict(new EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		Graph<Integer, Float> g8w2 = new Graph<Integer, Float>(LocalizerBasedStatsEntry.TRACE_LENGTH,
				LocalizerBasedStatsEntry.DURATION);
		g8w2.plotGroup(s8w2, LocalizerBasedStatsEntry.W_SIZE, Graph.PlotStyle.POINTS);
		g8w2.setFileName("similarity_between_duration_and_trace_length_w2");
		g8w2.export();
		// StatsSet s8w3 = new StatsSet(s8);
		// s8w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// Graph<Integer, Float> g8w3 = new
		// Graph<Integer,Float>(LocalizerBasedStatsEntry.TRACE_LENGTH,
		// LocalizerBasedStatsEntry.DURATION);
		// g8w3.plotGroup(s8w3,LocalizerBasedStatsEntry.W_SIZE,
		// Graph.PlotStyle.POINTS);
		// g8w3.setFileName("similarity_between_duration_and_trace_length_w3");
		// g8w3.export();

		// Graph<Integer, Integer> g_locker = new
		// Graph<>(LocalizerBasedStatsEntry.INPUT_SYMBOLS,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// StatsSet s_locker = new StatsSet(withoutSpeedUp);
		// s_locker.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// s_locker.restrict(new
		// RangeRestriction<Integer>(LocalizerBasedStatsEntry.STATE_NUMBER, 5, 5));
		// s_locker.restrict(new
		// RangeRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0,
		// 0));
		// g_locker.plotGroup(s_locker, LocalizerBasedStatsEntry.AUTOMATA,
		// PlotStyle.MEDIAN);
		// g_locker.setFileName("lockers");
		// g_locker.export();

		// {StatsSet s_speedUp = new StatsSet(s);
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.INPUT_SYMBOLS, 5));
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.OUTPUT_SYMBOLS, 5));
		// s_speedUp.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.STATE_BOUND_OFFSET, 0));
		// s_speedUp.restrict(new
		// EqualsRestriction<String>(LocalizerBasedStatsEntry.AUTOMATA, new
		// RandomMealyDriver().getSystemName()));
		//
		// {StatsSet s_speedUp_w1 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_w1 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s_speedUp_w1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		// g_speedUp_w1.plotGroup(s_speedUp_w1, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w1.setForceOrdLogScale(false);
		// g_speedUp_w1.setFileName("influence_of_state_number_speedUp_w1");
		// g_speedUp_w1.export();}
		//
		// {StatsSet s_speedUp_w2 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_w2 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s_speedUp_w2.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// g_speedUp_w2.plotGroup(s_speedUp_w2, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w2.setForceOrdLogScale(false);
		// g_speedUp_w2.setFileName("influence_of_state_number_speedUp_w2");
		// g_speedUp_w2.export();}
		// {StatsSet s_speedUp_w3 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_w3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.TRACE_LENGTH);
		// s_speedUp_w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g_speedUp_w3.plotGroup(s_speedUp_w3, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w3.setForceOrdLogScale(false);
		// g_speedUp_w3.setFileName("influence_of_state_number_speedUp_w3");
		// g_speedUp_w3.export();}
		//
		// {StatsSet s_speedUp1 = new StatsSet(s_speedUp);
		// s_speedUp1.restrict(new
		// RangeRestriction<Integer>(LocalizerBasedStatsEntry.MIN_TRACE_LENGTH,0,1000000000));
		// {StatsSet s_speedUp_w1 = new StatsSet(s_speedUp1);
		// Graph<Integer, Integer> g_speedUp_w1 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MIN_TRACE_LENGTH);
		// s_speedUp_w1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		// g_speedUp_w1.plotGroup(s_speedUp_w1, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w1.setForceOrdLogScale(false);
		// g_speedUp_w1.setFileName("influence_of_state_number_speedUp_min_w1");
		// g_speedUp_w1.export();}
		// {StatsSet s_speedUp_w2 = new StatsSet(s_speedUp1);
		// Graph<Integer, Integer> g_speedUp_w2 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MIN_TRACE_LENGTH);
		// s_speedUp_w2.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// g_speedUp_w2.plotGroup(s_speedUp_w2, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w2.setForceOrdLogScale(false);
		// g_speedUp_w2.setFileName("influence_of_state_number_speedUp_min_w2");
		// g_speedUp_w2.export();}
		// {StatsSet s_speedUp_w3 = new StatsSet(s_speedUp1);
		// Graph<Integer, Integer> g_speedUp_w3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MIN_TRACE_LENGTH);
		// s_speedUp_w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g_speedUp_w3.plotGroup(s_speedUp_w3, LocalizerBasedStatsEntry.WITH_SPEEDUP,
		// Graph.PlotStyle.MEDIAN);
		// g_speedUp_w3.setForceOrdLogScale(false);
		// g_speedUp_w3.setFileName("influence_of_state_number_speedUp_min_w3");
		// g_speedUp_w3.export();}
		// }
		//
		//
		// {StatsSet s_speedUp_time_w1 = new StatsSet(s_speedUp);
		// Graph<Integer, Float> g_speedUp_time_w1 = new
		// Graph<Integer,Float>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.DURATION);
		// s_speedUp_time_w1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		// g_speedUp_time_w1.plotGroup(s_speedUp_time_w1,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_time_w1.setForceOrdLogScale(false);
		// g_speedUp_time_w1.setFileName("influence_of_state_number_time_speedUp_w1");
		// g_speedUp_time_w1.export();}
		// {StatsSet s_speedUp_time_w2 = new StatsSet(s_speedUp);
		// Graph<Integer, Float> g_speedUp_time_w2 = new
		// Graph<Integer,Float>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.DURATION);
		// s_speedUp_time_w2.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// g_speedUp_time_w2.plotGroup(s_speedUp_time_w2,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_time_w2.setForceOrdLogScale(false);
		// g_speedUp_time_w2.setFileName("influence_of_state_number_time_speedUp_w2");
		// g_speedUp_time_w2.export();}
		// {StatsSet s_speedUp_time_w3 = new StatsSet(s_speedUp);
		// Graph<Integer, Float> g_speedUp_time_w3 = new
		// Graph<Integer,Float>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.DURATION);
		// s_speedUp_time_w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g_speedUp_time_w3.plotGroup(s_speedUp_time_w3,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_time_w3.setForceOrdLogScale(false);
		// g_speedUp_time_w3.setFileName("influence_of_state_number_time_speedUp_w3");
		// g_speedUp_time_w3.export();}
		//
		// {StatsSet s_speedUp_mem_w1 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_mem_w1 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MEMORY);
		// s_speedUp_mem_w1.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 1));
		// g_speedUp_mem_w1.plotGroup(s_speedUp_mem_w1,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_mem_w1.setForceOrdLogScale(false);
		// g_speedUp_mem_w1.setFileName("influence_of_state_number_mem_speedUp_w1");
		// g_speedUp_mem_w1.export();}
		// {StatsSet s_speedUp_mem_w2 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_mem_w2 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MEMORY);
		// s_speedUp_mem_w2.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 2));
		// g_speedUp_mem_w2.plotGroup(s_speedUp_mem_w2,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_mem_w2.setForceOrdLogScale(false);
		// g_speedUp_mem_w2.setFileName("influence_of_state_number_mem_speedUp_w2");
		// g_speedUp_mem_w2.export();}
		// {StatsSet s_speedUp_mem_w3 = new StatsSet(s_speedUp);
		// Graph<Integer, Integer> g_speedUp_mem_w3 = new
		// Graph<Integer,Integer>(LocalizerBasedStatsEntry.STATE_NUMBER,
		// LocalizerBasedStatsEntry.MEMORY);
		// s_speedUp_mem_w3.restrict(new
		// EqualsRestriction<Integer>(LocalizerBasedStatsEntry.W_SIZE, 3));
		// g_speedUp_mem_w3.plotGroup(s_speedUp_mem_w3,
		// LocalizerBasedStatsEntry.WITH_SPEEDUP, Graph.PlotStyle.MEDIAN);
		// g_speedUp_mem_w3.setForceOrdLogScale(false);
		// g_speedUp_mem_w3.setFileName("influence_of_state_number_mem_speedUp_w3");
		// g_speedUp_mem_w3.export();}
		// }

	}

	private String makeMaxTheoricalFunction(StatsSet s, Attribute<?> a) {
		if (s.size() == 0)
			return "0";
		String n = (a == LocalizerBasedStatsEntry.STATE_NUMBER_BOUND) ? "x"
				: s.attributeMax(LocalizerBasedStatsEntry.STATE_NUMBER_BOUND).toString();
		String f = (a == LocalizerBasedStatsEntry.INPUT_SYMBOLS) ? "x"
				: s.attributeMax(LocalizerBasedStatsEntry.INPUT_SYMBOLS).toString();
		String p = (a == LocalizerBasedStatsEntry.W_SIZE) ? "x" : s.attributeMax(LocalizerBasedStatsEntry.W_SIZE).toString();
		String w1 = (a == LocalizerBasedStatsEntry.W1_LENGTH) ? "x" : s.attributeMax(LocalizerBasedStatsEntry.W1_LENGTH).toString();
		String L = "(" + w1 + "*(2*" + n + "-1)**" + p + ")";
		// String coarseBound = p + "*(" + f + "+" + p + ")*(2**"+ p + ") * (" +
		// n + "**(" + p + "+" + 2 +"))";
		return "(" + n + "*(" + f + "+" + p + ")*" + p + "+1" + ")" + "*" + "(" + L + "+" + n + "**2+" + n + "+1)";
	}
}
