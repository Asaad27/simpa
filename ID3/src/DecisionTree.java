import java.util.LinkedList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

public class DecisionTree {
	   private Tag tag;
       private DecisionTree parent;
       private LinkedList<DecisionTree> children;
       int height;
       private Condition condition;
       
       public DecisionTree() {
    	   tag = new Tag();
    	   children = new LinkedList<DecisionTree>();
    	   height = 0;
    	   condition = new Condition();
    	   parent = null;
       }
       
       public DecisionTree(Tag t) {
    	   tag = t;
    	   children = new LinkedList<DecisionTree>();
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
       
       public static String toString(DecisionTree n) {
    	   if (n == null)
    		   return new String("");
    	   
    	   n.calculHeight();
	       String str = new String("");
	       if (n.getHeight() == 0) {
	    	   Iterator<DecisionTree> itr = n.children.iterator();
		       while(itr.hasNext()) {
		    	   str += DecisionTree.toString(itr.next());
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
			       Iterator<DecisionTree> itr = n.children.iterator();
			       while(itr.hasNext()) {
			    	   str += DecisionTree.toString(itr.next());
			       }
		       }
	       }
	       return str;
       }
       
       public static void AddChildrenNode (DecisionTree node, DecisionTree child) {
    	   if (child != null) {
    		   node.children.add(child);
    		   child.parent = node;
    		   //child.setHeight(node.getHeight()+1);
    		   //child.calculHeight();
    	   }
       }
       
       public void calculHeight() {
    	   if (children.size() > 0) {
    		   Iterator<DecisionTree> it = children.iterator();
    		   while (it.hasNext()) {
    			   DecisionTree tmp = it.next();
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
       
       public static void AddChildren (DecisionTree node, Tag t) {
       	DecisionTree child = new DecisionTree(t);
           child.children = new LinkedList<DecisionTree>();
       	AddChildrenNode(node, child);
       }
       
       public static DecisionTree getChild(DecisionTree DT, String value) {
    	   Iterator<DecisionTree> itr = DT.children.iterator();
    	   while (itr.hasNext()) {
    		   DecisionTree tmp = itr.next();
    		   if (tmp.tag.getValue().equals(value)) {
    			   return tmp; 
    		   }
    	   }
    	   return (DecisionTree)null;
       }
       
       public  LinkedList<DecisionTree> getChildren() {
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
       
      private static String Reduction(DecisionTree DT) {
    	  String val_red = new String("");
    	  Stack<DecisionTree> stack_DT = new Stack<DecisionTree>();
    	  
    	  stack_DT.push(DT);
    	  while (!stack_DT.empty()) {
    		  DecisionTree tmp = stack_DT.pop();
    		  if (tmp != null) {
    			  if (tmp.isLeaf() && val_red.equals("")) {
    				  val_red = tmp.getTag().getValue();
    			  }
    			  else if (tmp.isLeaf() && !val_red.equals(tmp.getTag().getValue())) {
    				  return new String("");
    			  }
    			  LinkedList<DecisionTree> children = tmp.getChildren();
    			  Iterator<DecisionTree> itr = children.iterator();
    			  while (itr.hasNext()) {
    				  stack_DT.push(itr.next());
    			  }
    		  }
    	  }
    	  return val_red;
      }
      
      public static DecisionTree Optimize(DecisionTree DT) {
    	  if (DT != null) {
    		  if (!DT.isLeaf()){
    			  String optimization = DecisionTree.Reduction(DT);
    			  if (!optimization.equals("")) {
    				  DT.removeChildren();
    				  DecisionTree child = new DecisionTree (new Tag("leaf", optimization));
    				  DecisionTree.AddChildrenNode(DT, child);
    			  }
    			  else {
    				  LinkedList<DecisionTree> LL_children = DT.getChildren();
    				  Iterator<DecisionTree> itr = LL_children.iterator();
    				  while (itr.hasNext()) {
    					  DecisionTree tmp = itr.next();
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
    	  Iterator<DecisionTree> itr = children.iterator();
    	  while (itr.hasNext()) {
    		  itr.next().DecorTree();
    	  }
      }
      
      public static ClassificationResult Predict(DecisionTree DT) {
    	  DT.DecorTree();
    	  ClassificationResult pred = new ClassificationResult();
    	  Stack<DecisionTree> stack_DT = new Stack<DecisionTree>();
    	  stack_DT.push(DT);
    	  while (!stack_DT.empty()) {
    		  DecisionTree tmp = stack_DT.pop();
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
    				  Iterator<DecisionTree> itr = tmp.getChildren().iterator();
    		    	  while (itr.hasNext()) {
    		    		  stack_DT.push(itr.next());
    		    	  }
    		    		   
    			  }
    		  }
    	  }
    	  return pred;
      }
}