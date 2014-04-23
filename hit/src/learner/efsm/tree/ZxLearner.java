package learner.efsm.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.Node;
import main.simpa.Options;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import drivers.Driver;
import drivers.efsm.real.SimpleClient;

public class ZxLearner extends Learner {
	private SimpleClient driver;
	private List<InputSequence> z;
	private List<String> i;
	private XObservationNode u;
	private List<XObservationNode> states;

	public ZxLearner(Driver driver) {
		this.driver = (SimpleClient) driver;

		// Initialize I and Z with specified options
		if (Options.INITIAL_INPUT_SYMBOLS.length() > 0)
			this.i = Arrays.asList(Options.INITIAL_INPUT_SYMBOLS.split(","));
		else
			this.i = new ArrayList<String>();
		this.z = new ArrayList<InputSequence>();
		if (Options.INITIAL_INPUT_SEQUENCES.length() > 0) {
			for (String inputSeqString : Options.INITIAL_INPUT_SEQUENCES
					.split(",")) {
				InputSequence seq = new InputSequence();
				for (String inputSym : inputSeqString.split("-")) {
					seq.addInput(inputSym);
				}
				z.add(seq);
			}
		}

		if (Options.INITIAL_INPUT_SYMBOLS_EQUALS_TO_X)
			this.i = driver.getInputSymbols();

		// an observation tree U, initialized with {e}.
		this.u = new XObservationNode();

		LogManager.logConsole("Options : I -> " + i.toString());
		LogManager.logConsole("Options : Z -> " + z.toString());
	}

	private LmConjecture fixPointConsistency(LmConjecture K) {
		InputSequence inconsistency;

		// 1. while there exists a witness w for state q in U such that its
		// input projection is not in Z
		do {
			inconsistency = findInconsistency(K);
			if (inconsistency != null) {
				// 4. Z' = Z U {w|I} and I' = I U inp(w)
				// 6. Z = Z' and I = I'
				z = extendInputSequencesWith(z, inconsistency);
				i = extendInputSymbolsWith(inconsistency);

				// 5. Build_quotient(A, I', Z', U) returning an updated
				// observation tree and quotient
				K = buildQuotient(z);
			}
		} while (inconsistency != null);

		// 7. Return the last labelled observation tree (global) and quotient.
		return K;
	}

	private LmConjecture buildQuotient(List<InputSequence> z) {
		LogManager.logInfo("Build Quotient");
		LogManager.logInfo("Z : " + z.toString());
		LogManager.logInfo("I : " + i.toString());

		// 1. q0 := e, Q : = {q0}
		this.states = new ArrayList<XObservationNode>();

		// 2. for (each state u of U being traversed during Breadth First Search
		List<Node> queue = new ArrayList<Node>();
		queue.add(u);
		XObservationNode currentNode = null;
		while (!queue.isEmpty()) {
			currentNode = (XObservationNode) queue.remove(0);

			// 2. such that u has no labelled predecessor
			if (noLabelledPred(currentNode)) {
				// 4. Extend_Node(A, u, Z),
				extendNodeWithInputSeqs(currentNode, z);

				// 5. if (u is Z-equivalent to a traversed state w of U)
				XObservationNode w = findFirstEquivalent(currentNode, z);
				if (w != null) {
					// 6. Label u with w
					currentNode.label = w.state;
					currentNode.state = -1;
				} else {
					// 8. Add u into Q
					addState(currentNode);
					// 9. Extend_Node(A, u, I)
					extendNodeWithSymbols(currentNode, i);
				}
			}
			queue.addAll(currentNode.children.values());
		}

		// 11. for (each transition (u, ab, v), such that neither state u nor
		// any of its predecessors is labelled)
		// 12. begin
		// 13. if (v is not labelled)
		// 14. Add transition (u, ab, v) to K
		// 15. else
		// 16. Add transition (u, ab, w), where w = label(v), to K.
		// 17. end
		LmConjecture ret = createConjecture();

		// NEEDED ?
		// 18. for each node u labelled with u label its successor nodes such
		// that for each transition (u, ab, v), if there is a transition (u,
		// ab, w) in K, then label(v) = w, else v is not labelled.
		labelNodes(ret);

		return ret;
	}

