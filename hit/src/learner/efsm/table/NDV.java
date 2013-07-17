package learner.efsm.table;

import automata.efsm.ParameterizedInputSequence;
import drivers.efsm.EFSMDriver.Types;

public class NDV implements Cloneable {
	public ParameterizedInputSequence pis;
	public int paramIndex;
	public int indexNdv;
	public drivers.efsm.EFSMDriver.Types type;

	public NDV(ParameterizedInputSequence pis, Types type, int paramIndex,
			int iNdv) {
		this.paramIndex = paramIndex;
		this.pis = pis;
		this.indexNdv = iNdv;
		this.type = type;
	}

	@Override
	public NDV clone() {
		return new NDV(pis.clone(), type, paramIndex, indexNdv);
	}

	public boolean equals(Object to) {
		if (this == to)
			return true;
		if (!(to instanceof NDV))
			return false;
		NDV comp = (NDV) to;
		return (pis.isSame(comp.pis) && (paramIndex == comp.paramIndex));
	}

	public int hashCode() {
		return 7 * pis.toString().hashCode() + 31 * paramIndex;
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}

	@Override
	public String toString() {
		return "Output parameter " + (paramIndex + 1) + " of " + pis;
	}
}
