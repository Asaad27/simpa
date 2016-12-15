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
			while (r < i - 1) {
				r++;
				nt.subtrace(i - r + s, i + 1);
				System.err.println("   ==== We can do something!! ====" + " i =" + i + " s =" + s + " r = " + r + " sub --" + nt.subtrace(i - r + s, i + 1));
				for (int j = i - r; j < i - 1; j++) {
					
					
					
				}
				// j<i-r+s;

			}
			// System.err.println("==== NO predictable ====");
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

	// public static void main(String[] args) throws IOException {
	// AdaptativeLocalizerLearner all = new AdaptativeLocalizerLearner();
	// File file = new File("/Users/wang/Desktop/sptree.txt");
	// if (!file.exists()) {
	// throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
	// }
	// ANTLRInputStream stream = new ANTLRInputStream(new
	// FileInputStream(file));
	// SplittingTreeLexer lexer = new SplittingTreeLexer(stream);
	// CommonTokenStream tokens = new CommonTokenStream(lexer);
	// SplittingTreeParser parser = new SplittingTreeParser(tokens);
	// // tell ANTLR to build a parse tree
	// parser.setBuildParseTree(true);
	// ParseTree tree = parser.splitting_tree();
	// SplittingTreeVisitorImpl antlrVisitor = new SplittingTreeVisitorImpl();
	// NoEmptySplittingTree st = (NoEmptySplittingTree)
	// antlrVisitor.visit(tree);
	//
	// for (NodeSplittingTree at : all.getLeaves(st)) {
	// System.err.println("NodeSplittingTree ---- " + at);
	// }
	// }

}
