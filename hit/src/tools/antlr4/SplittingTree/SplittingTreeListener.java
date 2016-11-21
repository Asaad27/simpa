// Generated from SplittingTree.g4 by ANTLR 4.5.3
package tools.antlr4.SplittingTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SplittingTreeParser}.
 */
public interface SplittingTreeListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#splitting_tree}.
	 * @param ctx the parse tree
	 */
	void enterSplitting_tree(SplittingTreeParser.Splitting_treeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#splitting_tree}.
	 * @param ctx the parse tree
	 */
	void exitSplitting_tree(SplittingTreeParser.Splitting_treeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#branch}.
	 * @param ctx the parse tree
	 */
	void enterBranch(SplittingTreeParser.BranchContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#branch}.
	 * @param ctx the parse tree
	 */
	void exitBranch(SplittingTreeParser.BranchContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#subsplitting_tree}.
	 * @param ctx the parse tree
	 */
	void enterSubsplitting_tree(SplittingTreeParser.Subsplitting_treeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#subsplitting_tree}.
	 * @param ctx the parse tree
	 */
	void exitSubsplitting_tree(SplittingTreeParser.Subsplitting_treeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#leaf}.
	 * @param ctx the parse tree
	 */
	void enterLeaf(SplittingTreeParser.LeafContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#leaf}.
	 * @param ctx the parse tree
	 */
	void exitLeaf(SplittingTreeParser.LeafContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(SplittingTreeParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(SplittingTreeParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(SplittingTreeParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(SplittingTreeParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#output}.
	 * @param ctx the parse tree
	 */
	void enterOutput(SplittingTreeParser.OutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#output}.
	 * @param ctx the parse tree
	 */
	void exitOutput(SplittingTreeParser.OutputContext ctx);
}