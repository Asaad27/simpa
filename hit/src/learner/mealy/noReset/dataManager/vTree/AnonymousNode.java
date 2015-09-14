package learner.mealy.noReset.dataManager.vTree;

import java.util.ArrayList;
import java.util.List;

import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyQualifiedState;

public class AnonymousNode extends AbstractNode {
	private AbstractNode father;

	public AnonymousNode(AbstractNode father) {
		super();
		this.father = father;
	}

	@Override
	public boolean isStateNode() {
		return false;
	}

	@Override
	protected String nodeToDotOptions() {
		String desc = this.desc;
		if (getMergedWith() == null) {
			desc = desc + "incompatible with " + getIncompatibleStateNode();
		}
		return "style=dotted,label=\"" + dotName() + "\\n" + desc.replace("\n", "\\n") + "\"";
	}

	@Override
	public List<AbstractNode> getParents() {
		List<AbstractNode> r = new ArrayList<>();
		if (father != null)
			r.add(father);
		return r;
	}

	@Override
	protected void changeFatherLink(AbstractNode old, AbstractNode newFather) {
		assert(father == old || old == null);
		father = newFather;
	}

	public String toString() {
		return dotName();
	}

	@Override
	public List<StateNode> getIncompatibleStateNode_full() {
		List<StateNode> r = new ArrayList<>();
		for (FullyQualifiedState s : DataManager.instance.getStates()) {
			if (!isCompatibleWith_full(s.getVNode())) {
				r.add(s.getVNode());
			}
		}
		return r;
	}

}
