package options;

import java.util.ArrayList;
import java.util.List;

import options.valueHolders.ValueHolder;

public class OptionsGroup extends NoArgumentOption {
	String groupName;
	List<OptionTree> subOptions = new ArrayList<>();

	public OptionsGroup(String name) {
		this.groupName = name;
	}

	/**
	 * Record sub-option in the group. You should call
	 * {@link validateSubOptions} after adding all your sub-options.
	 * 
	 * @param option
	 *            the option to record in the group.
	 */
	protected void addSubOption(OptionTree option) {
		subOptions.add(option);
	}

	/**
	 * Must be called only one time after adding sub-options.
	 */
	protected void validateSubOptions() {
		assert getSortedChildren().size() == 0;
		addSortedChildren(subOptions);
	}

	@Override
	protected void createMainComponent() {
		mainComponent = null;
		updateSubTreeComponent("options related to " + groupName);
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return subOptions;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert false;
	}

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		return null;
	}

}
