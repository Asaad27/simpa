package datamining.weka;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.trees.M5P;

public class WekaExpressionTreeNode {
	
	List<String> terms;
	
	public WekaExpressionTreeNode() {
		terms = new ArrayList<String>();
	}
	
	public void loadFromString(M5P tree){
		String[] lines = tree.toString().split("\n");
		int i = 0;
		while (!lines[i].equals("LM num: 1")) i++;
		i++;
		while (!lines[i].equals("")) terms.add(reduce(lines[++i].trim()));
		terms.remove(terms.size()-1);
	}
	
	private String reduce(String term) {
		if (term.startsWith("1 * ")) term = term.substring(4);
		if (term.equals("+ 0")) term = "";
		if (term.endsWith(",")) term = term.substring(0, term.length()-1);
		return term;
	}

	public String toString(){
		StringBuffer res = new StringBuffer();
		if (terms.size()>0) res.append(terms.get(0));	
		for(int i=1; i<terms.size(); i++) res.append(" " + terms.get(i));	
		return res.toString();
	}
}
