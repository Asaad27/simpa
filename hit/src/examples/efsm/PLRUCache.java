package examples.efsm;

import java.util.ArrayList;

/**
 *
 * @author guillem
 */
public class PLRUCache extends MemoryCache{
    
    ArrayList<Integer> tree = new ArrayList<Integer>();
    
    public PLRUCache(){
        
        this(4);
    }
    
    public PLRUCache(int blocks){
        
        this.memBlocks= blocks;
        this.numberMises = 0;
        
        for (int i = 0; i < (this.memBlocks-1) ; i++){ 
            
            tree.add(0);
        
        }
        
        for (int i = 0; i < this.memBlocks ; i++){
            
            this.CacheBlocks.add(-1);
        }
        
    }

    @Override
    boolean missBlock(Integer block) {
       
        int missPos = pointingPosition();
        this.CacheBlocks.set(missPos, block);
        avoidPosition(missPos);
        return false;
    }

    @Override
    boolean hitBlock(Integer block) {
        
        int hitPos = this.CacheBlocks.indexOf(block);
        
        avoidPosition(hitPos);
      
        return true;
    }
    
    void avoidPosition(int pos){
        
        int path = pos + (this.memBlocks-1);
        
        while (path > 0){
            
            int parent = (path - 1)/2;
            tree.set(parent, path%2);
            path = parent;
            
        }
        
    }
    
    
    int pointingPosition(){
        
        int targetPosition = 0;
        int path = 0;
        
        while (path < this.memBlocks-1){
            
            if(tree.get(path) == 0) path = 2*path + 1;
            else path = 2*path + 2;
            
        }
        
        targetPosition = path - (this.memBlocks-1);
        
        return targetPosition;
    }

    
}
