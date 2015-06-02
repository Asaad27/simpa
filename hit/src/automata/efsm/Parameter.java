package automata.efsm;

import java.io.Serializable;

import drivers.efsm.EFSMDriver.Types;
import org.apache.commons.lang3.math.NumberUtils;

public class Parameter implements Cloneable, Serializable {
	private static final long serialVersionUID = 5486744974363387501L;
	public static final String PARAMETER_INIT_VALUE = "init";
	public static final String PARAMETER_NO_VALUE = "novalue";
	
	public String value;
	public Types type;
	private int ndv = -1;
	
	public Parameter(){
		this.value = PARAMETER_NO_VALUE;
		this.type = Types.NOMINAL;
	}
	
	public Parameter(String v){
		if (v== null){
			this.value = PARAMETER_INIT_VALUE;
			this.type = Types.NOMINAL;
			return;
		}
		
		this.value = v;
		if(v.equals(PARAMETER_INIT_VALUE) || v.equals(PARAMETER_NO_VALUE)){
			this.type = Types.NOMINAL;
		} else if (NumberUtils.isNumber(v)){
			this.type = Types.NUMERIC;
		} else {
			this.type = Types.STRING;
		}
	}
	
	public Parameter(String v, Types t) {
		this.value = v;
		this.type = t;
	}

	public Parameter(String v, Types t, int Ndv) {
		this(v, t);
		this.ndv = Ndv;
	}

	public Parameter clone() {
		return new Parameter(value, type, ndv);
	}

	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Parameter)){
			return false;
		}
		Parameter other = (Parameter)o;
		if (other.isNDV() || isNDV())
			return other.ndv == ndv;
		else
			return other.value.equals(value);
	}

	@Override
	public int hashCode() {
		if(isNDV()){
			return ndv;
		} else {
			return value.hashCode();
		}
	}

	public String toString() {
		return value;
	}

	public boolean isInit() {
		return (value == null ? false : value.equals(PARAMETER_INIT_VALUE)) && type == Types.NOMINAL;
	}

	public boolean isNDV() {
		return ndv != -1;
	}

	public void setNdv(int ndv) {
		if(this.ndv == -1 && ndv != -1){
			System.err.println("Parameter [" + this.value + "] is now considered as a NDV : " + ndv);
		}
		this.ndv = ndv;
	}

	public int getNdv() {
		return ndv;
	}

}
