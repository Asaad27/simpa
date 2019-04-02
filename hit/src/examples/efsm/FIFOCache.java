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
