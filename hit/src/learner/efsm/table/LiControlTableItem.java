package learner.efsm.table;

import java.util.List;

import main.Options;
import automata.efsm.EFSM;
import automata.efsm.Parameter;

public class LiControlTableItem{
	private List<Parameter> parameters;
	private String outputSymbol;
	
	public LiControlTableItem(List<Parameter> list, String outputSymbol){
		this.outputSymbol = outputSymbol;
		this.parameters = list;
	}
	
	public String getOutputSymbol() {
		return outputSymbol;
	}
	
	public Parameter getParameter(int index) {
		return parameters.get(index);
	}
	
	public Integer getParameterNDVIndex(int iParameter){
		return parameters.get(iParameter).ndv;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public String getParamHash(){
		StringBuffer s = new StringBuffer();
		for(int i=0; i<parameters.size(); i++){
			if (i>0) s.append("|");
			if (parameters.get(i).ndv!=-1) s.append("Ndv" + parameters.get(i).ndv);
			else s.append(parameters.get(i).value);
		}
		return s.toString();
	}

	public boolean isOmegaSymbol(){
		return outputSymbol.equals(EFSM.OMEGA);
	}
	
	public void setNdv(int indexParam, int indexNdv){
		parameters.get(indexParam).ndv = indexNdv;
	}
	
	@Override
	public String toString(){
		StringBuffer res = new StringBuffer("((");
		for(int i=0; i<parameters.size(); i++){
			if (i>0) res.append(", ");
			if (parameters.get(i).ndv==-1) res.append(parameters.get(i).value);
			else res.append("Ndv" + parameters.get(i).ndv);
		}
		return res.append("), " + (outputSymbol.equals(EFSM.OMEGA)?Options.SYMBOL_OMEGA_UP:outputSymbol) + ")").toString();
	}
}
