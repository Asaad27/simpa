package examples.efsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import automata.State;
import automata.efsm.EFSM;
import automata.efsm.EFSMTransition;
import automata.efsm.IOutputFunction;
import automata.efsm.Parameter;
import drivers.efsm.EFSMDriver.Types;

public class SamlSSOSP {

	public static EFSM getAutomata() {
		EFSM test = new EFSM("SamlSSO_SP");
		State s0 = test.addState(true);
		State s1 = test.addState();
		
		test.addTransition(new EFSMTransition(test, s0, s1, "httpReq1", "httpResp1", new IOutputFunction() {			
			@Override
			public List<Parameter> process(EFSM automata, List<Parameter> inputParameters) {
				if (!inputParameters.get(0).value.equals("http://go.od/")) return null;
				List<Parameter> p = new ArrayList<Parameter>();
				p.add(new Parameter("redirect", Types.NOMINAL));
				p.add(new Parameter("IdP", Types.NOMINAL));
				p.add(new Parameter("SamlReq", Types.NOMINAL));
				int state = new Random().nextInt(1000);
				automata.setMemory("id", String.valueOf(state));
				p.add(new Parameter(String.valueOf(state), Types.NUMERIC));
				return p;
			}
		}));
		test.addTransition(new EFSMTransition(test, s0, s0, "httpReq1", "err", new IOutputFunction() {			
			@Override
			public List<Parameter> process(EFSM automata, List<Parameter> inputParameters) {
				if (inputParameters.get(0).value.equals("http://go.od/")) return null;
				List<Parameter> p = new ArrayList<Parameter>();
				p.add(new Parameter("url_invalid", Types.NOMINAL));
				return p;
			}
		}));
		test.addTransition(new EFSMTransition(test, s1, s0, "httpReq2", "httpResp2", new IOutputFunction() {			
			@Override
			public List<Parameter> process(EFSM automata, List<Parameter> inputParameters) {
				if (!inputParameters.get(0).value.equals("http://go.od/")) return null;
				if (!inputParameters.get(1).value.equals("SamlResp")) return null;
				if (!automata.getMemory("id").equals(inputParameters.get(2).value)) return null;
				List<Parameter> p = new ArrayList<Parameter>();
				p.add(new Parameter("ok", Types.NOMINAL));
				p.add(new Parameter("ressource", Types.NOMINAL));
				return p;
			}
		}));
		test.addTransition(new EFSMTransition(test, s1, s1, "httpReq2", "err", new IOutputFunction() {			
			@Override
			public List<Parameter> process(EFSM automata, List<Parameter> inputParameters) {
				boolean url = inputParameters.get(0).value.equals("http://go.od/");
				boolean state = automata.getMemory("id").equals(inputParameters.get(2).value); 
				if (url && state) return null;
				List<Parameter> p = new ArrayList<Parameter>();
				if (!url && state) p.add(new Parameter("url_invalid", Types.NOMINAL));
				else if (url && !state) p.add(new Parameter("id_invalid", Types.NOMINAL));
				else p.add(new Parameter("url_and_id_invalid", Types.NOMINAL));
				return p;
			}
		}));
		return test;
	}
}
