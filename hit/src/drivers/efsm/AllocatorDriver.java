package drivers.efsm;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;
import examples.efsm.LibcAllocator;

public class AllocatorDriver extends EFSMDriver {
	
	
	public static String MALLOC = "MALLOC";
	public static String FREE = "FREE";
	
	private LibcAllocator allocator;
	
	
	public AllocatorDriver() {
		super(null);
		this.allocator = new LibcAllocator();
	}
	
	
	@Override 
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		is.add(AllocatorDriver.MALLOC);
		is.add(AllocatorDriver.FREE);
		return is;
	}
	
	
	@Override
	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		os.add("address_out");
		os.add("freed");
		os.add("malloc_error");
		os.add("free_error");
		return os;
		
	}
	
	public ParameterizedOutput execute(ParameterizedInput pi) {
		ParameterizedOutput po = null;
	
		if (!pi.isEpsilonSymbol()) {
			numberOfAtomicRequest++;
			
			List<Parameter> p = new ArrayList<Parameter>();
			
			if (pi.getInputSymbol().equals(AllocatorDriver.FREE)) {
				int free_result;
				try {
					free_result = this.allocator.free(Long.valueOf(pi.getParameterValue(0)));
				} catch (NumberFormatException e) {
					free_result = -1;
				}
				if (free_result == 0) {
					p.add(new Parameter(String.valueOf(free_result), Types.NOMINAL));
					po = new ParameterizedOutput("freed", p);
				} else {
					p.add(new Parameter("ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("free_error", p);
				}
			} else {
				long addr;
				try {
					addr = this.allocator.malloc(Integer.valueOf(pi.getParameterValue(0)));
				} catch (NumberFormatException e) {
					addr = -1;
				}
				if (addr != -1) {
					p.add(new Parameter(String.valueOf(addr), Types.NOMINAL));
					po = new ParameterizedOutput("address_out", p);
				} else {
					p.add(new Parameter("ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("malloc_error", p);
				}
			}
			LogManager.logRequest(pi, po);
		}

		return po;
	}
	
	
	@Override
	public String getSystemName() {
		return this.allocator.getClass().getSimpleName();
	}
	
	
	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		
		/*
		ArrayList<ArrayList<Parameter>> req_params =  new ArrayList<ArrayList<Parameter>>();
		req_params.add(Utils.createArrayList(new Parameter(AllocatorDriver.FREE, Types.STRING)));
		req_params.add(Utils.createArrayList(new Parameter(AllocatorDriver.MALLOC, Types.STRING)));
		defaultParamValues.put("request", req_params);
		*/
		
		ArrayList<ArrayList<Parameter>> addr_params = new ArrayList<ArrayList<Parameter>>();
		Random rn = new SecureRandom();
		for(int i=0; i<1; i++)
			addr_params.add(Utils.createArrayList(new Parameter(String.valueOf(rn.nextInt()), Types.NOMINAL)));		
		defaultParamValues.put(AllocatorDriver.FREE, addr_params);
		
		ArrayList<ArrayList<Parameter>> size_params = new ArrayList<ArrayList<Parameter>>();
		size_params.add(Utils.createArrayList(new Parameter("1", Types.NOMINAL)));	
		size_params.add(Utils.createArrayList(new Parameter("2", Types.NOMINAL)));	
		defaultParamValues.put(AllocatorDriver.MALLOC, size_params);
		
		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put(AllocatorDriver.MALLOC,
				Utils.createArrayList("pMalloc"));
		defaultParamNames.put(AllocatorDriver.FREE,
				Utils.createArrayList("pFree"));
		defaultParamNames.put("freed",
				Utils.createArrayList("pFreed"));
		defaultParamNames.put("malloc_error",
				Utils.createArrayList("pMallocError"));
		defaultParamNames.put("free_error",
				Utils.createArrayList("pFreeError"));
		defaultParamNames.put("address_out",
				Utils.createArrayList("pAddress_out"));
		return defaultParamNames;
	}

	@Override
	public void reset() {
		super.reset();
		this.allocator.reset();
	}

}
