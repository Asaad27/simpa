package learner.efsm.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.simpa.Options;
import automata.efsm.Parameter;
import java.util.LinkedList;

public class LiDataTableItem implements Cloneable, Serializable {

	private static final long serialVersionUID = 7893795283839427233L;
	private List<Parameter> inputParameters;
	private List<Parameter> outputParameters;
	private String outputSymbol;
	private TreeMap<String, List<Parameter>> automataState;

	public LiDataTableItem(List<Parameter> inputParameters,
			TreeMap<String, List<Parameter>> automataState,
			List<Parameter> outputParameters, String outputSymbol) {
		this.outputParameters = outputParameters;
		this.inputParameters = inputParameters;
		this.automataState = automataState;
		this.outputSymbol = outputSymbol;
	}

	public TreeMap<String, List<Parameter>> getAutomataState() {
		return automataState;
	}

	public List<Parameter> getAutomataStateParams() {
		List<Parameter> res = new ArrayList<Parameter>();
		Set<Map.Entry<String, List<Parameter>>> s = automataState.entrySet();
		for (Map.Entry<String, List<Parameter>> en : s) {
			for (Parameter p : en.getValue())
				res.add(p);
		}
		return res;
	}

	public List<String> getAutomataStateValues() {
		List<String> res = new ArrayList<String>();
		Set<Map.Entry<String, List<Parameter>>> s = automataState.entrySet();
		for (Map.Entry<String, List<Parameter>> en : s) {
			for (Parameter p : en.getValue())
				res.add(p.value);
		}
		return res;
	}

	public List<Parameter> getInputParameters() {
		return inputParameters;
	}

	/**
	 * @return 	List of input parameters in the given data item
	 */
	public List<String> getInputParametersValues() {
		List<String> res = new ArrayList<String>();
		for (Parameter p : inputParameters)
			res.add(p.value);
		return res;
	}

	public List<Parameter> getOutputParameters() {
		return outputParameters;
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}

	public void setNdvParam(int i, int iNdv) {
		outputParameters.get(i).setNdv(iNdv);
	}

	@Override
	public boolean equals(Object to) {
		if (this == to)
			return true;
		if (!(to instanceof LiDataTableItem))
			return false;
		LiDataTableItem dti2 = (LiDataTableItem) to;
		if (inputParameters.size() != dti2.inputParameters.size())
			return false;
		for (int i = 0; i < inputParameters.size(); i++) {
			if (!inputParameters.get(i).value.equals(dti2.inputParameters
					.get(i).value))
				return false;
		}
		if (getAutomataStateValues().size() != dti2.getAutomataStateValues()
				.size())
			return false;
		List<String> a = getAutomataStateValues();
		List<String> b = dti2.getAutomataStateValues();
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).equals(b.get(i)))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder("((");
		for (int i = 0; i < inputParameters.size(); i++) {
			if (i > 0)
				res.append(", ");
			res.append(inputParameters.get(i).value);
		}
		res.append("), [");
		for (List<Parameter> s : automataState.values()) {
			res.append('(');
			if (s.size() > 0)
				res.append(s.get(0).isNDV() ? "Ndv" + s.get(0).getNdv()
						: s.get(0).value);
			for (int i = 1; i < s.size(); i++)
				res.append(", ").append(s.get(i).isNDV() ? "Ndv" + s.get(i).getNdv()
						: s.get(i).value);
			res.append(')');
		}
		res.append("] -> ");
		if (outputParameters.isEmpty())
			res.append(Options.SYMBOL_OMEGA_LOW);
		else {
			res.append('(');
			for (int i = 0; i < outputParameters.size(); i++) {
				if (i > 0)
					res.append(", ");
				if (outputParameters.get(i).isNDV())
					res.append("Ndv").append(outputParameters.get(i).getNdv());
				else
					res.append(outputParameters.get(i).value);
			}
			res.append(')');
		}
		return res.append(')').toString();
	}
	
	@Override
	public Object clone(){
		TreeMap<String, List<Parameter>> automataStateClone = new TreeMap<>(
				automataState);
		List<Parameter> inputParametersClone = new LinkedList<>();
		for (Parameter inputParameter : inputParameters) {
			inputParametersClone.add(inputParameter.clone());
		}
		List<Parameter> outputParametersClone = new LinkedList<>();
		for (Parameter outputParameter : outputParameters) {
			outputParametersClone.add(outputParameter.clone());
		}
		
		return new LiDataTableItem(inputParametersClone, automataStateClone, outputParametersClone, outputSymbol);
	}
}