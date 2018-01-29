package options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * 
 * This class aims to represent an option of SIMPA. It can be an option for the
 * algorithms, for the drivers or for the general use of SIMPA. Those options
 * should be used from command-line or from graphical interface.
 * 
 * The options are organized as a tree because some options can allow the use of
 * others options. For instance, some heuristics are linked to a specific
 * algorithm and thus those option should be visible only when we select this
 * algorithm.
 * 
 * @author Nicolas BREMOND
 */

public abstract class OptionTree {

	private List<List<OptionTree>> sortedChildren = new ArrayList<>();
	private List<OptionTree> children = new ArrayList<>();
	protected JPanel mainContainer = null;
	protected JPanel subTreeContainer = null;

	/**
	 * get the graphical component for this option and build it if necessary.
	 * 
	 * @return the component to display.
	 */
	public Component getComponent() {
		if (mainContainer == null)
			createMainComponent();
		return mainContainer;

	}

	/**
	 * Add or update the children's components in this component.
	 * 
	 * Should be called each time the value of this option is changed and
	 * implies a change in available sub-options.
	 * 
	 * @param subTreeTitle
	 *            a title for the box containing children's options
	 */
	public void updateSubTreeComponent(String subTreeTitle) {
		if (mainContainer != null) {

			if (subTreeContainer != null) {
				mainContainer.remove(subTreeContainer);
				subTreeContainer = null;
			}
			List<OptionTree> selectedSubTree = getSelectedChildren();

			if (selectedSubTree.size() != 0) {
				subTreeContainer = new JPanel();
				subTreeContainer.setBorder(
						BorderFactory.createTitledBorder(subTreeTitle));
				for (OptionTree subOption : selectedSubTree) {
					subTreeContainer.add(subOption.getComponent());
				}
				subTreeContainer.setLayout(
						new BoxLayout(subTreeContainer, BoxLayout.Y_AXIS));
				mainContainer.add(subTreeContainer);
			}
			mainContainer.validate();
		}
	}

	/**
	 * This function must create and fill the Component this.mainComponent
	 */
	protected abstract void createMainComponent();

	/**
	 * Record a new set of sub-options which can be activated for a value of
	 * this option.
	 * 
	 * Should be called while constructing the option.
	 * 
	 * @param children
	 *            a list of options which can be activated for a specific value
	 *            of this option.
	 */
	protected void addSortedChildren(List<OptionTree> children) {
		this.children.addAll(children);
		this.sortedChildren.add(children);
	}

	/**
	 * Get access to all children (even not enabled/selected) of this node with
	 * depth 1
	 * 
	 * @return a list of all children of depth 1
	 */
	protected List<OptionTree> getChildren() {
		return children;
	}

	/**
	 * Get access to all children of depth 1 but sorted by possible group of
	 * selection. For instance, if this node can be in two state and in first
	 * state have one sub tree 'a' and in the other state have no sub tree, it
	 * should return [[a][]]
	 */
	protected List<List<OptionTree>> getSortedChildren() {
		return sortedChildren;
	}

	/**
	 * Get the list of children activated by the current value of this option.
	 * 
	 * @return the list of children activated by the current value of this
	 *         option.
	 */
	protected abstract List<OptionTree> getSelectedChildren();

	/**
	 * Indicate if an argument can be parsed to select value of this option
	 * (this should not include arguments of any sub-option, only this one). If
	 * this function returns <code>true</code>, this mean that we can call
	 * setValueFromArg with this argument.
	 * 
	 * @param arg
	 *            the argument to test.
	 * @return <code>true</code> if and only if the argument can be parsed by
	 *         this option.
	 */
	protected abstract boolean isActivatedByArg(String arg);

	/**
	 * Set the value of this option from one argument.
	 * 
	 * @param arg
	 *            the argument used to set value of this option.
	 * 
	 * @warning if {@link isActivatedByArg(String)} do not accept
	 *          <code>arg</code>, the behavior of this function is undefined.
	 */
	protected abstract void setValueFromArg(String arg);

