/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.DTOracle;

import java.util.Collections;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.IntegerOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.RandomOption;

public class DTOption extends MultiArgChoiceOptionItem {

	public static class ModeOption extends MultiArgChoiceOption {

		public static class ExtendedMode extends MultiArgChoiceOptionItem {

			RandomOption rand = new RandomOption("--ODT_seed",
					"random sequences");
			private IntegerOption sequenceLength = new IntegerOption(
					"--ODT_sequence_length", "length for added sequences",
					"Length for the random sequences added after each transition.",
					5);
			private BooleanOption randomLength = new BooleanOption(
					"use random length", "ODT_random_length",
					"Reduce randomly the length of each sequence.",
					Collections.emptyList(), Collections.emptyList(), true) {
				@Override
				protected void makeArgumentDescriptors(String argument) {
					super.makeArgumentDescriptors(argument);
					disableArgumentDescriptor = new ArgumentDescriptor(
							AcceptedValues.NONE, "--ODT_fixed_length", this);
				}

			};
			private IntegerOption sequenceNumber = new IntegerOption(
					"--ODT_sequence_number", "number a sequences",
					"Number of random sequences added after each transition.",
					2);

			public ExtendedMode(GenericMultiArgChoiceOption<?> parent) {
				super("extended (add a random path after each transition)",
						"--ODT_extended", parent);
				sequenceLength.setDefaultValue(5);
				sequenceNumber.setDefaultValue(2);
				subTrees.add(rand);
				subTrees.add(sequenceLength);
				subTrees.add(randomLength);
				subTrees.add(sequenceNumber);
			}

			public int getLength() {
				return sequenceLength.getValue();
			}

			public int getSequenceNumber() {
				return sequenceNumber.getValue();
			}

			public boolean useRandomLength() {
				return randomLength.isEnabled();
			}

			public String toCSV() {
				return getSequenceNumber() + ";" + getLength() + ";"
						+ useRandomLength();
			}

		}

		/**
		 * not enabled yet
		 */
		public static class RandomMode extends MultiArgChoiceOptionItem {

			public RandomMode(GenericMultiArgChoiceOption<?> parent) {
				super("test random paths", "--ODT_rand", parent);
			}

			public String toCSV() {
				// TODO Auto-generated method stub
				return null;
			}

		}

		MultiArgChoiceOptionItem simpleMode;
		ExtendedMode extendedMode;
		RandomMode randomMode;

		public ModeOption() {
			super("Distinction Tree mode");
			simpleMode = new MultiArgChoiceOptionItem(
					"simple (test each transition with full characterization)",
					"--ODT_simple", this);
			extendedMode = new ExtendedMode(this);
			randomMode = new RandomMode(this);
			new MultiArgChoiceOptionItem("test random paths", "--ODT_rand",
					this);
			addChoice(simpleMode);
			addChoice(extendedMode);
			// addChoice(randomMode);//TODO
		}

	}

	ModeOption DTmode = new ModeOption();

	public String toCSV() {
		if (DTmode.getSelectedItem() == DTmode.simpleMode) {
			return "DT_simple";
		} else if (DTmode.getSelectedItem() == DTmode.extendedMode)
			return "DT_extended(" + DTmode.extendedMode.toCSV() + ")";
		else if (DTmode.getSelectedItem() == DTmode.randomMode)
			return "DT_random(" + DTmode.randomMode.toCSV() + ")";
		else
			throw new RuntimeException("must be implemented");
	}

	public DTOption(GenericMultiArgChoiceOption<?> parent) {
		super("pseudo checking sequence using distinction tree", "--O_DT",
				parent);
		subTrees.add(DTmode);
	}

}
