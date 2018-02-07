package learner.mealy.table;

import drivers.mealy.MealyDriver;
import learner.mealy.tree.ZStatsEntry;

public class LmStatsEntry extends ZStatsEntry {

	public LmStatsEntry(MealyDriver d, LmOptions options) {
		super(d, options.oracle);
	}

	public LmStatsEntry(String s) {
		super(s);
	}

}
