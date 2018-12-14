package detection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import drivers.efsm.real.GenericDriver;
import learner.efsm.table.LiDataTableItem;
import options.RandomOption;
import tools.StandaloneRandom;
import tools.loggers.LogManager;

public class XSSDetector {

	/**
	 * @TODO should be put in an option tree.
	 */
	RandomOption rand = new StandaloneRandom();

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
				List<ParameterizedInput> path = currentItem.path.clone().sequence;
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
									= new Reflection(currentItem.path.clone().sequence,
											path.indexOf(pi),
											piParameters.indexOf(param),
											outputParameters.indexOf(outputParameter),
											currentItem.result.getOutputSymbol());

							boolean foundSimilar = false;
							for (Reflection reflection : potentialReflectionsFound) {
								if (newReflection.path.containsAll(reflection.path)
										&& newReflection.path.get(newReflection.inputElementIndex).getInputSymbol()
										.equals(
												reflection.path.get(reflection.inputElementIndex).getInputSymbol())
										&& newReflection.inputElementParamIndex == reflection.inputElementParamIndex
										&& newReflection.expectedOutputSymbol.equals(reflection.expectedOutputSymbol)
										&& reflection.outputElementParamIndex == newReflection.outputElementParamIndex) {
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
			String randomString = rand.randAlphaNumString(10);

			//retrieve the input parameter that is to be reflected
			List<ParameterizedInput> sequence = potentialReflection.path;
			Parameter inputParameter = sequence.get(potentialReflection.inputElementIndex)
					.getParameters().get(potentialReflection.inputElementParamIndex);

			//replace its value by the randomly generated one
			inputParameter.value = randomString;

			//test if this value is still reflected
			driver.reset();
			ParameterizedOutput result = null;
			boolean abort = false;
			for (ParameterizedInput pi : sequence) {
				try{
					result = driver.execute(pi);
				/* Sometimes, htmlunit doesn't manage to interpret XML-based error messages */
				} catch(ClassCastException e){
					abort = true;
					break;
				}
			}
			if (abort){
				continue;
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
					LogManager.logFoundReflection(reflectionClone, driver);
				} else {
					LogManager.logInfo("No reflection");
				}
			} else {
				LogManager.logInfo("No reflection");
			}
		}

	}

	private static final String[] payloads = {
		"'';!--\"<XSS>=&{()}",
		"<script>alert(\"XSS\");</script>",
		"<script>alert(\'XSS\');</script>",
		"<IMG SRC=\"javascript:alert(\'XSS\');\">",
		"<IMG SRC=javascript:alert(\'XSS\')>",
		"<IMG SRC=JaVaScRiPt:alert('XSS')>",
		"<IMG SRC=javascript:alert(\"XSS\")>",
		"<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>",
		"<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\">",
		"<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>",
		"<IMG SRC=/ onerror=\"alert(String.fromCharCode(88,83,83))\"></img>"
	};
	private static final String[] payloadsExpectedResults = {
		"<XSS",
		"<script>alert(\"XSS\");</script>",
		"<script>alert(\'XSS\');</script>",
		"<IMG SRC=\"javascript:alert(\'XSS\');\">",
		"<IMG SRC=javascript:alert(\'XSS\')>",
		"<IMG SRC=JaVaScRiPt:alert(\'XSS\')>",
		"<IMG SRC=javascript:alert(\"XSS\")>",
		"<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>",
		"<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\">",
		"<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>",
		"<IMG SRC=/ onerror=\"alert(String.fromCharCode(88,83,83))\">"
	};

	public void testReflections() {
		for (Reflection r : reflectionsFound) {
			if (r.hasBeenTested) {
				continue;
			} else {
				r.hasBeenTested = true;
			}
			LogManager.logInfo("[XSS] Testing reflection : " + r);

			Reflection reflection = r.clone();


			for (int indexPayload = 0; indexPayload < payloads.length; indexPayload++) {
				String startPattern = rand.randAlphaNumString(4);
				String endPattern = rand.randAlphaNumString(4);
				String xssPayload = payloads[indexPayload];
				String expectedReflection = payloadsExpectedResults[indexPayload];
				StringBuilder completePayload = new StringBuilder(startPattern);
				completePayload.append(xssPayload);
				completePayload.append(endPattern);

				String response = "";
				driver.reset();
				for (int i = 0; i < reflection.path.size(); i++) {
					ParameterizedInput pi = reflection.path.get(i);
					if (i == reflection.inputElementIndex) {
						pi.getParameters().get(reflection.inputElementParamIndex).value = completePayload.toString();
					}
					response = driver.submit(pi, true);
				}
				if (response.toLowerCase().contains(expectedReflection.toLowerCase())) {
					LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' reflected as \'" + expectedReflection + "\', as exepected");
					LogManager.logInfo("[XSS] SUCCESS : XSS found for reflection " + reflection);
					LogManager.logFoundXSS(reflection, driver);
				}

				/* Both patterns were found : the data in between should contain our payload */
				if (response.contains(startPattern) && response.contains(endPattern)) {
					int indexStartFilteredPayload = response.indexOf(startPattern) + startPattern.length();
					int indexEndFilteredPayload = response.indexOf(endPattern);
					String filteredPayload = response.substring(indexStartFilteredPayload, indexEndFilteredPayload); //TODO : handle multiple reflexions
					LogManager.logInfo("[XSS] Payload \'" + xssPayload + "\' reflected as \'" + filteredPayload + "\'");
					LogManager.logInfo("[XSS] Distance : " + StringUtils.getLevenshteinDistance(filteredPayload, xssPayload));

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
			LogManager.logInfo("[XSS]");
		}
	}

}
