/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package options;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import options.OptionTree;

/**
 * An option to select one choice in a list. The command line argument can be
 * either one argument per choice using class {@link MultiArgChoiceOption} or
 * one argument for the option and one value per choice with
 * {@link GenericOneArgChoiceOption}.
 * 
 * @author Nicolas BREMOND
 */
public abstract class GenericChoiceOption<T extends ChoiceOptionItem>
		extends OptionTree {
	private final String optionName;

	private JComboBox<T> choiceCombo = null;
	protected List<T> choices = new ArrayList<>();
	private T selectedItem = null;
	protected T defaultItem = null;

	public GenericChoiceOption(String optionName) {
		assert !Character.isLowerCase(optionName.charAt(0));
		this.optionName = optionName;
	}

	/**
	 * Add a possible choice in the list.
	 * 
	 * @param choice
	 *            the choice to add.
	 * @warning must be called in constructor, like {@link addSortedChildren}.
	 */
	protected void addChoice(T choice) {
		assert mainComponent == null;
		choices.add(choice);
		addSortedChildren(choice.subTrees);
		if (selectedItem == null) {
			selectChoice(choice);
		}
	}

	@Override
	protected void createMainComponent() {
		assert choices.size() != 0;
		choiceCombo = new JComboBox<>();
		for (T choice : choices) {
			choiceCombo.addItem(choice);
		}
		choiceCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				selectChoice(choiceCombo.getSelectedIndex());
			}
		});
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.LINE_START;
		panel.setLayout(layout);
		panel.add(new JLabel(getName()), constraints);
		constraints.weightx = 1;
		panel.add(choiceCombo, constraints);
		mainComponent = panel;
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
	 * get the list of available choices.
	 * 
	 * @return
	 */
	public List<T> getChoices() {
		return Collections.unmodifiableList(choices);
	}

	/**
	 * Set value of this option
	 * 
	 * @param choice
	 *            the value to use.
	 */
	public void selectChoice(T choice) {
		assert choices.contains(choice);
		selectedItem = choice;
		if (choiceCombo != null && choiceCombo.getSelectedItem() != choice) {
			choiceCombo.setSelectedItem(choice);
			choiceCombo.validate();
		}
		updateSubTreeComponent("options for " + selectedItem.displayName);
		validateSelectedTree();
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return selectedItem.subTrees;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		for (T choice : choices) {
			if (choice.subTrees == selectedChildren) {
				selectChoice(choice);
				assert getSelectedChildren() == selectedChildren;
				return;
			}
		}
		assert false : "sub tree do not match any item";
	}

	/**
	 * Get the current value of this option.
	 * 
	 * @return the current value of this option.
	 */
	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * set the default item for this choice option. classes inheriting this
	 * class must implements the method {@link #getDefaultValue()}
	 * 
	 * @param item
	 *            an item in the choices recorded.
	 */
	public void setDefaultItem(T item) {
		assert choices.contains(item);
		defaultItem = item;
	}

	@Override
	public String getName() {
		return optionName;
	}
}
