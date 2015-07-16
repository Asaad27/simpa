package learner.mealy.noReset;

import stats.Graph;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.restriction.EqualsRestriction;

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
		g1.plotGroup(s1,NoResetStatsEntry.W_SIZE, Graph.PlotStyle.POINTS);
		g1.export();
	}

}
