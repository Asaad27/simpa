package crawler.page;

import java.util.ArrayList;
import java.util.List;

public class GenericTreeNode<T> {

    public T data;
    public List<GenericTreeNode<T>> children;
    public GenericTreeNode<T> parent;

    public GenericTreeNode() {
        super();
        children = new ArrayList<GenericTreeNode<T>>();
        parent = null;
    }

    public GenericTreeNode(T data) {
        this();
        setData(data);
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
        this.children = new ArrayList<GenericTreeNode<T>>();
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

    public String toString() {
        return getData().toString();
    }

    public boolean equals(GenericTreeNode<T> node) {
        return node.getData().equals(getData());
    }

    public int hashCode() {
        return getData().hashCode();
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
