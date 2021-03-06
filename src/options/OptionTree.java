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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.OptionValidator.CriticalityLevel;
import options.valueHolders.ValueHolder;
import tools.NullStream;
import tools.Utils;
import tools.loggers.LogManager;

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
	public static class ArgumentDescriptor {
		public enum AcceptedValues {
			NONE, ONE, SEVERAL,
		};

		public AcceptedValues acceptedValues;
		public String name;
		public OptionTree parentOption;

		public ArgumentDescriptor(AcceptedValues acceptedValues, String name,
				OptionTree parentOption) {
			assert name.startsWith("--")
					|| (name.startsWith("-") && name.length() == 2);
			this.acceptedValues = acceptedValues;
			this.name = name;
			this.parentOption = parentOption;
		}

		@Override
		public String toString() {
			return name;
		}

		public OptionCategory getCategory() {
			return parentOption.getCategorie(this);
		}

		public String getHelp() {
			String help = parentOption.getHelpByArgument(this);
			assert Character.toUpperCase(help.charAt(0)) == help
					.charAt(0) : "help message '" + help
							+ "' should start with a capital letter";
			assert help.endsWith(".") : "help message '" + help
					+ "' should end with a dot";
			return help;
		}

		public List<SampleArgumentValue> getSampleValues() {
			assert acceptedValues != AcceptedValues.NONE;
			return parentOption.getSampleArgumentValues(this);
		}

		public String getHelpDisplay() {
			switch (acceptedValues) {
			case NONE:
				return name;
			case ONE:
				return name + "=<>";
			case SEVERAL:
				return name + "=<> ...";
			}
			return null;
		}
	}

	static protected class ArgumentValue {
		private List<String> values = new ArrayList<>();
		ArgumentDescriptor descriptor; // descriptor is hidden at this time
										// because in creation of list of
										// ArgumentsValues, we use Descriptors
										// which may not have the same
										// parentOption

		public void addValue(String value) {
			assert descriptor.acceptedValues != ArgumentDescriptor.AcceptedValues.NONE;
			values.add(value);
			assert values.size() == 1
					|| descriptor.acceptedValues == ArgumentDescriptor.AcceptedValues.SEVERAL;
		}

		public List<String> getValues() {
			return Collections.unmodifiableList(values);
		}

		public ArgumentValue(ArgumentDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		public String getName() {
			return descriptor.name;
		}

		@Override
		public String toString() {
			return getName();
		}

		public String toStringWithValues() {
			StringBuilder s = new StringBuilder();
			for (String arg : asCLI()) {
				s.append(arg);
				s.append(" ");
			}
			if (s.length() > 0)
				s.deleteCharAt(s.length() - 1);
			return s.toString();
		}

		/**
		 * get the list of CLI argument needed to build this object
		 * 
		 * @return the list of argument needed to build this object.
		 * @see #asCLI(List)
		 */
		public List<String> asCLI() {
			List<String> r = new ArrayList<>();
			asCLI(r);
			return r;
		}

		/**
		 * add the CLI arguments needed to build back this object to the
		 * provided List
		 * 
		 * @param buildList
		 *            the list were arguments are added
		 * @see #asCLI()
		 */
		public void asCLI(List<String> buildList) {
			if (descriptor.acceptedValues == ArgumentDescriptor.AcceptedValues.NONE)
				buildList.add(descriptor.name);
			else
				for (String value : values) {
					buildList.add(descriptor.name + '=' + value);
				}
		}

		public ArgumentDescriptor getDescriptor() {
			return descriptor;
		}

	}

	static protected class SampleArgumentValue {
		/**
		 * The argument for which this value is written.
		 */
		public final ArgumentDescriptor argument;
		/**
		 * A possible value for the {@link #argument}
		 */
		public final String value;
		/**
		 * an optional text describing the value (or {@code null}).
		 */
		public final String help;
		/**
		 * Indicate whether this value can be parsed or if it is an example for
		 * human reading. For instance the value "a number" can be used if an
		 * integer is wanted and real must be false.
		 */
		public final boolean real;

		private boolean hidden = false;

		public SampleArgumentValue(ArgumentDescriptor argument, String value,
				boolean real, String help) {
			super();
			this.argument = argument;
			this.value = value;
			this.help = help;
			this.real = real;
		}

		public SampleArgumentValue(ArgumentDescriptor argument, String value,
				boolean real) {
			this(argument, value, real, null);
		}

		/**
		 * indicate that this value should not be displayed in global help.
		 * 
		 * @see #isHidden();
		 */
		public void hide() {
			hidden = true;
		}

		/**
		 * This method indicate whether this value should be displayed in global
		 * help or not.
		 * 
		 * @return {@code true} if the value should be hidden in global help.
		 * @see #hide()
		 */
		boolean isHidden() {
			return hidden;
		}
	}

	private List<List<OptionTree>> sortedChildren = new ArrayList<>();
	private List<OptionTree> children = new ArrayList<>();
	private OptionTree parent = null;
	private List<OptionValidator> validators = new ArrayList<>();
	protected Component mainComponent = null;
	private JPanel mainContainer = null;
	protected JPanel subTreeContainer = null;
	private JPanel validationContainer = null;
	protected String description = "";
	private OptionCategory category = null;

	/**
	 * get the graphical component for this option and build it if necessary.
	 * 
	 * @return the component to display.
	 */
	public Component getComponent() {
		if (mainContainer == null) {
			mainContainer = new JPanel();
			mainContainer.setLayout(new GridBagLayout());
			JPanel topContainer = new JPanel();
			topContainer.setLayout(
					new BoxLayout(topContainer, BoxLayout.LINE_AXIS));
			validationContainer = new JPanel();
			validationContainer.setLayout(
					new BoxLayout(validationContainer, BoxLayout.PAGE_AXIS));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.LINE_START;
			mainContainer.add(topContainer, constraints);
			constraints.gridy = 0;
			constraints.gridx = 1;
			constraints.weightx = 1;
			mainContainer.add(Box.createGlue(), constraints);
			createMainComponent();
			if (mainComponent != null)
				topContainer.add(mainComponent);
			topContainer.add(Box.createHorizontalGlue());
			topContainer.add(validationContainer);
			checkValidators();
		}
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

			GridBagConstraints verticalListConstraints = new GridBagConstraints();
			verticalListConstraints.gridx = 0;
			verticalListConstraints.gridy = GridBagConstraints.RELATIVE;
			verticalListConstraints.anchor = GridBagConstraints.LINE_START;
			if (selectedSubTree.size() == 1
					&& selectedSubTree.get(0).getChildren().isEmpty()) {
				subTreeContainer = new JPanel();
				subTreeContainer.setLayout(
						new BoxLayout(subTreeContainer, BoxLayout.X_AXIS));
				subTreeContainer.add(Box.createHorizontalStrut(20));
				subTreeContainer.add(selectedSubTree.get(0).getComponent());
				subTreeContainer.add(Box.createGlue());
				mainContainer.add(subTreeContainer, verticalListConstraints);
			} else if (selectedSubTree.size() != 0) {
				subTreeContainer = new JPanel();
				mainContainer.add(subTreeContainer, verticalListConstraints);
				subTreeContainer.setBorder(
						BorderFactory.createTitledBorder(subTreeTitle));
				subTreeContainer.setLayout(new GridBagLayout());
				for (OptionTree subOption : selectedSubTree) {
					subTreeContainer.add(subOption.getComponent(),
							verticalListConstraints);
				}
			}
			mainContainer.revalidate();
		}
	}

	/**
	 * This function must create and fill the Component this.mainComponent
	 */
	protected abstract void createMainComponent();

	/**
	 * Record a new validator for this option.
	 * 
	 * @param validator
	 *            the validator to record.
	 */
	protected void addValidator(OptionValidator validator) {
		validators.add(validator);
	}

	/**
	 * @return the list of validators
	 */
	protected List<OptionValidator> getValidators() {
		return validators;
	}

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
		for (OptionTree child : children) {
			assert child.parent == null;
			child.parent = this;
		}
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
	 * Get the list of immediate children activated by the current value of this
	 * option.
	 * 
	 * @return the list of children activated by the current value of this
	 *         option.
	 */
	protected abstract List<OptionTree> getSelectedChildren();

	/**
	 * Get the list of (grand)*children currently activated.
	 * 
	 * @return the list of all children which are currently activated.
	 */
	public List<OptionTree> getAllSelectedChildren() {
		List<OptionTree> r = new ArrayList<>();
		Stack<OptionTree> toProcess = new Stack<>();
		toProcess.addAll(getSelectedChildren());
		while (!toProcess.isEmpty()) {
			OptionTree current = toProcess.pop();
			r.add(current);
			toProcess.addAll(current.getSelectedChildren());
		}
		return r;
	}

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
	 * @return the argument which bring this option in the current state
	 */
	protected abstract ArgumentValue getSelectedArgument();

	/**
	 * Get argument which can help the user to debug this configuration. It
	 * should be the same as {@link #getSelectedArgument()} most of the time but
	 * can be changed for some options, typically log option and random options
	 * 
	 * @return the argument which can bring this option in a state which produce
	 *         the same processing.
	 */
	protected ArgumentValue getDebugArgument() {
		return getSelectedArgument();
	}

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
	 * build a string which can be used to call back SIMPA in CLI with the
	 * current values;
	 * 
	 * @param forDebug
	 *            {@code true} to launch the same configuration for debug or
	 *            {@code false} to launch a similar configuration. The main
	 *            differences are the storage of seeds (see
	 *            {@link #getSelectedArgument()} and
	 *            {@link #getDebugArgument()}.
	 * @return a single string were argument are separated with spaces (spaces
	 *         in arguments are escaped).
	 */
	public String buildBackCLILine(boolean forDebug) {
		List<OptionTree> options = getAllSelectedChildren();
		options.add(this);
		List<String> arguments = new ArrayList<>();
		for (OptionTree option : options) {
			ArgumentValue arg;
			if (forDebug)
				arg = option.getDebugArgument();
			else
				arg = option.getSelectedArgument();
			assert arg != null || option instanceof NoArgumentOption;
			if (arg != null)
				arg.asCLI(arguments);
		}
		return Utils.listToString(arguments);
	}

	/**
	 * same as {@link #parseArguments(List, PrintStream)} but take arguments as
	 * a space separated single string.
	 * 
	 * @param args
	 *            the string to parse
	 * @param parsingErrorStream
	 *            a stream to output errors
	 * @return false if the parsing cannot be achieved
	 * @see #parseArguments(List, PrintStream) parseArguments for more details.
	 */
	public boolean parseArguments(String args, PrintStream parsingErrorStream) {
		StringBuilder warnings = new StringBuilder();
		boolean r = parseArguments(Utils.stringToList(args, warnings),
				parsingErrorStream);
		if (warnings.length() != 0) {
			parsingErrorStream.append(LogManager.prefixMultiLines("Warning : ",
					warnings.toString()));
			warnings = new StringBuilder();
		}
		return r;
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
	 * @see #parseArguments(String, PrintStream)
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
						parsingErrorStream.println("Warning : argument "
								+ descriptor.name
								+ " should not have a value and '" + value
								+ "' cannot be parsed as an argument");
					}
					break;
				case ONE:
					if (valueArg.values.size() > 1) {
						parsingErrorStream
								.println("Warning : argument " + descriptor.name
										+ " should have only one value and '"
										+ valueArg.values
										+ "' cannot be parsed as arguments");
					} else if (valueArg.values.size() == 0) {
						parsingErrorStream
								.println("Warning : argument " + descriptor.name
										+ " is waiting for some value");
					}
					break;
				case SEVERAL:
					if (valueArg.values.size() == 0) {
						parsingErrorStream
								.println("Warning : argument " + descriptor.name
										+ " is waiting for some value");
					}
				}

			}
		}
		boolean parsingInternalSucces = parseArgumentsInternal(valuesList,
				parsingErrorStream);
		if (valuesList.size() != 0) {
			parsingErrorStream.println(
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
		StringBuilder helpForSubTreeErrors = new StringBuilder();
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
		}
		boolean deducedFromChildren = false;
		if ((activatingArg == null || parseError)
				&& getSortedChildren().size() != 1) {
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
				helpForSubTreeErrors.append("Info : value of option '"
						+ getName() + "' was deduced from sub options."
						+ " It is better to specify directly the value of this option (with "
						+ getSelectedArgument().toStringWithValues()
						+ ") to remove ambiguity." + System.lineSeparator());
				assert (getSelectedChildren() == subTrees.get(0));
				deducedFromChildren = true;
			}
		}
		if (activatingArg == null && !deducedFromChildren
				&& !(this instanceof NoArgumentOption)) {
			ArgumentValue defaultValue = getDefaultValue();
			if (defaultValue == null) {
				parseError = true;
				parsingErrorStream.println("Cannot define value of option '"
						+ getName() + "' with arguments " + args + ".");
				parsingErrorStream.println("\tUse one argument in "
						+ getAcceptedArguments() + ".");
			} else {
				boolean r = setValueFromArg(defaultValue, parsingErrorStream);
				assert r;
				helpForSubTreeErrors
						.append("Info : using default value for option '"
								+ getName() + "'");
				if (!defaultValue.values.isEmpty())
					helpForSubTreeErrors.append(
							" (" + defaultValue.toStringWithValues() + ")");
				else {
					ArrayList<ArgumentDescriptor> others = new ArrayList<ArgumentDescriptor>(
							getAcceptedArguments());
					others.remove(defaultValue.descriptor);
					if (!others.isEmpty())
						helpForSubTreeErrors.append("." + System.lineSeparator()
								+ "\tOther values can be selected with one argument in "
								+ others);
				}
				helpForSubTreeErrors.append("." + System.lineSeparator());
			}
		}

		// now parse arguments in subtree
		if (parseError) {
			// If there is an error in this option, we try to parse sub-options
			// but without displaying a bunch of errors and warnings.
			parsingErrorStream = new NullStream();
		}
		boolean subTreeSuccessfullyParsed = true;
		List<OptionTree> selectedSubTrees = getSelectedChildren();
		for (OptionTree subtree : selectedSubTrees)
			if (!subtree.parseArgumentsInternal(args, parsingErrorStream)) {
				subTreeSuccessfullyParsed = false;
			}
		if (!subTreeSuccessfullyParsed && helpForSubTreeErrors.length() > 0)
			parsingErrorStream.println(helpForSubTreeErrors.toString());
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
					}

				}
				if (!seen)
					subTreeArguments.add(newArg);
			}
		}
		return subTreeArguments;
	}

	/**
	 * Get the list of arguments to display in global help. This method can be
	 * overridden to hide some arguments (e.g. hide the default arguments).
	 * 
	 * @return the list of arguments which should be displayed in general help.
	 */
	protected List<ArgumentDescriptor> getHelpArguments() {
		return getAcceptedArguments();
	}

	/**
	 * Get description/helping text for the provided argument.
	 * 
	 * @param arg
	 *            an argument activating this option.
	 * @return a text to display in CLI or GUI.
	 */
	public abstract String getHelpByArgument(ArgumentDescriptor arg);

	/**
	 * Get sample values for a specific argument. This method is designed to be
	 * overridden.
	 * 
	 * @param arg
	 *            the argument for which sample values are requested.
	 * @return some values readable by the user.
	 */
	public List<SampleArgumentValue> getSampleArgumentValues(
			ArgumentDescriptor arg) {
		return Collections.emptyList();
	}

	public void printHelp(PrintStream stream) {
		Stack<OptionTree> toCompute = new Stack<>();
		toCompute.add(this);
		Map<OptionCategory, List<ArgumentDescriptor>> descriptors = new HashMap<>();
		for (OptionCategory cat : OptionCategory.values()) {
			descriptors.put(cat, new ArrayList<>());
		}

		while (!toCompute.isEmpty()) {
			OptionTree currentOption = toCompute.pop();
			toCompute.addAll(currentOption.getChildren());
			for (ArgumentDescriptor descriptor : currentOption
					.getHelpArguments()) {
				List<ArgumentDescriptor> list = descriptors
						.get(descriptor.getCategory());
				list.add(descriptor);
			}
		}

		for (OptionCategory cat : OptionCategory.values()) {
			stream.println(cat.title);
			List<ArgumentDescriptor> args = descriptors.get(cat);
			stream.println();
			printArguments(args, stream);
			if (args.size() > 0)
				stream.println();
		}
	}

	public void printAllHelp(PrintStream stream) {
		Stack<OptionTree> toCompute = new Stack<>();
		toCompute.add(this);
		List<ArgumentDescriptor> sortedArguments = new ArrayList<>();
		while (!toCompute.isEmpty()) {
			OptionTree current = toCompute.pop();
			for (ArgumentDescriptor descriptor : current
					.getAcceptedArguments()) {
				sortedArguments.add(descriptor);
			}
			toCompute.addAll(current.getChildren());
		}

		printArguments(sortedArguments, stream);
	}

	public static void printArguments(List<ArgumentDescriptor> args,
			PrintStream stream) {
		Map<String, ArgumentDescriptor> usedArguments = new HashMap<>();
		Map<String, List<SampleArgumentValue>> sampleValues = new HashMap<>();
		int maxArgDispLength = 0;
		List<ArgumentDescriptor> keptArgs = new ArrayList<>();
		List<String> displayedArgs = new ArrayList<>();
		for (ArgumentDescriptor descriptor : args) {

			// add sample values if they are not already in the list.
			if (descriptor.acceptedValues != AcceptedValues.NONE) {
				List<SampleArgumentValue> descriptorSamples = descriptor
						.getSampleValues();
				if (!sampleValues.containsKey(descriptor.name))
					sampleValues.put(descriptor.name, new ArrayList<>());
				for (SampleArgumentValue sample : descriptorSamples) {
					if (sample.isHidden())
						continue;
					boolean exist = false;
					List<SampleArgumentValue> existingValues = sampleValues
							.get(descriptor.name);
					for (SampleArgumentValue existing : existingValues) {
						if (existing.value.equals(sample.value)) {
							assert (existing.help == null
									&& sample.help == null)
									|| (existing.help != null && existing.help
											.equals(sample.help));
							exist = true;
						}
					}
					if (!exist) {
						existingValues.add(sample);
					}
				}
			}

			if (usedArguments.containsKey(descriptor.name)) {
				assert usedArguments.get(descriptor.name).getHelp()
						.equals(descriptor.getHelp()) : descriptor.name
								+ " has two differents help messages : \n"
								+ descriptor.getHelp() + "\n"
								+ usedArguments.get(descriptor.name).getHelp();
				assert usedArguments.get(
						descriptor.name).acceptedValues == descriptor.acceptedValues;
				continue;
			}

			usedArguments.put(descriptor.name, descriptor);
			String disp = descriptor.getHelpDisplay();
			keptArgs.add(descriptor);
			displayedArgs.add(disp);
			if (disp.length() > maxArgDispLength)
				maxArgDispLength = disp.length();
		}

		int MAX_LENGTH = tools.Utils.terminalWidth();
		int argColumnWidth = maxArgDispLength;
		argColumnWidth++;// put a blank space between argument and help.
		for (int i = 0; i < keptArgs.size(); i++) {
			ArgumentDescriptor descriptor = keptArgs.get(i);
			String argDisp = displayedArgs.get(i);
			StringBuilder argumentHelp = new StringBuilder(
					descriptor.getHelp());

			List<SampleArgumentValue> descriptorSamples = sampleValues
					.get(descriptor.name);
			if (descriptorSamples != null && descriptorSamples.size() != 0) {
				argumentHelp = argumentHelp.append(" Possible values are ");
				Collections.sort(descriptorSamples,
						new Comparator<SampleArgumentValue>() {
							@Override
							public int compare(SampleArgumentValue o1,
									SampleArgumentValue o2) {
								if (o1.help == null && o2.help != null)
									return -1;
								if (o2.help == null && o1.help != null)
									return 1;
								if (o1.real && !o2.real)
									return 1;
								if (o2.real && !o1.real)
									return -1;
								return 0;
							}
						});
				boolean hasHelp = false;
				Iterator<SampleArgumentValue> it = descriptorSamples.iterator();
				SampleArgumentValue sample;
				while (it.hasNext()) {
					sample = it.next();
					assert !sample.isHidden();
					String value = sample.value;
					if (!sample.real)
						value = "<" + sample.value + ">";
					if (sample.help != null) {
						hasHelp = true;
						argumentHelp = argumentHelp
								.append("\n" + value + " : " + sample.help);
					} else {
						argumentHelp = argumentHelp.append(value);
						if (it.hasNext())
							argumentHelp = argumentHelp.append(" | ");
					}
				}

				if (!hasHelp)
					argumentHelp = argumentHelp.append(".");
			}

			StringBuilder prefix = new StringBuilder();
			prefix.append(argDisp);
			while (prefix.length() < argColumnWidth)
				prefix.append(' ');
			stream.print(Utils.prefixString(prefix.toString(),
					argumentHelp.toString(), MAX_LENGTH));
		}
	}

	/**
	 * Get the value of this option or {@code null} if this option has no value.
	 * 
	 * @return the {@link ValueHolder} use by this option or {@code null} if
	 *         this option do not use one.
	 */
	public abstract ValueHolder<?, ?> getValueHolder();

	/**
	 * Check validators of this option and update graphical interface if needed.
	 */
	private void checkValidators() {
		if (validationContainer != null)
			validationContainer.removeAll();
		for (OptionValidator validator : validators) {
			validator.check();
			if (validationContainer != null) {
				validationContainer.add(validator.createComponent());
			}
		}
		if (validationContainer != null)
			validationContainer.revalidate();
	}

	/**
	 * Validate every selected option and return the maximum criticality level
	 * encountered.
	 * 
	 * @return the maximum critiality level in the selected tree.
	 * @see #validateSelectedTree(boolean) to print messages in console.
	 */
	public OptionValidator.CriticalityLevel validateSelectedTree() {
		return validateSelectedTree(false, null);
	}

	/**
	 * Validate every selected option and return the maximum criticality level
	 * encountered.
	 * 
	 * @param printConsole
	 *            indicate whether to print messages encountered in console or
	 *            not. Graphical messages are updated in any case.
	 * @param filter
	 *            a predicate to indicate which validators to take into account.
	 *            if {@code null}, all validators are used.
	 * 
	 * @return the maximum criticality level encountered in the tree.
	 */
	public OptionValidator.CriticalityLevel validateSelectedTree(
			boolean printConsole, Predicate<OptionValidator> filter) {
		CriticalityLevel max = CriticalityLevel.NOTHING;
		checkValidators();
		for (OptionValidator validator : validators) {
			if (filter != null && !filter.test(validator))
				continue;
			if (printConsole
					&& validator.getCriticality() != CriticalityLevel.NOTHING) {
				System.out.println(Utils.prefixString(
						validator.getCriticality() + " in option '" + getName()
								+ "' : ",
						validator.getMessage() + "\nTry to change \""
								+ buildBackCLILine(false)
								+ "\" with other options or with another value.",
						Utils.terminalWidth()));
			}
			if (validator.getCriticality().compareTo(max) > 0)
				max = validator.getCriticality();
		}
		for (OptionTree option : getSelectedChildren()) {
			CriticalityLevel optionLevel = option
					.validateSelectedTree(printConsole, filter);
			if (optionLevel.compareTo(max) > 0)
				max = optionLevel;
		}
		return max;

	}

	public String getName() {
		ValueHolder<?, ?> value = getValueHolder();
		if (value != null)
			return value.getName();
		if (description != null && !description.isEmpty())
			return description;
		return getClass().getName();
	}

	/**
	 * Set the category of this option.
	 * 
	 * @param cat
	 *            the category of this option.
	 */
	public final void setCategory(OptionCategory cat) {
		assert category == null;
		category = cat;
	}

	/**
	 * set the category of this option unless the category is already defined.
	 * 
	 * @param cat
	 *            the category to set.
	 */
	public final void setCategoryIfUndef(OptionCategory cat) {
		if (category == null)
			setCategory(cat);
	}

	/**
	 * Get the category of this option.
	 * 
	 * @return the category of this option.
	 */
	public final OptionCategory getCategory() {
		if (category != null)
			return category;
		assert parent != null : "Root option must have a category";
		return parent.getCategory();
	}

	/**
	 * Get the category of an argument of this option. The default is the
	 * category of the option itself but this method can be overridden to put
	 * arguments in different categories.
	 * 
	 * @param arg
	 *            the argument to classify
	 * @return the category of the given argument.
	 */
	public OptionCategory getCategorie(ArgumentDescriptor arg) {
		return getCategory();
	}
}
