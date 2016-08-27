package tools.antlr4;// Generated from DotMealy.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DotMealyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DotMealyVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#graph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph(DotMealyParser.GraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#mealy_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMealy_list(DotMealyParser.Mealy_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#mealy_trans}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMealy_trans(DotMealyParser.Mealy_transContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#mealy_attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMealy_attrs(DotMealyParser.Mealy_attrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#label_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel_name(DotMealyParser.Label_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#edge}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdge(DotMealyParser.EdgeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#state}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitState(DotMealyParser.StateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#edgeop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdgeop(DotMealyParser.EdgeopContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#trans}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrans(DotMealyParser.TransContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(DotMealyParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(DotMealyParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotMealyParser#output}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutput(DotMealyParser.OutputContext ctx);
}