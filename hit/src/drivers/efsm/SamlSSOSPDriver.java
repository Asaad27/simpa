package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import automata.efsm.Parameter;
import examples.efsm.SamlSSOSP;

public class SamlSSOSPDriver extends EFSMDriver {

	public SamlSSOSPDriver() {
		super(SamlSSOSP.getAutomata());
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(){
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;
		
		//httpReq1
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(new Parameter("http://go.od/", Types.STRING)));
			params.add(Utils.createArrayList(new Parameter("http://err.or/", Types.STRING)));
			defaultParamValues.put("httpReq1", params);		
		}
		
		// httpReq2
		{
			params = new ArrayList<ArrayList<Parameter>>();			
			params.add(Utils.createArrayList(new Parameter("http://go.od/", Types.STRING), new Parameter("SamlResp", Types.NOMINAL), new Parameter("666", Types.NUMERIC)));
			params.add(Utils.createArrayList(new Parameter("http://err.or/", Types.STRING), new Parameter("SamlResp", Types.NOMINAL), new Parameter("666", Types.NUMERIC)));
			defaultParamValues.put("httpReq2", params);
		}
				
		return defaultParamValues;
	}
	
	public TreeMap<String, List<String>> getParameterNames(){
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("httpReq1", Utils.createArrayList("URL1"));
		defaultParamNames.put("httpReq2", Utils.createArrayList("URL2", "SAMLResponse", "SessionID"));
		defaultParamNames.put("err", Utils.createArrayList("Msg"));
		defaultParamNames.put("httpResp1", Utils.createArrayList("Action", "IdP", "SAMLRequest", "GenSessionID"));
		defaultParamNames.put("httpResp2", Utils.createArrayList("Status", "Content"));
		return defaultParamNames;
	}
}
