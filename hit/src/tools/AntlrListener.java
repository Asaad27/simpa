package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import tools.antlr4.DotMealyBaseListener;
import tools.antlr4.DotMealyParser;

public class AntlrListener extends DotMealyBaseListener {

	Map<String, ArrayList> transation = new HashMap<String, ArrayList>();
	ArrayList<ArrayList> set = new ArrayList<ArrayList>();

	@Override
	public void enterGraph(DotMealyParser.GraphContext ctx) {
		ArrayList<String> value = new ArrayList<String>();
		System.err.println("start-------" + ctx.ID().getText());
		String dotName = ctx.ID().getText();
		value.add(dotName);
		transation.put("GNAME", value);
	}

	@Override
	public void exitGraph(DotMealyParser.GraphContext ctx) {
		transation.put("TRANSALTION", set);
		System.err.println("fin-------" + ctx.ID().getText());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitMealy_list(DotMealyParser.Mealy_listContext ctx) {
//		System.out.println("Mealy_list>>>" + ctx.getChildCount());

	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	@Override
	public void exitMealy_trans(DotMealyParser.Mealy_transContext ctx) {
		ArrayList<String> value = new ArrayList<String>();
		if (ctx.getChildCount() > 3) {
			String node = ctx.getChild(0).getText();
			String io = ctx.getChild(2).getText();
			if (node.contains("->") && io.contains("/")) {

				String statein = ctx.getChild(0).getChild(0).getText();
				value.add(statein);
				String stateout = ctx.getChild(0).getChild(2).getText();
				value.add(stateout);
				String input = ctx.getChild(2).getChild(3).getText();
				value.add(input);
				String output = ctx.getChild(2).getChild(5).getText();
				value.add(output);
				// transation.put("TRANSALTION", value);
				set.add(value);
			}
			if (!node.contains("->") && io.contains("=")) {
				String initStates = ctx.getChild(0).getChild(0).getText();
				value.add(initStates);
				String label = ctx.getChild(2).getChild(0).getText();
				value.add(label);
				String val = ctx.getChild(2).getChild(2).getText();
				value.add(val);
				// System.out.println(" " + initStates + " " + label + " " +
				// val);
				transation.put("INIT", value);
			}
		}

	}

	// @Override
	// public void exitAttr_list(DotMealyParser.Attr_listContext ctx) {
	// }

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	@Override
	public void exitMealy_attrs(DotMealyParser.Mealy_attrsContext ctx) {
		 }

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitLabel_name(DotMealyParser.Label_nameContext ctx) {
		 
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitEdge(DotMealyParser.EdgeContext ctx) {
	 
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitState(DotMealyParser.StateContext ctx) {

 
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitTrans(DotMealyParser.TransContext ctx) {
 
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitValue(DotMealyParser.ValueContext ctx) {

	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitInput(DotMealyParser.InputContext ctx) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitOutput(DotMealyParser.OutputContext ctx) {

	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	@Override
	public void visitTerminal(TerminalNode node) {


	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	@Override
	public void visitErrorNode(ErrorNode node) {
	}

	public void allElements(ErrorNode node) {

	}

}
