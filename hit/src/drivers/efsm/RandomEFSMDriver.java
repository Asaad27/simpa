package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import automata.efsm.Parameter;
import examples.efsm.RandomEFSM;

public class RandomEFSMDriver extends EFSMDriver {

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
						String.valueOf(((float) numberOfAtomicRequest / numberOfRequest)),
						String.valueOf(numberOfRequest), String
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
