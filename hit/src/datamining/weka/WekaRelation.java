package datamining.weka;

import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;
import tools.loggers.LogManager;
import weka.core.Instance;
import weka.core.UnassignedDatasetException;

public class WekaRelation {	
	public static enum RelationType {
		 EQUALS;
	}

	private int a1;
	private int a2;
	private String name1;
	private String name2;
	private RelationType type;
	
	public WekaRelation(RelationType type, int a1, String name1, int a2, String name2){
		this.a1 = a1;
		this.a2 = a2;
		this.name1 = name1;
		this.name2 = name2;
		this.type = type;
	}
	
	public RelationType getType(){
		return type;
	}
	
	public boolean isTrueOn(Instance inst){
		switch(type){
			case EQUALS :
				if (inst.attribute(a1).isNumeric()){
					if (inst.value(a1) != inst.value(a2)) return false;
				}else{
					if (!inst.stringValue(a1).equals(inst.stringValue(a2))) return false;
				}	
				break;
		}
		return true;
	}
	
	public boolean isTrueOn(List<Instance> insts, int percent){
		int nbTrue = 0;
		try{
			for(Instance inst : insts){
				if (isTrueOn(inst)) nbTrue++;
			}
			if ((100*nbTrue/insts.size()) >= percent) return true;
		} catch (IllegalArgumentException e) {
			LogManager.logException("Error checking relation on instance (IllegalType)", e);
			return false;
		} catch (UnassignedDatasetException e) {
			LogManager.logException("Error checking relation on instance (No Dataset)", e);
			return false;
		}
		return false;
	}
	
	public static List<WekaRelation> findOn(List<Instance> insts,
			int supportMin) {
		List<WekaRelation> lr = new ArrayList<WekaRelation>();		
		if (!insts.isEmpty()){
			int attrCount = insts.get(0).numAttributes()-1;
			Instance inst = insts.get(0);
			try{
				for (RelationType t:RelationType.values()){
					switch(t){
					case EQUALS :
						for(int i=0; i<attrCount; i++){
							for(int j=i+1; j<attrCount; j++){
								WekaRelation r = null;
								if (i==j) continue;
								if (inst.attribute(i).type() == inst.attribute(j).type()){ 
									if (inst.attribute(i).isNumeric()){
										if (inst.value(i) == inst.value(j)) r = new WekaRelation(t, i, inst.attribute(i).name(), j, inst.attribute(j).name());
									}else{
										if (!inst.stringValue(i).equals(inst.stringValue(j))) r = new WekaRelation(t, i, inst.attribute(i).name(), j, inst.attribute(j).name());
									}
								}
								if (r != null){
									if (r.isTrueOn(insts, supportMin))
										lr.add(r);
								}
							}
						}							
						break;
					}
				}
			} catch (IllegalArgumentException e) {
				LogManager.logException("Error finding on instance (IllegalType)", e);
				return null;
			} catch (UnassignedDatasetException e) {
				LogManager.logException("Error finding relation on instance (No Dataset)", e);
				return null;
			}
		}
		return lr;
	}
	
	public String toString(){
		String res = name1;
		switch(type){
			case EQUALS : res += " = ";
		}
		return res + name2;
	}
	
	public String toNotString(){
		String res = name1;
		switch(type){
			case EQUALS : res += " "+ Options.SYMBOL_NOT_EQUAL + " ";
		}
		return res + name2;
	}
	
	public ArrayList<String> getValues(){
		ArrayList<String> fv = new ArrayList<String>();
		fv.add(this.toString());
		fv.add(this.toNotString());
		return fv;
	}
	
	public boolean equals(Object to){
		if ( this == to ) return true;
	    if ( !(to instanceof WekaRelation) ) return false;
	    WekaRelation comp = (WekaRelation)to;
	    switch (type){
	    case EQUALS:
	    	return (((a1 == comp.a1) && (a2 == comp.a2)) || ((a1 == comp.a2) && (a2 == comp.a1))) && (type == comp.type);
	    default:
	    	LogManager.logError("Unknown relation type");
	    	return false;
	    }		
	}
	
	public int hashCode(){
		switch (type){
	    case EQUALS:
	    	return 13 * a1 + 13* a2 + 31 * type.hashCode();
	    default:
	    	LogManager.logError("Unknown relation type");
	    	return 0;
	    }		
	}
	
}
