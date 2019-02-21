package options.valueHolders;

import java.util.List;

import automata.mealy.InputSequence;

public class InputSequenceHolder
		extends SequenceHolder<String, InputSequence, StringHolder> {

	public InputSequenceHolder(String name, String description) {
		super(name, description, new InputSequence(), '.', '\\');
	}

	@Override
	protected StringHolder createNewElement() {
		return new StringHolder("input", "input symbol", "");
	}

	@Override
	public void clear() {
		setValue(new InputSequence());
	}

	@Override
	public void addElementFromString(String element) {
		InputSequence newSeq = new InputSequence();
		newSeq.addInputSequence(getValue());
		newSeq.addInput(element);
		setValue(newSeq);
	}

	@Override
	protected InputSequence InnerToUser(List<StringHolder> holders) {
		List<String> inputs = holdersTypeToList(holders);
		InputSequence seq = new InputSequence();
		seq.sequence.addAll(inputs);
		return seq;
	}

	@Override
	protected List<StringHolder> UserToInnerType(InputSequence u,
			List<StringHolder> previousValue) {
		return listToHolder(u.sequence, previousValue);
	}

	@Override
	protected String getAddButtonText() {
		return "add new input";
	}
}
