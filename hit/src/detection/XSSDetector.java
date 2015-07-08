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
import com.gargoylesoftware.htmlunit.WebRequest;
import drivers.efsm.real.GenericDriver;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import learner.efsm.table.LiDataTableItem;
import org.apache.commons.lang3.StringUtils;
import tools.Utils;
import tools.loggers.LogManager;

public class XSSDetector {

	/**
	 * Parameters values that are not used to seach reflections
	 */
	private final ArrayList<String> ignoredValues;
	/**
	 * Minimal size of parameters value that are used to search reflections
	 */
	private final int MINIMAL_SIZE = 4;
	/**
	 * Input sequence + Output to test for a reflection
	 */
	private final LinkedList<SimplifiedDataItem> itemsToCheck;
	/**
	 * Potential reflections found
	 */
	private final List<Reflection> potentialReflectionsFound;
	/**
	 * Confirmed reflections
	 */
	private final List<Reflection> reflectionsFound;
	/**
	 * Driver used to test input in the system
	 */
	private final GenericDriver driver;

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
		return outputValue.toLowerCase().contains(inputValue.toLowerCase());
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
		/**
		 * The output symbol where we expect to find thr reflected value
		 */
		private final String expectedOutputSymbol;
		/**
		 * True if the reflection has been confirmed, or tested for XSS
		 */
		private boolean hasBeenTested = false;

		public Reflection(List<ParameterizedInput> path, int inputElementIndex, int inputElementParamIndex, int outputElementParamIndex, String outputSymbol) {
			this.path = path;
			this.inputElementIndex = inputElementIndex;
			this.inputElementParamIndex = inputElementParamIndex;
			this.outputElementParamIndex = outputElementParamIndex;
			this.expectedOutputSymbol = outputSymbol;
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

		@Override
		public Reflection clone() {
			List<ParameterizedInput> pathClone = new LinkedList<>();
			for (ParameterizedInput pi : path) {
				pathClone.add(pi.clone());
			}
			return new Reflection(pathClone, inputElementIndex, inputElementParamIndex, outputElementParamIndex, expectedOutputSymbol);
		}
	}

	public XSSDetector(ArrayList<String> ignoredValues, GenericDriver driver) {
		this.ignoredValues = ignoredValues;
		this.itemsToCheck = new LinkedList<>();
		this.potentialReflectionsFound = new LinkedList<>();
		this.reflectionsFound = new LinkedList<>();
		this.driver = driver;
	}

	/**
	 * Record an observation made for the data table, and store a succinct copy
	 * of it. Make a clone of the data in order not to alter the inference
	 *
	 * @param dti The observation to save
	 * @param pis The sequence that has been sent to the system to obtain the
	 * observation
	 */
	public void recordItem(LiDataTableItem dti, ParameterizedInputSequence pis) {
		ParameterizedInputSequence pisClone = pis.clone();
		LiDataTableItem dtiClone = (LiDataTableItem) dti.clone();
		SimplifiedDataItem simplifiedDataItem = new SimplifiedDataItem(pisClone, new ParameterizedOutput(dtiClone.getOutputSymbol(), dtiClone.getOutputParameters()));
		itemsToCheck.add(simplifiedDataItem);
	}

	/**
	 * Use the recorded data to search potential reflections.
	 *
	 * @return True if at least one reflection was found, false otherwise
	 */
	public boolean detectReflections() {
		boolean reflectionsHaveBeenFound = false;
		/* Iterate on the new items */
		while (!itemsToCheck.isEmpty()) {
			SimplifiedDataItem currentItem = itemsToCheck.pop();
			List<Parameter> outputParameters = currentItem.result.getParameters();
			for (Parameter outputParameter : outputParameters) {
				String outputValue = outputParameter.value;
				if (ignoredValues.contains(outputValue)) {
					continue;
				}

				/* Iterate on the sequence of inputs */
				List<ParameterizedInput> path = currentItem.path.sequence;
				for (ParameterizedInput pi : path) {
					List<Parameter> piParameters = pi.getParameters();

					/* Iterate on the different parameter of the input */
					for (Parameter param : piParameters) {
						String inputValue = param.value;

						if (inputValue.length() < MINIMAL_SIZE && ignoredValues.contains(inputValue)) {
							continue;
						}

						if (isReflected(inputValue, outputValue)) {
							Reflection newReflection
									= new Reflection(currentItem.path.sequence,
											path.indexOf(pi),
											piParameters.indexOf(param),
											outputParameters.indexOf(outputParameter),
											currentItem.result.getOutputSymbol());

							boolean foundSimilar = false;
							for (Reflection reflection : potentialReflectionsFound) {
								if (newReflection.path.containsAll(reflection.path)) {
									foundSimilar = true;
									break;
								}
							}

							if (!foundSimilar && !potentialReflectionsFound.contains(newReflection)) {
								LogManager.logInfo("[XSS] Potential reflection found : ");
								LogManager.logInfo("[XSS]\t" + (newReflection.inputElementParamIndex + 1) + "th parameter of input "
										+ currentItem.path.sequence.get(newReflection.inputElementIndex));
								LogManager.logInfo("[XSS]\tis reflected in the " + (newReflection.outputElementParamIndex + 1) + "th parameter of the output "
										+ currentItem.result);
								LogManager.logInfo("[XSS]\t" + "in the sequence :");
								LogManager.logInfo(currentItem.path.toString());

								potentialReflectionsFound.add(newReflection);
								reflectionsHaveBeenFound = true;
							} else {
								filtered++;
							}
						}
					}
				}
			}
		}
		return reflectionsHaveBeenFound;
	}

