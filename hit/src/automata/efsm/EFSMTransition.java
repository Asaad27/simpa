/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime PEYRARD
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 ********************************************************************************/
package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import automata.State;
import automata.Transition;
import drivers.efsm.EFSMDriver.Types;
import learner.efsm.table.LiDataTableItem;
import main.simpa.Options;
import tools.RandomGenerator;

public class EFSMTransition extends Transition {
	private static final long serialVersionUID = -7379789456013157473L;

	protected IOutputFunction outputFunction;
	protected GeneratedOutputFunction genOutputFunction;
	protected String output;
	protected EFSM automata;
	protected List<LiDataTableItem> paramsData;
	public boolean generateNdv = false;

	public class Label implements Serializable {
		private static final long serialVersionUID = -3119005509594327935L;
		private ParameterizedInput input;
		private List<String> predicates;
		private List<String> var;
		private ParameterizedOutput output;

		public Label(String in, String out) {
			input = new ParameterizedInput(in);
			predicates = new ArrayList<String>();
			var = new ArrayList<String>();
			output = new ParameterizedOutput(out);
		}

		public List<String> getPredicates() {
			return predicates;
		}

		public List<String> getOutputFunctions() {
			return var;
		}

		public ParameterizedInput getInput() {
			return input;
		}

		public ParameterizedOutput getOutput() {
			return output;
		}

		public String getName() {
			return input + "-" + output;
		}

		public void addInputParam(Parameter p) {
			input.getParameters().add(p);
		}

		public void addOutputParam(Parameter p) {
			output.getParameters().add(p);
		}

		public void addPredicate(String p) {
			predicates.add(p);
		}

		public void addVar(String v) {
			var.add(v);
		}
		
		@Override
		public String toString() {
			StringBuffer s = new StringBuffer();
			s.append(input + "\\n");
			Collections.sort(predicates);
			if (predicates.size()>0){
				s.append("("+predicates.get(0)+")");
				for(int i=1; i<predicates.size(); i++) 
					s.append(" " + Options.SYMBOL_OR + " (" + predicates.get(i)+")");
				s.append(",\\n");
			}
			Collections.sort(var);
			if (var.size()>0){
				s.append(var.get(0) + ",");
				for(int i=1; i<var.size(); i++) s.append("\\n" + var.get(i) + ",");
				s.append("\\n");
			}
			return s.append(output).toString().replaceAll("\"", "\\\\\"");
		}
		
		public String toDotString(){
			return this.toString().replaceAll("_[0-9]{5,}", "");
		}
	}

	private EFSMTransition(EFSM automata, State s1, State s2, String input, String output){
		super(s1, s2, input);
		this.output = output;
		this.automata = automata;
	}
	
	public EFSMTransition(EFSM automata, State s1, State s2, String input,
			String output, List<LiDataTableItem> paramsData) {
		this(automata, s1, s2, input, output);
		this.paramsData = paramsData;
	}

	public EFSMTransition(EFSM automata, State s1, State s2, String input,
			String output, IOutputFunction of) {
		this(automata, s1, s2, input, output);
		this.outputFunction = of;
		paramsData = new ArrayList<>();
		if (of instanceof GeneratedOutputFunction)
			((GeneratedOutputFunction) of).getGuard().memory = automata.memory;
	}

	public int getParamsDataCount() {
		return paramsData.size();
	}

	public List<LiDataTableItem> getParamsData() {
		return paramsData;
	}

	public LiDataTableItem getParamsData(int index) {
		return paramsData.get(index);
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String symbol) {
		output = symbol;
	}

	public GeneratedOutputFunction getGenOutputFunction() {
		return genOutputFunction;
	}

	public List<Parameter> getOutputParameters(List<Parameter> inputParameters) {
		return outputFunction.process(automata, inputParameters);
	}

	public Label initializeLabel(TreeMap<String, List<String>> paramNames) {
		Label l = new Label(input, output);
		for (int i = 0; i < paramsData.get(0).getInputParameters().size(); i++) {
			l.addInputParam(new Parameter(paramNames.get(input).get(i),
					Types.STRING));
		}
		for (int i = 0; i < paramsData.get(0).getOutputParameters().size(); i++)
			l.addOutputParam(new Parameter(paramNames.get(output).get(i),
					Types.STRING));
		return l;
	}

	public String toRawDot() {
		StringBuffer dot = new StringBuffer(from + " -> " + to + "[label=\""
				+ input + "/" + output);
		/*
		 * if (outputFunction instanceof GeneratedOutputFunction){
		 * dot.append("\\n" +
		 * ((GeneratedOutputFunction)outputFunction).toString()); }else{ for
		 * (LiDataTableItem dti : paramsData){ dot.append("\\n" + dti); } }
		 */
		dot.append("\"];");
		return dot.toString();
	}

	@Override
	public String toString() {
		return from + " to " + to + " by " + input + "/" + output;
	}

	public String getName() {
		return from + "-" + to;
	}

	public void setGuard(Guard g) {
		if (outputFunction instanceof GeneratedOutputFunction) {
			((GeneratedOutputFunction) outputFunction).setGuard(g);
		}
	}

	public Guard getGuard() {
		if (outputFunction instanceof GeneratedOutputFunction) {
			return ((GeneratedOutputFunction) outputFunction).getGuard();
		}
		return null;
	}

	public ArrayList<Parameter> randomizeGuard(RandomGenerator rand,
			int domainSize) {
		if (outputFunction instanceof GeneratedOutputFunction) {
			return ((GeneratedOutputFunction) outputFunction).getGuard()
					.randomize(rand, domainSize);
		}
		return null;
	}

	public void generateNdv(int nbNdv, RandomGenerator rand) {
		if (outputFunction instanceof GeneratedOutputFunction) {
			((GeneratedOutputFunction) outputFunction).generateNdv(nbNdv, rand);
			generateNdv = true;
		}
	}

	public void checkNdv(int nbNdv, RandomGenerator rand, int domainSize) {
		if (outputFunction instanceof GeneratedOutputFunction) {
			((GeneratedOutputFunction) outputFunction).getGuard()
					.checkNdv(nbNdv, rand, domainSize);
		}
	}
}
