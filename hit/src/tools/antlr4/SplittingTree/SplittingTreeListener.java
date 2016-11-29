// Generated from SplittingTree.g4 by ANTLR 4.4
package tools.antlr4.SplittingTree;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SplittingTreeParser}.
 */
public interface SplittingTreeListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#output}.
	 * @param ctx the parse tree
	 */
	void enterOutput(@NotNull SplittingTreeParser.OutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#output}.
	 * @param ctx the parse tree
	 */
	void exitOutput(@NotNull SplittingTreeParser.OutputContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(@NotNull SplittingTreeParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(@NotNull SplittingTreeParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#state}.
	 * @param ctx the parse tree
	 */
	void enterState(@NotNull SplittingTreeParser.StateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#state}.
	 * @param ctx the parse tree
	 */
	void exitState(@NotNull SplittingTreeParser.StateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SplittingTreeParser#splitting_tree}.
	 * @param ctx the parse tree
	 */
	void enterSplitting_tree(@NotNull SplittingTreeParser.Splitting_treeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SplittingTreeParser#splitting_tree}.
	 * @param ctx the parse tree
	 */
	void exitSplitting_tree(@NotNull SplittingTreeParser.Splitting_treeContext ctx);
}