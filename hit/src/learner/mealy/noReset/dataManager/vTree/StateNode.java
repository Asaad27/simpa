package learner.mealy.noReset.dataManager.vTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import learner.mealy.noReset.dataManager.OptimizedDataManager;
import learner.mealy.noReset.dataManager.FullyKnownTrace;
import learner.mealy.noReset.dataManager.FullyQualifiedState;

public class StateNode extends AbstractNode {
	private FullyQualifiedState state;
	private List<AbstractNode> fathers;

	public StateNode(FullyQualifiedState s) {
		state = s;
		fathers = new ArrayList<>();
	}

	@Override
	public List<AbstractNode> getParents() {
		return fathers;
	}

	@Override
	public boolean isStateNode() {
		return true;
	}

	@Override
	protected String nodeToDotOptions() {
		return "label=\"" + state.getState().getName() + "\n" + desc.replace("\n", "\\n") + "\"";
	}

	public void addFullyKnownTrace(FullyKnownTrace v) {
		AbstractNode previous = null;
		AbstractNode current = this;
		int currentPos = 0;
		String input = null;
		while (currentPos < v.getTrace().size()) {
			previous = current;
			input = v.getTrace().getInput(currentPos);
			current = current.getChildOrCreate(input, v.getTrace().getOutput(currentPos));
			currentPos++;
		}
		previous.changeChild(input, v.getEnd().getVNode());
	}

	@Override
	protected void changeFatherLink(AbstractNode old, AbstractNode newFather) {
		fathers.remove(old);
		if (newFather != null)
			fathers.add(newFather);
	}

	public FullyQualifiedState getState() {
		return state;
	}

	public String toString() {
		return state.getState().getName();
	}

	@Override
	public List<StateNode> getIncompatibleStateNode() {
		List<StateNode> r = new ArrayList<>();
		for (FullyQualifiedState s : OptimizedDataManager.instance.getStates())
			if (s.getVNode() != this)
				r.add(s.getVNode());
		return r;
	}

	public List<StateNode> getIncompatibleStateNode_full() {
		return getIncompatibleStateNode();
	}

	@Override
	protected boolean isCompatibleWith_full_intern(AbstractNode other, Set<Pair> computed) {
		if (other.isStateNode())
			return other == this;
		return super.isCompatibleWith_full_intern(other, computed);
	}
}
