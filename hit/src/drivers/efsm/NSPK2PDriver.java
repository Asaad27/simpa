/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 ********************************************************************************/
package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import automata.efsm.Parameter;
import examples.efsm.NSPK2P;

public class NSPK2PDriver extends EFSMDriver {

	public NSPK2PDriver() {
		super(NSPK2P.getAutomata());
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		ArrayList<ArrayList<Parameter>> params = null;

		// m1
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(
					new Parameter("666", Types.NUMERIC), new Parameter("5",
							Types.NUMERIC)));
			defaultParamValues.put("m1", params);
		}

		// m3
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(
					new Parameter("666", Types.NUMERIC), new Parameter("10",
							Types.NUMERIC)));
			defaultParamValues.put("m3", params);
		}
		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("m1",
				Utils.createArrayList("default1", "sendedID"));
		defaultParamNames.put("m2",
				Utils.createArrayList("receivedID", "receivedSessionID"));
		defaultParamNames.put("m3",
				Utils.createArrayList("default2", "sendedSessionID"));
		defaultParamNames.put("KO", Utils.createArrayList("default3"));
		defaultParamNames.put("OK", Utils.createArrayList("default4"));
		return defaultParamNames;
	}
}
