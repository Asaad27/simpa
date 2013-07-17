package tools;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import learner.efsm.LiConjecture;
import automata.State;
import automata.efsm.EFSMTransition;
import automata.efsm.Parameter;
import drivers.efsm.EFSMDriver.Types;

public class XMLModel {
	private String name;
	private LiConjecture conj = null;
	private TreeMap<String, String> arguments, symbols, gsymbols;
	private TreeMap<Types, String> mappingSIMPAXML;

	public XMLModel(String name) {
		this.name = name;
		arguments = new TreeMap<String, String>();
		symbols = new TreeMap<String, String>();
		gsymbols = new TreeMap<String, String>();
		mappingSIMPAXML = new TreeMap<Types, String>();
		mappingSIMPAXML.put(Types.STRING, "int");
		mappingSIMPAXML.put(Types.NUMERIC, "int");
		mappingSIMPAXML.put(Types.NOMINAL, "int");
	}

	public void addSymbol(String name, String type) {
		symbols.put(name, type);
	}

	public void addGlobalSymbol(String name, String type) {
		gsymbols.put(name, type);
	}

	public void addArgument(String name, String type) {
		arguments.put(name, type);
	}

	public String toString() {
		StringBuilder res = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

		StringBuilder header = new StringBuilder("<nta>\n");
		header.append("    <declaration>\n");
		header.append("        /*\n");
		header.append("         * Generated by SIMPA\n");
		header.append("         */\n");
		header.append("    </declaration>\n");
		header.append("    <template>\n");
		header.append("	    <name>" + name + "</name>\n");

		StringBuilder states = new StringBuilder();
		for (State s : conj.getStates()) {
			states.append("        <location id=\"" + s.getName() + "\">\n");
			states.append("            <name>" + s.getName() + "</name>\n");
			states.append("        </location>\n");
		}
		states.append("	    <init ref=\"S0\" />\n");

		StringBuilder transitions = new StringBuilder();
		for (EFSMTransition t : conj.getTransitions()) {
			transitions.append("        <transition>\n");
			transitions.append("            <source ref=\"" + t.getFrom()
					+ "\" />\n");
			transitions.append("            <target ref=\"" + t.getTo()
					+ "\" />\n");

			if (!conj.getLabelForTransition(t).getPredicates().isEmpty()) {
				List<String> predicates = conj.getLabelForTransition(t)
						.getPredicates();
				Collections.sort(predicates);
				transitions.append("            <label kind=\"guard\">"
						+ Utils.joinAndClean(predicates, " | ")
								.replace("&", "and").replace("|", "or")
								.replace("=", "==") + "</label>\n");
			}

			for (Parameter p : conj.getLabelForTransition(t).getInput()
					.getParameters()) {
				symbols.put(p.value, mappingSIMPAXML.get(p.type));
			}

			for (String s : conj.getLabelForTransition(t).getOutputFunctions()) {
				if (s.indexOf("=>") == -1) {
					String[] value = s.split(" = ");
					if (value[1].startsWith("\"Ndv"))
						transitions
								.append("            <label kind=\"assignment\">"
										+ value[0] + " := random();</label>\n");
					else {
						try {
							Float.parseFloat(value[1].substring(1,
									value[1].length() - 1));
							transitions
									.append("            <label kind=\"assignment\">"
											+ value[0]
											+ " := "
											+ value[1].substring(1,
													value[1].length() - 1)
													.trim() + "</label>\n");
						} catch (NumberFormatException e) {
							transitions
									.append("            <label kind=\"assignment\">"
											+ value[0]
											+ " := "
											+ value[1].trim() + "</label>\n");
							addGlobalSymbol(value[1].trim(), "text");
						}
					}
				} else {
					String[] value1 = s.split(" => ");
					String[] value2 = value1[1].split(" = ");
					transitions
							.append("            <label kind=\"assignment\"> "
									+ "if ("
									+ Utils.filter(value1[0])
											.replace("&", "and")
											.replace("|", "or") + "){ "
									+ value2[0] + " := " + value2[1]
									+ "; } </label>\n");
				}
			}

			List<String> paramNames = conj.getParamNames(conj
					.getLabelForTransition(t).getOutput().getOutputSymbol());
			String output = "";
			if (!paramNames.isEmpty())
				output += Utils.capitalize(paramNames.get(0));
			for (int i = 1; i < paramNames.size(); i++)
				output += ", " + Utils.capitalize(paramNames.get(i));
			output = conj.getLabelForTransition(t).getOutput()
					.getOutputSymbol().toLowerCase()
					+ "(" + output + ")";
			transitions.append("            <label kind=\"output\">" + output
					+ "</label>\n");

			for (Parameter p : conj.getLabelForTransition(t).getOutput()
					.getParameters()) {
				symbols.put(p.value, mappingSIMPAXML.get(p.type));
			}

			transitions.append("        </transition>\n");
		}

		StringBuilder declarations = new StringBuilder();
		declarations.append("        <declaration>\n");
		for (String key : gsymbols.keySet()) {
			declarations.append("            " + "int " + key + ";\n");
		}
		for (String key : symbols.keySet()) {
			declarations.append("            " + symbols.get(key) + " "
					+ Utils.capitalize(key) + ";\n");
		}
		declarations.append("        </declaration>\n");

		StringBuilder footer = new StringBuilder();
		footer.append("    </template>\n");
		footer.append("	<system>system " + name + ";</system>\n");
		footer.append("</nta>\n");

		res.append(header);
		res.append(declarations);
		res.append(states);
		res.append(transitions);
		res.append(footer);

		return res.toString();
	}

	public void loadFromEFSM(LiConjecture conjecture) {
		for (String sym : conjecture.gSymbols) {
			gsymbols.put(sym, "int");
		}
		conjecture.cleanMark();
		conj = conjecture;
	}

}
