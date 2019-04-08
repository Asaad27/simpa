/********************************************************************************
 * Copyright (c) 2014,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.Utils;
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
	
	
	public ParameterizedOutput execute_implem(ParameterizedInput pi) {
		ParameterizedOutput po = null;
		if (!pi.isEpsilonSymbol()) {
			
			List<Parameter> p = new ArrayList<Parameter>();
			p.add(new Parameter(String.valueOf(cache.find(Integer.valueOf(pi.getParameterValue(0)))), Types.NOMINAL));
			
			if(cache.access(Integer.parseInt(pi.getParameterValue(0)))){
				po = new ParameterizedOutput("hit", p);
			}
			else{
				po = new ParameterizedOutput("miss", p);
			}
			
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
	public void reset_implem() {
		cache.reset();
	}
	
	
}