	/**
	 * Set the value of this option in order to allow the use of sub-options
	 * provided in <code>selectedChildren</code>.
	 * 
	 * @param selectedChildren
	 *            a list of sub-options as recorded in {@link addSortedChildren}
	 */
	protected abstract void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren);

	/**
	 * Get the argument which can switch this option to the current value (i.e.
	 * calling setValueWithArg(getSelectedArgument()) should have no effect).
	 * 
	 * This function is used when no argument can set the value of this option
	 * but an argument is used by a sub-option and can be used to guess the
	 * value of this option.
	 * 
	 * @return the argument which bring this option in the current state
	 */
	protected abstract String getSelectedArgument();

	/**
	 * Indicate that an argument is accepted by one option in this tree. This is
	 * an extension of {@link isActivatedByArg} to the whole tree.
	 * 
	 * @param arg
	 *            the argument to test
	 * @return <code>true</code> if one option in this tree accept the argument.
	 */
	private boolean thisOrSubTreeIsActivatedByArg(String arg) {
		if (isActivatedByArg(arg))
			return true;
		for (OptionTree child : getChildren())
			if (child.thisOrSubTreeIsActivatedByArg(arg))
				return true;
		return false;
	}

	/**
	 * Indicate that one argument is accepted by one option in the whole tree.
	 * This is an extension of {@link thisOrsubTreeIsActivatedByArg} to a list
	 * of options.
	 * 
	 * @param group
	 *            aList of OptionTree to test.
	 * @param arg
	 *            the argument to test
	 * @return <code>true</code> if one option in the list of trees accept the
	 *         argument.
	 */
	private static boolean oneSubTreeIsActivatedByArg(List<OptionTree> group,
			String arg) {
		for (OptionTree tree : group)
			if (tree.thisOrSubTreeIsActivatedByArg(arg))
				return true;
		return false;
	}

	/**
	 * Try to define the value of this tree and subTrees from a list of
	 * arguments. This function can be called before starting the graphical
	 * interface and thus the parsing should do as many work as possible and not
	 * stop on first error.
	 * 
	 * @param args
	 *            the list of arguments. Arguments successfully parsed will be
	 *            removed from this list.
	 * @return <code>false</code> if the parsing cannot be achieved,
	 *         <code>true</code> if the option tree is defined from arguments,
	 *         even if there was minor mistakes.
	 */
	public boolean parseArguments(List<String> args) {
		// first check if this option is activated by an argument provided
		String activatingArg = null;
		for (String arg : args) {
			if (isActivatedByArg(arg)) {
				if (activatingArg != null) {
					System.err.println("Warning : both arguments '"
							+ activatingArg + "' and '" + arg
							+ "' can activate this option."
							+ " Ignoring the first and using the last one.");
				}
				activatingArg = arg;
			}
		}
		if (activatingArg != null) {
			args.remove(activatingArg);
			setValueFromArg(activatingArg);
		} else {
			// this option is not activated, but maybe a sub option can be
			// activated to choose which subTree to select
			List<List<OptionTree>> subTrees = getSortedChildren();
			for (String arg : args) {
				if (thisOrSubTreeIsActivatedByArg(arg)) {
					List<List<OptionTree>> keptSubTrees = new ArrayList<>();
					for (List<OptionTree> selectableGroup : subTrees) {
						if (oneSubTreeIsActivatedByArg(selectableGroup, arg)) {
							keptSubTrees.add(selectableGroup);
						}

					}
					subTrees = keptSubTrees;
				}
			}
			if (subTrees.size() == 1) {
				setValueFromSelectedChildren(subTrees.get(0));
				System.out.println(
						"Warning : deduced value of this option by trying sub options."
								+ " It is better to specify directly the value of this option ("
								+ getSelectedArgument()
								+ ") to remove ambiguity");
				assert (getSelectedChildren() == subTrees.get(0));
			} else {
				return false;
			}
		}

		// now parse arguments in subtree
		boolean subTreeSuccessfullyParsed = true;
		List<OptionTree> selectedSubTrees = getSelectedChildren();
		for (OptionTree subtree : selectedSubTrees)
			if (!subtree.parseArguments(args)) {
				System.out.println("cannot define value of " + subtree
						+ " with arguments " + args);
				subTreeSuccessfullyParsed = false;
			}
		return subTreeSuccessfullyParsed;
	}
}
