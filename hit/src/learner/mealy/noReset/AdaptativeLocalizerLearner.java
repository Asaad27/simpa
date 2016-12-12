package learner.mealy.noReset;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import examples.mealy.Test3Mealy;
import learner.mealy.LmTrace;
import learner.mealy.tree.Branch;
import learner.mealy.tree.NoEmptySplittingTree;
import learner.mealy.tree.NodeSplittingTree;
import learner.mealy.tree.SplittingTree;
import learner.mealy.tree.StateSplittingTree;

public class AdaptativeLocalizerLearner {

	InputSequence omegaInputSequence = new InputSequence();
	OutputSequence omegaOutputSequence = new OutputSequence();
	// NodeSplittingTree nt = new NodeSplittingTree();
	boolean flag = true;

	/**
	 * @param depth
	 * @param spTree
	 * @param n
	 * @param driver
	 * @return SplittingTree
	 */
	public NodeSplittingTree localize(int depth, NoEmptySplittingTree spTree, int n, MealyDriver driver) {
		NodeSplittingTree node = new NodeSplittingTree();

		if (depth == 0) {
			/** if depth= 0 return root(T). **/
			NodeSplittingTree ns = new NodeSplittingTree(spTree);
			node.append(ns);

		} else if (depth == 1) {
			/**
			 * if d = 1, then apply I(root(T)), observe some O(N), return N,
			 **/

			for (String input : spTree.getInputSequence().sequence) {
				String output = driver.execute(input);
				node.append(input, output);
			}

		} else {

			node.append(localize_intern(depth, spTree, n, driver));
		}
		return node;

	}

	public NodeSplittingTree localize_intern(int depth, NoEmptySplittingTree spTree, int n, MealyDriver driver) {

		NodeSplittingTree ns = new NodeSplittingTree();

		if (depth > 1) {
			/** if depth > 1, depth-- **/
			ns.append(localize_intern(depth - 1, spTree, n, driver));

		} else if (depth == 1) {
			/** if depth =1, here will do L(1, T) **/
			NodeSplittingTree t = new NodeSplittingTree();

			for (String input : spTree.getInputSequence().sequence) {
				String in = "", out = "";
				for (int e = 1; e <= input.length(); e++) {
					String output = driver.execute(input.substring(e - 1, e));
					in += input.substring(e - 1, e);
					out += output;
				}
				t.append(in, out);
			}

			for (Branch b : spTree.getBranch()) {
				/** comparer with every branch **/
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(spTree.getInputSequence(), b.getOutputSequence());
				/** if Nt is a leaf **/
				if (t.equals(tmp) && b.getSPTree().toString().equals("ε()")) {
					/** Here is a leaf, so return it **/
					ns.append(t);
					// return ns;

				} else if (t.equals(tmp) && !b.getSPTree().toString().equals("ε()")) {
					/** Here is not a leaf, so here is an iteration **/

					boolean flag = true;
					while (flag) {

						NodeSplittingTree ite = new NodeSplittingTree();
						// for (int nb = 0; nb < n; nb++) {
						for (String input : spTree.getInputSequence().sequence) {
							String in = "", out = "";
							for (int e = 1; e <= input.length(); e++) {
								String output = driver.execute(input.substring(e - 1, e));
								in += input.substring(e - 1, e);
								out += output;
							}
							t.append(in, out);

							// System.err.println("ns ------ " + t);

							// }
						}

						NodeSplittingTree n1 = new NodeSplittingTree();

						if (predictable(t.size() - 1, t, n1)) {
							flag = false;
							ns.append(n1);
							// System.err.println("N1 ======= " + n1);
						}
					}
				}
			}

		}
		return ns;

	}

	public boolean predictable(int i, NodeSplittingTree nt, NodeSplittingTree n1) {
		boolean sign = false;
		/** repetition factor **/
		int r = 0;

		/** set of states in last r elements of Nt **/

		/** Set of all leaf node : card(Nt(i-r)) **/
		int s = 0;
		while (r < i || s > r) {
			r++;
			NodeSplittingTree ns = new NodeSplittingTree();
			int pos = i - r;
			ns.append(nt.subtrace(0, pos));
			s = ns.size();
			System.out.println("pos => " + pos + " i = " + i + ", r = " + r + ", s = " + s + ",  ns = " + ns);

			if (s == r) {
				// System.err.println("==== NO predictable ====" + ns);
				sign = false;
			} else {
				while (r < i - 1) {
					r++;

				}
				System.err.println("==== NO predictable ====" + ns);
				sign = true;
			}
		}
		return sign;
	}

}
