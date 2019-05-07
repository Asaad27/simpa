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
package stats.table.usual;

import java.util.Locale;

import stats.StatsSet;
import stats.attribute.Attribute;
import stats.table.TableColumn;

/**
 * A column to compare the cost of a reset making a set better than a reference
 * set.
 * 
 * @author Nicolas BREMOND
 *
 */
public class ResetCostComparisonCol extends TableColumn {
	final TableColumn ref;
	final TableColumn test;

	public ResetCostComparisonCol(TableColumn ref, TableColumn test) {
		super();
		this.ref = ref;
		this.test = test;
	}

	@Override
	public String getRawTitle() {
		return "reset cost \n #input";
		// return "cost of reset for which " + test.getTitle()
		// + " is cheaper than " + ref.getTitle();
	}

	@Override
	public StatsSet restrict(StatsSet set) {
		StatsSet s = new StatsSet();
		s.add(ref.restrict(set));
		s.add(test.restrict(set));
		return s;
	}

	static String floatToString(float f) {
		String string = String.format(Locale.ENGLISH, "%.2g", f);
		if (f >= 100 && f < 1000)
			string = String.format(Locale.ENGLISH, "%.2g", f / 10) + "0";
		return string;
	}

	@Override
	public String getRawData(StatsSet stats) {
		StatsSet refStats = ref.restrict(stats);
		StatsSet testStats = test.restrict(stats);
		if (refStats.size() == 0 || testStats.size() == 0)
			return "missing data";
		float refReset = refStats.attributeAVG(Attribute.RESET_CALL_NB);
		float testLength = testStats.attributeAVG(Attribute.TRACE_LENGTH);
		float refLength = refStats.attributeAVG(Attribute.TRACE_LENGTH);
		float testReset = testStats.attributeAVG(Attribute.RESET_CALL_NB);

		float resetRatio = (testLength - refLength) / (refReset - testReset);
		if (testLength > refLength) {
			if (resetRatio < 0) {
				return "-";
			} else {
				String ratioString = floatToString(resetRatio);
				if (resetRatio > 1) {
					return " ≥ " + ratioString;
				} else {
					return " ≥ " + ratioString;
				}
			}
		} else {
			if (testReset > refReset) {
				String ratioString = floatToString(resetRatio);
				return " ≤ " + ratioString;
			}
			return "0";
		}
	}

}
