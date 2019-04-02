/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package stats.attribute.restriction;

import stats.StatsEntry;
import stats.attribute.Attribute;

public class EqualsRestriction<T extends Comparable<T>> extends Restriction {
	private T value;
	Attribute<T> a;

	public EqualsRestriction(Attribute<T> a, T value) {
		this.a = a;
		this.value = value;
	}

	@Override
	public boolean contains(StatsEntry s) {
		return s.get(a).equals(value);
	}

	public String toString() {
		return a.toString() + "=" + value;
	}
}
