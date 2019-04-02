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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClassificationResult extends HashMap<String, Prediction> {
	private static final long serialVersionUID = 5242833613182727636L;

	public ClassificationResult() {
		super();
	}
	
	public String toString() {
		String str = "";
		Iterator<Map.Entry<String, Prediction>> it = this.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Prediction> pairs = it.next(); 
			str += pairs.getValue().toString() + "\n";
		}
		return str;
	}

}
