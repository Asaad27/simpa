package options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import options.OptionTree;

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
	private String name;
	private String argument;

	/**
	 * Get the argument which enable this option.
	 * 
	 * @return the argument which enable this option.
	 */
	private String getSelectArgument() {
		return "--" + argument;
	}

	/**
	 * Get the argument which disable this option.
	 * 
	 * @return the argument which disable this option.
	 */
	private String getUnSelectArgument() {
		return "--no-" + argument;
	}

	/**
	 * 
	 * @param name
	 *            the name of this option
	 * @param argument
	 *            the base for the argument of this option. It will be
	 *            automatically completed in <code>--{argument}</code> and
	 *            <code>--no-{argument}</code>
	 * @param subTreeIfTrue
	 *            the list of sub-options available when this option is
	 *            activated
	 * @param subTreeIfFalse
	 *            the list of sub-options available when this option is not
	 *            selected
	 * @param enabled
	 *            the initial status of this option.
	 */
	public BooleanOption(String name, String argument,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse,
			boolean enabled) {
		assert !argument.startsWith("-");
		this.name = name;
		this.argument = argument;
		this.subTreeIfTrue = subTreeIfTrue;
		this.subTreeIfFalse = subTreeIfFalse;
		addSortedChildren(subTreeIfTrue);
		addSortedChildren(subTreeIfFalse);
		setEnabled(enabled);
	}

	public BooleanOption(String name, String argument,
			List<OptionTree> subTreeIfTrue, List<OptionTree> subTreeIfFalse) {
		this(name, argument, subTreeIfTrue, subTreeIfFalse, false);
	}

	public BooleanOption(String name, String argument) {
		this(name, argument, new ArrayList<OptionTree>(),
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

	public String getSubTreeTitle() {
		return "options for " + name
				+ (isEnabled ? " activated" : " disactivated");
	}

	@Override
	protected boolean isActivatedByArg(String arg) {
		return (arg.equals(getSelectArgument())
				|| arg.equals(getUnSelectArgument()));
	}

	@Override
	protected void setValueFromArg(String arg) {
		assert isActivatedByArg(arg);
		setEnabled(arg.equals(getSelectArgument()));
	}

	@Override
	public String getSelectedArgument() {
		return isEnabled ? getSelectArgument() : getUnSelectArgument();
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
	public String toString() {
		return "option " + name;
	}
}
