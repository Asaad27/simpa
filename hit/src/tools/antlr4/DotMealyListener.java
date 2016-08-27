package tools.antlr4;// Generated from DotMealy.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DotMealyParser}.
 */
public interface DotMealyListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(DotMealyParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(DotMealyParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_list}.
	 * @param ctx the parse tree
	 */
	void enterMealy_list(DotMealyParser.Mealy_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_list}.
	 * @param ctx the parse tree
	 */
	void exitMealy_list(DotMealyParser.Mealy_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_trans}.
	 * @param ctx the parse tree
	 */
	void enterMealy_trans(DotMealyParser.Mealy_transContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_trans}.
	 * @param ctx the parse tree
	 */
	void exitMealy_trans(DotMealyParser.Mealy_transContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#mealy_attrs}.
	 * @param ctx the parse tree
	 */
	void enterMealy_attrs(DotMealyParser.Mealy_attrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#mealy_attrs}.
	 * @param ctx the parse tree
	 */
	void exitMealy_attrs(DotMealyParser.Mealy_attrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#label_name}.
	 * @param ctx the parse tree
	 */
	void enterLabel_name(DotMealyParser.Label_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#label_name}.
	 * @param ctx the parse tree
	 */
	void exitLabel_name(DotMealyParser.Label_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#edge}.
	 * @param ctx the parse tree
	 */
	void enterEdge(DotMealyParser.EdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#edge}.
	 * @param ctx the parse tree
	 */
	void exitEdge(DotMealyParser.EdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#state}.
	 * @param ctx the parse tree
	 */
	void enterState(DotMealyParser.StateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#state}.
	 * @param ctx the parse tree
	 */
	void exitState(DotMealyParser.StateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterEdgeop(DotMealyParser.EdgeopContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitEdgeop(DotMealyParser.EdgeopContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#trans}.
	 * @param ctx the parse tree
	 */
	void enterTrans(DotMealyParser.TransContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#trans}.
	 * @param ctx the parse tree
	 */
	void exitTrans(DotMealyParser.TransContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(DotMealyParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(DotMealyParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(DotMealyParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(DotMealyParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link DotMealyParser#output}.
	 * @param ctx the parse tree
	 */
	void enterOutput(DotMealyParser.OutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link DotMealyParser#output}.
	 * @param ctx the parse tree
	 */
	void exitOutput(DotMealyParser.OutputContext ctx);
}