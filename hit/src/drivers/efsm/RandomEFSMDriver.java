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
 *     Emmanuel PERRIER
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import automata.efsm.Parameter;
import examples.efsm.RandomEFSM;
import examples.efsm.RandomEFSM.RandomEFSMOption;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;
import tools.Utils;

public class RandomEFSMDriver extends EFSMDriver {
	public static class RandomDriveroption
			extends DriverChoiceItem<EFSMDriver> {
		RandomEFSMOption automatonOptions = new RandomEFSMOption();

		public RandomDriveroption(DriverChoice<?> parent) {
			super(parent, RandomEFSMDriver.class);
			subTrees.addAll(automatonOptions.getOptions());
		}

		@Override
		public EFSMDriver createDriver() {
			RandomEFSM automaton = new RandomEFSM(automatonOptions);
			return new RandomEFSMDriver(automaton);
		}

	}

	private HashMap<String, List<ArrayList<Parameter>>> dpv = null;
	private TreeMap<String, List<String>> pn = null;
	private int nbStates = 0;
	private RandomEFSM efsm = null;

	public RandomEFSMDriver(RandomEFSM a) {
		super(a);
		efsm = a;
		dpv = a.getDefaultParamValues();
		pn = a.getDefaultParamNames();
		nbStates = a.getStateCount();
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs",
				"Equalities", "NDVs", "ARL", "Requests", "Duration");
	}

	public List<String> getStats() {
		return Utils
				.createArrayList(
						String.valueOf(nbStates),
						String.valueOf(getInputSymbols().size()),
						String.valueOf(getOutputSymbols().size()),
						String.valueOf(efsm.getSimpleGuardCount()),
						String.valueOf(efsm.getNdvGuardCount()),
						String.valueOf(((float) getNumberOfAtomicRequest()
								/ getNumberOfRequest())),
						String.valueOf(getNumberOfRequest()),
						String
								.valueOf(((float) duration / 1000000000)));
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> res = new HashMap<String, List<ArrayList<Parameter>>>();
		for (String s : dpv.keySet()) {
			List<ArrayList<Parameter>> i = new ArrayList<ArrayList<Parameter>>();
			for (ArrayList<Parameter> lp : dpv.get(s)) {
				ArrayList<Parameter> tmp = new ArrayList<Parameter>();
				for (Parameter tmpp : lp)
					tmp.add(tmpp.clone());
				i.add(tmp);
			}
			res.put(s, i);
		}
		return res;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		return pn;
	}

}
