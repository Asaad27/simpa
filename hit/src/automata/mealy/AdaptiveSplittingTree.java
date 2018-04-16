package automata.mealy;

import java.io.IOException;
import java.io.Writer;

public class AdaptiveSplittingTree
		extends SplittingTree<AdaptiveSymbolSequence, AdaptiveSymbolSequence> {

	public AdaptiveSplittingTree() {
		super();
	}

	@Override
	protected AdaptiveStructure<AdaptiveSymbolSequence, AdaptiveSymbolSequence> createNewNode() {
		return new AdaptiveSplittingTree();
	}

	@Override
	protected boolean checkCompatibility(AdaptiveSymbolSequence input,
			AdaptiveSymbolSequence output) {
		if (!output.isAnswerTo(input))
			return false;
		return true;
	}

	@Override
	protected String getDotName() {
		if (input != null)
			return input.getDotName();
		return super.getDotName();
	}

	@Override
	protected void dot_appendNode(Writer writer) throws IOException {
		if (input != null) {
			input.dot_appendAll(writer);
		} else {
			writer.write(getDotName() + "[label=''];");
		}
	}

	@Override
	protected void dot_appendChild(Writer writer,
			AdaptiveStructure<AdaptiveSymbolSequence, AdaptiveSymbolSequence> child)
			throws IOException {
		writer.write(child.output.getDotName() + " -> " + child.getDotName()
				+ "[label='reset',color=red];");
	}

}
