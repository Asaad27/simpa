package stats.table.usual;

import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.Restriction;
import stats.table.TableColumn;

/**
 * a column to show trace length.
 * 
 * @author Nicolas BREMOND
 *
 */
public class TraceLengthCol extends TableColumn {

	protected final String title;
	final boolean dispReset;
	public boolean dispOracle = false;
	final Restriction[] restrictions;

	@Override
	public String getRawTitle() {
		String r = title;
		if (dispOracle)
			r = r + "[#oracle]";
		if (dispReset && dispOracle)
			r = r + "\n";
		if (dispReset)
			r = r + " (#resets)";
		return r;
	}

	@Override
	public String getRawData(StatsSet stats) {
		assert stats.size() != 0;
		String out = "" + (int) stats.attributeAVG(Attribute.TRACE_LENGTH);
		boolean showReset = dispReset;
		if (showReset) {
			if (stats.get(0).hasAttribute(Attribute.USE_RESET)) {
				assert (stats.sortByAtribute(Attribute.USE_RESET)
						.size() == 1) : "cannot print at the same time inferences with and without reset";
				if (!stats.get(0).get(Attribute.USE_RESET))
					showReset = false;
			}
		}
		if (dispOracle)
			out = out + " ["
					+ ((int) stats
							.attributeAVG(Attribute.ASKED_COUNTER_EXAMPLE))
					+ "]";
		if (showReset) {
			if (dispOracle)
				out = out + "\n";
			out = out + " ("
					+ ((int) stats.attributeAVG(Attribute.RESET_CALL_NB)) + ")";
		}
		return out;
	}

	@Override
	public StatsSet restrict(StatsSet set) {
		return new StatsSet(set, restrictions);
	}

	public TraceLengthCol(String title, boolean dispReset,
			Restriction[] restrictions) {
		this.title = title;
		this.dispReset = dispReset;
		this.restrictions = restrictions;
	}

}
