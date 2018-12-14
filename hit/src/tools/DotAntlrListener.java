package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

import tools.antlr4.DotMealy.DotMealyBaseListener;
import tools.antlr4.DotMealy.DotMealyParser;

public class DotAntlrListener extends DotMealyBaseListener {
	private Map<String, String> allInputs = new HashMap<>();
	private Map<String, String> allOutputs = new HashMap<>();

	public class ParseException extends RuntimeException {
		private static final long serialVersionUID = 3326922575373002881L;

		public ParseException(String what) {
			super(what);
		}
	}

	class WaitingEdge {
		String start;
		String end;
		String input;
		String output;
	}

	static protected String getNodeId(ParserRuleContext upperCtx) {
		DotMealyParser.Node_idContext nodeIdCtx = upperCtx.getRuleContext(
				DotMealyParser.Node_idContext.class, 0);
		assert nodeIdCtx != null;
		return getId(nodeIdCtx);
	}

	/**
	 * take the first ID of a context
	 * 
	 * @see #getId(ParserRuleContext,int)
	 */
	static protected String getId(ParserRuleContext upperCtx) {
		return getId(upperCtx, 0);
	}

	/**
	 * IDs in dot files can be expressed in different forms (quoted string, HTML
	 * string or simple word) with a different syntax.
	 * 
	 * @param upperCtx
	 *            the context containing the ID
	 * @param index
	 *            the index of the ID to return (
	 *            {@link #getId(ParserRuleContext) optional})
	 * @return the ID (without external quotes or other dot delimiter)
	 */
	static protected String getId(ParserRuleContext upperCtx, int index) {
		DotMealyParser.IdContext idCtx = upperCtx.getRuleContext(
				DotMealyParser.IdContext.class, index);
		assert idCtx != null;
		if (idCtx.DOUBLE_QUOTED_STRING() != null) {
			String full = idCtx.DOUBLE_QUOTED_STRING().getText();
			return full.substring(1, full.length() - 1).replace("\\\"", "\"");
		} else if (idCtx.HTML_STRING() != null) {
			String full = idCtx.HTML_STRING().getText();
			return full.substring(1, full.length() - 1);
		} else
			return idCtx.getText();

	}

	Mealy automaton = null;

	String name = "";
	String startNode = null;
	// store attributes definitions for each node
	Map<String, List<DotMealyParser.A_listContext>> nodeDeclarations = new HashMap<>();
	List<WaitingEdge> waitingEdges = new ArrayList<>();
	String firstStateDefined = null;// first state seen and defined as a state
	String firstStateUsed = null;// first state seen in a transition
	String startEdgeNode = null;// start state for next transition
	String edgeInput = null;// input symbol for next transition
	String edgeOutput = null;// output symbol for next transition

	@Override
	public void enterGraph(DotMealyParser.GraphContext ctx) {
		if (ctx.getChild(DotMealyParser.IdContext.class, 0) != null)
			name = getId(ctx);
		else
			name = "unamed dot graph";
	}

	@Override
	public void exitGraph(DotMealyParser.GraphContext ctx) {
		searchInitialState();
		buildAutomaton();
	}

	private void searchInitialState() {
		// first, try to find a transition '__start0 -> someState'
		final String START_NODE = "__start0";
		WaitingEdge found = null;
		for (WaitingEdge w : waitingEdges) {
			if (w.start.equals(START_NODE)) {
				if (found != null)
					throw new ParseException(
							"There is a node named "
									+ START_NODE
									+ " but it has two output transitions instead of only one.");
				startNode = w.end;
				found = w;
			}
		}
		if (found != null) {
			waitingEdges.remove(found);
			nodeDeclarations.remove(START_NODE);
		} else {
			// if first state is not defined, try to find a state with different
			// attribute declaration
			assert startNode == null;
			for (Entry<String, List<DotMealyParser.A_listContext>> pair : nodeDeclarations
					.entrySet()) {
				List<DotMealyParser.A_listContext> attributes = pair.getValue();
				if (!attributes.isEmpty()) {
					if (startNode != null && startNode != pair.getKey()) {
						System.err.println(
								"Two states (at least) have attributes. We cannot use attribute presence to detect initial state");
						startNode = null;
						break;
					}
					startNode = pair.getKey();
				}
			}
		}
		if (startNode == null) {
			// if initial state is still undefined, take the first declared or
			// first used state
			if (firstStateDefined != null) {
				startNode = firstStateDefined;
				System.err
						.println(
								"Warning : using first state defined as initial state of the automaton.");
			} else {
				startNode = firstStateUsed;
				assert firstStateUsed != null;
				System.err
						.println(
								"Warning : using state of first transition as initial state of the automaton.");
			}
			System.out.println(startNode);
		}

	}

