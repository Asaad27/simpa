package learner.mealy.combinatorial;

import learner.mealy.noReset.NoResetStatsEntry;
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
		
		Graph<Integer, Integer> g3 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.NODES_NB);
		StatsSet s3 = new StatsSet(s);
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 3));
		g3.plot(s3, Graph.PlotStyle.AVERAGE_WITH_EXTREMA);
		g3.plotFunc(makeMaxTheoricalFunction(s3, CombinatorialStatsEntry.INPUT_SYMBOLS), "n^{nf}");
		g3.export();
		
		Graph<Integer, Integer> g4 = new Graph<Integer, Integer>(CombinatorialStatsEntry.STATE_NUMBER, CombinatorialStatsEntry.NODES_NB);
		StatsSet s4 = new StatsSet(s);
		s4.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s4.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, 2));
		g4.plot(s4, Graph.PlotStyle.AVERAGE_WITH_EXTREMA);
		g4.plotFunc(makeMaxTheoricalFunction(s4, CombinatorialStatsEntry.STATE_NUMBER), "n^{nf}");
		g4.export();
	}
	private String makeMaxTheoricalFunction(StatsSet s, Attribute<?> a){
		String n = (a == CombinatorialStatsEntry.STATE_NUMBER) ? "x" : s.attributeMax(CombinatorialStatsEntry.STATE_NUMBER).toString();
		String f = (a == CombinatorialStatsEntry.INPUT_SYMBOLS) ? "x" : s.attributeMax(CombinatorialStatsEntry.INPUT_SYMBOLS).toString();
		return "(" + n + "**("+n+"*"+f+"))";
	}

}