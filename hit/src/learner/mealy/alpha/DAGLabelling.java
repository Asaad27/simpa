package learner.mealy.alpha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DAGLabelling {
	public DAGNode root;
	
	public ArrayList<List<DAGNode>> mapDepthLabels;
	private Set<DAGNode> alreadyVisited ;
	
	public DAGLabelling(StateLabel label0){
		root = new DAGNode(label0,null);
		mapDepthLabels = new ArrayList<>();
		/*mapDepthLabels.add(0,  new LinkedList<DAGNode>());
		mapDepthLabels.get(0).add(root);
		*/
	}
	
	public StateLabel getLabelAt(int index){
		List<DAGNode> l = mapDepthLabels.get(index);
		if(l.size()==1){
			return l.get(0).label;
		}
		return null;
	}

	public void replaceAll(StateLabel old, StateLabel newl) {
		alreadyVisited = new HashSet<>();
		replaceAllRec(old,newl,root);
	}

	private void replaceAllRec(StateLabel old, StateLabel newl,DAGNode currentNode) {
		if(currentNode.label.equals(old)) currentNode.label = newl;
		alreadyVisited.add(currentNode);
		for(DAGNode node:currentNode.successors){
			if(!alreadyVisited.contains(node)){
				replaceAllRec(old,newl,node);
			}
		}
		
	}
	
	public String mapToString(){
		String res = "Labelling Map : {";
		for(List<DAGNode> list:mapDepthLabels){
			res += "[";
			for(DAGNode n : list){
				res += n.label.name+" ";
			}
			res += "];";
		}
		return res;
	}
	
	public void rebuildMapDepthLabels(){
		int depth =0;
		List<DAGNode> currentDepthQueue = new LinkedList<>();
		List<DAGNode> nextDepthQueue = new LinkedList<>();
		mapDepthLabels = new ArrayList<>();
		
		currentDepthQueue.addAll(root.successors);
		while(!currentDepthQueue.isEmpty()){
			mapDepthLabels.add(new LinkedList<DAGNode>());
			for(DAGNode node: currentDepthQueue){
				mapDepthLabels.get(depth).add(node);
				nextDepthQueue.addAll(node.successors);
			}
			currentDepthQueue.retainAll(new LinkedList<DAGNode>());
			currentDepthQueue.addAll(nextDepthQueue);
			nextDepthQueue.retainAll(new LinkedList<DAGNode>());
			depth++;
		}
		
		
		
		//TODO terminer cette methode
	}

}
