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

/**
 *
 * @author guillem
 */

public class LRUCache extends MemoryCache{
    
    
    public LRUCache(){
        
        this(4);
        
    }
    
    public LRUCache(int blocks){
        
        this.memBlocks= blocks;
        this.numberMises = 0;
        
        
        
    }
   
    @Override
    boolean hitBlock(Integer block){
        
        this.CacheBlocks.remove(block);
        this.CacheBlocks.add(block);
        
        return true;
    }

    @Override
    boolean missBlock(Integer block) {
        
        if (this.CacheBlocks.size()<this.getNumBlock()){
            
            this.CacheBlocks.add(block);
        
        }else{
            
            this.CacheBlocks.remove(0);
            this.CacheBlocks.add(block);
        
        }
        
        this.numberMises++;
        return false;
    }

   
}
