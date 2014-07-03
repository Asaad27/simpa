package learner.mealy.alpha;

import tools.Utils;

public class StateLabel implements Cloneable{
	
	public StateLabel clone(){
		return new StateLabel(name,father,isQuestionMark);
	}
	
	public static String realStateLetter = "A";
	
	public static int questionMarkNumerotation = 0;
	
	public String name;
	
	public StateLabel father;
	
	public boolean isQuestionMark;
	
	public StateLabel(boolean isQuestionMark){
		father =null;
		this.isQuestionMark=isQuestionMark;
		if(isQuestionMark){
			name = "?"+questionMarkNumerotation;
			questionMarkNumerotation++;			
		}
		else{
			name =realStateLetter;
			realStateLetter = Utils.nextSymbols(realStateLetter);
		}
		
		
	}
	
	public StateLabel(StateLabel father,int sonNumber){
		name = father.name+sonNumber;
		this.father = father;
		isQuestionMark=false;
	}
	public StateLabel(String name,StateLabel father,boolean isQM){
		this.name=name;
		this.father=father;
		this.isQuestionMark=isQM;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateLabel other = (StateLabel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean hasAncestors(){
		return father!=null;
	}

}
