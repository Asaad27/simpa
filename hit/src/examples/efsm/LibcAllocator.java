package examples.efsm;

import java.util.ArrayList;
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
	
	public static int minSize = 1;
	public static int maxSize = 2;
	
	private Random rn;
	private ArrayList<ArrayList<Long>> allocated = new ArrayList<ArrayList<Long>>();
	private ArrayList<ArrayList<Long>> freed = new ArrayList<ArrayList<Long>>();
	
	public LibcAllocator() {
		rn = new Random();
		for (int i = LibcAllocator.minSize; i <= LibcAllocator.maxSize; i++) {
			this.allocated.add(new ArrayList<Long>());
			this.freed.add(new ArrayList<Long>());
		}
		return;
	}
	
	public long malloc(int size) {
		long addr;
		if (size < LibcAllocator.minSize || size > LibcAllocator.maxSize) {
			return -1;
		}
		size = size - LibcAllocator.minSize;
		if (this.freed.get(size).isEmpty()) {
			addr = (long) rn.nextInt();
			while (this.allocated.get(size).contains(addr)) {
				addr = (long) rn.nextInt();
			}
		} else {
			addr = this.freed.get(size).remove(0);
		}
		this.allocated.get(size).add(addr);
		return addr;
	}
	
	public int free(long addr) {
		for (int i = 0; i <= LibcAllocator.maxSize - LibcAllocator.minSize; i++) {
			System.out.println(i);
			if (this.allocated.get(i).contains(addr)) {
				this.freed.get(i).add(this.allocated.get(i).remove(this.allocated.get(i).indexOf(addr)));
				return 0;
			} 
		}
		return -1;
	}

	public void reset() {
		this.allocated.clear();
		this.freed.clear();
		for (int i = LibcAllocator.minSize; i <= LibcAllocator.maxSize; i++) {
			this.allocated.add(new ArrayList<Long>());
			this.freed.add(new ArrayList<Long>());
		}
		System.out.println(this.allocated);
		return;
	}
}


