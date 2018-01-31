package options;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent one possible choice in the list of a
 * {@link GenericChoiceOption}.
 */
public class ChoiceOptionItem {
	public final String displayName;
	public List<OptionTree> subTrees = new ArrayList<>();

	public ChoiceOptionItem(String name, GenericChoiceOption<?> parent,
			List<OptionTree> subTrees) {
		this.displayName = name;
		if (subTrees != null)
			this.subTrees = subTrees;
	}

	public String toString() {
		return displayName;
	}
}
