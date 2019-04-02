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

import java.util.Arrays;
import java.util.List;

import stats.StatsEntry;
import stats.attribute.Attribute;


public class InSetRestriction<T extends Comparable<T>> extends Restriction {
	Attribute<T> a;
	List<T> values;

	public InSetRestriction(Attribute<T> a, T[] values) {
		this.a = a;
		this.values = Arrays.asList(values);
	}
	
	@Override
	public boolean contains(StatsEntry s) {
		return values.contains(s.get(a));
	}

	public String toString(){
		return a.getName() + " in " + values + a.getUnits().getSymbol();
	}
}
