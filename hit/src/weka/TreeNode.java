package weka;

import java.util.ArrayList;
import java.util.List;

import main.Options;
import weka.classifiers.Classifier;

public class TreeNode {
	private String[] lines;
	private int currLine = 0;
	
	List<TreeNode> child = null;
	String data = null;
	String condition = null;
	
	public TreeNode() {
		child = new ArrayList<TreeNode>();
	}
	
	public TreeNode(String data, String condition) {
		this.child = new ArrayList<TreeNode>();
		this.data = data;
		this.condition = condition;
	}
	
	private int nodeLevel(){
		if (lines[currLine].length()==0) return -1;
		else{
			int l = 0;
			while (lines[currLine].substring(l*4).startsWith("|   ")) l++;
			return l;
		}
	}
	
	private TreeNode readNode(int level) {
		String line = lines[currLine].substring(4*level);				
		String cond = null, data = null;
		if (line.indexOf(": ") != -1) cond = line.substring(0, line.indexOf(": ")).trim();
		else cond = line.trim();
		if (cond.startsWith("rel")) cond = cond.substring(cond.indexOf("=")+2);
		if (line.indexOf(": ") == -1) data = null;
		else data = line.substring(line.indexOf(": ")+2, line.indexOf("(", line.indexOf(": ")+2)).trim();
		
		TreeNode n = new TreeNode(data, cond);
		currLine++;		
		while (nodeLevel() == level+1){
			n.child.add(readNode(level+1));			
		}		
		return n;
	}
	
	public void loadFromString(Classifier cl){
		lines = cl.toString().split("\n");	
			while (!lines[currLine].isEmpty()) currLine++;
			currLine++;
			if (currLine == 4){
				data = lines[2].substring(lines[2].indexOf(": ")+2, lines[2].indexOf("(", lines[2].indexOf(": ")+2)).trim();
				condition = "(true)";
			}else{
				while (nodeLevel() == 0){
					child.add(readNode(0));			
				}
			}
	}

	private List<String> getPredicatesFor_Rec(String leaf, String c, List<String> pred){
		if (leaf.equals(data)) pred.add(c+ "(" + condition + ")");
		else{
			if (condition != null){
				for(TreeNode ntn : child){
					ntn.getPredicatesFor_Rec(leaf, c + "(" + condition + ") "+ Options.SYMBOL_AND + " ", pred);
				}
			}else{
				for(TreeNode ntn : child){
					ntn.getPredicatesFor_Rec(leaf, c, pred);
				}
			}
		}
		return pred;
	}
	
	public List<String> getPredicatesFor(String leaf){
		List<String> preds = new ArrayList<String>();
		getPredicatesFor_Rec(leaf, "", preds);
		return preds;
	}
	
	public String toString(int level){
		StringBuffer res = new StringBuffer();
		for (int i=0; i<level; i++) res.append("   ");
		if (condition == null){
			for(TreeNode ntn : child){
				res.append(ntn.toString(level));				
			}
		}else{
			res.append(condition);
			if (data != null) res.append(" : " + data);
			res.append("\n");
			for(TreeNode ntn : child){
				res.append(ntn.toString(level+1));				
			}
		}
		return res.toString();
	}
	
	public String toString(){
		return toString(0);
	}
}
