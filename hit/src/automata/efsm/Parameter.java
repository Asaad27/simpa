package automata.efsm;

import java.io.Serializable;

import drivers.efsm.EFSMDriver.Types;
import main.simpa.Options;
import org.apache.commons.lang3.math.NumberUtils;

public class Parameter implements Cloneable, Serializable {
	private static final long serialVersionUID = 5486744974363387501L;
	public static final String PARAMETER_INIT_VALUE = "init";
	public static final String PARAMETER_DEFAULT_VALUE = Options.SYMBOL_OMEGA_LOW;
	
	public String value;
	public Types type;
	private int ndv = -1;
	
	public Parameter(){
		value = PARAMETER_INIT_VALUE;
		type = Types.NOMINAL;
	}
	
	public Parameter(String v){
		this.value = v;
		if (NumberUtils.isNumber(v)){
			this.type = Types.NUMERIC;
		} else {
			this.type = Types.STRING;
		}
	}
	
	public Parameter(String v, Types t) {
		if(v.equals(PARAMETER_INIT_VALUE)){
			throw new IllegalArgumentException("A parameter with default value "
					+ "must not be manually created. Use empty constructor instead.");
		}
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

	public boolean equals(Parameter a) {
		if (a.isNDV() || isNDV())
			return a.ndv == ndv;
		else
			return a.value.equals(value);
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