	public void confirmReflections() {
		for (Reflection potentialReflection : potentialReflectionsFound) {
			if (potentialReflection.hasBeenTested) {
				continue;
			} else {
				potentialReflection.hasBeenTested = true;
			}
			//Generate a random alpha numeric string to test the reflection again
			String randomString = Utils.randAlphaNumString(10);

			//retrieve the input parameter that is to be reflected
			List<ParameterizedInput> sequence = potentialReflection.path;
			Parameter inputParameter = sequence.get(potentialReflection.inputElementIndex)
					.getParameters().get(potentialReflection.inputElementParamIndex);

			//replace its value by the randomly generated one
			inputParameter.value = randomString;

			//test if this value is still reflected
			driver.reset();
			ParameterizedOutput result = null;
			for (ParameterizedInput pi : sequence) {
				result = driver.execute(pi);
			}

			if (result != null
					&& result.getOutputSymbol().equals(potentialReflection.expectedOutputSymbol)) {
				String outoutParameterValue = result
						.getParameterValue(potentialReflection.outputElementParamIndex);
				if (isReflected(inputParameter.value, outoutParameterValue)) {
					Reflection reflectionClone = potentialReflection.clone();
					reflectionClone.hasBeenTested = false;
					reflectionsFound.add(reflectionClone);
					LogManager.logInfo("Reflection confirmed");
				} else {
					LogManager.logInfo("Reflection disconfirmed");
				}
			} else {
				LogManager.logInfo("Reflection disconfirmed");
			}
		}

	}

	private static final String[] payloads = {
		"'';!--\"<XSS>=&{()}",
		"<script>alert(\"XSS\");</script>",
		"<script>alert(\'XSS\');</script>"
	};
	private static final String[] payloadsExpectedResults = {
		"<XSS",
		"<script>alert(\"XSS\");</script>",
		"<script>alert(\'XSS\');</script>"
	};

	public void testReflections() {
		for (Reflection r : reflectionsFound) {
			if (r.hasBeenTested) {
				continue;
			} else {
				r.hasBeenTested = true;
			}
			Reflection reflection = r.clone();

			String startPattern = Utils.randAlphaNumString(4);
			String endPattern = Utils.randAlphaNumString(4);

			for (int indexPayload = 0; indexPayload < payloads.length; indexPayload++) {
				String xssPayload = payloads[indexPayload];
				String expectedReflection = payloadsExpectedResults[indexPayload];
				StringBuilder completePayload = new StringBuilder(startPattern);
				completePayload.append(xssPayload);
				completePayload.append(endPattern);

				for (int i = 0; i < reflection.path.size(); i++) {
					ParameterizedInput pi = reflection.path.get(i);
					if (i == reflection.inputElementIndex) {
						pi.getParameters().get(reflection.inputElementParamIndex).value = completePayload.toString();
					}
					String response = driver.submit(pi, true);

					if (response.toLowerCase().contains(expectedReflection.toLowerCase())) {
						LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' reflected as \'" + expectedReflection + "\', as exepected");
					}

					/* Both patterns were found : the data in between should contain our payload */
					if (response.contains(startPattern) && response.contains(endPattern)) {
						int indexStartFilteredPayload = response.indexOf(startPattern) + startPattern.length();
						int indexEndFilteredPayload = response.indexOf(endPattern);
						String filteredPayload = response.substring(indexStartFilteredPayload, indexEndFilteredPayload); //TODO : handle multiple reflexions
						LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' reflected as \'" + filteredPayload + "\'");
						LogManager.logInfo("[XSS] Score :" + StringUtils.getLevenshteinDistance(filteredPayload, xssPayload));

						/* Only one pattern was found, our payload should be located before or after */
					} else if (response.contains(startPattern) ^ response.contains(endPattern)) {
						String stringAroundPattern = null;
						if (response.contains(startPattern)) {
							int indexStartFilteredPayload = response.indexOf(startPattern) + startPattern.length();
							stringAroundPattern = response.substring(
									indexStartFilteredPayload,
									indexStartFilteredPayload + xssPayload.length() + 10)
									+ "...";
						} else {
							int indexEndFilteredPayload = response.indexOf(endPattern) - 1;
							stringAroundPattern = "..."
									+ response.substring(
											indexEndFilteredPayload - xssPayload.length() - 10,
											indexEndFilteredPayload);
						}
						LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' was probably found in \'" + stringAroundPattern + "\'");
						//TODO : find behaviour
					/* None of the patterns were found : the payload has probably been filtered entirely */
					} else {
						LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' was not found");
						//TODO : find behaviour
					}

					ParameterizedOutput responsePO = driver.htmlToParameterizedOutput(response);
					if (!responsePO.getOutputSymbol().equals(reflection.expectedOutputSymbol)) {
						LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' do not produce the expected page");
						//TODO : find behaviour
					}
				}

				indexPayload++;
			}
		}
	}

}
