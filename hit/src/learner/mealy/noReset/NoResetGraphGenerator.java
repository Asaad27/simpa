package learner.mealy.noReset;

import stats.Graph;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.restriction.EqualsRestriction;
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
		g2.setForceOrdLogScale(false);
		g2.setFileName("influence_of_input_symbols");
		g2.export();
		
		Graph<Integer, Integer> g3 = new Graph<Integer,Integer>(NoResetStatsEntry.W_SIZE, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s3 = new StatsSet(s);
		s3 .restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s3 .restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s3.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_NUMBER, 12));
		s3.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0));
		g3.plot(s3, Graph.PlotStyle.POINTS);
		g3.setForceOrdLogScale(false);
		g3.setFileName("influence_of_W_size");
		g3.export();
		
		Graph<Integer, Integer> g4 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_NUMBER_BOUND, NoResetStatsEntry.TRACE_LENGTH);
		StatsSet s4 = new StatsSet(s);
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		s4.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		g4.plotGroup(s4,NoResetStatsEntry.STATE_NUMBER, Graph.PlotStyle.MEDIAN);
		g4.export();
		
		Graph<Integer, Integer> g5 = new Graph<Integer,Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, NoResetStatsEntry.LOCALIZER_CALL_NB);
		StatsSet s5 = new StatsSet(s);
		s5.restrict(new RangeRestriction<Integer>(NoResetStatsEntry.STATE_BOUND_OFFSET, 0, 30));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.OUTPUT_SYMBOLS, 5));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.W_SIZE, 2));
		s5.restrict(new EqualsRestriction<Integer>(NoResetStatsEntry.INPUT_SYMBOLS, 5));
		g5.plotGroup(s5,NoResetStatsEntry.STATE_NUMBER, Graph.PlotStyle.MEDIAN);
		g5.export();
	}

}
