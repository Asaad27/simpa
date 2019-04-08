/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
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