	@SuppressWarnings("unused")
	public void learn() {
		LogManager.logConsole("Inferring the system");
		InputSequence ce;

		// 1. Build-quotient(A, I, Z, {€}) returning U and K = (Q, q0, I, O, hK)
		LmConjecture Z_Q = buildQuotient(z);

		// 2. Fix_Point_Consistency(A, I, Z, U, K)
		Z_Q = fixPointConsistency(Z_Q);

		// 4. while there exists an unprocessed counterexample CE
		do {
			ce = null; //driver.getCounterExample(Z_Q);
			if (ce != null) {
				LogManager.logInfo("Adding the counter example to tree");

				// 5. U = U U CE
				askInputSequenceToNode(u, ce);

				// LogManager.logObservationTree(u);

				// 6. Fix_Point_Consistency(A, I, Z, U, K)
				Z_Q = fixPointConsistency(Z_Q);

			}
		} while (ce != null);

	}

	private boolean noLabelledPred(XObservationNode node) {
		boolean noLabelledPred = true;
		while (node.parent != null) {
			XObservationNode parent = (XObservationNode) node.parent;
			if (parent.isLabelled())
				return false;
			node = parent;
		}
		return noLabelledPred;
	}

	private int compareNodesUsingSeqs(Node node1, Node node2,
			List<InputSequence> z) {
		for (InputSequence seq : z) {
			Node currentNode1 = node1;
			Node currentNode2 = node2;
			InputSequence dfs = new InputSequence();
			for (String input : seq.sequence) {
				dfs.addInput(input);
/*				if (!currentNode1.haveChildBy(input)
						|| !currentNode2.haveChildBy(input))
					return -1;
				if (!currentNode1.childBy(input).output.equals(currentNode2
						.childBy(input).output))
					return -1;*/
				currentNode1 = currentNode1.childBy(input);
				currentNode2 = currentNode2.childBy(input);
			}
		}
		return 0;
	}

	private XObservationNode findFirstEquivalent(XObservationNode node,
			List<InputSequence> z) {
		List<Node> queue = new ArrayList<Node>();
		queue.add(u);
		XObservationNode currentNode = null;
		while (!queue.isEmpty()) {
			currentNode = (XObservationNode) queue.get(0);
			if (currentNode.id == node.id)
				break;
			if (compareNodesUsingSeqs(node, currentNode, z) == 0 && currentNode.isState())
				return currentNode;
			queue.remove(0);
			queue.addAll(currentNode.children.values());
		}
		return null;
	}

	private void addState(XObservationNode node) {
		node.state = states.size();

		// ADDED
		node.label = -1;

		states.add(node);
	}

	private void extendNodeWithInputSeqs(Node node, List<InputSequence> Z) {
		for (InputSequence seq : Z) {
			askInputSequenceToNode(node, seq);
		}
	}

	private void extendNodeWithSymbols(Node node, List<String> symbols) {
		for (String symbol : symbols) {
			askInputSequenceToNode(node, new InputSequence(symbol));
		}
	}

	private List<String> extendInputSymbolsWith(InputSequence ce) {
		List<String> ret = new ArrayList<String>(i);
		for (String sym : ce.sequence) {
			if (!ret.contains(sym))
				ret.add(sym);
		}
		return ret;
	}

	private List<InputSequence> extendInputSequencesWith(
			List<InputSequence> into, InputSequence seq) {
		List<InputSequence> ret = new ArrayList<InputSequence>();
		boolean exists = false;
		for (InputSequence s : into) {
			ret.add(s);
			if (s.equals(seq))
				exists = true;
		}
		if (!exists && seq.getLength() > 0)
			ret.add(seq);
		return ret;
	}

	private void labelNodes(LmConjecture q) {
		LogManager.logInfo("Labeling nodes");
		labelNodesRec(q, u, q.getInitialState(), false);
		// LogManager.logObservationTree(u);
	}

