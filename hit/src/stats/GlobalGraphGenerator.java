package stats;

import java.io.File;

import drivers.mealy.transparent.RandomMealyDriver;
import learner.mealy.combinatorial.CutterCombinatorialStatsEntry;
import learner.mealy.localizerBased.LocalizerBasedStatsEntry;
import learner.mealy.rivestSchapire.RivestSchapireStatsEntry;
import main.simpa.Options;
import stats.Graph.PlotStyle;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.attribute.restriction.Restriction;

/**
 * This class aim to make graphs with data of multiple kind of inferences
 */
public class GlobalGraphGenerator extends GraphGenerator {
	private StatsSet noReset = null;// ICTSS2015
	private StatsSet combinatorialPruning = null;
	private StatsSet rivestSchapire = null;

	@Override
	public void generate(StatsSet s) {
		String outdir = Options.OUTDIR;
		if (s.size() == 0)
			return;
		StatsEntry s1 = s.get(0);
		if (s1 instanceof LocalizerBasedStatsEntry)
			noReset = s;
		if (s1 instanceof RivestSchapireStatsEntry)
			rivestSchapire = s;
		if (s1 instanceof CutterCombinatorialStatsEntry)
			combinatorialPruning = s;

		File outFile = new File(outdir + "/../RS_Comb_ICTSS");
		outFile.mkdir();
		Options.OUTDIR = outFile.getAbsolutePath();
		if (rivestSchapire != null && combinatorialPruning != null && noReset != null)
			makeRS_Comb_NoReset();
		Options.OUTDIR = outdir;
	}

	private void makeRS_Comb_NoReset() {
		assert rivestSchapire != null && combinatorialPruning != null && noReset != null;
		Graph<Integer, Integer> g = new Graph<>(Attribute.STATE_NUMBER, Attribute.TRACE_LENGTH);
		StatsSet NRw1 = new StatsSet(noReset);
		NRw1.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 1));
		StatsSet NRw2 = new StatsSet(noReset);
		NRw2.restrict(new EqualsRestriction<>(Attribute.W_SIZE, 2));
		StatsSet RS = new StatsSet(rivestSchapire);
		StatsSet comb = new StatsSet(combinatorialPruning);

		Restriction automaRestriction = new EqualsRestriction<String>(Attribute.AUTOMATA,
				new RandomMealyDriver().getSystemName());
		Restriction inputsRestriction = new EqualsRestriction<>(Attribute.INPUT_SYMBOLS, 5);
		Restriction outputsRestriction = new EqualsRestriction<>(Attribute.OUTPUT_SYMBOLS, 5);

		Restriction[] restrictions = new Restriction[] { automaRestriction, inputsRestriction, outputsRestriction };

		NRw1.restrict(restrictions);
		NRw2.restrict(restrictions);
		RS.restrict(restrictions);
		comb.restrict(restrictions);

		g.setDataDescriptionFields(
				new Attribute[] { Attribute.INPUT_SYMBOLS, Attribute.OUTPUT_SYMBOLS, Attribute.AUTOMATA });
		g.plot(NRw1, PlotStyle.MEDIAN, "ICTSS2015, p=1");
		g.plot(NRw2, PlotStyle.MEDIAN, "ICTSS2015, p=2");
		g.plot(RS, PlotStyle.MEDIAN, "\\\\\\\\algRS*");
		g.plot(comb, PlotStyle.MEDIAN, "combinatorial with pruning");

		g.forceOrdRange(null, 100000);
		g.setFileName("influence_of_states_number_length");
		g.export();
	}

}
