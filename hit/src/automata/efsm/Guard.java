package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import drivers.efsm.EFSMDriver.Types;

public class Guard implements Serializable {
	private static final long serialVersionUID = 7535698532882329634L;

	public boolean alwaysTrue = true;
	public boolean alwaysFalse = false;
	public List<Relation> rels = null;
	public boolean not = false;
	public int nbInputParam = 1;
	public Map<String, String> memory = null;
	public boolean checkNdv = false;

	public Guard(int nbInputParam) {
		rels = new ArrayList<Relation>();
		this.nbInputParam = nbInputParam;
	}

	public boolean isTrue(EFSM automata, List<Parameter> inputParameters) {
		boolean res = false;
		if (alwaysTrue)
			res = true;
		else if (alwaysFalse)
			res = false;
		else {
			boolean tmp = true;
			for (Relation r : rels) {
				if (!r.isTrue(inputParameters)) {
					tmp = false;
					break;
				}
			}
			res = tmp;
		}
		if (not)
			return !res;
		else
			return res;
	}

	public Guard clone() {
		Guard g = new Guard(nbInputParam);
		g.memory = memory;
		g.nbInputParam = nbInputParam;
		g.alwaysFalse = alwaysFalse;
		g.alwaysTrue = alwaysTrue;
		g.rels = rels;
		g.not = not;
		return g;
	}

	public Guard alwaysTrue() {
		alwaysFalse = false;
		alwaysTrue = true;
		not = false;
		return this;
	}

	public Guard alwaysFalse() {
		alwaysFalse = true;
		alwaysTrue = false;
		not = false;
		return this;
	}

	public Guard not() {
		not = !not;
		return this;
	}

	public String toString() {
		String res = "";
		if (alwaysTrue)
			res = "true";
		else if (alwaysFalse)
			res = "false";
		else {
			if (rels.size() > 0)
				res = rels.get(0).toString();
			for (int i = 1; i < rels.size(); i++) {
				res += " & " + rels.get(i).toString();
			}
		}
		if (not)
			res = "not (" + res + ")";
		return res;
	}

	public ArrayList<Parameter> randomize() {
		alwaysFalse = false;
		alwaysTrue = false;
		rels.clear();

		rels.add(new Relation(nbInputParam));

		ArrayList<Parameter> ll = new ArrayList<Parameter>();
		for (int i = 0; i < nbInputParam; i++) {
			ll.add(new Parameter(String.valueOf(Options.DOMAINSIZE * 10
					+ (i + 1)), Types.NUMERIC));
		}

		for (Relation r : rels) {
			switch (r.type) {
			case EQUALSTOVALUE:
				ll.get(r.op1).value = String.valueOf(r.op2i);
			case NDVCHECK:
				break;
			default:
				break;
			}
		}

		return ll;
	}

	public void checkNdv(int nbNdv) {
		alwaysFalse = false;
		alwaysTrue = false;
		rels.clear();
		Relation r = new Relation(nbInputParam);

		r.ndvCheck(nbNdv, memory);
		rels.add(r);
		checkNdv = true;
	}

}
