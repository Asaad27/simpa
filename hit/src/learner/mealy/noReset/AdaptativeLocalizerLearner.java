package learner.mealy.noReset;

import java.util.ArrayList;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import examples.mealy.Test3Mealy;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

public class AdaptativeLocalizerLearner {
	// int time = 0;
	InputSequence omegaInputSequence = new InputSequence();
	OutputSequence omegaOutputSequence = new OutputSequence();
	NodeSplittingTree trace = new NodeSplittingTree();

	/**
	 * @param depth
	 * @param spTree
	 * @param n
	 * @param driver
	 * @return SplittingTree
	 */
	public NodeSplittingTree localize(int depth, NoEmptySplittingTree spTree, int n, MealyDriver driver) {
		NodeSplittingTree node = new NodeSplittingTree();

		/** if depth= 0 return root(T). **/
		if (depth == 0) {
			NodeSplittingTree ns = new NodeSplittingTree(spTree);
			node.append(ns);
			LogManager.logConsole("Depth of splitting tree is 0, and we discovered all states.");

		} else if (depth == 1) {
			/**
			 * if d = 1, then apply I(root(T)), observe some O(N), return N,
			 **/

			node.append(getDriverIO(spTree.getInputSequence(), driver));
			LogManager.logConsole("Depth of splitting tree is 1, and we put inputs for getting all states.");

		} else {
			// time++;
			node.append(localize_intern(depth, spTree, n, driver));
			LogManager.logConsole("Return the node : " + node + ", and " + trace.size() + " steps.");
			LogManager.logConsole("Trace is " + trace);
		}

		return node;

	}

	public NodeSplittingTree localize_intern(int depth, NoEmptySplittingTree spTree, int n, MealyDriver driver) {
		NodeSplittingTree ns = new NodeSplittingTree();
		if (depth > 1) {
			/** if depth > 1, depth-- **/
			ns.append(localize_intern(depth - 1, spTree, n, driver));

		} else if (depth == 1) {

			NodeSplittingTree driverIO = new NodeSplittingTree();

			/** if depth =1, here will do L(1, T) **/

			for (String input : spTree.getInputSequence().sequence) {

				for (int e = 1; e <= input.length(); e++) {
					String in = input.substring(e - 1, e);
					String out = driver.execute(in);
					trace.append(in, out);
					driverIO.append(in, out);
				}

			}

			// trace.append(driverIO);

			for (Branch b : spTree.getBranch()) {

				/** comparer with every branch **/
				NodeSplittingTree tmp = new NodeSplittingTree();
				if (spTree.getInputSequence().getLength() == b.getOutputSequence().getLength()) {
					tmp = handleIO(spTree.getInputSequence(), b.getOutputSequence());
					if (driverIO.equals(tmp)) {

						/** if Nt is a leaf **/
						if (b.getSPTree().toString().equals("ε()")) {

							/** Here is a leaf, so return it **/
							ns.append(driverIO);
							// LogManager.logConsole("Predictable is true, we
							// get N1 = " + ns);

						} else {

							/** if it's not a leaf, so here is an iteration **/
							boolean flag = true;
							NodeSplittingTree n1 = new NodeSplittingTree();

							while (flag) {

								for (String input : spTree.getInputSequence().sequence) {

									for (int e = 1; e <= input.length(); e++) {
										String in = input.substring(e - 1, e);
										String out = driver.execute(in);
										trace.append(in, out);
										driverIO.append(in, out);
									}

								}

								if (predictable(driverIO.size() - 1, driverIO, n1, spTree)) {
									LogManager.logConsole("Predictable is true, we get N1 = " + n1);

									ns.append(n1);
									if (spTree.getInputSequence().equals(n1.getInputsProjection())) {

										for (Branch sub : spTree.getBranch()) {
											if (sub.getOutputSequence().equals(n1.getOutputsProjection())) {

												NodeSplittingTree t = new NodeSplittingTree();
												NoEmptySplittingTree subTree = (NoEmptySplittingTree) sub.getSPTree();

												t.append(getDriverIO(subTree.getInputSequence(), driver));

												ns.append(t.getInputsProjection().toString(),
														t.getOutputsProjection().toString());

												trace.append(t);

												for (Branch br : subTree.getBranch()) {

													if (br.getOutputSequence().toString()
															.equals(t.getOutputsProjection().toString())) {
														if (br.getSPTree().toString().equals("ε()")) {
															LogManager.logConsole("We get a leaf, state is " + ns);
														} else {
															LogManager.logConsole("We get a subTree: " + ns);
															ns.append(localize_intern(depth,
																	(NoEmptySplittingTree) spTree, n, driver));

														}

													}
												}

											}

										}
									}
									flag = false;
								}
							}

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
			System.out.println(" i = " + i + ", r = " + r + ", s = " + s);
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

			// System.out.println(" i = " + i + ", r = " + r + ", s = " + s);
			// System.out.println("pos => " + pos + " i = " + i + ", r = " + r +
			// ", s = " + s + ", nt = " + nt);

		}

		if (s == r) {
			sign = false;
		} else {

			NodeSplittingTree p = new NodeSplittingTree();
			// p.append(nt.getInput(i - r - 1), nt.getOutput(i - r - 1));
			p.append(nt.getInput(i - r), nt.getOutput(i - r));
			ArrayList<NodeSplittingTree> ntleaves = new ArrayList<NodeSplittingTree>();

			for (NodeSplittingTree it : getLeaves(st)) {
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(it.getInput(0), it.getOutput(0));

				if (p.equals(tmp)) {
					ntleaves.add(it);
				}
			}

			/** leaves(Nt(i-r-1)) is a subset Ns **/
			int j = 0;
			while (r < i - 1) {
				r++;

				NodeSplittingTree q1 = new NodeSplittingTree();
				q1.append(nt.subtrace(i - r - 1, i - r + s));

				NodeSplittingTree q2 = new NodeSplittingTree();
				q2.append(nt.subtrace(i - r + s, i + 1));

				if (findJ(q1, q2) != 0) {
					j = findJ(q1, q2);
				}

				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(nt.getInput(i - 1 - r + j), nt.getOutput(i - 1 - r + j));
				n1.append(tmp);
				sign = true;
			}
			while (j > i - s) {

				j = j - 1;
				// TODO : ...
				sign = false;
			}

		}

		return sign;
	}

	/**
	 * for splitting tree, we get input like "ab", "aab"..., so we need to
	 * separate them. for example, ab/10 -->> a/1 b/0
	 **/

	public NodeSplittingTree handleIO(InputSequence is, OutputSequence os) {
		InputSequence istmp = new InputSequence();
		OutputSequence ostmp = new OutputSequence();
		NodeSplittingTree result = new NodeSplittingTree();
		if (is.getLength() == os.getLength()) {

			for (String in : is.sequence) {

				for (int e = 1; e <= in.length(); e++) {
					istmp.addInput(in.substring(e - 1, e));
				}
			}

			for (String out : os.sequence) {

				for (int e = 1; e <= out.length(); e++) {
					ostmp.addOutput(out.substring(e - 1, e));
				}
			}
			result.append(istmp, ostmp);
		}
		return result;
	}

	public NodeSplittingTree getDriverIO(InputSequence inSeq, MealyDriver driver) {

		NodeSplittingTree result = new NodeSplittingTree();

		for (String input : inSeq.sequence) {

			for (int e = 1; e <= input.length(); e++) {
				String in = input.substring(e - 1, e);
				String out = driver.execute(in);
				result.append(in, out);
			}

		}

		return result;
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

	public int findJ(NodeSplittingTree q1, NodeSplittingTree q2) {

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
				j = i + j;
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
