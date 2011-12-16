package learner.efsm.table;

import java.util.ArrayList;
import java.util.List;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInputSequence;

public class NDF implements Cloneable {
	private ParameterizedInputSequence pis;
	private String inputSymbol;
	public List<ArrayList<Parameter>> parameters;
	
	public NDF(ParameterizedInputSequence pis, String inputSymbol, List<ArrayList<Parameter>> parameters){
		this.pis = pis;
		this.inputSymbol = inputSymbol;
		this.parameters = parameters;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public NDF clone(){
		return new NDF(pis.clone(), inputSymbol, (ArrayList<ArrayList<Parameter>>)((ArrayList<ArrayList<Parameter>>)parameters).clone());
	}
	
	public boolean equals(Object to){
		if ( this == to ) return true;
	    if ( !(to instanceof NDF) ) return false;
	    NDF comp = (NDF)to;
		return (pis.isSame(comp.pis) && (inputSymbol.equals(comp.inputSymbol)));
	}
	
	public int hashCode(){
		return 7 * pis.toString().hashCode() + 31 * inputSymbol.hashCode();
	}
	
	public String getInputSymbol(){
		return inputSymbol;
	}
	
	public ParameterizedInputSequence getPIS(){
		return pis.clone();
	}
	
	@Override
	public String toString(){
		StringBuffer s = new StringBuffer(pis.toString() + " " + inputSymbol + "(");
		for(int i=0; i<parameters.size(); i++){
			if (i>0) s.append("/");
			s.append("(");
			for(int j=0; j<parameters.get(i).size(); j++){
				if (j>0) s.append(", ");
				if (parameters.get(i).get(j).ndv!=-1) s.append("Ndv" + parameters.get(i).get(j).ndv);
				else s.append(parameters.get(i).get(j).value);
			}
			s.append(")");
		}
		return s.append(")").toString();
	}
}
