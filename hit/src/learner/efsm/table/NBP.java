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
