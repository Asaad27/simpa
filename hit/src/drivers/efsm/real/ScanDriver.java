package drivers.efsm.real;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public class ScanDriver extends RealDriver {

	public ScanDriver(String sYSTEM) {
		type = DriverType.SCAN;
	}

	@Override
	public ParameterizedOutput execute(ParameterizedInput pi) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
