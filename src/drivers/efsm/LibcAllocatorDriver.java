/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Franck DE GOËR
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import tools.Utils;
import automata.Automata;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import examples.efsm.LibcAllocator;

public class LibcAllocatorDriver extends EFSMDriver {
	
	
	public static String MALLOC = "MALLOC";
	public static String MALLOC_ALL = "MALLOC_ALL";
	public static String MALLOC_HALF = "MALLOC_HALF";
	public static String MALLOC_QUARTER = "MALLOC_QUARTER";
	public static String FREE = "FREE";
	public static String FREE_ALL = "FREE_ALL";
	public static String FREE_HALF = "FREE_HALF";
	public static String FREE_QUARTER = "FREE_QUARTER";
	public static String REALLOC = "REALLOC";
	
	public static boolean WITH_REALLOC = false;
	public static boolean PARAMETERIZED = true;
	
	private LibcAllocator allocator;
	
	
	public LibcAllocatorDriver() {
		super(null);
		this.allocator = new LibcAllocator(PARAMETERIZED);
	}
	
	
	@Override 
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		if (PARAMETERIZED) {
			is.add(MALLOC);
			is.add(FREE);
		} else {
			is.add(MALLOC_ALL);
			is.add(MALLOC_HALF);
			is.add(MALLOC_QUARTER);
			is.add(FREE_ALL);
			is.add(FREE_HALF);
			is.add(FREE_QUARTER);
		}		
	
