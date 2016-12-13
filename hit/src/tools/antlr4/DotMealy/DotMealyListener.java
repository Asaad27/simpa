// Generated from DotMealy.g4 by ANTLR 4.4
package tools.antlr4.DotMealy;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DotMealyParser}.
 */
public interface DotMealyListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#output}.
	 * @param ctx the parse tree
	 */
	void enterOutput(@NotNull DotMealyParser.OutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#output}.
	 * @param ctx the parse tree
	 */
	void exitOutput(@NotNull DotMealyParser.OutputContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(@NotNull DotMealyParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(@NotNull DotMealyParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_trans}.
	 * @param ctx the parse tree
	 */
	void enterMealy_trans(@NotNull DotMealyParser.Mealy_transContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_trans}.
	 * @param ctx the parse tree
	 */
	void exitMealy_trans(@NotNull DotMealyParser.Mealy_transContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterEdge(@NotNull DotMealyParser.EdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitEdge(@NotNull DotMealyParser.EdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterEdgeop(@NotNull DotMealyParser.EdgeopContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitEdgeop(@NotNull DotMealyParser.EdgeopContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_list}.
	 * @param ctx the parse tree
	 */
	void enterMealy_list(@NotNull DotMealyParser.Mealy_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_list}.
	 * @param ctx the parse tree
	 */
	void exitMealy_list(@NotNull DotMealyParser.Mealy_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_attrs}.
	 * @param ctx the parse tree
	 */
	void enterMealy_attrs(@NotNull DotMealyParser.Mealy_attrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_attrs}.
	 * @param ctx the parse tree
	 */
	void exitMealy_attrs(@NotNull DotMealyParser.Mealy_attrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#state}.
	 * @param ctx the parse tree
	 */
	void enterState(@NotNull DotMealyParser.StateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#state}.
	 * @param ctx the parse tree
	 */
	void exitState(@NotNull DotMealyParser.StateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#label_name}.
	 * @param ctx the parse tree
	 */
	void enterLabel_name(@NotNull DotMealyParser.Label_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#label_name}.
	 * @param ctx the parse tree
	 */
	void exitLabel_name(@NotNull DotMealyParser.Label_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(@NotNull DotMealyParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(@NotNull DotMealyParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(@NotNull DotMealyParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(@NotNull DotMealyParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#trans}.
	 * @param ctx the parse tree
	 */
	void enterTrans(@NotNull DotMealyParser.TransContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#trans}.
	 * @param ctx the parse tree
	 */
	void exitTrans(@NotNull DotMealyParser.TransContext ctx);
}