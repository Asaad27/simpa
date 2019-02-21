package options.valueHolders;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import options.ParseException;

public abstract class ListHolder<E, H extends ValueHolder<E, ?> & Stringifyable>
		extends ValueHolder<List<E>, List<H>> implements Stringifyable {

	/**
	 * separator used to strigify the list of values
	 */
	final char separator;
	/**
	 * escape char used to escape separator in while stringify values.
	 */
	final char escape;

	JButton addButton = null;
	JPanel mainComponent = null;
	private GridBagConstraints constraints;

	public ListHolder(String name, String description) {
		this(name, description, ',', '\\');
	}

	public ListHolder(String name, String description, char separator,
			char escape) {
		super(name, description, new ArrayList<>());
		this.separator = separator;
		this.escape = escape;
	}

	@Override
	protected JComponent createMainComponent() {
		mainComponent = new JPanel();
		constraints = new GridBagConstraints();
		mainComponent.setLayout(new GridBagLayout());
		String title = getName();
		if (title != null)
			mainComponent.setBorder(BorderFactory.createTitledBorder(title));

		addButton = new JButton(getAddButtonText());
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addElementFromString(createNewElement().getValue());
			}

		});
		mainComponent.add(addButton);

		return mainComponent;
	}

	@Override
	protected void updateWithValue() {
		if (mainComponent == null)
			return;
		mainComponent.removeAll();
		for (H simpleHolder : getInnerValue()) {

			Component valueComp = simpleHolder.getComponent();
			JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeValue(simpleHolder);
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
		}
		constraints.gridx = 0;
		constraints.gridwidth = 2;
		mainComponent.add(addButton, constraints);
		mainComponent.revalidate();

	}

	protected String getAddButtonText() {
		return "add new";
	}

	public void clear() {
		setValue(new ArrayList<>());
	}

	public void addElementFromString(E element) {
		List<E> newValue = new ArrayList<>(getValue());
		newValue.add(element);
		setValue(newValue);
	}

	public void addElement(String s) throws ParseException {
		H parser = createNewElement();
		parser.setValueFromString(s);
		addElementFromString(parser.getValue());
	}

	public List<String> getValueAsStrings(boolean forDebug) {
		List<String> r = new ArrayList<>();
		for (H e : getInnerValue()) {
			r.add(e.getValueAsString(forDebug));
		}
		return r;
	}

	/**
	 * Remove one Inner value from list. Nothing happens if the value is not in
	 * the list.
	 * 
	 * @param toRemove
	 *            the InnerValue to remove from the list.
	 */
	protected void removeValue(H toRemove) {
		List<E> values = new ArrayList<>();
		for (H holder : getInnerValue())
			if (holder != toRemove)
				values.add(holder.getValue());
		setValue(values);
	}

	protected abstract H createNewElement();

	@Override
	protected List<E> InnerToUser(List<H> inner) {
		List<E> r = new ArrayList<>();
		for (H e : inner) {
			r.add(e.getValue());
		}
		return Collections.unmodifiableList(r);
	}

	@Override
	protected List<H> UserToInnerType(List<E> user, List<H> previousValue) {
		if (previousValue == null)
			previousValue = new ArrayList<>();
		for (int i = 0; i < user.size(); i++) {
			while (previousValue.size() > user.size()
					&& !previousValue.get(i).getValue().equals(user.get(i)))
				previousValue.remove(i);
			if (previousValue.size() <= i) {
				H element = createNewElement();
				previousValue.add(element);
			}
			if (!previousValue.get(i).getValue().equals(user.get(i)))
				previousValue.get(i).setValue(user.get(i));
		}

		while (user.size() < previousValue.size())
			previousValue.remove(previousValue.size() - 1);
		return previousValue;
	}

	@Override
	public void setValueFromString(String strValue) throws ParseException {
		clear();
		for (String s : tools.Utils.stringToList(strValue, separator, escape))
			addElement(s);
	}

	@Override
	public String getValueAsString(boolean debug) {
		return tools.Utils.listToString(getValueAsStrings(debug), separator,
				escape);
	}
}
