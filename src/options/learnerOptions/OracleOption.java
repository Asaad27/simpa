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
package options.learnerOptions;

import drivers.mealy.PartialMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import options.*;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.automataOptions.TransparentDriverValidator;
import tools.loggers.LogManager;

import java.util.ArrayList;
import java.util.List;

public class OracleOption extends MultiArgChoiceOption {
	protected final boolean resetAllowed;

	private final TransparentDriverValidator driverValidator = new TransparentDriverValidator() {
		@Override
		public void check() {
			if (getSelectedItem() == shortest)
				super.check();
			else
				clear();
		}
	};

	public class MrBeanOptionItem extends MultiArgChoiceOptionItem {

		BooleanOption mrBeanOnlyIfExists;
		private final TransparentDriverValidator ifExistValidator = new TransparentDriverValidator() {
			@Override
			public void check() {
				if (MrBeanOptionItem.this.mrBeanOnlyIfExists.isEnabled())
					super.check();
				else
					clear();
			}
		};
		private final TransparentDriverValidator maxTraceNumberValidator = new TransparentDriverValidator() {
			@Override
			public void check() {
				if (MrBeanOptionItem.this.maxTraceNumber != null
						&& MrBeanOptionItem.this.maxTraceNumber.useAutoValue())
					super.check();
				else
					clear();
			}
		};
		AutoIntegerOption maxTraceLength;
		AutoIntegerOption maxTraceNumber;// null if reset is not allowed
		public final RandomOption random;

		public MrBeanOptionItem(GenericMultiArgChoiceOption<?> parent) {
			super("random walk on conjecture", "--ORnd", parent);
			random = new RandomOption("--ORnd_seed", "random walks");
			subTrees.add(random);
			List<OptionTree> randomWalkOptions = new ArrayList<>();
			maxTraceLength = new AutoIntegerOption("--ORnd_maxlength",
					"maximum length of one random walk",
					"The length of random walk from a reset if there is a reset or for all the walk."
							+ " The automatic value use a length proportional to the size of driver.",
					100);
			randomWalkOptions.add(maxTraceLength);
			if (resetAllowed) {
				maxTraceNumber = new AutoIntegerOption("--ORnd_maxresets",
						"maximum number of reset ",
						"Maximum number of random walk from initial state for oracle."
								+ " The automatic value reset the driver a number of time proportional to its size.",
						5) {
					{
						addValidator(maxTraceNumberValidator);
					}
				};
				randomWalkOptions.add(maxTraceNumber);
			}
			mrBeanOnlyIfExists = new BooleanOption(
					"check existence of counter example before calling MrBean",
					"OTRnd_precheck",
					"First do an exhaustive check to see if a counter example exists and if it exists do a random walk to find it.",
					new ArrayList<OptionTree>(), randomWalkOptions, false) {
				{
					addValidator(ifExistValidator);
				}

				@Override
				public String getSubTreeTitle() {
					return isEnabled() ? ""
							: "options for random walk when there is no pre-check on the automaton";
				}

				@Override
				public String getDisableHelp() {
					return "Call MrBean without searching counterExample in transparent box.";
				}

				@Override
				protected void makeArgumentDescriptors(String argument) {
					super.makeArgumentDescriptors(argument);
					disableArgumentDescriptor = new ArgumentDescriptor(
							AcceptedValues.NONE,
							"--ORnd_no-exhaustive-before-MrBean", this);
				}
			};

			subTrees.add(mrBeanOnlyIfExists);
		}

		public boolean onlyIfCEExists() {
			return mrBeanOnlyIfExists.isEnabled();
		}

		public int getMaxTraceLength() {
			return maxTraceLength.getValue();
		}

		public int getMaxTraceNumber() {
			if (resetAllowed)
				return maxTraceNumber.getValue();
			assert maxTraceNumber == null;
			return 1;
		}

		public void updateWithDriver(PartialMealyDriver d) {
			ifExistValidator.setLastDriver(d);
			maxTraceNumberValidator.setLastDriver(d);
			final boolean isSelected = OracleOption.this
					.getSelectedItem() == this;
			if (maxTraceLength.useAutoValue()) {
				maxTraceLength.setValueAuto(d.getDefinedInputs().size() * 25000);
				if (isSelected)
					LogManager.logInfo("Maximum counter example length set to ",
							getMaxTraceLength());
			}
			if (maxTraceNumber != null) {
				maxTraceNumber.clearAutoValueError();
				if (maxTraceNumber.useAutoValue()) {
					if (d instanceof TransparentMealyDriver) {
						TransparentMealyDriver transparent = (TransparentMealyDriver) d;
						maxTraceNumber
								.setValueAuto(transparent.getStateCount() * 20);
						if (isSelected && !mrBeanOnlyIfExists.isEnabled()) {
							LogManager.logInfo(
									"Maximum number of random walk set to ",
									getMaxTraceNumber());
						}
					}
				}
			}
		}

	}

	public MultiArgChoiceOptionItem shortest;
	public MrBeanOptionItem mrBean;
	public MultiArgChoiceOptionItem interactive;
	public MultiArgChoiceOptionItem distinctionTreeBased;

	public OracleOption(boolean resetAllowed) {
		super("Oracle choice");
		setCategory(OptionCategory.ORACLE);
		this.resetAllowed = resetAllowed;
		addValidator(driverValidator);
		shortest = new MultiArgChoiceOptionItem("use shortest counter example",
				"--OT_shortest", this);

		mrBean = new MrBeanOptionItem(this);
		interactive = new MultiArgChoiceOptionItem(
				"prompt user each time a CE is needed", "--O_interactive",
				this);
		distinctionTreeBased = new MultiArgChoiceOptionItem(
				"pseudo checking sequence using distinction tree", "--O_DT",
				this);
		addChoice(shortest);
		addChoice(mrBean);
		addChoice(interactive);
		addChoice(distinctionTreeBased);
	}

	public boolean isResetAllowed() {
		return resetAllowed;
	}

	/**
	 * compute parameters which depends of driver and check that options are
	 * compatible with the selected driver.
	 *
	 * @param driver the SUI
	 */
	public void updateWithDriver(PartialMealyDriver driver) {
		driverValidator.setLastDriver(driver);
		mrBean.updateWithDriver(driver);
		validateSelectedTree();
	}
}
