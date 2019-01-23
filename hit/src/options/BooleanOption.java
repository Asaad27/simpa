package options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

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
	 *            activated. If {@code null}, you MUST call
	 *            {@link #setSubTreeIfTrue(List)} to set it during construction.
	 * @param subTreeIfFalse
	 *            the list of sub-options available when this option is not
	 *            selected. If {@code null}, you MUST call
	 *            {@link #setSubTreeIfFalse(List)} to set it during
	 *            construction.
	 * 
	 * @param enabled
	 *            the default status of this option.
	 */
	public BooleanOption(String name, String argument, String description,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse,
			boolean enabled) {
		this(name, argument, description, subTreeIfTrue, subTreeIfFalse);
		setEnabledByDefault(enabled);
	}

	public BooleanOption(String name, String argument, String description,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse) {
		assert !argument.startsWith("-");
		this.name = name;
		makeArgumentDescriptors(argument);
		this.description = description;
		this.subTreeIfTrue = subTreeIfTrue;
		this.subTreeIfFalse = subTreeIfFalse;
		if (subTreeIfTrue != null)
			addSortedChildren(subTreeIfTrue);
		if (subTreeIfFalse != null)
			addSortedChildren(subTreeIfFalse);
		setEnabled(false);
	}

	public BooleanOption(String name, String argument, String description) {
		this(name, argument, description, new ArrayList<OptionTree>(),
				new ArrayList<OptionTree>(), false);
	}

	/**
	 * MUST be call if and only if {@code null} was passed in constructor. This
	 * method should be called during construction of object (as said in
	 * {@link #addSortedChildren(List)}
	 * 
	 * @param subTreeIfFalse
	 *            list of option available when this one is disabled.
	 */
	protected void setSubTreeIfFalse(List<OptionTree> subTreeIfFalse) {
		assert this.subTreeIfFalse == null;
		this.subTreeIfFalse = subTreeIfFalse;
		addSortedChildren(subTreeIfFalse);
	}

	/**
	 * MUST be call if and only if {@code null} was passed in constructor. This
	 * method should be called during construction of object (as said in
	 * {@link #addSortedChildren(List)}
	 * 
	 * @param subTreeIfTrue
	 *            list of option available when this one is enabled.
	 */
	protected void setSubTreeIfTrue(List<OptionTree> subTreeIfTrue) {
		assert this.subTreeIfTrue == null;
		this.subTreeIfTrue = subTreeIfTrue;
		addSortedChildren(subTreeIfTrue);
	}

	@Override
	protected void createMainComponent() {
		checkBox = new JCheckBox(name);
		checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(checkBox.isSelected());

			}
		});
		mainConponent = checkBox;

		setEnabled(isEnabled);
	}

	/**
	 * set the value of this option
	 * 
	 * @param checked
	 *            the value to set.
	 */
	public void setEnabled(Boolean checked) {
		if (mainConponent != null) {
			assert checkBox != null;
			if (checkBox.isSelected() != checked)
				checkBox.setSelected(checked);
		}
		isEnabled = checked;
		updateSubTreeComponent(getSubTreeTitle());
		validateSelectedTree();
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

	public boolean isEnabled() {
		return isEnabled;
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
	public ArgumentValue getSelectedArgument() {
		return new ArgumentValue(isEnabled ? enableArgumentDescriptor
				: disableArgumentDescriptor);
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
	public String getHelpByArgument(ArgumentDescriptor arg) {
		if (arg == enableArgumentDescriptor)
			return "enable " + name;
		else {
			assert arg == disableArgumentDescriptor;
			return "disable " + name;
		}
	}

	@Override
	public String toString() {
		return "option " + name;
	}

}
