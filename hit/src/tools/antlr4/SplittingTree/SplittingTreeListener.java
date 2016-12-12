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
	 * Enter a parse tree produced by {@link SplittingTreeParser#state}.
	 * @param ctx the parse tree
	 */
	void enterState(SplittingTreeParser.StateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#state}.
	 * @param ctx the parse tree
	 */
	void exitState(SplittingTreeParser.StateContext ctx);
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