	private void labelNodesRec(LmConjecture q, XObservationNode node, State s,
			boolean label) {
		if (label)
			node.label = s.getId();
		if (node.isLabelled())
			label = true;
		if (!node.children.isEmpty()) {
			for (Node n : node.children.values()) {
				MealyTransition t = q.getTransitionFromWithInput(s, n.input);
				if (t != null)
					labelNodesRec(q, (XObservationNode) n, t.getTo(), label);
			}
		}
	}

	private InputSequence findInconsistency(LmConjecture c) {
		LogManager.logInfo("Searching inconsistency");
		InputSequence ce = findInconsistencyRec(c, c.getInitialState(), u,
				new InputSequence());
		if (ce != null)
			LogManager.logInfo("Inconsistency found : " + ce);
		else
			LogManager.logInfo("No inconsistency found");
		return ce;
	}

	private InputSequence findInconsistencyRec(LmConjecture c, State s,
			XObservationNode node, InputSequence ce) {
		if (!node.children.isEmpty()) {
			for (Node n : node.children.values()) {
				if (!((XObservationNode) n).isState())
					ce.addInput(n.input);
				MealyTransition t = c.getTransitionFromWithInput(s, n.input);
				if (t != null && t.getOutput().equals(n.output)) {
					InputSequence otherCE = findInconsistencyRec(c, t.getTo(),
							(XObservationNode) n, ce);
					if (otherCE != null) {
						boolean processed = false;
						for (InputSequence seq : z) {
							if (seq.equals(ce)) {
								processed = true;
								break;
							}
						}
						if (!processed && ce.getLength() > 0)
							return otherCE;
					}
				} else {
					boolean processed = false;
					for (InputSequence seq : z) {
						if (seq.equals(ce)) {
							processed = true;
							break;
						}
					}
					if (ce.getLength() == 1 && !processed)
						return ce;
					else {
						if (ce.getLength() > 1)
							return ce.removeFirstInput();
					}
				}
				if (!((XObservationNode) n).isState())
					ce.removeLastInput();
			}
		}
		return null;
	}

	private void askInputSequenceToNode(Node node, InputSequence sequence) {
//		Node currentNode = node;
//		InputSequence seq = sequence.clone();
//		InputSequence previousSeq = getPreviousInputSequenceFromNode(currentNode);
//		while (seq.getLength() > 0
//				&& currentNode.haveChildBy(seq.getFirstSymbol())) {
//			currentNode = currentNode.childBy(seq.getFirstSymbol());
//			previousSeq.addInput(seq.getFirstSymbol());
//			seq.removeFirstInput();
//		}
//		if (seq.getLength() > 0) {
//			driver.reset();
//			for (String input : previousSeq.sequence) {
//				driver.execute(input);
//			}
//			for (String input : seq.sequence) {
//				if (currentNode.haveChildBy(input))
//					currentNode = currentNode.childBy(input);
//				else
//					currentNode = currentNode.addChild(new XObservationNode(
//							input, driver.execute(input)));
//			}
//		}
	}

	private InputSequence getPreviousInputSequenceFromNode(Node node) {
		Node currentNode = node;
		InputSequence seq = new InputSequence();
		while (currentNode.parent != null) {
			seq.prependInput(currentNode.input);
			currentNode = currentNode.parent;
		}
		return seq;
	}

	public LmConjecture createConjecture() {
		LogManager.logInfo("Building conjecture");
		LogManager.logXObservationTree(u);

		LmConjecture c = new LmConjecture(driver);

		for (int i = 0; i < states.size(); i++)
			c.addState(new State("S" + i, i == 0));

		for (XObservationNode s : states) {
			for (String input : i) {
				XObservationNode child = (XObservationNode) s.childBy(input);
				if (child.output.length() > 0) {
					if (child.isState())
						c.addTransition(new MealyTransition(c, c
								.getState(s.state), c.getState(child.state),
								input, child.output));
					else
						c.addTransition(new MealyTransition(c, c
								.getState(s.state), c.getState(child.label),
								input, child.output));
				}
			}
		}

		LogManager.logInfo("Z : " + z);
		LogManager.logInfo("I : " + i);

		LogManager.logInfo("Conjecture have " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		for (MealyTransition t : c.getTransitions())
			LogManager.logTransition(t.toString());
		LogManager.logLine();

		c.exportToDot();

		return c;
	}
}
