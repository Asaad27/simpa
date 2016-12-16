package learner.mealy.noReset;

import java.util.ArrayList;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import examples.mealy.Test3Mealy;
import learner.mealy.LmTrace;
import learner.mealy.tree.Branch;
import learner.mealy.tree.NoEmptySplittingTree;
import learner.mealy.tree.NodeSplittingTree;

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

				} else if (t.equals(tmp) && !b.getSPTree().toString().equals("ε()")) {
					/** Here is not a leaf, so here is an iteration **/
					NodeSplittingTree ite = new NodeSplittingTree();
					ite.append(t);
					boolean flag = true;
					while (flag) {
						for (int nb = 0; nb < 6; nb++) {
							for (String input : spTree.getInputSequence().sequence) {
								String in = "", out = "";
								for (int e = 1; e <= input.length(); e++) {
									String output = driver.execute(input.substring(e - 1, e));
									in += input.substring(e - 1, e);
									out += output;
								}

								ite.append(in, out);

							}
						}

						NodeSplittingTree n1 = new NodeSplittingTree();
						System.err.println("nt ----- " + ite);
						if (predictable(ite.size() - 1, ite, n1, spTree)) {
							ns.append(n1);
							flag = false;
						}
					}
				}
			}

		}
		return ns;

	}

	public boolean predictable(int i, NodeSplittingTree nt, NodeSplittingTree n1, NoEmptySplittingTree st) {

		boolean sign = false;
		/** repetition factor **/
		int r = 0;

		/** Set of all leaf node : card(Nt(i-r)) **/
		int s = 0;

		while (r < i && s >= r) {

			r++;
			int pos = i - r;
			NodeSplittingTree p = new NodeSplittingTree();
			p.append(nt.getInput(pos), nt.getOutput(pos));

			/** set of states in last r elements of Nt **/
			ArrayList<NodeSplittingTree> ns = new ArrayList<NodeSplittingTree>();
			for (NodeSplittingTree it : getLeaves(st)) {
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(it.getInput(0), it.getOutput(0));
				if (p.equals(tmp)) {
					ns.add(it);
				}
			}
			s = ns.size();

			System.out.println("pos => " + pos + " i = " + i + ", r = " + r + ", s = " + s + ", ns = " + ns);
		}
		// System.err.println("Here ns = " + ns );
		if (s == r) {
			System.err.println("Here s = " + s + "==== So! NO predictable, again ====");
			sign = true;

		} else {

			NodeSplittingTree p = new NodeSplittingTree();
			p.append(nt.getInput(i - r - 1), nt.getOutput(i - r - 1));

			ArrayList<NodeSplittingTree> ntleaves = new ArrayList<NodeSplittingTree>();

			for (NodeSplittingTree it : getLeaves(st)) {
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(it.getInput(0), it.getOutput(0));

				if (p.equals(tmp)) {
					ntleaves.add(it);
				}
			}

			// System.err.println(" ==== We can do something!! ====" +" i ="+ i
			// +" s ="+ s + " r = " + r);
			/** leaves(Nt(i-r-1)) is a subset Ns **/
			while (r < i - 1) {
				r++;

				NodeSplittingTree q1 = new NodeSplittingTree();
				q1.append(nt.subtrace(i - r - 1, i - r + s));

				NodeSplittingTree q2 = new NodeSplittingTree();
				q2.append(nt.subtrace(i - r + s, i + 1));

				int m = findM(q1, q2);
				// System.err.println("q1 ----------- >>" + q1);
				// System.err.println("q2 ----------- >>" + q2);
				// System.err.println("m ----------- >>" + m);
				// System.err.println("i ----------- >>" + i);
				// System.err.println("i-1-m ----------- >>" + (i - 1 - m));

				NodeSplittingTree tmp = new NodeSplittingTree();

			}
			sign = true;
		}

		//
		return sign;
	}

	public ArrayList<NodeSplittingTree> getLeaves(NoEmptySplittingTree st) {

		ArrayList<NodeSplittingTree> list = new ArrayList<NodeSplittingTree>();

		for (Branch b : st.getBranch()) {

			if (b.getSPTree().toString().equals("ε()")) {
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(st.getInputSequence(), b.getOutputSequence());
				list.add(tmp);
			} else {

				for (NodeSplittingTree n : getLeaves((NoEmptySplittingTree) b.getSPTree())) {
					NodeSplittingTree tmp = new NodeSplittingTree();
					tmp.append(st.getInputSequence(), b.getOutputSequence());
					tmp.append(n);
					list.add(tmp);
				}

			}

		}
		return list;
	}

	public int findM(NodeSplittingTree q1, NodeSplittingTree q2) {

		int j = 0;

		for (int i = 0; i < q2.size(); i++) {
			int k = i;
			int count = 0;

			for (int it = 0; it < q1.size(); it++) {
				if (it < q2.size() && k < q2.size() && q2.getIO(k).equals(q1.getIO(it))) {
					k++;
					count++;
				}

			}
			if (count >= j) {
				j = count;
			}
		}

		return j;
	}

	// public static void main(String[] args) {
	// AdaptativeLocalizerLearner all = new AdaptativeLocalizerLearner();
	// NodeSplittingTree q1 = new NodeSplittingTree();
	// NodeSplittingTree q2 = new NodeSplittingTree();
	//
	// q1.append("a", "0");
	// q1.append("a", "1");
	// q1.append("a", "0");
	// q1.append("a", "1");
	// q1.append("a", "0");
	//
	// q2.append("a", "0");
	// q2.append("a", "1");
	// q2.append("a", "0");
	//
	// System.err.println(all.findJ(q1, q2));
	// File file = new File("/Users/wang/Desktop/sptree.txt");
	// if (!file.exists()) {
	// throw new IOException("'" + file.getAbsolutePath() + "' do not
	// exists");
	// }
	// ANTLRInputStream stream = new ANTLRInputStream(new
	// FileInputStream(file));
	// SplittingTreeLexer lexer = new SplittingTreeLexer(stream);
	// CommonTokenStream tokens = new CommonTokenStream(lexer);
	// SplittingTreeParser parser = new SplittingTreeParser(tokens);
	// // tell ANTLR to build a parse tree
	// parser.setBuildParseTree(true);
	// ParseTree tree = parser.splitting_tree();
	// SplittingTreeVisitorImpl antlrVisitor = new
	// SplittingTreeVisitorImpl();
	// NoEmptySplittingTree st = (NoEmptySplittingTree)
	// antlrVisitor.visit(tree);
	//
	// for (NodeSplittingTree at : all.getLeaves(st)) {
	// System.err.println("NodeSplittingTree ---- " + at);
	// }
	// }
	// }
}