		if (WITH_REALLOC)
			is.add(LibcAllocatorDriver.REALLOC);
		return is;
	}
	
	
	@Override
	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		os.add("address_out");
		os.add("freed");
		if (!PARAMETERIZED) {
			os.add("mem_leak");
			os.add("all_freed");
		}
		
		if (WITH_REALLOC)
			os.add("realloc_error");
		
		os.add("malloc_error");
		os.add("free_error");
		os.add("size_error");
		os.add("mem_full");
		return os;
		
	}
	
	public ParameterizedOutput execute_implem(ParameterizedInput pi) {
		ParameterizedOutput po = null;
		if (!pi.isEpsilonSymbol()) {
			List<Parameter> p = new ArrayList<Parameter>();
			
			if (pi.getInputSymbol() == FREE) {
				int free_result;
				try {
					free_result = this.allocator.free(Long.valueOf(pi.getParameterValue(0)));
				} catch (NumberFormatException e) {
					System.out.println("[DRIVER] Parsing error with " + pi.getParameterValue(0));
					free_result = LibcAllocator.FREE_ERROR;
				}
				if (free_result == LibcAllocator.ALL_FREED) {
					p.add(new Parameter("ALL_FREED", Types.NOMINAL));
					po = new ParameterizedOutput("all_freed", p);
				} else if (free_result == LibcAllocator.MEM_LEAK) {
					p.add(new Parameter("MEM_LEAK", Types.NOMINAL));
					po = new ParameterizedOutput("mem_leak", p);
				} else if (free_result == LibcAllocator.FREED) {
					p.add(new Parameter("FREED", Types.NOMINAL));
					po = new ParameterizedOutput("freed", p);
				} else if (free_result == LibcAllocator.FREE_ERROR) {
					p.add(new Parameter("FREE_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("free_error", p);
				} else {
					System.out.println(free_result);
					p.add(new Parameter(String.valueOf(free_result), Types.NOMINAL));
					po = new ParameterizedOutput("freed", p);				
				}
			} else if (pi.getInputSymbol() == FREE_ALL || pi.getInputSymbol() == FREE_HALF || pi.getInputSymbol() == FREE_QUARTER) {
				int size = this.allocator.memSize;
				if (pi.getInputSymbol() == FREE_QUARTER) {
					size = size/4;
				} else if (pi.getInputSymbol() == FREE_HALF) {
					size = size/2;
				}
				int free_result = this.allocator.freeBySize(size);
				if (free_result == LibcAllocator.ALL_FREED) {
					p.add(new Parameter("ALL_FREED", Types.NOMINAL));
					po = new ParameterizedOutput("all_freed", p);
				} else if (free_result == LibcAllocator.MEM_LEAK) {
					p.add(new Parameter("MEM_LEAK", Types.NOMINAL));
					po = new ParameterizedOutput("mem_leak", p);
				} else if (free_result == LibcAllocator.FREED) {
					p.add(new Parameter("FREED", Types.NOMINAL));
					po = new ParameterizedOutput("freed", p);
				} else {
					p.add(new Parameter("FREE_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("free_error", p);
				}
			} else if (pi.getInputSymbol() == MALLOC) {
				long addr;
				try {
					addr = this.allocator.malloc(Integer.valueOf(pi.getParameterValue(0)));
				} catch (NumberFormatException e) {
					addr = this.allocator.ret_code(LibcAllocator.MALLOC, LibcAllocator.SIZE_ERROR);
				}
				if (addr == LibcAllocator.MALLOC_ERROR) {
					p.add(new Parameter("MALLOC_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("malloc_error", p);
				} else if (addr == LibcAllocator.MEM_FULL) {
					p.add(new Parameter("MEM_FULL", Types.NOMINAL));
					po = new ParameterizedOutput("mem_full", p);
				} else if (addr == LibcAllocator.SIZE_ERROR) {
					p.add(new Parameter("SIZE_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("size_error", p);
				} else {
					p.add(new Parameter(String.valueOf(addr), Types.NOMINAL));
					po = new ParameterizedOutput("address_out", p);
				}
			} else if (pi.getInputSymbol() == MALLOC_ALL || pi.getInputSymbol() == MALLOC_HALF || pi.getInputSymbol() == MALLOC_QUARTER) {
				int size = this.allocator.memSize;
				if (pi.getInputSymbol() == MALLOC_QUARTER) {
					size = size/4;
				} else if (pi.getInputSymbol() == MALLOC_HALF) {
					size = size/2;
				}
				long addr;
				addr = this.allocator.malloc(size);
				if (addr == LibcAllocator.MALLOC_ERROR) {
					p.add(new Parameter("MALLOC_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("malloc_error", p);
				} else if (addr == LibcAllocator.MEM_FULL) {
					p.add(new Parameter("MEM_FULL", Types.NOMINAL));
					po = new ParameterizedOutput("mem_full", p);
				} else if (addr == LibcAllocator.SIZE_ERROR) {
					p.add(new Parameter("SIZE_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("size_error", p);
				} else {
					p.add(new Parameter(String.valueOf(addr), Types.NOMINAL));
					po = new ParameterizedOutput("address_out", p);
				}
			} else {
				long addr;
				try {
					addr = this.allocator.realloc(Long.valueOf(pi.getParameterValue(0)), Integer.valueOf(pi.getParameterValue(1)));
				} catch (NumberFormatException e) {
					addr = this.allocator.ret_code(LibcAllocator.REALLOC, LibcAllocator.SIZE_ERROR);
				}
				if (addr == LibcAllocator.REALLOC_ERROR) {
					p.add(new Parameter("REALLOC_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("realloc_error", p);
				} else if (addr == LibcAllocator.MEM_FULL) {
					p.add(new Parameter("MEM_FULL", Types.NOMINAL));
					po = new ParameterizedOutput("mem_full", p);
				} else if (addr == LibcAllocator.SIZE_ERROR) {
					p.add(new Parameter("SIZE_ERROR", Types.NOMINAL));
					po = new ParameterizedOutput("size_error", p);
				} else {
					p.add(new Parameter(String.valueOf(addr), Types.NOMINAL));
					po = new ParameterizedOutput("address_out", p);
				}
			}
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
		
		Random rn = new Random();
		
		if (PARAMETERIZED) {
			ArrayList<String> size_values = new ArrayList<String>();
			size_values.add("500");
			size_values.add("1000");
			// size_values.add("100");
			
			ArrayList<ArrayList<Parameter>> malloc_params = new ArrayList<ArrayList<Parameter>>();
			Iterator<String> size_it = size_values.iterator();
			while ( size_it.hasNext() ) {
				malloc_params.add(Utils.createArrayList(new Parameter(size_it.next(), Types.NOMINAL)));	
			}
			defaultParamValues.put(LibcAllocatorDriver.MALLOC, malloc_params);
			
			ArrayList<ArrayList<Parameter>> addr_params = new ArrayList<ArrayList<Parameter>>();
			for(int i=0; i<2; i++)
				addr_params.add(Utils.createArrayList(new Parameter(String.valueOf(rn.nextInt()), Types.NOMINAL)));		
			defaultParamValues.put(LibcAllocatorDriver.FREE, addr_params);
			
			if (WITH_REALLOC) {
				ArrayList<ArrayList<Parameter>> realloc_params = new ArrayList<ArrayList<Parameter>>();
				size_it = size_values.iterator();
				int addr = rn.nextInt();
				while ( size_it.hasNext() ) {
					realloc_params.add(Utils.createArrayList(new Parameter(String.valueOf(addr), Types.NOMINAL), new Parameter(size_it.next(), Types.NOMINAL)));	
				}
				defaultParamValues.put(LibcAllocatorDriver.REALLOC, realloc_params);
			}
		} else {
			
			ArrayList<ArrayList<Parameter>> malloc_params = new ArrayList<ArrayList<Parameter>>();
			malloc_params.add(Utils.createArrayList(new Parameter("CAFE", Types.NOMINAL)));	
			defaultParamValues.put(LibcAllocatorDriver.MALLOC_ALL, malloc_params);
			defaultParamValues.put(LibcAllocatorDriver.MALLOC_HALF, malloc_params);
			defaultParamValues.put(LibcAllocatorDriver.MALLOC_QUARTER, malloc_params);
			
			ArrayList<ArrayList<Parameter>> free_params = new ArrayList<ArrayList<Parameter>>();
			free_params.add(Utils.createArrayList(new Parameter("CAFE", Types.NOMINAL)));
			free_params.add(Utils.createArrayList(new Parameter("TEA", Types.NOMINAL)));
			defaultParamValues.put(FREE_ALL, free_params);
			defaultParamValues.put(FREE_HALF, free_params);
			defaultParamValues.put(FREE_QUARTER, free_params);
		}
		
		return defaultParamValues;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		if (PARAMETERIZED) {
			defaultParamNames.put(MALLOC,
					Utils.createArrayList("pMalloc"));
			defaultParamNames.put(FREE,
					Utils.createArrayList("pFree"));
		} else {
			defaultParamNames.put(MALLOC_ALL,
					Utils.createArrayList("pMallocAll"));
			defaultParamNames.put(MALLOC_HALF,
					Utils.createArrayList("pMallocHalf"));
			defaultParamNames.put(MALLOC_QUARTER,
					Utils.createArrayList("pMallocQuarter"));
			defaultParamNames.put(FREE_ALL,
					Utils.createArrayList("pFreeAll"));
			defaultParamNames.put(FREE_HALF,
					Utils.createArrayList("pFreeHalf"));
			defaultParamNames.put(FREE_QUARTER,
					Utils.createArrayList("pFreeQuarter"));
			defaultParamNames.put("all_freed",
					Utils.createArrayList("pAllFreed"));
			defaultParamNames.put("mem_leak",
					Utils.createArrayList("pMemLeak"));
		}
		
		if (WITH_REALLOC) {
			defaultParamNames.put(REALLOC,
					Utils.createArrayList("pRealloc"));
			defaultParamNames.put("realloc_error",
					Utils.createArrayList("pReallocError"));
		}
		
		defaultParamNames.put("freed",
				Utils.createArrayList("pFreed"));
		defaultParamNames.put("malloc_error",
				Utils.createArrayList("pMallocError"));
		defaultParamNames.put("free_error",
				Utils.createArrayList("pFreeError"));
		defaultParamNames.put("address_out",
				Utils.createArrayList("pAddress_out"));
		defaultParamNames.put("size_error",
				Utils.createArrayList("pSizeError"));
		defaultParamNames.put("mem_full",
				Utils.createArrayList("pMemFull"));
				
		return defaultParamNames;
	}

	private boolean counterex = false;
	
	@Override
	public ParameterizedInputSequence getCounterExample(Automata a) {
		if (!this.counterex) {
			this.counterex = true;
			ParameterizedInputSequence pis = new ParameterizedInputSequence();
			ArrayList<Parameter> malloc_param = new ArrayList<Parameter>();
			malloc_param.add(new Parameter("500", Types.NOMINAL));
			ParameterizedInput pi_malloc = new ParameterizedInput(LibcAllocatorDriver.MALLOC, malloc_param);
			ArrayList<Parameter> free_param = new ArrayList<Parameter>();
			free_param.add(new Parameter("CAFE", Types.NOMINAL));
			// free_param.add(new Parameter("1000", Types.NOMINAL));
			ParameterizedInput pi_free = new ParameterizedInput(LibcAllocatorDriver.FREE, free_param);
			pis.addParameterizedInput(pi_malloc);
			pis.addParameterizedInput(pi_malloc);
			pis.addParameterizedInput(pi_free);
			pis.addParameterizedInput(pi_free);
			

			
			return pis;
			/*
			malloc_param.add(new Parameter("500", Types.NOMINAL));	
			ParameterizedInput pi_malloc = new ParameterizedInput(LibcAllocatorDriver.MALLOC, malloc_param);
			ArrayList<Parameter> free_param = new ArrayList<Parameter>();
			free_param.add(new Parameter("0", Types.NOMINAL));	
			pis.addParameterizedInput(pi_malloc);
			pis.addParameterizedInput(pi_malloc);
			pis.addParameterizedInput(pi_malloc);
			//pis.addParameterizedInput(pi_free);
			pis.addParameterizedInput(pi_malloc);
			pis.addParameterizedInput(pi_malloc);
			
			return pis;
			*/
		} else {
			return null;
		}
	}

	@Override
	public boolean isCounterExample(Object ce, Object conjecture) {
		return false;
	}
	
	@Override
	public void reset_implem() {
		this.allocator.reset();
	}

}
