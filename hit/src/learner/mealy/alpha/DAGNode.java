package learner.mealy.alpha;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DAGNode {
	public StateLabel label;
	public FSMConjecture conjecture;
	public List<DAGNode> successors;
	public DAGNode father;
	public List<StateLabel> localLabelling;
	
	public DAGNode(StateLabel label,DAGNode father){
		this.label=label;
		successors = new LinkedList<>();
		this.father = father;
		localLabelling = new ArrayList<>();
	}

}
