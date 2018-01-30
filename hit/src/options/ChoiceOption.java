package options;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;

/**
 * An option to select one choice in a list.
 * 
 * @author Nicolas BREMOND
 */
public class ChoiceOption extends OptionTree {
	/**
	 * A class to represent one possible choice in the list of a
	 * {@link ChoiceOption}.
	 */
	protected class ChoiceItem {
		public final String name;
		public final ArgumentDescriptor argument;
		public List<OptionTree> subTrees = new ArrayList<>();

		public ChoiceItem(String name, String argument, ChoiceOption parent) {
			assert argument.startsWith("-");
			this.name = name;
			this.argument = new ArgumentDescriptor(AcceptedValues.NONE,
					argument, parent);
		}

		public ChoiceItem(String name, String argument, ChoiceOption parent,
				List<OptionTree> subTrees) {
			this(name, argument, parent);
			if (subTrees != null)
				this.subTrees = subTrees;
		}

		public String toString() {
			return name;
		}
	}

	private JComboBox<ChoiceItem> choiceCombo = null;
	private List<ChoiceItem> choices = new ArrayList<>();
	private ChoiceItem selectedItem = null;

	/**
	 * Add a possible choice in the list.
	 * 
	 * @param choice
	 *            the choice to add.
	 * @warning must be called in constructor, like {@link addSortedChildren}.
	 */
	protected void addChoice(ChoiceItem choice) {
		assert mainContainer == null;
		choices.add(choice);
		addSortedChildren(choice.subTrees);
		if (selectedItem == null) {
			selectChoice(choice);
		}
	}

	@Override
	protected void createMainComponent() {
		assert choices.size() != 0;
		mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

		choiceCombo = new JComboBox<>();
		for (ChoiceItem choice : choices) {
			choiceCombo.addItem(choice);
		}
		choiceCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				selectChoice(choiceCombo.getSelectedIndex());
			}
		});
		mainContainer.add(choiceCombo);
		selectChoice(selectedItem);
	}

	/**
	 * Set value of this option.
	 * 
	 * @param index
	 *            the index of the choice to select in the list.
	 * @see selectChoice(ChoiceItem)
	 */
	public void selectChoice(int index) {
		assert index < choices.size();
		selectChoice(choices.get(index));
	}

	/**
	 * Set value of this option
	 * 
	 * @param choice
	 *            the value to use.
	 */
	public void selectChoice(ChoiceItem choice) {
		assert choices.contains(choice);
		selectedItem = choice;
		if (choiceCombo != null && choiceCombo.getSelectedItem() != choice) {
			choiceCombo.setSelectedItem(choice);
			choiceCombo.validate();
		}
		updateSubTreeComponent("options for " + selectedItem.name);
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return selectedItem.subTrees;
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		for (ChoiceItem choice : choices) {
			if (choice.argument.name.equals(arg.getName()))
				return true;
		}
		return false;
	}

	@Override
	protected void setValueFromArg(ArgumentValue arg) {
		for (ChoiceItem choice : choices) {
			if (choice.argument.name.equals(arg.getName())) {
				selectChoice(choice);
				return;
			}
		}
		assert false;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		for (ChoiceItem choice : choices) {
			if (choice.subTrees == selectedChildren) {
				selectChoice(choice);
				assert getSelectedChildren() == selectedChildren;
				return;
			}
		}
		assert false : "sub tree do not match any item";
	}

	@Override
	protected String getSelectedArgument() {
		return selectedItem.argument.name;
	}

	/**
	 * Get the current value of this option.
	 * 
	 * @return the current value of this option.
	 */
	public ChoiceItem getSelectedItem() {
		return selectedItem;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> args = new ArrayList<>();
		for (ChoiceItem choice : choices) {
			args.add(choice.argument);
		}
		return args;
	}
}
