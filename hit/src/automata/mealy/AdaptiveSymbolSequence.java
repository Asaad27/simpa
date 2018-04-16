/**
 * 
 */
package automata.mealy;

/**
 * @author Nicolas BREMOND
 *
 */
public class AdaptiveSymbolSequence extends AdaptiveStructure<String, String> {

	public AdaptiveSymbolSequence() {
		super();
	}

	@Override
	protected AdaptiveStructure<String, String> createNewNode() {
		return new AdaptiveSymbolSequence();
	}

	@Override
	protected boolean checkCompatibility(String inputT, String outputT) {
		return true;
	}

}
