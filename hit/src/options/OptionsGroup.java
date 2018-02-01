package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class OptionsGroup extends OptionTree {
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
		mainContainer = new JPanel();
		updateSubTreeComponent("options related to " + groupName);
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return subOptions;
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return false;
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert false;
		return false;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert false;
	}

	@Override
	protected String getSelectedArgument() {
		return null;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		return new ArrayList<>();
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert false;
		return null;
	}

}
