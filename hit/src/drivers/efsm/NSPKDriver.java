package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import automata.efsm.Parameter;
import examples.efsm.NSPK;

public class NSPKDriver extends EFSMDriver {

	public NSPKDriver() {
		super(NSPK.getAutomata());
	}
	
	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(){
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		ArrayList<ArrayList<Parameter>> params = null;
		
		//m1
		{
			params = new ArrayList<ArrayList<Parameter>>();	
			params.add(Utils.createArrayList(new Parameter("5", Types.NUMERIC)));
			params.add(Utils.createArrayList(new Parameter("6", Types.NUMERIC)));
			defaultParamValues.put("m1", params);		
		}
		
		//m3
		{
			params = new ArrayList<ArrayList<Parameter>>();	
			params.add(Utils.createArrayList(new Parameter("10", Types.NUMERIC)));
			params.add(Utils.createArrayList(new Parameter("11", Types.NUMERIC)));
			defaultParamValues.put("m3", params);
		}
		
		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames(){
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("m1", Utils.createArrayList("sendedID"));
		defaultParamNames.put("m2", Utils.createArrayList("receivedID", "receivedSessionID"));
		defaultParamNames.put("m3", Utils.createArrayList("sendedSessionID"));
		defaultParamNames.put("KO", Utils.createArrayList("default1"));
		defaultParamNames.put("OK", Utils.createArrayList("default2"));
		return defaultParamNames;
	}
	
}
