package learner.efsm.table;

import java.util.List;

import automata.efsm.Parameter;

public class NBP {
	public List<Parameter> params;
	public int iInputSymbol;

	public NBP(List<Parameter> params, int iInputSymbol) {
		this.iInputSymbol = iInputSymbol;
		this.params = params;
	}

	public String getParamHash() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < params.size(); i++) {
			if (i > 0)
				s.append('|');
			if (params.get(i).ndv != -1)
				s.append("Ndv" + params.get(i).ndv);
			else
				s.append(params.get(i).value);
		}
		return s.toString();
	}

	public void setNdvIndex(int iVar, int iNdv) {
		this.params.get(iVar).ndv = iNdv;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer("(");
		for (int i = 0; i < params.size(); i++) {
			if (i > 0)
				s.append(", ");
			s.append(params.get(i).value);
			if (params.get(i).ndv != -1)
				s.append("(Ndv" + params.get(i).ndv + ")");
		}
		return s.append(") for input symbol " + (iInputSymbol + 1)).toString();
	}
}
