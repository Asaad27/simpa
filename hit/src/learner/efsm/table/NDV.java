package learner.efsm.table;

import automata.efsm.ParameterizedInputSequence;
import drivers.efsm.EFSMDriver.Types;

public class NDV implements Cloneable {
	public ParameterizedInputSequence pis;
	public int paramIndex;
	public int indexNdv;
	private final String outputSymbol;
	public drivers.efsm.EFSMDriver.Types type;

	
	/**
	 * Constructor for a new NDV element 
	 * 
	 * @param pis 			Parameterized Input Sequence containing a parameter that is NDV
	 * @param type 			Type of the NDV 
	 * @param paramIndex	Index of the input parameter, in the last parametrized input of pis, that is NDV value
	 * @param iNdv			Index of this NDV in NDV list
	 * @param outputSymbol Output symbol related to the NDV
	 */
	public NDV(ParameterizedInputSequence pis, Types type, int paramIndex,
			int iNdv, String outputSymbol) {
		this.paramIndex = paramIndex;
		this.pis = pis;
		this.indexNdv = iNdv;
		this.type = type;
		this.outputSymbol = outputSymbol;
	}

	@Override
	public NDV clone() {
		return new NDV(pis.clone(), type, paramIndex, indexNdv, outputSymbol);
	}

	@Override
	public boolean equals(Object to) {
		if (this == to)
			return true;
		if (!(to instanceof NDV))
			return false;
		NDV comp = (NDV) to;
		return (pis.isSame(comp.pis) && (paramIndex == comp.paramIndex));
	}

	@Override
	public int hashCode() {
		return 7 * pis.hashCode() + 31 * paramIndex;
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}

	@Override
	public String toString() {
		return "Output parameter " + (paramIndex + 1) + " of " + pis;
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}
}
