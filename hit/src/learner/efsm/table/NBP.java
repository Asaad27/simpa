package learner.efsm.table;

import java.util.List;

import automata.efsm.Parameter;

public class NBP {
	public List<Parameter> params;
	public int iInputSymbol;
	public String inputSymbol;

	public NBP(List<Parameter> params, int IInputSymbol, String inputSymbol) {
		this.inputSymbol = inputSymbol;
		this.iInputSymbol = IInputSymbol;
		this.params = params;
	}

	/**
	 * @deprecated You should call params.equals() to compare two lists of Parameters 
	 * @return 
	 */
	public String getParamHash() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < params.size(); i++) {
			if (i > 0)
				s.append('|');
			if (params.get(i).isNDV())
				s.append("Ndv" + params.get(i).getNdv());
			else
				s.append(params.get(i).value);
		}
		return s.toString();
	}

	public boolean hasSameParameters(LiControlTableItem cti){
		return cti.getParameters().equals(this.params);
	}
	
	public void setNdvIndex(int iVar, int iNdv) {
		this.params.get(iVar).setNdv(iNdv);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("(");
		for (int i = 0; i < params.size(); i++) {
			if (i > 0)
				s.append(", ");
			s.append(params.get(i).value);
			if (params.get(i).isNDV())
				s.append("(Ndv").append(params.get(i).getNdv()).append(")");
		}
		return s.append(") for input symbol ").append(inputSymbol).toString();
	}
}
