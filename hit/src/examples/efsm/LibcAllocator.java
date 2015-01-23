package examples.efsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LibcAllocator {
/*
	static { System.loadLibrary("c_alloc"); }
    public native long malloc(int size);
    public native void free(long addr);
    
	public static void main(String[] args) {
        LibcAllocator a = new LibcAllocator(); 
        int i; 
        long mem;
        
        for (i=0; i < 1000; i++) {
        	mem = a.malloc(100);
        	System.out.println(mem);
        	a.free(mem);
        }
        
    }
*/
	
	public static int MALLOC = 0;
	public static int FREE = 1;
	public static int REALLOC = 2;
	
	public static int MALLOC_ERROR = -2;
	public static int FREE_ERROR = -3;
	public static int REALLOC_ERROR = -4;
	public static int FREED = -5;
	public static int MEM_LEAK = -6;
	public static int ALL_FREED = -7;
	public static int MEM_FULL = -8;
	public static int SIZE_ERROR = -9;
	public static int GENERIC_ERROR = -99;
	
	private boolean PARAMETERIZED = false;
	private boolean SPECIFIC_RETURN_CODES = false;
	
	public int memSize = 1000;
	private int memUsed = 0;
	private ArrayList<Integer> blockSizes = new ArrayList<Integer>();
	private Random rn;
	private Map<Integer, ArrayList<Long>> allocated = new HashMap<Integer, ArrayList<Long>>();
	private Map<Integer, ArrayList<Long>> freed = new HashMap<Integer, ArrayList<Long>>();
	
	public LibcAllocator(boolean parameterized) {
		rn = new Random();
		this.PARAMETERIZED = parameterized;
		this.blockSizes.add(this.memSize);
		this.blockSizes.add(this.memSize/2);
		this.blockSizes.add(this.memSize/10);
		return;
	}
	
	public long malloc(int size) {
		long addr;
		
		if (!this.blockSizes.contains(size)) {
			return this.ret_code(MALLOC, SIZE_ERROR);
		}
		
		if (size > (this.memSize - this.memUsed)) {
			return this.ret_code(MALLOC, MEM_FULL);
		}
		
		if (!this.freed.containsKey(size) || this.freed.get(size).isEmpty()) {
			boolean valid_addr = false;
			addr = 0;
			while (!valid_addr) {
				valid_addr = true;
				addr = (long) rn.nextInt();
				for (ArrayList<Long> list : this.allocated.values()) {
					if (list.contains(addr)) {
						valid_addr = false;
						break;
					}
				}
			}
		} else {
			addr = this.freed.get(size).remove(0);
		}
		if (!this.allocated.containsKey(size)) {
			this.allocated.put(size, new ArrayList<Long>());
		}
		this.allocated.get(size).add(addr);
		this.memUsed += size;
		// System.out.println("[ALLOCATOR] Allocate " + addr);
		return addr;
	}
	
	public long realloc(long addr, int new_size) {
		if (!this.blockSizes.contains(new_size)) {
			return this.ret_code(REALLOC, SIZE_ERROR);
		}
		int old_size = -1;
		/* Looking for the block previous allocation size */
		for (Integer it_size : this.allocated.keySet()) {
			if (this.allocated.get(it_size).contains(addr)) {
				old_size = it_size;
				break;
			}
		}
		if (old_size == -1) {
			return this.ret_code(REALLOC, REALLOC_ERROR);
		}
		/* If there is not enough memory left to allocate the new size */
		if (this.memUsed + (new_size - old_size) > this.memSize) {
			/* Return an explicit error */
			return this.ret_code(REALLOC, MEM_FULL);
		}
		/* If the old size is the same as the new one */
		if (old_size == new_size) {
			/* We return the same block */
			return addr;
		}
		
		/* Otherwise, we free the old block */
		if (free(addr) == FREE_ERROR) {
			return this.ret_code(REALLOC, REALLOC_ERROR);
		/* and we allocate another one */ 
		} else {
			return malloc(new_size);
		}
	}
	
	public int freeBySize(int size) {
		if (!this.blockSizes.contains(size)) {
			return FREE_ERROR;
		}
		try {
			if (this.allocated.get(size).size() > 0) {
				if (!this.freed.containsKey(size)) {
					this.freed.put(size, new ArrayList<Long>());
				}
				this.freed.get(size).add(this.allocated.get(size).remove(0));
				this.memUsed -= size;
				if (this.memUsed > 0) {
					return this.ret_code(FREE, MEM_LEAK);
				} else {
					return this.ret_code(FREE, ALL_FREED);
				}
			} 
		} catch (NullPointerException e) {
			return FREE_ERROR;			
		}
		return FREE_ERROR;
	}
	
	public int free(long addr) {
		// System.out.println("[ALLOCATOR] Free " + addr);
		for (int size : this.allocated.keySet()) {
			if (this.allocated.get(size).contains(addr)) {
				if (!this.freed.containsKey(size)) {
					this.freed.put(size, new ArrayList<Long>());
				}
				this.freed.get(size).add(this.allocated.get(size).remove(this.allocated.get(size).indexOf(addr)));
				this.memUsed -= size;
				if (this.memUsed > 0) {
					return this.ret_code(FREE, MEM_LEAK);
				} else {
					return this.ret_code(FREE, ALL_FREED);
				}
			} 
		}
		return FREE_ERROR;
	}

	public int ret_code(int fn, int retcode) {
		if (this.PARAMETERIZED && fn == FREE && (retcode == ALL_FREED || retcode == MEM_LEAK)) {
			return memSize - memUsed;
		}
		if (this.SPECIFIC_RETURN_CODES) return retcode;
		if (fn == MALLOC) return MALLOC_ERROR;
		else if (fn == REALLOC) return REALLOC_ERROR;
		else if (fn == FREE) return FREED;
		return GENERIC_ERROR;
	}
	
	public void reset() {
		System.out.println("[ALLOCATOR] RESET");
		this.allocated.clear();
		this.freed.clear();
		this.memUsed = 0;
		return;
	}
}


