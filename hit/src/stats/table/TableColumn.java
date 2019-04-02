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
