package automata.efsm;

import java.io.Serializable;

import drivers.efsm.EFSMDriver.Types;

public class Parameter implements Cloneable, Serializable{
	private static final long serialVersionUID = 5486744974363387501L;

	public String value;
	public Types type;
	public int ndv = -1;
	
	public Parameter(String v, Types t){
		this.value = v;
		this.type = t;
	}

	public Parameter(String v, Types t, int Ndv){
		this(v, t);
		this.ndv = Ndv;
	}
	
	public Parameter clone(){
		return new Parameter(new String(value), type, ndv);
	}
		
	public boolean equals(Parameter a){
		if (a.isNDV() || isNDV()) return a.ndv == ndv;
		else return a.value.equals(value);
	}
		
	public String toString(){
		return value;
	}

	public boolean isInit() {
		return value.equals("init") && type==Types.NOMINAL;
	}
	
	public boolean isNDV() {
		return ndv != -1;
	}
}
