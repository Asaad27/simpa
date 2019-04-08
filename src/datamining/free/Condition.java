/********************************************************************************
 * Copyright (c) 2013,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Maxime PEYRARD
 *     Emmanuel PERRIER
 *     Karim HOSSEN
 ********************************************************************************/
package datamining.free;

import java.util.Iterator;
import java.util.LinkedList;

import main.simpa.Options;

public class Condition extends LinkedList<Tag> {
	private static final long serialVersionUID = -6467576331816693683L;

	public Condition() {
		super();
	}
	
	public Condition(Tag t) {
		super();
		this.add(t);
	}
	
	public Condition(Condition c) {
		super();
		Iterator<Tag> itr = c.iterator();
		while (itr.hasNext()) {
			this.add(itr.next());
		}
	}
	
	public String toString() {
		String str = "";
		Iterator<Tag> itr = this.iterator();
		if (itr.hasNext()) {
			str += itr.next().toString();
		}
		while (itr.hasNext()) {
			str += " " + Options.SYMBOL_AND + " ";
			str += itr.next().toString();
		}
		return str;
	}
}
