package stats.table;

import stats.StatsSet;

public abstract class TableColumn {
	public abstract String getRawTitle();

	public String getFormatedTitle(TableOutputFormat format) {
		return Table.defaultFormat(getRawTitle(), format);
	}

	public abstract StatsSet restrict(StatsSet set);

	public abstract String getRawData(StatsSet stats);

	public String getFormatedData(StatsSet stats, TableOutputFormat format) {
		return Table.defaultFormat(getRawData(stats), format);
	}
}
