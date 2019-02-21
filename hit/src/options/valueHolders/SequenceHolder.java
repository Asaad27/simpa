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

public abstract class SequenceHolder<E, L, H extends ValueHolder<E, ?> & Stringifyable>
		extends ValueHolder<L, List<H>> implements Stringifyable {

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

	final ValueChangeHandler handlersTransmiter = new ValueChangeHandler() {
		@Override
		public void valueChanged() {
			callChangeHandlers();
		}
	};

	public SequenceHolder(String name, String description, L initialValue) {
		this(name, description, initialValue, ',', '\\');
	}

	public SequenceHolder(String name, String description, L initialValue,
			char separator, char escape) {
		super(name, description, initialValue);
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

	public abstract void clear();

	public abstract void addElementFromString(E element);

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
		List<H> kept = new ArrayList<>();
		for (H holder : getInnerValue())
			if (holder != toRemove)
				kept.add(holder);
		setValue(InnerToUser(kept));
	}

	protected abstract H createNewElement();

	protected List<E> holdersTypeToList(List<H> holders) {
		List<E> r = new ArrayList<>();
		for (H holder : holders) {
			r.add(holder.getValue());
		}
		return Collections.unmodifiableList(r);
	}

	protected List<H> listToHolder(List<E> user, List<H> previousValue) {
		if (previousValue == null)
			previousValue = new ArrayList<>();
		for (int i = 0; i < user.size(); i++) {
			while (previousValue.size() > user.size()
					&& !previousValue.get(i).getValue().equals(user.get(i)))
				previousValue.remove(i);
			if (previousValue.size() <= i) {
				H element = createNewElement();
				element.addHandler(handlersTransmiter);
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
		for (String s : tools.Utils.stringToList(strValue, separator, escape,
				null))
			addElement(s);
	}

	@Override
	public String getValueAsString(boolean debug) {
		return tools.Utils.listToString(getValueAsStrings(debug), separator,
				escape);
	}
}
