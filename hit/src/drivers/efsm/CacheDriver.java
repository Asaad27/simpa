package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;
import examples.efsm.FIFOCache;
import examples.efsm.MemoryCache;

public class CacheDriver extends EFSMDriver{
	
	MemoryCache cache;
	
	public CacheDriver(){
		super(null);
		this.cache = new FIFOCache(4);
	}
	

	@Override 
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		is.add("access");
		return is;
	}
	
	
	@Override
	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		os.add("hit");
		os.add("miss");
		return os;
		
	}
	
	
	public ParameterizedOutput execute(ParameterizedInput pi) {
		ParameterizedOutput po = null;
		if (!pi.isEpsilonSymbol()) {
			numberOfAtomicRequest++;
			
			List<Parameter> p = new ArrayList<Parameter>();
			p.add(new Parameter(String.valueOf(cache.find(Integer.valueOf(pi.getParameterValue(0)))), Types.NOMINAL));
			
			if(cache.access(Integer.parseInt(pi.getParameterValue(0)))){
				po = new ParameterizedOutput("hit", p);
			}
			else{
				po = new ParameterizedOutput("miss", p);
			}
			
			
			LogManager.logRequest(pi, po);
		}
		return po;
	}
	
	
	@Override
	public String getSystemName() {
		return cache.getClass().getSimpleName();
	}
	
	
	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		ArrayList<ArrayList<Parameter>> params = null;
		params = new ArrayList<ArrayList<Parameter>>();
		for(int i=0; i<cache.getNumBlock()+1; i++)
			params.add(Utils.createArrayList(new Parameter(String.valueOf(i+1), Types.NUMERIC)));		
		defaultParamValues.put("access", params);

		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("access",
				Utils.createArrayList("pAccess"));
		defaultParamNames.put("hit",
				Utils.createArrayList("pHit"));
		defaultParamNames.put("miss",
				Utils.createArrayList("pMiss"));
		return defaultParamNames;
	}

	@Override
	public void reset() {
		super.reset();
		cache.reset();
	}
	
	
}
