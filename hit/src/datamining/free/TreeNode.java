package datamining.free;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TreeNode {
	   private Tag tag;
       private TreeNode parent;
       private LinkedList<TreeNode> children;
       int height;
       private Condition condition;
       
       public TreeNode() {
    	   tag = new Tag();
    	   children = new LinkedList<TreeNode>();
    	   height = 0;
    	   condition = new Condition();
    	   parent = null;
       }
       
       public TreeNode(Tag t) {
    	   tag = t;
    	   children = new LinkedList<TreeNode>();
    	   height = 0;
    	   condition = new Condition();
    	   parent = null;
       }
       
       public Tag getTag() {
    	   return tag;
       }
       
       public void setTag(Tag t) {
    	   this.tag = t;
       }
       
       public static String toString(TreeNode n) {
    	   if (n == null)
    		   return "";
    	   
    	   n.calculHeight();
	       String str = "";
	       if (n.getHeight() == 0) {
	    	   Iterator<TreeNode> itr = n.children.iterator();
		       while(itr.hasNext()) {
		    	   str += TreeNode.toString(itr.next());
		       }
	       }
	       else {
		       if (n.children.size() == 0) {
		    	   str += "  --> ";
		    	   str += n.tag.getValue();
		       }
		       else {
		    	   str += "\n";
			       for(int i = 0; i < (n.getHeight()-1); i++)
			    	   	str += "| ";
			       str += n.tag.toString();
			       Iterator<TreeNode> itr = n.children.iterator();
			       while(itr.hasNext()) {
			    	   str += TreeNode.toString(itr.next());
			       }
		       }
	       }
	       return str;
       }
       
       public static void AddChildrenNode (TreeNode node, TreeNode child) {
    	   if (child != null) {
    		   node.children.add(child);
    		   child.parent = node;
    		   //child.setHeight(node.getHeight()+1);
    		   //child.calculHeight();
    	   }
       }
       
       public void calculHeight() {
    	   if (children.size() > 0) {
    		   Iterator<TreeNode> it = children.iterator();
    		   while (it.hasNext()) {
    			   TreeNode tmp = it.next();
    			   tmp.setHeight(this.height+1);
    			   tmp.calculHeight();
    		   }
    	   }
       }
       
       
       public void setHeight(int h) {
    	   height = h;
       }
       
       public int getHeight() {
    	   return height;
       }
       
       public static void AddChildren (TreeNode node, Tag t) {
       	TreeNode child = new TreeNode(t);
           child.children = new LinkedList<TreeNode>();
       	AddChildrenNode(node, child);
       }
       
       public static TreeNode getChild(TreeNode DT, String value) {
    	   Iterator<TreeNode> itr = DT.children.iterator();
    	   while (itr.hasNext()) {
    		   TreeNode tmp = itr.next();
    		   if (tmp.tag.getValue().equals(value)) {
    			   return tmp; 
    		   }
    	   }
    	   return (TreeNode)null;
       }
       
       public  LinkedList<TreeNode> getChildren() {
    	   return children;
       }
       
       public boolean isLeaf() {
    	   if (children.size() > 0)
    		   return false;
    	   return true;
       }
       
       public void removeChildren() {
    	   children.clear();
       }
       
      private static String Reduction(TreeNode DT) {
    	  String val_red = "";
    	  Stack<TreeNode> stack_DT = new Stack<TreeNode>();
    	  
    	  stack_DT.push(DT);
    	  while (!stack_DT.empty()) {
    		  TreeNode tmp = stack_DT.pop();
    		  if (tmp != null) {
    			  if (tmp.isLeaf() && val_red.equals("")) {
    				  val_red = tmp.getTag().getValue();
    			  }
    			  else if (tmp.isLeaf() && !val_red.equals(tmp.getTag().getValue())) {
    				  return "";
    			  }
    			  LinkedList<TreeNode> children = tmp.getChildren();
    			  Iterator<TreeNode> itr = children.iterator();
    			  while (itr.hasNext()) {
    				  stack_DT.push(itr.next());
    			  }
    		  }
    	  }
    	  return val_red;
      }
      
      public static TreeNode Optimize(TreeNode DT) {
    	  if (DT != null) {
    		  if (!DT.isLeaf()){
    			  String optimization = TreeNode.Reduction(DT);
    			  if (!optimization.equals("")) {
    				  DT.removeChildren();
    				  TreeNode child = new TreeNode (new Tag("leaf", optimization));
    				  TreeNode.AddChildrenNode(DT, child);
    			  }
    			  else {
    				  LinkedList<TreeNode> LL_children = DT.getChildren();
    				  Iterator<TreeNode> itr = LL_children.iterator();
    				  while (itr.hasNext()) {
    					  TreeNode tmp = itr.next();
    					  tmp = Optimize(tmp);
    				  }
    			  }
    		  }
    		  return DT;
    	  }
    	  else 
    		  return null;
      }
      
      public Condition getCondition () {
    	  return condition;
      }
      
      private void DecorTree () {
    	  if (parent != null ) {
    		  condition = new Condition(parent.getCondition());
    		  condition.add(tag);
    	  }
    	  Iterator<TreeNode> itr = children.iterator();
    	  while (itr.hasNext()) {
    		  itr.next().DecorTree();
    	  }
      }
      
      public static ClassificationResult Predict(TreeNode DT) {
    	  DT.DecorTree();
    	  ClassificationResult pred = new ClassificationResult();
    	  Stack<TreeNode> stack_DT = new Stack<TreeNode>();
    	  stack_DT.push(DT);
    	  while (!stack_DT.empty()) {
    		  TreeNode tmp = stack_DT.pop();
    		  if (tmp != null) {
    			  if (tmp.isLeaf()) {
    				  Condition cond = tmp.parent.getCondition();
    				  String value = tmp.getTag().getValue();
    				  if (!pred.containsKey(value)) {
    					  pred.put(value, new Prediction(cond, value));
    				  }
    				  else {
    					  pred.get(value).add(cond);
    				  }
    			  }
    			  else {
    				  Iterator<TreeNode> itr = tmp.getChildren().iterator();
    		    	  while (itr.hasNext()) {
    		    		  stack_DT.push(itr.next());
    		    	  }
    		    		   
    			  }
    		  }
    	  }
    	  return pred;
      }
      
      public List<String> getPredicatesFor(String leaf) {
    	  List<String> res = new ArrayList<String>();
    	  Prediction pred = Predict(this).get(leaf);
    	  if (pred != null) {
	    	  Iterator<Condition> itr = pred.iterator();
	    	  while (itr.hasNext()) {
	    		  Condition cond = itr.next();
	    		  res.add(cond.toString());
	    	  }
    	  }
    	  return res;
      }
      
      public int size() {
    	  int sum = 1;
    	  Iterator<TreeNode> itr = children.iterator();
    	  while(itr.hasNext()) {
    		  sum += itr.next().size();
    	  }
    	  return sum;
      }
}