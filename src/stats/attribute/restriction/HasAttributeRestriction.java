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
package stats.attribute.restriction;

import stats.StatsEntry;
import stats.attribute.Attribute;

public class HasAttributeRestriction<T extends Comparable<T>>
		extends Restriction {
	private T value;
	Attribute<T> a;

	/**
	 * create a restriction to keep only entries which have the given attribute.
	 * 
	 * @param a
	 *            the attribute that entries must have
	 * @param value
	 *            if not {@code null}, keep only entries which have the given
	 *            attribute and for which the attribute has the given value.
	 *            (This is a shortcut for an HasAttribute and an
	 *            eEqualRestriction).
	 */
	public HasAttributeRestriction(Attribute<T> a, T value) {
		this.a = a;
		this.value = value;
	}

	@Override
	public boolean contains(StatsEntry s) {
		return s.hasAttribute(a) && (value == null || s.get(a).equals(value));
	}

	public String toString() {
		return a.toString() + "=" + value;
	}
}
