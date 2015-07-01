/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detection;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import learner.efsm.table.LiDataTableItem;

public class XSSDetector {

	/**
	 * Parameters values that are not used to seach reflections
	 */
	private final ArrayList<String> ignoredValues;
	/**
	 * Minimal size of parameters value that are used to search reflections
	 */
	private final int MINIMAL_SIZE = 5;
	/**
	 * Input sequence + Output to test for a reflection
	 */
	private final LinkedList<SimplifiedDataItem> itemsToCheck;

	private final List<Reflection> reflectionsFound;
	private int filtered = 0;

	/**
	 * Method that defines what is a reflection. Basically, we search an output
	 * that contains exactly a input
	 *
	 * @param inputValue
	 * @param outputValue
	 * @return true if a reflection is found, false otherwise
	 */
	private boolean isReflected(String inputValue, String outputValue) {
		return outputValue.contains(inputValue);
	}

	/**
	 * Class that represents a couple 'input/output'
	 */
	private class SimplifiedDataItem {

		private final ParameterizedInputSequence path;
		private final ParameterizedOutput result;

		private SimplifiedDataItem(ParameterizedInputSequence path, ParameterizedOutput result) {
			this.path = path;
			this.result = result;
		}

	}

	/**
	 * Class that caracterizes a reflection
	 */
	private class Reflection {

		/**
		 * The input sequence that triggers the reflection
		 */
		private final List<ParameterizedInput> path;
		/**
		 * The index of the parameterized input concerned by the reflection
		 */
		private final int inputElementIndex;
		/**
		 * The index of the input parameter concerned by the reflection
		 */
		private final int inputElementParamIndex;
		/**
		 * The index of the output parameter concerned by the reflection
		 */
		private final int outputElementParamIndex;

		public Reflection(List<ParameterizedInput> path, int inputElementIndex, int inputElementParamIndex, int outputElementParamIndex) {
			this.path = path;
			this.inputElementIndex = inputElementIndex;
			this.inputElementParamIndex = inputElementParamIndex;
			this.outputElementParamIndex = outputElementParamIndex;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + Objects.hashCode(this.path);
			hash = 31 * hash + this.inputElementIndex;
			hash = 31 * hash + this.inputElementParamIndex;
			hash = 31 * hash + this.outputElementParamIndex;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Reflection other = (Reflection) obj;
			if (!Objects.equals(this.path, other.path)) {
				return false;
			}
			if (this.inputElementIndex != other.inputElementIndex) {
				return false;
			}
			if (this.inputElementParamIndex != other.inputElementParamIndex) {
				return false;
			}
			if (this.outputElementParamIndex != other.outputElementParamIndex) {
				return false;
			}
			return true;
		}

	}

	public XSSDetector(ArrayList<String> ignoredValues) {
		this.ignoredValues = ignoredValues;
		this.itemsToCheck = new LinkedList<>();
		this.reflectionsFound = new LinkedList<>();
	}

	public void recordItem(LiDataTableItem dti, ParameterizedInputSequence pis) {
		SimplifiedDataItem simplifiedDataItem = new SimplifiedDataItem(pis, new ParameterizedOutput(dti.getOutputSymbol(), dti.getOutputParameters()));
		itemsToCheck.add(simplifiedDataItem);

		if (itemsToCheck.size() > 00) {
			detect();
		}
	}

	public void detect() {
		//System.out.println("Detection started");
		long startTime = System.currentTimeMillis();
		/* Iterate on the new items */
		while (!itemsToCheck.isEmpty()) {
			SimplifiedDataItem currentItem = itemsToCheck.pop();
			List<Parameter> outputParameters = currentItem.result.getParameters();
			for (Parameter outputParameter : outputParameters) {
				String outputValue = outputParameter.value;
				if (outputValue.length() < MINIMAL_SIZE && ignoredValues.contains(outputValue)) {
					continue;
				}

				/* Iterate on the sequence of inputs */
				List<ParameterizedInput> path = currentItem.path.sequence;
				for (ParameterizedInput pi : path) {
					List<Parameter> piParameters = pi.getParameters();

					/* Iterate on the different parameter of the input */
					for (Parameter param : piParameters) {
						String inputValue = param.value;

						if (outputValue.length() < MINIMAL_SIZE && ignoredValues.contains(inputValue)) {
							continue;
						}

						if (isReflected(inputValue, outputValue)) {
							Reflection newReflection
									= new Reflection(currentItem.path.sequence,
											path.indexOf(pi),
											piParameters.indexOf(param),
											outputParameters.indexOf(outputParameter));

							boolean foundSimilar = false;
							for (Reflection reflection : reflectionsFound) {
								if (newReflection.path.containsAll(reflection.path)) {
									foundSimilar = true;
									break;
								}
							}

							if (!foundSimilar && !reflectionsFound.contains(newReflection)) {
								System.out.println("Reflexion detected : \'" + inputValue + "\' contained in \'" + outputValue + "\'");
								reflectionsFound.add(newReflection);
								System.out.println("\tPath : " + currentItem.path);
							} else {
								filtered++;
							}
						}
					}
				}
			}
		}
		//System.out.println("Detection finished in " + (System.currentTimeMillis() - startTime));
		//System.out.println(filtered);
	}
}
