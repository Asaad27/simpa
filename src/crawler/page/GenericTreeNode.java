/********************************************************************************
 * Copyright (c) 2013,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 ********************************************************************************/
package crawler.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericTreeNode<T> {

    public T data;
    public List<GenericTreeNode<T>> children;
    public GenericTreeNode<T> parent;

    public GenericTreeNode() {
        super();
        children = new ArrayList<>();
        parent = null;
    }

    public GenericTreeNode(T data) {
        this();
        this.data = data;
    }
    
    public GenericTreeNode<T> getParent(){
    	return parent;
    }

    public List<GenericTreeNode<T>> getChildren() {
        return this.children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    public void setChildren(List<GenericTreeNode<T>> children) {
    	for (GenericTreeNode<T> n : children) n.parent = this;
        this.children = children;        
    }

    public void addChild(GenericTreeNode<T> child) {
    	child.parent = this;
        children.add(child);
    }

    public void addChildAt(int index, GenericTreeNode<T> child) throws IndexOutOfBoundsException {
    	child.parent = this;
        children.add(index, child);
    }

    public void removeChildren() {
        this.children = new ArrayList<>();
    }

    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    public GenericTreeNode<T> getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

	@Override
    public String toString() {
        return data.toString();
    }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GenericTreeNode<?> other = (GenericTreeNode<?>) obj;
		return Objects.equals(this.data, other.data);
	}

	@Override
    public int hashCode() {
        return data.hashCode();
    }

	private String space(int depth) {
    	String s = "";
		for (int i=0; i<depth*3; i++) s += " ";
		return s;
	}
    
    public String toStringFull() {
    	return toString(0);
    }

	public String toString(int i) {
        String s = data.toString() + "\n";
        for(GenericTreeNode<T> child : children){
        	s += space(i+1) + child.toString(i+1);
        }
        return s;
	}
}
