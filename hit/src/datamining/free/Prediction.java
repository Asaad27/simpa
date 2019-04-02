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

import java.util.ArrayList;
import java.util.Iterator;

import main.simpa.Options;

public class Prediction extends ArrayList<Condition>{
	private static final long serialVersionUID = -7461576989004249870L;
	String final_state;
	
	public Prediction (String f_state) {
		super();
		final_state = new String(f_state);
	}
	
	public Prediction (Condition c, String f_state) {
		super();
		final_state = new String(f_state);
		this.add(c);
	}
	
	public String toString() {
		String str = "";
		str += final_state+" :\n";
		Iterator<Condition> itr = this.iterator();
		if (itr.hasNext()) {
			str += "   (";
			str += itr.next().toString();
			str += ")\n";
		}
		while (itr.hasNext()) {
			str += Options.SYMBOL_OR + " (";
			str += itr.next().toString();
			str += ")\n";
		}
		return str;
	}
}
