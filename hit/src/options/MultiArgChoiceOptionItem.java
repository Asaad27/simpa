package options;

import java.util.List;

import options.OptionTree.ArgumentDescriptor;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class MultiArgChoiceOptionItem extends ChoiceOptionItem {
	public MultiArgChoiceOptionItem(String name, String argument,
			GenericMultiArgChoiceOption<?> parent) {
		this(name, argument, parent, null);
	}

	public MultiArgChoiceOptionItem(String name, String argument,
			GenericMultiArgChoiceOption<?> parent, List<OptionTree> subTree) {
		super(name, parent, subTree);
		assert argument.startsWith("-");
		this.argument = new ArgumentDescriptor(AcceptedValues.NONE, argument,
				parent);
	}

	public final ArgumentDescriptor argument;

}
