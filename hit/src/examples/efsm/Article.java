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

public class Article {

	public static EFSM getAutomata() {
		EFSM test = new EFSM("Article");
		State s0 = test.addState(true);
		State s1 = test.addState();
		State s2 = test.addState();
		test.addTransition(new EFSMTransition(test, s0, s1, "a", "d",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						p.add(new Parameter(inputParameters.get(0).value,
								Types.NUMERIC));
						return p;
					}
				}));
		test.addTransition(new EFSMTransition(test, s0, s2, "b", "d",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						automata.setMemory("v2", inputParameters.get(0).value);
						p.add(new Parameter(inputParameters.get(0).value,
								Types.NUMERIC));
						return p;
					}
				}));
		test.addTransition(new EFSMTransition(test, s1, s1, "b", "d",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						p.add(new Parameter("0", Types.NUMERIC));
						return p;
					}
				}));
		test.addTransition(new EFSMTransition(test, s1, s2, "a", "c",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						int n = new SecureRandom().nextInt(1000);
						automata.setMemory("v3", String.valueOf(n));
						p.add(new Parameter(automata.getMemory("v3"),
								Types.NUMERIC));
						return p;
					}
				}));
		test.addTransition(new EFSMTransition(test, s2, s1, "b", "d",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						if (!inputParameters.get(0).value.equals(automata
								.getMemory("v3"))
								&& !inputParameters.get(0).value
										.equals(automata.getMemory("v2"))) {
							p.add(new Parameter("0", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		test.addTransition(new EFSMTransition(test, s2, s0, "b", "e",
				new IOutputFunction() {
					@Override
					public List<Parameter> process(EFSM automata,
							List<Parameter> inputParameters) {
						List<Parameter> p = new ArrayList<Parameter>();
						if (inputParameters.get(0).value.equals(automata
								.getMemory("v3"))
								|| inputParameters.get(0).value.equals(automata
										.getMemory("v2"))) {
							p.add(new Parameter("1", Types.NUMERIC));
							return p;
						} else
							return null;
					}
				}));
		return test;
	}
}