	private void buildAutomaton() {
		automaton = new Mealy("dot_file(" + name + ")");
		Map<String, State> states = new HashMap<>();
		Set<String> inputs = new HashSet<>();
		// first, create states declared in node statements
		for (Entry<String, List<DotMealyParser.A_listContext>> entry : nodeDeclarations
				.entrySet()) {
			String stateName = entry.getKey();
			String visualName = stateName;
			List<DotMealyParser.A_listContext> attributes = entry.getValue();
			for (DotMealyParser.A_listContext aList : attributes) {
				String key = getId(aList);
				if (key.equalsIgnoreCase("label")) {
					visualName = getId(aList, 1);
				}
			}
			State s = new State(visualName, stateName.equals(startNode));
			states.put(stateName, s);
			automaton.addState(s);
		}
		// if the initial state was not declared in a node statement, create it
		// in conjecture
		if (!states.containsKey(startNode)) {
			State s = new State(startNode, true);
			states.put(startNode, s);
			automaton.addState(s);
		}
		// add transitions and create missing states
		for (WaitingEdge e : waitingEdges) {
			assert e.end != null && e.start != null;
			if (e.input == null || e.output == null)
				throw new ParseException(
						"input/output not defined for transition from "
								+ e.start + " to " + e.end + ".");
			State from = states.get(e.start);
			if (from == null) {
				from = new State(e.start, false);
				automaton.addState(from);
				states.put(e.start, from);
			}
			State to = states.get(e.end);
			if (to == null) {
				to = new State(e.end, false);
				automaton.addState(to);
				states.put(e.end, to);
			}
			automaton.addTransition(new MealyTransition(automaton, from, to,
					e.input, e.output));
			inputs.add(e.input);
		}
		// check completion of builded automaton
		for (String i : inputs) {
			for (State s : states.values())
				if (automaton.getTransitionFromWithInput(s, i) == null)
					throw new ParseException("automaton is not complete");
		}
	}

	@Override
	public void enterNode_stmt(DotMealyParser.Node_stmtContext ctx) {
		String id = getNodeId(ctx);
		List<DotMealyParser.A_listContext> attributes;
		if (nodeDeclarations.containsKey(id)) {
			attributes = nodeDeclarations.get(id);
		} else {
			attributes = new ArrayList<>();
			nodeDeclarations.put(id, attributes);
		}
		DotMealyParser.Attr_listContext attrs = ctx.getRuleContext(
				DotMealyParser.Attr_listContext.class, 0);
		while (attrs != null) {
			DotMealyParser.A_listContext aList = attrs.getRuleContext(
					DotMealyParser.A_listContext.class, 0);
			while (aList != null) {
				attributes.add(aList);
				aList = aList.getRuleContext(
						DotMealyParser.A_listContext.class, 0);
			}
			attrs = attrs.getRuleContext(DotMealyParser.Attr_listContext.class,
					0);
		}
	}

