package learner.efsm.table;

import java.io.Serializable;
import java.util.List;

import main.simpa.Options;
import automata.efsm.EFSM;
import automata.efsm.Parameter;

public class LiControlTableItem implements Serializable {

	private static final long serialVersionUID = -8484677909227738772L;
	private List<Parameter> inputParameters;
	private String outputSymbol;

	public LiControlTableItem(List<Parameter> list, String outputSymbol) {
		this.outputSymbol = outputSymbol;
		this.inputParameters = list;
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}

	public Parameter getParameter(int index) {
		return inputParameters.get(index);
	}

	public Integer getParameterNDVIndex(int iParameter) {
		return inputParameters.get(iParameter).getNdv();
	}

	public List<Parameter> getParameters() {
		return inputParameters;
	}

	/**
	 * @deprecated Please use parameters.equals() to compare them
	 * @return 
	 */
	public String getParamHash() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < inputParameters.size(); i++) {
			if (i > 0)
				s.append('|');
			if (inputParameters.get(i).isNDV())
				s.append("Ndv" + inputParameters.get(i).getNdv());
			else
				s.append(inputParameters.get(i).value);
		}
		return s.toString();
	}

	public boolean isOmegaSymbol() {
		return outputSymbol.equals(EFSM.OMEGA);
	}

	public void setNdv(int indexParam, int indexNdv) {
		inputParameters.get(indexParam).setNdv(indexNdv);
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer("((");
		for (int i = 0; i < inputParameters.size(); i++) {
			if (i > 0)
				res.append(", ");
			if (!inputParameters.get(i).isNDV())
				res.append(inputParameters.get(i).value);
			else
				res.append("Ndv" + inputParameters.get(i).getNdv());
		}
		return res
				.append("), "
						+ (outputSymbol.equals(EFSM.OMEGA) ? Options.SYMBOL_OMEGA_UP
								: outputSymbol) + ")").toString();
	}
}
