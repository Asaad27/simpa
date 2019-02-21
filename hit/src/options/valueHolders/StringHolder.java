package options.valueHolders;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import options.ParseException;

public class StringHolder extends SingleTypeValueHolder<String>
		implements Stringifyable {

	private JTextField field;

	public StringHolder(String name, String description, String initial) {
		super(name, description, initial);
	}

	@Override
	public void setValueFromString(String strValue) throws ParseException {
		setValue(strValue);
	}

	@Override
	public String getValueAsString(boolean debug) {
		return getValue();
	}

	@Override
	protected void updateWithValue() {
		if (field != null) {
			if (!field.getText().equals(getValue())) {
				field.setText(getValue());
				field.validate();
			}
		}
	}

	@Override
	protected JComponent createMainComponent() {
		field = new JTextField(50);
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		pane.add(new JLabel(getName()), c);
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		pane.add(field, c);

		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				setValue(field.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setValue(field.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setValue(field.getText());
			}
		});
		return pane;
	}

}
