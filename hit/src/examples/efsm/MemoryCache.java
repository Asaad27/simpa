package examples.efsm;

import java.util.ArrayList;

/**
 *
 * @author guillem
 */
public abstract class MemoryCache {
    
    protected int memBlocks;
    protected int numberMises;
    ArrayList<Integer> CacheBlocks = new ArrayList<Integer>();
   
    public boolean setNumBlock(int numBlock){
    
        memBlocks = numBlock;
        return true;
    
    }
    
    public int getNumBlock() {
        
        return memBlocks;
        
        }
    
    public int getNumMisses(){
    
        return numberMises;
        
    }
    
    public void resetMises(){
        
        numberMises = 0;
    
    }
    
    abstract boolean missBlock(Integer block);
    
    abstract boolean hitBlock(Integer block);
    
    public int find(Integer block){
    	return CacheBlocks.indexOf(block);
    }
    
    public boolean access(Integer block){
        
        
        if (CacheBlocks.contains(block)) {
            
            return hitBlock(block);
            
        }else{
            
            numberMises++;
            return missBlock(block);
           
        }
     

    }
    
    public void reset(){
    	CacheBlocks.clear();
    	numberMises = 0;
    }
    
    
}