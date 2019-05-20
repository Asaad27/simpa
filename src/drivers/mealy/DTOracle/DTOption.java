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

import options.GenericMultiArgChoiceOption;
import options.IntegerOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.RandomOption;

public class DTOption extends MultiArgChoiceOptionItem {

	public static class ModeOption extends MultiArgChoiceOption {

		public static class ExtendedMode extends MultiArgChoiceOptionItem {

			RandomOption rand = new RandomOption("--ODT_seed",
					"random sequences");
			private IntegerOption sequenceLength = new IntegerOption(
					"--ODT_sequence_length"// TODO check name
					, "length for added sequences",
					"Length for the random sequences added after each transition.",
					2);

			public ExtendedMode(GenericMultiArgChoiceOption<?> parent) {
				super("extended (add a random path after each transition)",
						"--ODT_extended", parent);
				subTrees.add(rand);
				subTrees.add(sequenceLength);
			}

			public int getLength() {
				return sequenceLength.getValue();
			}

			public String toCSV() {
				return Integer.toString(getLength());
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
