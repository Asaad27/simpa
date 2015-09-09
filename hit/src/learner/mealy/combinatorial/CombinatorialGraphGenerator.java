package learner.mealy.combinatorial;

import java.io.File;

import drivers.mealy.transparent.RandomMealyDriver;
import main.simpa.Options;
import stats.Graph;
import stats.Graph.PlotStyle;
import stats.GraphGenerator;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.RangeRestriction;

public class CombinatorialGraphGenerator extends GraphGenerator {
	private static StatsSet withCut = null;
	private static StatsSet withoutCut = null;
	public CombinatorialGraphGenerator(){
	}

	@Override
	public void generate(StatsSet s) {
		if (s.get(0) instanceof CutterCombinatorialStatsEntry)
			withCut = s;
		else
			withoutCut = s;

		StatsSet random = new StatsSet(s);
		random.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));

		Graph<Integer, Integer> g1 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.TRACE_LENGTH);
		StatsSet s1 = new StatsSet(random);
		//s1.restrict(new EqualsRestriction<Integer>(CombinatorialEntry.STATE_NUMBER, 12));
		s1.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 5));
		g1.plot(s1, Graph.PlotStyle.POINTS);
		g1.setForceOrdLogScale(false);
		g1.setFileName("influence_of_input_symbols");
		g1.export();

		Graph<Integer, Float> g2 = new Graph<Integer, Float>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.DURATION);
		StatsSet s2 = new StatsSet(random);
		//s1.restrict(new EqualsRestriction<Integer>(CombinatorialEntry.STATE_NUMBER, 12));
		s2.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 5));
		g2.plot(s2, Graph.PlotStyle.POINTS);
		//g2.setFileName("influence_of_input_symbols");
		g2.export();

		Graph<Integer, Integer> g3 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.NODES_NB);
		StatsSet s3 = new StatsSet(random);
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 4));
		g3.plot(s3, Graph.PlotStyle.MEDIAN);
		g3.plot(s3, Graph.PlotStyle.POINTS,"de la même chose");
		g3.plotFunc(makeMaxTheoricalFunction(s3, CombinatorialStatsEntry.INPUT_SYMBOLS), "n^{nf}");
		g3.setFileName("problem_median_influence_of_input_symbols_on_nodes");
		g3.export();

		g3 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.NODES_NB);
		s3 = new StatsSet(random);
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 3));
		g3.plot(s3, Graph.PlotStyle.MEDIAN);
		g3.plotFunc(makeMaxTheoricalFunction(s3, CombinatorialStatsEntry.INPUT_SYMBOLS), "n^{nf}");
		g3.setFileName("influence_of_input_symbols_on_nodes_b");
		g3.export();

		Graph<Integer, Integer> g4 = new Graph<Integer, Integer>(CombinatorialStatsEntry.STATE_NUMBER, CombinatorialStatsEntry.NODES_NB);
		StatsSet s4 = new StatsSet(random);
		s4.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s4.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, 2));
		g4.plot(s4, Graph.PlotStyle.MEDIAN);
		g4.plotFunc(makeMaxTheoricalFunction(s4, CombinatorialStatsEntry.STATE_NUMBER), "n^{nf}");
		g4.setFileName("influence_of_states_number_on_nodes");
		g4.export();

		Graph<Integer, Float> g5 = new Graph<Integer,Float>(CombinatorialStatsEntry.NODES_NB, CombinatorialStatsEntry.DURATION);
		StatsSet s5 = new StatsSet(random);
		//s5.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 5));
		s5.restrict(new RangeRestriction<Float>(CombinatorialStatsEntry.DURATION, new Float(0), new Float(20)));
		//s5.restrict(new RangeRestriction<Integer>(CombinatorialStatsEntry.TRACE_LENGTH, 0, 25));
		//s5.restrict(new InSetRestriction<>(CombinatorialStatsEntry.TRACE_LENGTH, new Integer[]{2, 9, 13, 28, 32}));
		g5.plot(s5, Graph.PlotStyle.POINTS);
		g5.setFileName("relation_between_nodes_number_and_duration");
		g5.export();

		Graph<Integer, Integer> g_locker = new Graph<>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.TRACE_LENGTH);
		StatsSet s_locker = new StatsSet(s);
		s_locker.restrict(new RangeRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 3, 4));
		g_locker.plotGroup(s_locker, CombinatorialStatsEntry.AUTOMATA, PlotStyle.MEDIAN);
		g_locker.setFileName("lockers");
		//g_locker.export();
		makeBothCurves();
	}
	private String makeMaxTheoricalFunction(StatsSet s, Attribute<?> a){
		String n = (a == CombinatorialStatsEntry.STATE_NUMBER) ? "x" : s.attributeMax(CombinatorialStatsEntry.STATE_NUMBER).toString();
		String f = (a == CombinatorialStatsEntry.INPUT_SYMBOLS) ? "x" : s.attributeMax(CombinatorialStatsEntry.INPUT_SYMBOLS).toString();
		return "(" + n + "**("+n+"*"+f+"))";
	}

	private void makeBothCurves(){
		if (withCut == null || withoutCut == null)
			return;

		String previousOutdir = Options.OUTDIR;
		File outFile = new File(previousOutdir+"/../bothCombinatorial");
		outFile.mkdir();
		Options.OUTDIR = outFile.getAbsolutePath();

		StatsSet s3withCut = new StatsSet(withCut);
		s3withCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s3withCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3withCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 3));
		StatsSet s3withoutCut = new StatsSet(withoutCut);
		s3withoutCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s3withoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3withoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 3));
		StatsSet s3both = new StatsSet(s3withCut);
		for (int i=0; i < s3withoutCut.size(); i++)
			s3both.add(s3withoutCut.get(i));

		Graph<Integer, Integer> g3 = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.NODES_NB);
		g3.plot(s3withCut, Graph.PlotStyle.MEDIAN,"with cutting");
		g3.plot(s3withoutCut, Graph.PlotStyle.MEDIAN,"without cutting");
		g3.plotFunc(makeMaxTheoricalFunction(s3both, CombinatorialStatsEntry.INPUT_SYMBOLS), "n^{nf}");
		g3.setFileName("influence_of_input_symbols_on_nodes");
		g3.export();
		Graph<Integer, Float> g3b = new Graph<Integer, Float>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.DURATION);
		//		g3b.plot(s3withCut, Graph.PlotStyle.POINTS,"with cutting");
		//		g3b.plot(s3withoutCut, Graph.PlotStyle.POINTS,"without cutting");
		g3b.plot(s3withCut, Graph.PlotStyle.MEDIAN,"with cutting");
		g3b.plot(s3withoutCut, Graph.PlotStyle.MEDIAN,"without cutting");
		g3b.setFileName("influence_of_input_symbols_on_duration");
		g3b.setForceOrdLogScale(true);
		g3b.export();


		StatsSet s3bwithCut = new StatsSet(withCut);
		s3bwithCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s3bwithCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3bwithCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 4));
		StatsSet s3bwithoutCut = new StatsSet(withoutCut);
		s3bwithoutCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s3bwithoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s3bwithoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.STATE_NUMBER, 4));
		StatsSet s3bboth = new StatsSet(s3bwithCut);
		for (int i=0; i < s3bwithoutCut.size(); i++)
			s3bboth.add(s3bwithoutCut.get(i));

		Graph<Integer, Integer> g3b_ = new Graph<Integer, Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, CombinatorialStatsEntry.NODES_NB);
		g3b_.plot(s3bwithCut, Graph.PlotStyle.MEDIAN,"with cutting");
		g3b_.plot(s3bwithoutCut, Graph.PlotStyle.MEDIAN,"without cutting");
		g3b_.plotFunc(makeMaxTheoricalFunction(s3bboth, CombinatorialStatsEntry.INPUT_SYMBOLS), "n^{nf}");
		g3b_.setFileName("influence_of_input_symbols_on_nodes_b");
		g3b_.export();


		StatsSet s4withCut = new StatsSet(withCut);
		s4withCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s4withCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s4withCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, 2));
		StatsSet s4withoutCut = new StatsSet(withoutCut);
		s4withoutCut.restrict(new EqualsRestriction<String>(CombinatorialStatsEntry.AUTOMATA, new RandomMealyDriver().getSystemName()));
		s4withoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.OUTPUT_SYMBOLS, 4));
		s4withoutCut.restrict(new EqualsRestriction<Integer>(CombinatorialStatsEntry.INPUT_SYMBOLS, 2));
		StatsSet s4both = new StatsSet(s4withCut);
		for (int i=0; i < s4withoutCut.size(); i++)
			s3both.add(s4withoutCut.get(i));

		Graph<Integer, Integer> g4 = new Graph<Integer, Integer>(CombinatorialStatsEntry.STATE_NUMBER, CombinatorialStatsEntry.NODES_NB);
		g4.plot(s4withCut, Graph.PlotStyle.MEDIAN,"with cut");
		g4.plot(s4withoutCut, Graph.PlotStyle.MEDIAN,"without cut");
		g4.plotFunc(makeMaxTheoricalFunction(s4both, CombinatorialStatsEntry.STATE_NUMBER), "n^{nf}");
		g4.setFileName("influence_of_states_number_on_nodes");
		g4.export();

		Graph<Integer, Float> g4b = new Graph<Integer, Float>(CombinatorialStatsEntry.STATE_NUMBER, CombinatorialStatsEntry.DURATION);
		g4b.plot(s4withCut, Graph.PlotStyle.MEDIAN,"with cut");
		g4b.plot(s4withoutCut, Graph.PlotStyle.MEDIAN,"without cut");
		g4b.setForceOrdLogScale(true);
		g4b.setFileName("influence_of_states_number_on_duration");
		g4b.export();



		Options.OUTDIR = previousOutdir;
	}
}
