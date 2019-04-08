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
 ********************************************************************************/
package examples.efsm;

import java.util.ArrayList;

/**
 *
 * @author guillem
 */

public class MRUCache extends MemoryCache{
    
    ArrayList<Boolean> state = new ArrayList<Boolean>();
    
    public MRUCache(){
        
        this(4);
        
    }
    
    public MRUCache(int blocks){
        
        this.memBlocks= blocks;
        this.numberMises = 0;
        
        for (int i = 0; i < this.memBlocks ; i++){ 
            
            state.add(false);
        
        }
        
        for (int i = 0; i < this.memBlocks ; i++){
            
            this.CacheBlocks.add(-1);
        }
        
    }
    
    private void allZeros(){
        
        int allElementsZeroFlag = 0;

        for(int i=0; i < this.memBlocks; i++){
        
            if(state.get(i)){
                
                allElementsZeroFlag++;
                
              }
        
        } 
    
        if(allElementsZeroFlag == this.memBlocks){
      
            for(int i=0;i<memBlocks;i++){
        
                state.set(i,false);
        
            }
            allElementsZeroFlag = 0;
                
        }
        
    }

    @Override
    boolean missBlock(Integer block) {
        
       for(int i=0;i<this.memBlocks;i++){     
     
           if(!state.get(i)){
           this.CacheBlocks.set(i, block);
           state.set(i,true);
           allZeros();
           break;
           }

        } 
        
       return false;
        
    }

    @Override
    boolean hitBlock(Integer block) {
        
        state.set(this.CacheBlocks.indexOf(block),true);
        
        allZeros();
        
        return true;
    }


}
