package learner.mealy.combinatorial;

import drivers.mealy.MealyDriver;

public class CutterCombinatorialStatsEntry extends CombinatorialStatsEntry {

	public CutterCombinatorialStatsEntry(String line) {
		super(line);
	}

	protected CutterCombinatorialStatsEntry(MealyDriver d,
			CombinatorialOptions options) {
		super(d, options);
		traceLength = 0;
	}

	public void addTraceLength(int l){
		traceLength += l;
	}
}
