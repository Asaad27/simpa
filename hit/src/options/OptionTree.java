package options;

import java.awt.Component;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	static protected class ArgumentDescriptor {
		enum AcceptedValues {
			NONE, ONE, SEVERAL,
		};

		public AcceptedValues acceptedValues;
		public String name;
		public OptionTree parentOption;

		public ArgumentDescriptor(AcceptedValues acceptedValues, String name,
				OptionTree parentOption) {
			this.acceptedValues = acceptedValues;
			this.name = name;
			this.parentOption = parentOption;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static protected class ArgumentValue {
		public List<String> values = new ArrayList<>();
		ArgumentDescriptor descriptor; // descriptor is hidden at this time
										// because in creation of list of
										// ArgumentsValues, we use Descriptors
										// which may not have the same
										// parentOption

		public ArgumentValue(ArgumentDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		public String getName() {
			return descriptor.name;
		}

		public String toString() {
			return getName();
		}

	}

	private List<List<OptionTree>> sortedChildren = new ArrayList<>();
	private List<OptionTree> children = new ArrayList<>();
	protected JPanel mainContainer = null;
	protected JPanel subTreeContainer = null;
	protected String description = "";

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
	protected abstract boolean isActivatedByArg(ArgumentValue arg);

	/**
	 * Set the value of this option from one argument.
	 * 
	 * @param arg
	 *            the argument used to set value of this option.
	 * @return <code>false</code> if an error occurred, <code>true</code> if
	 *         everything was fine
	 * 
	 * @warning if {@link isActivatedByArg(String)} do not accept
	 *          <code>arg</code>, the behavior of this function is undefined.
	 */
	protected abstract boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream);

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
	 * get the default value, if defined.
	 * 
	 * This method aims to be overridden.
	 * 
	 * @return the default value if it is defined, {@code null} otherwise.
	 */
	protected ArgumentValue getDefaultValue() {
		return null;
	}

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
	private boolean thisOrSubTreeIsActivatedByArg(ArgumentValue arg) {
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
			ArgumentValue arg) {
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
	 * The first part of this function is to translate String arguments into
	 * ArgumentValues and try to detect errors like misspelling. Then it calls
	 * {@link parseArgumentsInternal} which will transpose ArgumentValue in the
	 * OptionTree
	 * 
	 * @param args
	 *            the list of arguments.
	 * @param parsingErrorStream
	 *            a stream on which errors should be written (typically
	 *            System.out when the graphical user interface will let the user
	 *            repair her/his mistakes and System.err when errors are fatal).
	 * @return <code>false</code> if the parsing cannot be achieved,
	 *         <code>true</code> if the option tree is defined from arguments,
	 *         even if there was minor mistakes.
	 */
	public boolean parseArguments(List<String> args,
			PrintStream parsingErrorStream) {
		boolean parsingError = false;
		List<ArgumentDescriptor> descriptors = getSubTreeAcceptedArguments();
		Map<ArgumentDescriptor, ArgumentValue> values = new HashMap<>();
		List<ArgumentValue> valuesList = new ArrayList<>();
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			String value = null;
			if (arg.contains("=")) {
				int pos = arg.indexOf("=");
				value = arg.substring(pos + 1, arg.length());
				arg = arg.substring(0, pos);
			}
			ArgumentValue valueArg = null;
			ArgumentDescriptor descriptor = null;
			for (ArgumentDescriptor desc : descriptors) {
				if (desc.name.equals(arg)) {
					valueArg = values.get(desc);
					if (valueArg == null) {
						valueArg = new ArgumentValue(desc);
						values.put(desc, valueArg);
						valuesList.add(valueArg);
					}
					descriptor = desc;
					break;
				}
			}
			if (valueArg == null) {
				parsingErrorStream
						.println("Argument " + arg + " is not interpreted.");
				parsingError = true;
			} else {
				if (value == null) {
					// try to use next argument as value
					String nextArg = null;
					boolean nextArgIsOption = false;
					if (i + 1 < args.size()) {
						nextArg = args.get(i + 1);
						if (nextArg.contains("=")) {
							nextArg = nextArg.substring(0,
									nextArg.indexOf("="));
						}
						for (ArgumentDescriptor desc : descriptors) {
							if (desc.name.equals(nextArg)) {
								nextArgIsOption = true;
								break;
							}
						}
						if (!nextArgIsOption) {
							value = nextArg;
							i++;
						}
					}
				}
				if (value != null) {
					valueArg.values.add(value);
				}
				switch (descriptor.acceptedValues) {
				case NONE:
					if (valueArg.values.size() > 0) {
						System.out.println("Warning : argument "
								+ descriptor.name
								+ " should not have a value and '" + value
								+ "' cannot be parsed as an argument");
					}
					break;
				case ONE:
					if (valueArg.values.size() > 1) {
						System.out
								.println("Warning : argument " + descriptor.name
										+ " should have only one value and '"
										+ valueArg.values
										+ "' cannot be parsed as arguments");
					} else if (valueArg.values.size() == 0) {
						System.out
								.println("Warning : argument " + descriptor.name
										+ " is waiting for some value");
					}
					break;
				case SEVERAL:
					if (valueArg.values.size() == 0) {
						System.out
								.println("Warning : argument " + descriptor.name
										+ " is waiting for some value");
					}
				}

			}
		}
		boolean parsingInternalSucces = parseArgumentsInternal(valuesList,
				parsingErrorStream);
		if (valuesList.size() != 0) {
			System.out.println(
					"Warning : some arguments were not used because they are not compatible with options of upper level."
							+ " You can remove the following options : "
							+ valuesList);

		}
		return parsingInternalSucces && (!parsingError);
	}

	/**
	 * Recursive function to place arguments in the option tree.
	 * 
	 * @param args
	 *            the list of arguments. Arguments successfully parsed will be
	 *            removed from this list.
	 * @return <code>false</code> if the parsing cannot be achieved,
	 *         <code>true</code> if the option tree is defined from arguments,
	 *         even if there was minor mistakes.
	 */
	private boolean parseArgumentsInternal(List<ArgumentValue> args,
			PrintStream parsingErrorStream) {
		boolean parseError = false;
		// first check if this option is activated by an argument provided
		ArgumentValue activatingArg = null;
		for (ArgumentValue arg : args) {
			if (isActivatedByArg(arg)) {
				if (activatingArg != null) {
					parsingErrorStream.println("Warning : both arguments '"
							+ activatingArg + "' and '" + arg
							+ "' can activate this option."
							+ " Ignoring the first and using the last one.");
				}
				activatingArg = arg;
			}
		}
		if (activatingArg != null) {
			args.remove(activatingArg);
			if (!setValueFromArg(activatingArg, parsingErrorStream))
				parseError = true;
		} else {
			// this option is not activated, but maybe a sub option can be
			// activated to choose which subTree to select
			List<List<OptionTree>> subTrees = getSortedChildren();
			for (ArgumentValue arg : args) {
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
				parsingErrorStream.println(
						"Warning : deduced value of this option by trying sub options."
								+ " It is better to specify directly the value of this option ("
								+ getSelectedArgument()
								+ ") to remove ambiguity");
				assert (getSelectedChildren() == subTrees.get(0));
			} else {
				ArgumentValue defaultValue = getDefaultValue();
				if (defaultValue == null)
					return false;
				else {
					boolean r = setValueFromArg(defaultValue,
							parsingErrorStream);
					assert r;
				}
			}
		}

		// now parse arguments in subtree
		boolean subTreeSuccessfullyParsed = true;
		List<OptionTree> selectedSubTrees = getSelectedChildren();
		for (OptionTree subtree : selectedSubTrees)
			if (!subtree.parseArgumentsInternal(args, parsingErrorStream)) {
				parsingErrorStream.println("cannot define value of " + subtree
						+ " with arguments " + args);
				subTreeSuccessfullyParsed = false;
			}
		return subTreeSuccessfullyParsed && !parseError;
	}

	/**
	 * 
	 * @return the list of arguments which can be accepted by this option
	 *         (should not include sub-options)
	 */
	protected abstract List<ArgumentDescriptor> getAcceptedArguments();

	/**
	 * Get the list of arguments accepted by the sub-trees of a list of options
	 * and check for conflicting argument.
	 * 
	 * This function asserts that an argument do not appear two times in the
	 * same list.
	 * 
	 * @param options
	 *            a list of option which are selected simultaneously.
	 * @return the list of argument used by any option from the list or sub-tree
	 *         of the list.
	 */
	static private List<ArgumentDescriptor> getListAcceptedArguments(
			List<OptionTree> options) {
		List<ArgumentDescriptor> args = new ArrayList<>();
		for (OptionTree option : options) {
			for (ArgumentDescriptor optionArg : option
					.getSubTreeAcceptedArguments()) {
				for (ArgumentDescriptor seenArg : args) {
					assert !seenArg.name.equals(optionArg.name);
				}
				args.add(optionArg);
			}
		}
		return args;
	}

	/**
	 * Get the arguments accepted by this option and sub-tree (including
	 * sub-option not selected currently) and check for conflicts.
	 * 
	 * This function asserts that no sub-option use the same argument as its
	 * ancestor. Two options can use the same argument if they are not active at
	 * the same time but in this case, this function asserts that the argument
	 * used accept the same number of values.
	 * 
	 * @return the list of arguments accepted by this option and any of it's
	 *         sub-option.
	 */
	private List<ArgumentDescriptor> getSubTreeAcceptedArguments() {
		List<ArgumentDescriptor> thisArguments = new ArrayList<>(
				getAcceptedArguments());
		for (ArgumentDescriptor arg : thisArguments) {
			assert isActivatedByArg(new ArgumentValue(arg));
		}
		List<ArgumentDescriptor> subTreeArguments = new ArrayList<>(
				thisArguments);

		for (List<OptionTree> sortedOption : getSortedChildren()) {
			List<ArgumentDescriptor> childrenArguments = getListAcceptedArguments(
					sortedOption);
			for (ArgumentDescriptor newArg : childrenArguments) {
				for (ArgumentDescriptor thisArg : thisArguments) {
					// children cannot have same argument as their father
					assert !newArg.name.equals(thisArg.name);
				}
				boolean seen = false;
				for (ArgumentDescriptor brothersArg : subTreeArguments) {
					if (brothersArg.name.equals(newArg.name)) {
						// two option can use the same argument if they are not
						// activated at the same time, but the arguments should
						// be equals
						seen = true;
						assert brothersArg.acceptedValues == newArg.acceptedValues;
						assert brothersArg.parentOption
								.getClass() == newArg.parentOption.getClass();
					}

				}
				if (!seen)
					subTreeArguments.add(newArg);
			}
		}
		return subTreeArguments;
	}

	/**
	 * Get description/helping text for the provided argument.
	 * 
	 * @param arg
	 *            an argument activating this option.
	 * @return a text to display in CLI or GUI.
	 */
	public abstract String getHelpByArgument(ArgumentDescriptor arg);

	public void printHelp(PrintStream stream) {
		// first, list all argument and get their helping text.
		List<OptionTree> toCompute = new ArrayList<>();
		toCompute.add(this);
		Map<String, ArgumentDescriptor> seenDescriptors = new HashMap<>();
		Map<ArgumentDescriptor, String> helps = new HashMap<>();
		while (!toCompute.isEmpty()) {
			OptionTree current = toCompute.remove(0);
			for (ArgumentDescriptor descriptor : current
					.getAcceptedArguments()) {
				String help = current.getHelpByArgument(descriptor);
				if (seenDescriptors.containsKey(descriptor.name)) {
					assert helps
							.get(seenDescriptors.get(descriptor.name)) == help;
				} else {
					helps.put(descriptor, help);
					seenDescriptors.put(descriptor.name, descriptor);
				}
			}
			toCompute.addAll(current.getChildren());
		}

		// now print arguments in a readable format;
		int maxArgDispLength = 0;
		Map<ArgumentDescriptor, String> displayedArgs = new HashMap<>();
		for (ArgumentDescriptor descriptor : helps.keySet()) {
			String disp = descriptor.name;
			switch (descriptor.acceptedValues) {
			case NONE:
				break;
			case ONE:
				disp = disp + "=<>";
				break;
			case SEVERAL:
				disp = disp + "=<> ...";
				break;
			}
			displayedArgs.put(descriptor, disp);
			if (disp.length() > maxArgDispLength)
				maxArgDispLength = disp.length();
		}

		int MAX_LENGTH = 80;// maximum length of lines, for readability.
		int argColumnWidth = maxArgDispLength;
		argColumnWidth++;// put a blank space between argument and help.
		for (ArgumentDescriptor descriptor : helps.keySet()) {
			String argDisp = displayedArgs.get(descriptor);
			String help = helps.get(descriptor);
			stream.print(argDisp);
			int blankNumber = argColumnWidth - argDisp.length();

			do {
				for (int i = 0; i < blankNumber; i++) {
					stream.print(" ");
				}
				blankNumber = argColumnWidth;
				int helpLineLength = MAX_LENGTH - argColumnWidth;
				int lastWordEnd = help.length();
				if (help.length() > MAX_LENGTH - argColumnWidth) {
					lastWordEnd = help.lastIndexOf(' ', helpLineLength);
					if (lastWordEnd == -1)
						lastWordEnd = help.indexOf(' ');
					if (lastWordEnd == -1)
						lastWordEnd = help.length();
				}
				stream.println(help.substring(0, lastWordEnd));
				if (lastWordEnd < help.length() - 1)
					help = help.substring(lastWordEnd + 1);
				else
					help = "";
			} while (help.length() > 0);

		}
	}
}