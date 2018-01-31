package options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;

/**
 * An option to select a boolean choice.
 * 
 * @author Nicolas BREMOND
 */
public class BooleanOption extends OptionTree {

	private JCheckBox checkBox = null;

	private List<OptionTree> subTreeIfTrue = new ArrayList<>();
	private List<OptionTree> subTreeIfFalse = new ArrayList<>();
	private Boolean isEnabled = false;
	/**
	 * @see #setEnabledByDefault(Boolean)
	 */
	private Boolean isEnabledByDefault = null;
	private String name;

	protected ArgumentDescriptor enableArgumentDescriptor = null;
	protected ArgumentDescriptor disableArgumentDescriptor = null;

	/**
	 * This function is called by constructor to build the default arguments to
	 * enable or disable this option. You can override it to define your own
	 * arguments.
	 * 
	 * @param argument
	 *            a string used to build the default arguments
	 */
	protected void makeArgumentDescriptors(String argument) {
		enableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--" + argument, this);
		disableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--no-" + argument, this);
	}

	/**
	 * 
	 * @param name
	 *            the name of this option
	 * @param argument
	 *            the base passed to {@link makeArgumentDescriptors} to build
	 *            arguments. If {@link makeArgumentDescriptors} is not
	 *            overridden, it will be automatically completed in
	 *            <code>--{argument}</code> and <code>--no-{argument}</code>
	 * @param description
	 *            the description of this option
	 * @param subTreeIfTrue
	 *            the list of sub-options available when this option is
	 *            activated
	 * @param subTreeIfFalse
	 *            the list of sub-options available when this option is not
	 *            selected
	 * @param enabled
	 *            the initial status of this option.
	 */
	public BooleanOption(String name, String argument, String description,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse,
			boolean enabled) {
		assert !argument.startsWith("-");
		this.name = name;
		makeArgumentDescriptors(argument);
		this.description = description;
		this.subTreeIfTrue = subTreeIfTrue;
		this.subTreeIfFalse = subTreeIfFalse;
		addSortedChildren(subTreeIfTrue);
		addSortedChildren(subTreeIfFalse);
		setEnabled(enabled);
	}

	public BooleanOption(String name, String argument, String description,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse) {
		this(name, argument, description, subTreeIfTrue, subTreeIfFalse, false);
	}

	public BooleanOption(String name, String argument, String description) {
		this(name, argument, description, new ArrayList<OptionTree>(),
				new ArrayList<OptionTree>(), false);
	}

	@Override
	protected void createMainComponent() {
		mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

		checkBox = new JCheckBox(name);
		checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(checkBox.isSelected());

			}
		});
		mainContainer.add(checkBox);

		setEnabled(isEnabled);
	}

	/**
	 * set the value of this option
	 * 
	 * @param checked
	 *            the value to set.
	 */
	public void setEnabled(Boolean checked) {
		if (mainContainer != null) {
			assert checkBox != null;
			if (checkBox.isSelected() != checked)
				checkBox.setSelected(checked);
		}
		isEnabled = checked;
		updateSubTreeComponent(getSubTreeTitle());
	}

	/**
	 * set the default value when no option is parsed
	 * 
	 * @param def
	 *            {@code true} to set enabled by default, {@code false} to set
	 *            disabled by default or {@code null} to use no default value.
	 */
	public void setEnabledByDefault(Boolean def) {
		isEnabledByDefault = def;
	}

	public String getSubTreeTitle() {
		return "options for " + name
				+ (isEnabled ? " activated" : " disactivated");
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return (arg.getName().equals(enableArgumentDescriptor.name)
				|| arg.getName().equals(disableArgumentDescriptor.name));
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert isActivatedByArg(arg);
		setEnabled(arg.getName().equals(enableArgumentDescriptor.name));
		return true;
	}

	@Override
	protected ArgumentValue getDefaultValue() {
		if (isEnabledByDefault == null)
			return null;
		if (isEnabledByDefault)
			return new ArgumentValue(enableArgumentDescriptor);
		return new ArgumentValue(disableArgumentDescriptor);
	}

	@Override
	public String getSelectedArgument() {
		return isEnabled ? enableArgumentDescriptor.name
				: disableArgumentDescriptor.name;
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return isEnabled ? subTreeIfTrue : subTreeIfFalse;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		if (selectedChildren == subTreeIfTrue)
			setEnabled(true);
		else {
			assert selectedChildren == subTreeIfFalse;
			setEnabled(false);
		}

	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> descriptors = new ArrayList<>();
		descriptors.add(enableArgumentDescriptor);
		descriptors.add(disableArgumentDescriptor);
		return descriptors;
	}

	@Override
	public String toString() {
		return "option " + name;
	}

}
