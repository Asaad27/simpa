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