	@Override
	public void enterEdge_stmt(DotMealyParser.Edge_stmtContext ctx) {
		assert startEdgeNode == null;

		if (ctx.getRuleContext(DotMealyParser.Node_idContext.class, 0) != null) {
			startEdgeNode = getNodeId(ctx);
		} else {
			DotMealyParser.SubgraphContext subgraphCtx = ctx.getRuleContext(
					DotMealyParser.SubgraphContext.class, 0);
			assert subgraphCtx != null;
			startEdgeNode = getNodeId(subgraphCtx);
		}
		assert startEdgeNode != null;
		if (firstStateUsed == null)
			firstStateUsed = startEdgeNode;
		DotMealyParser.Attr_listContext attrs = ctx.getRuleContext(
				DotMealyParser.Attr_listContext.class, 0);
		String label = null;
		while (attrs != null) {
			DotMealyParser.A_listContext aList = attrs.getRuleContext(
					DotMealyParser.A_listContext.class, 0);
			while (aList != null) {
				String key = getId(aList);
				if (key.equalsIgnoreCase("label")) {
					if (label != null)
						throw new ParseException(
								"label is defined more than one time");
					label = getId(aList, 1);
				}
				aList = aList.getRuleContext(
						DotMealyParser.A_listContext.class, 0);
			}
			attrs = attrs.getRuleContext(DotMealyParser.Attr_listContext.class,
					0);

		}
		if (label != null) {
			String[] split = label.split("/");
			if (split.length > 2) {
				// in case where label is "input / output1/output2" we use
				// "input" and "output1/output2" as tokens
				String[] split2 = label.split(" / ");
				if (split2.length == 2)
					split = split2;
			}
			if (split.length > 2) {
				// trying to ignore '/' in html tags
				boolean fail = false;
				int opening = 0;
				int closing = 0;
				List<String> split2 = new ArrayList<>();
				int splitStart = 0;
				for (int i = 0; i < label.length() && !fail; i++) {
					if (label.charAt(i) == '<') {
						opening++;
						if (opening > closing + 1)
							fail = true;
					} else if (label.charAt(i) == '>') {
						closing++;
						if (closing > opening)
							fail = true;
					} else if (label.charAt(i) == '/') {
						if (opening == closing) {
							split2.add(label.substring(splitStart, i));
							splitStart = i + 1;
						}
					}
				}
				split2.add(label.substring(splitStart, label.length()));

				if (split2.size() == 2 && !fail) {
					split = new String[2];
					split2.toArray(split);
				}
			}
			if (split.length == 2) {
				edgeInput = split[0];
				edgeOutput = split[1];
				if (edgeInput.endsWith(" "))
					edgeInput = edgeInput.substring(0, edgeInput.length() - 1);
				if (edgeOutput.startsWith(" "))
					edgeOutput = edgeOutput.substring(1);
				// now check if the string already exist to share the same
				// object in order to accelerate comparison
				if (!allInputs.containsKey(edgeInput))
					allInputs.put(edgeInput, edgeInput);
				if (!allOutputs.containsKey(edgeOutput))
					allOutputs.put(edgeOutput, edgeOutput);
				edgeInput = allInputs.get(edgeInput);
				edgeOutput = allOutputs.get(edgeOutput);
			} else
				throw new ParseException("label must be 'input'/'output'");
		}

	}

	@Override
	public void enterEdgeRHS(DotMealyParser.EdgeRHSContext ctx) {
		WaitingEdge waitingEdge = new WaitingEdge();
		waitingEdge.start = startEdgeNode;
		String end;
		if (ctx.getRuleContext(DotMealyParser.Node_idContext.class, 0) != null) {
			end = getNodeId(ctx);
		} else {
			DotMealyParser.SubgraphContext subgraphCtx = ctx.getRuleContext(
					DotMealyParser.SubgraphContext.class, 0);
			assert subgraphCtx != null;
			end = getNodeId(subgraphCtx);
		}
		waitingEdge.end = end;
		waitingEdge.input = edgeInput;
		waitingEdge.output = edgeOutput;
		waitingEdges.add(waitingEdge);
	}

	@Override
	public void exitEdge_stmt(DotMealyParser.Edge_stmtContext ctx) {
		startEdgeNode = null;
		edgeInput = null;
		edgeOutput = null;
	}

	@Override
	public void enterSubgraph(DotMealyParser.SubgraphContext ctx) {
		throw new ParseException("subgraphs are not handled");
	}
}