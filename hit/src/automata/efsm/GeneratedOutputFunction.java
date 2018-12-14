package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import drivers.efsm.EFSMDriver.Types;
import main.simpa.Options;
import options.RandomOption;
import tools.StandaloneRandom;

public class GeneratedOutputFunction implements IOutputFunction, Serializable {
	private static final long serialVersionUID = 2354232336322191833L;

	private Guard guard = null;
	private int nbOutputParams = 1;
	private int nbInputParams = 1;
	private List<String> isNdv = null;

	public GeneratedOutputFunction(int nbInputParams, int nbOutputParams) {
		guard = new Guard(nbInputParams);
		this.nbOutputParams = nbOutputParams;
		this.nbInputParams = nbInputParams;
		this.isNdv = new ArrayList<String>();
		for (int i = 0; i < nbOutputParams; i++)
			this.isNdv.add("");
	}

	public Guard getGuard() {
		return guard;
	}

	public void setGuard(Guard g) {
		guard = g;
	}

	@Override
	public List<Parameter> process(EFSM automata,
			List<Parameter> inputParameters) {
		if (guard.isTrue(automata, inputParameters)) {
			List<Parameter> param = new ArrayList<Parameter>();
			for (int i = 0; i < nbOutputParams; i++) {
				if (!isNdv.get(i).equals("")) {
					String ndvVal = String.valueOf(new StandaloneRandom()
							.randIntBetween(Options.DOMAINSIZE * 100,
									Options.DOMAINSIZE * 1000));
					param.add(new Parameter(ndvVal, Types.NUMERIC));
					guard.memory.put(isNdv.get(i), ndvVal);
				} else {
					param.add(new Parameter(inputParameters.get(i
							% nbInputParams).value, Types.NUMERIC));
				}
			}
			return param;
		}
		return null;
	}

	public void generateNdv(int nbNdv, RandomOption rand) {
		isNdv.set(rand.randInt(nbOutputParams), "ndv" + nbNdv);
	}

	public String toString() {
		String g = guard.toString();
		for (int i = 0; i < isNdv.size(); i++)
			if (!isNdv.get(i).equals(""))
				g += "\\n" + "outp" + i + " = " + isNdv.get(i);
		return g;
	}
}
