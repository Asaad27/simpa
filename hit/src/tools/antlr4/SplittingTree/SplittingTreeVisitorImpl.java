package tools.antlr4.SplittingTree;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.localizerBased.SplittingTree;

public class SplittingTreeVisitorImpl extends
		SplittingTreeBaseVisitor<SplittingTree> {

	private InputSequence parseInputSequence(
			SplittingTreeParser.Input_sequenceContext ctx) {
		InputSequence seq = new InputSequence();
		addInputSequence(ctx, seq);
		return seq;
	}

	private void addInputSequence(
			SplittingTreeParser.Input_sequenceContext ctx, InputSequence seq) {
		seq.addInput(ctx.input().getText());
		if (ctx.input_sequence() != null)
			addInputSequence(ctx.input_sequence(), seq);
	}

	private OutputSequence parseOutputSequence(
			SplittingTreeParser.Output_sequenceContext ctx) {
		OutputSequence seq = new OutputSequence();
		addOutputSequence(ctx, seq);
		return seq;
	}

	private void addOutputSequence(
			SplittingTreeParser.Output_sequenceContext ctx, OutputSequence seq) {
		seq.addOutput(ctx.output().getText());
		if (ctx.output_sequence() != null)
			addOutputSequence(ctx.output_sequence(), seq);
	}

	private void create_children(SplittingTreeParser.Splitting_treeContext ctx,
			SplittingTree current) {
		for (int i = 0; i < ctx.splitting_tree().size(); i++) {
			create_child(ctx.splitting_tree(i), current,
					parseOutputSequence(ctx.output_sequence(i)));
		}
	}

	private void create_child(SplittingTreeParser.Splitting_treeContext ctx,
			SplittingTree parent, OutputSequence out) {
		SplittingTree current;
		if (ctx.input_sequence() == null)
			current = new SplittingTree(parent, out);
		else
			current = new SplittingTree(parent, out,
					parseInputSequence(ctx.input_sequence()));
		if (ctx.getChildCount() > 0) {
			create_children(ctx, current);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.
	 * </p>
	 */
	@Override
	public SplittingTree visitSplitting_tree(
			SplittingTreeParser.Splitting_treeContext ctx) {

		SplittingTree current = new SplittingTree(
				parseInputSequence(ctx.input_sequence()));
		create_children(ctx, current);

		return current;
	}

}
