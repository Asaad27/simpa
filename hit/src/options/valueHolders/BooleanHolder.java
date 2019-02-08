package options.valueHolders;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class BooleanHolder extends ValueHolder<Boolean> {

	private JCheckBox checkBox;

	public BooleanHolder(String name, String description) {
		super(name, description, false);
		updateWithValue();
	}

	@Override
	protected JComponent createMainComponent() {
		assert checkBox == null;
		checkBox = new JCheckBox(getName());
		checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setValue(checkBox.isSelected());

			}
		});
		return checkBox;
	}

	@Override
	protected void updateWithValue() {
		assert getValue() != null;
		if (checkBox != null)
			checkBox.setSelected(getValue());
	}

}
