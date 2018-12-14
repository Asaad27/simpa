package automata.efsm;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import options.RandomOption;

public class Relation implements Serializable {

	private static final long serialVersionUID = 4760736053715761877L;

	public static enum RelationType {
		EQUALSTOVALUE, NDVCHECK;
	}

	public int op1;
	public int op2i;
	public String op2s = null;
	public RelationType type;
	private int nbInputParam;
	private Map<String, String> memory = null;

	public Relation(int nbInputParam, RandomOption rand) {
		this.nbInputParam = nbInputParam;

		type = RelationType.EQUALSTOVALUE;

		switch (type) {
		case EQUALSTOVALUE:
			op1 = rand.randInt(nbInputParam);
			op2i = rand.randInt(Options.DOMAINSIZE);
			break;
		case NDVCHECK:
			break;
		default:
			break;
		}
	}

	public void ndvCheck(int nbNdv, Map<String, String> memory,
			RandomOption rand) {
		this.memory = memory;
		type = RelationType.NDVCHECK;
		op1 = rand.randInt(nbInputParam);
		op2s = "ndv" + nbNdv;
	}

	public boolean isTrue(List<Parameter> inputParameters) {
		switch (type) {
		case EQUALSTOVALUE:
			return Integer.valueOf(inputParameters.get(op1).value) == op2i;
		case NDVCHECK:
			return inputParameters.get(op1).value.equals(memory.get(op2s));
		}
		return false;
	}

	public String toString() {
		String res = "";
		switch (type) {
		case EQUALSTOVALUE:
			res = "inp" + op1 + " = " + op2i;
			break;
		case NDVCHECK:
			res = "inp" + op1 + " = " + op2s;
			break;
		}
		return res;
	}
}
