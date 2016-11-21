package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import tools.antlr4.SplittingTree.SplittingTreeBaseListener;
import tools.antlr4.SplittingTree.SplittingTreeParser;

public class SplittingTreeAntlrListener extends SplittingTreeBaseListener {

	public ArrayList<String> props = new ArrayList<String>();
	public ArrayList<String> inputSequence = new ArrayList<String>();
	public ArrayList<String> outputSequence = new ArrayList<String>();
	public ArrayList<String> branchs = new ArrayList<String>();
	public ArrayList<String> Leaf = new ArrayList<String>();
	public ArrayList<String> subSplittingTree = new ArrayList<String>();
	public String root;
	public String splittingTree;
	public Map<String, String> branch = new HashMap<String, String>();

	// public SplittingTreeAntlrListener() {
	//
	// }

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#branch}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterSplitting_tree(SplittingTreeParser.Splitting_treeContext ctx) {
		this.splittingTree = ctx.getText();

	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#splitting_tree}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitSplitting_tree(SplittingTreeParser.Splitting_treeContext ctx) {
	}

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#branch}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterBranch(SplittingTreeParser.BranchContext ctx) {
		// System.err.println("enterBranch ----- : " + ctx.getText());
		branchs.add(ctx.getText());
		String in = ctx.getParent().getChild(0).getText();
		String out = ctx.getChild(0).getText();
		String io = in + "/" + out;
		branch.put(ctx.getText(), io);

	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#branch}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitBranch(SplittingTreeParser.BranchContext ctx) {
	}

	/**
	 * Enter a parse tree produced by
	 * {@link SplittingTreeParser#subsplitting_tree}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterSubsplitting_tree(SplittingTreeParser.Subsplitting_treeContext ctx) {
		// System.err.println("enterSubsplitting_tree ----- : "+ ctx.getText());
		// System.out.println("getParent ---->" +
		// ctx.getParent().getChild(0).getText());
		subSplittingTree.add(ctx.getText());

	}

	/**
	 * Exit a parse tree produced by
	 * {@link SplittingTreeParser#subsplitting_tree}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitSubsplitting_tree(SplittingTreeParser.Subsplitting_treeContext ctx) {
	}

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#state}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterLeaf(SplittingTreeParser.LeafContext ctx) {
		// System.err.println("enterLeaf ----- : " + ctx.getText());
		Leaf.add(ctx.getText());
	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#state}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitLeaf(SplittingTreeParser.LeafContext ctx) {
	}

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#root}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterRoot(SplittingTreeParser.RootContext ctx) {
		// System.err.println("enterRoot ----- : " + ctx.getText());
		root = ctx.getText();
		inputSequence.add(root);

	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#root}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitRoot(SplittingTreeParser.RootContext ctx) {
	}

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#input}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterInput(SplittingTreeParser.InputContext ctx) {
		// System.err.println("enterInput ----- : " + ctx.getText());
		inputSequence.add(ctx.getText());
		// System.err.println(ctx.getChildCount());
	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#input}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitInput(SplittingTreeParser.InputContext ctx) {
	}

	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#output}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void enterOutput(SplittingTreeParser.OutputContext ctx) {
		// System.err.println("enterOutput ----- : " + ctx.getText());
		String out = ctx.getText();
		outputSequence.add(out);
		// System.err.println("ctx.getParent().getChild(0) ------- " +
		// ctx.getParent().getParent().getChild(0).getText());
		String in = ctx.getParent().getParent().getChild(0).getText();
		props.add(in + "/" + out);

	}

	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#output}.
	 * 
	 * @param ctx
	 *            the parse tree
	 */
	public void exitOutput(SplittingTreeParser.OutputContext ctx) {
	}

}
