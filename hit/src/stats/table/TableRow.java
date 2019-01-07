package stats.table;

import stats.StatsSet;

public abstract class TableRow {

	abstract public String getRawHeader();

	public String getFormattedHeader(TableOutputFormat format) {
		return Table.defaultFormat(getRawHeader(), format);
	}

	@Deprecated
	String getLaTeXHeader() {
		return Table.stringToLatex(getRawHeader());
	}

	@Deprecated
	String getHTMLHeader() {
		return getRawHeader();
	}

	abstract public StatsSet getData(StatsSet s);
}
