package drivers.mealy.real.mqtt;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import options.OptionTree;
import options.valueHolders.ValueHolder;

/**
 * This class was the generic List Option before the use of ValueHolders. It is
 * kept for MQTT driver but MQTT should be rewritten to use the now standard
 * option.ListOption.
 * 
 * @author Nicolas BREMOND
 *
 * @param <T>
 *            the type of element provided in each option
 */
public abstract class ListOption<T> extends OptionTree {

	private ArgumentDescriptor argument;
	private List<T> values = new ArrayList<>();

	JButton addButton = null;
	JPanel mainComponent = null;
	private GridBagConstraints constraints;

	public ListOption(String argument) {
		this.argument = new ArgumentDescriptor(
				ArgumentDescriptor.AcceptedValues.SEVERAL, argument, this);
	}

	/**
	 * convert a String to a value of template type T
	 * 
	 * @param s
	 *            the string to convert
	 * @param parsingErrorStream
	 *            a stream to outputs errors
	 * @return the object parsed or {@code null} if the string cannot be parsed
	 */
	protected abstract T fromString(String s, PrintStream parsingErrorStream);

	/**
	 * convert a value of template type T to a String
	 * 
	 * @param e
	 *            the value to convert
	 * @return a string which can be parsed to the same object using
	 *         {@link #fromString(String, PrintStream)}
	 */
	protected abstract String valueToString(T e);

	/**
	 * get the value represented by this option.
	 * 
	 * @return the list of value represented by this option.
	 * @see #addValue(Object);
	 */
	public List<T> getValues() {
		return Collections.unmodifiableList(values);
	}

	/**
	 * add a new value in list and add the corresponding graphical component.
	 * 
	 * @param v
	 *            the value to add in the list.
	 */
	protected void addValue(T v) {
		values.add(v);
		if (mainComponent != null) {
			mainComponent.remove(addButton);

			Component valueComp = createComponentFromValue(v);
			JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					values.remove(v);
					mainComponent.remove(valueComp);
					mainComponent.remove(removeButton);
					mainComponent.revalidate();
				}

			});
			constraints.gridwidth = 1;
			constraints.gridy = GridBagConstraints.RELATIVE;
			constraints.gridx = 0;
			constraints.fill = GridBagConstraints.VERTICAL;
			mainComponent.add(removeButton, constraints);
			constraints.fill = new GridBagConstraints().fill;

			constraints.gridx = 1;
			constraints.anchor = GridBagConstraints.LINE_START;
			mainComponent.add(valueComp, constraints);
			constraints.anchor = new GridBagConstraints().anchor;

			constraints.gridx = 0;
			constraints.gridwidth = 2;
			mainComponent.add(addButton, constraints);
			mainComponent.revalidate();
		}
	}

	@Override
	protected void createMainComponent() {
		mainComponent = new JPanel();
		constraints = new GridBagConstraints();
		super.mainComponent = mainComponent;
		mainComponent.setLayout(new GridBagLayout());
		String title = getOptionTitle();
		if (title != null)
			mainComponent.setBorder(BorderFactory.createTitledBorder(title));

		addButton = new JButton(getAddButtonText());
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addValue(createNewValue());
			}

		});
		mainComponent.add(addButton);

		List<T> _values = new ArrayList<>(values);
		values.clear();
		for (T v : _values) {
			addValue(v);
		}
	}

	/**
	 * create a component representing the object value. modifications to this
	 * component must be reflected into the object
	 * 
	 * @param v
	 *            the object to represent
	 * @return
	 */
	protected abstract Component createComponentFromValue(T value);

	/**
	 * Instantiate a new value to be added in the list
	 * 
	 * @return a new object set to defaults.
	 */
	protected abstract T createNewValue();

	/**
	 * get the graphical title for this list.
	 * 
	 * @return a title for the titled border or {@code null} to use no border.
	 */
	protected abstract String getOptionTitle();

	/**
	 * can be overridden to change the text of "add" button.
	 * 
	 * @return the string to display in the button
	 */
	protected String getAddButtonText() {
		return "add new";
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return getChildren();
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return argument.name.equals(arg.getDescriptor().name);
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		values.clear();
		boolean error = false;
		for (String stringValue : arg.getValues()) {
			T value = fromString(stringValue, parsingErrorStream);
			if (value == null)
				error = true;
			else {
				values.add(value);
				assert valueToString(value).equals(stringValue);
			}
		}
		return error;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert selectedChildren.equals(getChildren());
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue av = new ArgumentValue(argument);
		for (T value : values) {
			av.addValue(valueToString(value));
		}
		return av;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		return Arrays.asList(argument);
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg == argument;
		return getHelp();
	}

	/**
	 * get the help for this option
	 * 
	 * @return the text to be returned by
	 *         {@link #getHelpByArgument(ArgumentDescriptor)}
	 */
	public abstract String getHelp();

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		// TODO when value will be hold with a value Holder
		return null;
	}

}
