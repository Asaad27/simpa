package stats.attribute;

import stats.StatsEntry;
import stats.Units;

public abstract class ComputedAttribute<T extends Comparable<T>>
		extends Attribute<T> {

	public static ComputedAttribute<Integer> TRANSITION_COUNT = new ComputedAttribute<Integer>(
			"number of transitions", false, Units.TRANSITIONS, true, true) {

		@Override
		public Integer getValue(StatsEntry e) {
			return e.get(STATE_NUMBER) * e.get(INPUT_SYMBOLS);
		}
	};

	public ComputedAttribute(String name, boolean displayUnits, Units units,
			boolean useLogScale, boolean isParameter) {
		super(name, displayUnits, units, useLogScale, isParameter, true);
	}

	public abstract T getValue(StatsEntry e);

}
