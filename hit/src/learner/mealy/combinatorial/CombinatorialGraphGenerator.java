package learner.mealy.combinatorial;

import stats.Graph;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.InSetRestriction;
import stats.attribute.restriction.RangeRestriction;

public class CombinatorialGraphGenerator extends GraphGenerator {

	public CombinatorialGraphGenerator(){
	}

	@Override
	public void generate(StatsSet s) {

		Graph<Integer, Integer> g1 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.TRACE_LENGTH);
		StatsSet s1 = new StatsSet(s);
		//s1.restrict(new EqualsRestriction<Integer>(CombinatorialEntry.STATE_NUMBER, 12));
		s1.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 5));
		g1.plot(s1, Graph.PlotStyle.POINTS);
		g1.setForceOrdLogScale(false);
		g1.setFileName("influence_of_input_symbols");
		g1.export();

		Graph<Integer, Float> g2 = new Graph<Integer, Float>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.DURATION);
		StatsSet s2 = new StatsSet(s);
		//s1.restrict(new EqualsRestriction<Integer>(CombinatorialEntry.STATE_NUMBER, 12));
		s2.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 5));
		g2.plot(s2, Graph.PlotStyle.POINTS);
		//g2.setFileName("influence_of_input_symbols");
		g2.export();
	}


}
