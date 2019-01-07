package stats.table.usual;

import stats.StatsSet;
import stats.attribute.Attribute;
import stats.table.TableColumn;
import stats.table.TableOutputFormat;

/**
 * simple column to show transition number
 * 
 * @author Nicolas BREMOND
 */
public class TransitionCol extends TableColumn {

	@Override
	public String getRawTitle() {
		return "number of transitions";
	}

	@Override
	public String getFormatedTitle(TableOutputFormat format) {
		if (format == TableOutputFormat.LATEX)
			return "$|Q|\\times|I|$";
		else
			return super.getFormatedTitle(format);
	}

	@Override
	public StatsSet restrict(StatsSet set) {
		return new StatsSet(set);
	}

	@Override
	public String getRawData(StatsSet stats) {
		int states = stats.attributeMax(Attribute.STATE_NUMBER);
		int inputs = stats.attributeMax(Attribute.INPUT_SYMBOLS);
		return "" + (states * inputs);
	}

}
