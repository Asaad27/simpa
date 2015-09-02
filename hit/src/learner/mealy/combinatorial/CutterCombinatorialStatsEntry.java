package learner.mealy.combinatorial;

import drivers.mealy.MealyDriver;

public class CutterCombinatorialStatsEntry extends CombinatorialStatsEntry {

	public CutterCombinatorialStatsEntry(String line) {
		super(line);
		traceLength = 0;
	}

	protected CutterCombinatorialStatsEntry(MealyDriver d) {
		super(d);
	}

	public void addTraceLength(int l){
		traceLength += l;
	}
}
