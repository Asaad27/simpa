package learner.mealy.noReset;

import stats.Graph;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.InSetRestriction;
import stats.attribute.restriction.RangeRestriction;

public class NoResetGraphGenerator extends GraphGenerator {
	
	public NoResetGraphGenerator(){
	}

	@Override
	public void generate(StatsSet s) {
		
		Graph<Integer, Integer> g1 = new Graph<Integer, Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s1 = new StatsSet(s);
		s1.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		s1.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 12));
		s1.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s1.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W1_LENGTH, 1));
		g1.plotGroup(s1,NoResetStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		g1.setFileName("influence_of_output_symbols");
		g1.export();
		
		Graph<Integer, Integer> g2 = new Graph<Integer,Integer>(NoResetStatsEntry.INPUT_SYMBOLS, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s2 = new StatsSet(s);
		s2 .restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s2.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 12));
		s2.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		g2.plotGroup(s2,NoResetStatsEntry.W_SIZE, Graph.PlotStyle.MEDIAN);
		//g2.setForceOrdLogScale(false);
		g2.setFileName("influence_of_input_symbols");
		g2.plotFunc("0.003*"+makeMaxTheoricalFunction(s2, NoResetStatsEntry.INPUT_SYMBOLS), "shape of complexity bound");
		g2.export();
		
		Graph<Integer, Integer> g3 = new Graph<Integer,Integer>(NoResetStatsEntry.W_SIZE, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s3 = new StatsSet(s);
		s3 .restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s3 .restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s3.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 12));
		s3.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		if (s3.size() > 0){
			g3.plot(s3, Graph.PlotStyle.POINTS);
			g3.setFileName("influence_of_W_size");
			g3.plotFunc("0.006*"+makeMaxTheoricalFunction(s2, NoResetStatsEntry.W_SIZE), "shape of complexity bound");
			g3.export();
		}
		
		Graph<Integer, Integer> g4 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s4 = new StatsSet(s);
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		s4.restrict(new InSetRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, new Integer[]{0,5,10,15}));
		g4.plotGroup(s4,NoResetStatsEntry.STATE_BOUND_OFFSET, Graph.PlotStyle.MEDIAN);
		g4.setForceOrdLogScale(false);
		g4.setFileName("influence_of_state_number");
		g4.export();
		
		Graph<Integer, Integer> g5 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, NoResetStatsEntry.LOCALIZER_CALL_NB);
		StatsSet s5 = new StatsSet(s);
		s5.restrict(new RangeRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0, 30));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		g5.plotGroup(s5,NoResetStatsEntry.STATE_NUMBER, Graph.PlotStyle.MEDIAN);
		g5.export();
		
		Graph<Integer, Integer> g6 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER_BOUND, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s6 = new StatsSet(s);
		s6.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s6.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s6.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		//s6.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W1_LENGTH, 2));
		g6.plotGroup(s6,NoResetStatsEntry.STATE_NUMBER, Graph.PlotStyle.AVERAGE_WITH_EXTREMA);
		g6.export();
		
		Graph<Integer, Integer> g7 = new Graph<Integer,Integer>(NoResetStatsEntry.INPUT_SYMBOLS, NoResetStatsEntry.MEMORY);
		StatsSet s7 = new StatsSet(s);
		s7.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		//s7.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s7.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		s7.restrict(new InSetRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, new Integer[]{0,5,10,15}));
		g7.plotGroup(s7,NoResetStatsEntry.STATE_BOUND_OFFSET, Graph.PlotStyle.MEDIAN);
		//g7.setForceOrdLogScale(true);
		g7.setFileName("memory");
		g7.export();
		
		Graph<Integer, Float> g7bis = new Graph<Integer,Float>(NoResetStatsEntry.INPUT_SYMBOLS, NoResetStatsEntry.DURATION);
		g7bis.plotGroup(s7,NoResetStatsEntry.STATE_BOUND_OFFSET, Graph.PlotStyle.POINTS);
		//g7.setForceOrdLogScale(true);
		g7bis.setFileName("duration");
		g7bis.export();
		
		Graph<Integer, Float> g8 = new Graph<Integer,Float>(NoResetStatsEntry.TRACE_LENGTH, NoResetStatsEntry.DURATION);
		StatsSet s8 = new StatsSet(s);
		//s8.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 3));
		//s8.restrict(new RangeRestriction<Float>(NoResetStatsEntry.DURATION, new Float(0), new Float(300)));
		g8.plotGroup(s8,NoResetStatsEntry.W_SIZE, Graph.PlotStyle.SMOOTH);
		g8.setFileName("similarity_between_duration_and_trace_length");
		g8.export();
		
	}

	private String makeMaxTheoricalFunction(StatsSet s, Attribute<?> a){
		String n = (a == NoResetStatsEntry.STATE_NUMBER_BOUND) ? "x" : s.attributeMax(NoResetStatsEntry.STATE_NUMBER_BOUND).toString();
		String f = (a == NoResetStatsEntry.INPUT_SYMBOLS) ? "x" : s.attributeMax(NoResetStatsEntry.INPUT_SYMBOLS).toString();
		String p = (a == NoResetStatsEntry.W_SIZE) ? "x" : s.attributeMax(NoResetStatsEntry.W_SIZE).toString();
		String w1 = (a == NoResetStatsEntry.W1_LENGTH) ? "x" : s.attributeMax(NoResetStatsEntry.W1_LENGTH).toString();
		String L = "("+w1+"*(2*"+n+"-1)**"+p+")";
		String coarseBound = p + "*(" + f + "+" + p + ")*(2**"+ p + ") * (" + n + "**(" + p + "+" + 2 +"))";
		return "("+n+"*("+f+"+"+p+")*"+p+"+1"+")"+
				"*"+
				"("+L+"+"+n+"**2+"+n+"+1)";
	}
}
