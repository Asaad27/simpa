package tools.antlr4.SplittingTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.tree.Branch;
import learner.mealy.tree.NoEmptySplittingTree;
import learner.mealy.tree.SplittingTree;
import learner.mealy.tree.StateSplittingTree;
import tools.antlr4.SplittingTree.SplittingTreeParser.Splitting_treeContext;

public class SplittingTreeVisitorImpl extends SplittingTreeBaseVisitor<SplittingTree> {

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.
	 * </p>
	 */
	@Override
	public SplittingTree visitSplitting_tree(@NotNull SplittingTreeParser.Splitting_treeContext ctx) {

		ArrayList<Branch> branchList = new ArrayList<Branch>();
		NoEmptySplittingTree st = null;
		InputSequence inputSequence = new InputSequence();
		int nbChild = ctx.getChildCount();
		
		if (nbChild != 0) {
			String in = ctx.input().getText();

			inputSequence.addInput(in);
			for (int i = 0; i < ctx.output().size(); i++) {
				OutputSequence outputSequence = new OutputSequence();
				outputSequence.addOutput(ctx.output(i).getText());
				Branch b = new Branch(outputSequence, visitSplitting_tree(ctx.splitting_tree(i)));
				branchList.add(b);
				// System.err.println("Input ----->" + in.toString());
				// System.err.println("Output ----->" +
				// ctx.output(i).getText());
				// System.err.println("Branch ----->" +
				// b.getSPTree().toString());
				// if (!b.getSPTree().toString().equals("ε()")) {
				// ArrayList<String> tmp = getStates(ctx.splitting_tree(i));
				// tmp.add(in.toString() + "/" + ctx.output(i).getText());
				// System.out.println(in.toString() + "/" +
				// ctx.output(i).getText());
				// System.out.println("Etat intern >>>>>>>>>>>>>>>> " + tmp);
				//// if (tmp.contains("a/1")) {
				//// System.out.println("Yes Contains a/1");
				//// }
				// } else {
				// System.out.println("Etat fini >>>>>>>>>>>>>>>> " + in + "/" +
				// ctx.output(i).getText());
				// }
			}
		}
		st = new NoEmptySplittingTree(inputSequence, branchList);
		return st;
	}

	public ArrayList<String> getStates(Splitting_treeContext ctx) {
		ArrayList<String> states = new ArrayList<String>();
		if (ctx.getChildCount() != 0) {
			String in = ctx.input().getText();
			for (int i = 0; i < ctx.output().size(); i++) {
				states.add(in + "/" + ctx.output(i).getText());
				getStates(ctx.splitting_tree(i));
			}
		}
		return states;
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.
	 * </p>
	 */
	// @Override
	// public T visitOutput(@NotNull SplittingTreeParser.OutputContext ctx) {
	// return visitChildren(ctx);
	// }

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.
	 * </p>
	 */
	// @Override
	// public T visitInput(@NotNull SplittingTreeParser.InputContext ctx) {
	// return visitChildren(ctx);
	// }

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.
	 * </p>
	 */
	// @Override
	// public StateSplittingTree visitState(@NotNull
	// SplittingTreeParser.StateContext ctx) {
	// System.out.println("visitState----------->"+ ctx.getText());

	// return null;
	// }

}
