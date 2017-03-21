package learner.mealy.noReset;

import java.util.ArrayList;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;

import tools.loggers.LogManager;

public class AdaptativeLocalizerLearner {
	// int time = 0;

	NodeSplittingTree trace = new NodeSplittingTree();
	ArrayList<NodeSplittingTree> listState = new ArrayList<NodeSplittingTree>();
	// ArrayList<NodeSplittingTree> nt = new ArrayList<NodeSplittingTree>();
	/**
	 * list for outputs of driver
	 **/
	NodeSplittingTree driverIO = new NodeSplittingTree();
	NodeSplittingTree listIO = new NodeSplittingTree();

	// NodeSplittingTree tmp = new NodeSplittingTree();

	// MealyDriver driver;

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
			return node;

		} else if (depth == 1) {
			/**
			 * if d = 1, then apply I(root(T)), observe some O(N), return N,
			 **/

			node.append(getDriverIO(spTree.getInputSequence(), driver));
			System.err.println("Driver input/output : " + node);
			return node;
			/**
			 * System.err.println("node ===== " + node); return node;
			 * LogManager.logConsole("Depth of splitting tree is 1, and we put
			 * inputs for getting all states."); LogManager.logInfo("Depth of
			 * splitting tree is 1, and we put inputs for getting all states.");
			 **/

		} else {

			boolean flag = true;
			int i = 0;
			NodeSplittingTree nt = new NodeSplittingTree();
			NodeSplittingTree n1 = new NodeSplittingTree();

			do {
				System.out.println("i === " + i);
				
				nt.append(localize(depth - 1, spTree, n, driver));
				/** get input from SplittingTree, and get ouput from driver **/
				if (nt.getIO(i).getInputsProjection().toString().equals(spTree.getInputSequence().toString())) {
 
					Branch b = null;
					for (Branch btmp : spTree.getBranch()) {

						/** comparer with every branch **/
						if (btmp.getOutputSequence().toString().equals(nt.getIO(i).getOutputsProjection().toString())) {
							b = btmp;
						}
					}

					if (b.getSPTree().toString().equals("ε()")) {
						LogManager.logConsole("There is a leaf, so return it ");
						/** Here is a leaf, so return it **/
						flag = false;
						return (NodeSplittingTree) nt.getIO(i);

					} else {
						// System.out.println("predictable(i, nt, n1, spTree)
						// --- " + i);
						i = i + 1;
						/**
						 * if it's not a leaf, so here is an iteration
						 **/

						if (predictable(i, nt, n1, spTree)) {
							flag = false;
							listIO.append(nt);
							/** Predictable is true, return n1. **/
							LogManager.logConsole("Predictable is true, we get N1 = " + n1);
							if (spTree.getInputSequence().equals(n1.getInputsProjection())) {
								Branch sub = null;
								for (Branch subtmp : spTree.getBranch()) {
									if (subtmp.getOutputSequence().equals(n1.getOutputsProjection())) {
										sub = subtmp;
									}
								}

								NoEmptySplittingTree subTree = (NoEmptySplittingTree) sub.getSPTree();

								NodeSplittingTree tmp = new NodeSplittingTree();
								// System.err.println("subTree -------- " +
								// subTree + ", depth = " + depth);
								tmp.append(localize(depth - 1, subTree, n, driver));

								nt.append(tmp.getInputsProjection().toString(), tmp.getOutputsProjection().toString());
								node.append(n1);
								node.append(tmp.getInputsProjection().toString(),
										tmp.getOutputsProjection().toString());
								listIO.append(handleIO(tmp.getInputsProjection(), tmp.getOutputsProjection()));
								System.out.println("listIO ---- " + listIO);
								System.err.println("N ---- " + node);
							}

						}
					}

				} else {
					flag = false;
				}

			} while (flag);

			// node.append(localize_intern(depth, spTree, n, driver));

			// LogManager.logInfo("Total length of sequence is " + listIO.size()
			// + " steps.");
			// LogManager.logInfo("Sequence is " + listIO);
			return node;
		}

	}

	public boolean predictable(int i, NodeSplittingTree nt, NodeSplittingTree n1, NoEmptySplittingTree st) {

		boolean sign = false;
		boolean flag = true;
		ArrayList<NodeSplittingTree> ns = new ArrayList<NodeSplittingTree>();

		/** repetition factor **/
		int r = 0;
		int s = 0;
		while (flag) {
			r = r + 1;
			int pos = i - r;

			NodeSplittingTree p = new NodeSplittingTree();

			p.append(nt.getIO(pos));

			/** set of states in last r elements of Nt **/

			for (NodeSplittingTree it : getLeaves(st)) {
				NodeSplittingTree tmp = new NodeSplittingTree();
				tmp.append(it.getInput(0), it.getOutput(0));
				if (p.equals(tmp) && !ns.contains(it)) {
					ns.add(it);
				}
			}
			s = ns.size();
			if (r == i) {
				flag = false;
			} else if (s <= r) {
				flag = false;
			}

		}
		// System.err.println("i --- = " + i + ", r --- = " + r + ", s --- = " +
		// s + ", nt --- = " + nt);
		if (s > r) {
			sign = false;
		} else {
			int result = 0;

			if (i - r - 1 >= 0) {
				// System.err.println("Here --- man");
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

				/** leaves(Nt(i-r-1)) is a subset Ns **/

				int count = 0;

				while (ns.containsAll(ntleaves) && i - 1 - r >= 0) {

					r = r + 1;
					for (int m = 0; m <= r - s - 1; m++) {

						for (int j = i - 2; j >= i - r; j--) {

							if (nt.getIO(j - m).equals(nt.getIO(i - 1 - m))) {

								count = j;
							}
							if (count >= result) {

								result = count;
							}
						}
					}

					// System.err.println("i --- = " + i + ", r --- = " + r + ",
					// s --- = " + s + ", nt --- = " + nt);
					// System.err.println("result --- " + result);
					n1.append(nt.getIO(result + 1));
					// System.err.println(driverIO.getIO(count));
					System.err.println("i --- = " + i + ", r --- = " + r + ", s --- = " + s + ", nt --- = " + nt);
					sign = true;
				}
			}

			while (result > i - s) {

				result--;
				for (int m = 0; m <= r - s - 1; m++) {

					if (nt.getIO(result - m).equals(nt.getIO(i - 1 - m)) && !nt.getIO(result + 1).equals(n1)) {
						sign = false;
					}
					// System.err.println("j --- " + result + ", i-s = " +(i-s)
					// + ", m = " + m );
				}

			}

		}
		// System.out.println("sign --- " + sign);
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
		String i = "";
		String o = "";
		for (String input : inSeq.sequence) {

			for (int e = 1; e <= input.length(); e++) {
				String in = input.substring(e - 1, e);
				i += in;
				String out = driver.execute(in);
				o += out;
				// result.append(in, out);
			}

		}
		result.append(i, o);
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

}
