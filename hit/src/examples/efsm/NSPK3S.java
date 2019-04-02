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
package examples.efsm;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import automata.State;
import automata.efsm.EFSM;
import automata.efsm.EFSMTransition;
import automata.efsm.IOutputFunction;
import automata.efsm.Parameter;
import drivers.efsm.EFSMDriver.Types;

public class NSPK3S {

	public static EFSM getAutomata() {
		EFSM test = new EFSM("NSPK3S");
		State s1 = test.addState(true);
		State s2 = test.addState();
		State s3 = test.addState();
		test.addTransition(new EFSMTransition(test, s1, s2, "m1", "m2",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						int n = new SecureRandom().nextInt(1000);
						automata.setMemory("n", String.valueOf(n));
						p.add(new Parameter(inputParameters.get(0).value,
								Types.NUMERIC));
						p.add(new Parameter(String.valueOf(n), Types.NUMERIC));
						return p;
					}
				}));
		test.addTransition(new EFSMTransition(test, s2, s3, "m3", "OK",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						if (inputParameters.get(0).value.equals(automata
								.getMemory("n"))) {
							List<Parameter> p = new ArrayList<Parameter>();
							p.add(new Parameter("1", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		test.addTransition(new EFSMTransition(test, s2, s2, "m3", "KO",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						if (!(inputParameters.get(0).value.equals(automata
								.getMemory("n")))) {
							List<Parameter> p = new ArrayList<Parameter>();
							p.add(new Parameter("0", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		test.addTransition(new EFSMTransition(test, s3, s3, "m3", "KO",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						if (!(inputParameters.get(0).value.equals(automata
								.getMemory("n")))) {
							List<Parameter> p = new ArrayList<Parameter>();
							p.add(new Parameter("0", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		test.addTransition(new EFSMTransition(test, s3, s1, "m3", "OK2",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						if (inputParameters.get(0).value.equals(automata
								.getMemory("n"))) {
							List<Parameter> p = new ArrayList<Parameter>();
							p.add(new Parameter("1", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		return test;
	}
}
