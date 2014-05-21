package examples.efsm;

/**
 *
 * @author guillem
 */
public class FIFOCache extends MemoryCache {
	int nextRem = 0;
    
    public FIFOCache(){
        
        this(4);
        
    }
    
    public FIFOCache(int blocks){
        
        this.memBlocks= blocks;
        this.numberMises = 0;
        this.nextRem = 0;
    }
    
    @Override
    boolean hitBlock(Integer block) {
        
        return true;
        
    }

    @Override
    boolean missBlock(Integer block) {
               
        if (this.CacheBlocks.size()<this.getNumBlock()){
            
            this.CacheBlocks.add(block);
        
        }else{
            
            this.CacheBlocks.remove(nextRem);
            this.CacheBlocks.add(nextRem++, block);
            nextRem = nextRem%memBlocks;
        
        }
        
        this.numberMises++;
        return false;
    }

  
}
