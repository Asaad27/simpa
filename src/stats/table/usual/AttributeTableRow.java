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

import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.table.TableRow;

/**
 * a simple row filtering on one attribute
 * 
 * @author Nicolas BREMOND
 *
 * @param <T>
 *            the type of the filtering attribute
 */
public class AttributeTableRow<T extends Comparable<T>> extends TableRow {
	public AttributeTableRow(String header, Attribute<T> attribute,
			T attributeValue) {
		this.header = header;
		this.attribute = attribute;
		this.attributeValue = attributeValue;
	}

	final String header;
	final Attribute<T> attribute;
	final T attributeValue;

	@Override
	public String getRawHeader() {
		return header;
	}

	@Override
	public StatsSet getData(StatsSet s) {
		return new StatsSet(s,
				new EqualsRestriction<T>(attribute, attributeValue));
	}

}
