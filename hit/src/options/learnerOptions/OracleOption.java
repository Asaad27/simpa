package options.learnerOptions;

import java.util.ArrayList;
import java.util.List;

import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import options.AutoIntegerOption;
import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;
import options.RandomOption;
import options.automataOptions.TransparentDriverValidator;
import tools.loggers.LogManager;

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
			};
		};
		private final TransparentDriverValidator maxTraceNumberValidator = new TransparentDriverValidator() {
			@Override
			public void check() {
				if (MrBeanOptionItem.this.maxTraceNumber != null
						&& MrBeanOptionItem.this.maxTraceNumber.useAutoValue())
					super.check();
				else
					clear();
			};
		};
		AutoIntegerOption maxTraceLength;
		AutoIntegerOption maxTraceNumber;// null if reset is not allowed
		public final RandomOption random;

		public MrBeanOptionItem(GenericMultiArgChoiceOption<?> parent) {
			super("ask MrBean to find a counter example (random walk)",
					"--mrBean", parent);
			random = new RandomOption("--oracleSeed", "random walks");
			subTrees.add(random);
			List<OptionTree> randomWalkOptions = new ArrayList<>();
			maxTraceLength = new AutoIntegerOption("--maxcelength",
					"maximum length of one random walk",
					"The length of random walk from a reset if there is a reset or for all the walk."
							+ " The automatic value use a length proportional to the size of driver.",
					100);
			randomWalkOptions.add(maxTraceLength);
			if (resetAllowed) {
				maxTraceNumber = new AutoIntegerOption("--maxceresets",
						"maximum number of reset ",
						"Maximum number of random walk from initial state for oracle."
								+ " The automatic value reset the driver a number of time proprtional to its size.",
						5) {
					{
						addValidator(maxTraceNumberValidator);
					}
				};
				randomWalkOptions.add(maxTraceNumber);
			}
			mrBeanOnlyIfExists = new BooleanOption(
					"check existence of counter example before calling MrBean",
					"exhaustive-before-MrBean",
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

		public void updateWithDriver(MealyDriver d) {
			ifExistValidator.setLastDriver(d);
			maxTraceNumberValidator.setLastDriver(d);
			final boolean isSelected = OracleOption.this
					.getSelectedItem() == this;
			if (maxTraceLength.useAutoValue()) {
				maxTraceLength.setValueAuto(d.getInputSymbols().size() * 5000);
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
		this.resetAllowed = resetAllowed;
		addValidator(driverValidator);
		shortest = new MultiArgChoiceOptionItem("use shortest counter example",
				"--shortestCE", this);

		mrBean = new MrBeanOptionItem(this);
		interactive = new MultiArgChoiceOptionItem(
				"prompt user each time a CE is needed", "--interactiveCE",
				this);
		distinctionTreeBased = new MultiArgChoiceOptionItem(
				"pseudo checking sequence using distinction tree",
				"--DT-based-CE", this);
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
	 * @param driver
	 *            the SUI
	 */
	public void updateWithDriver(MealyDriver driver) {
		driverValidator.setLastDriver(driver);
		mrBean.updateWithDriver(driver);
		validateSelectedTree();
	}
}
