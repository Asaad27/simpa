package options.learnerOptions;

import java.util.ArrayList;
import java.util.List;

import options.BooleanOption;
import options.GenericMultiArgChoiceOption;
import options.IntegerOption;
import options.MultiArgChoiceOption;
import options.MultiArgChoiceOptionItem;
import options.OptionTree;

public class OracleOption extends MultiArgChoiceOption {
	protected final boolean resetAllowed;

	public class MrBeanOptionItem extends MultiArgChoiceOptionItem {
		public class MrBeanOnlyIfExistsOption extends BooleanOption {

			private MrBeanOnlyIfExistsOption(String name, String argument,
					String description, List<OptionTree> subTreeIfTrue,
					List<OptionTree> subTreeIfFalse, boolean enabled) {
				super("check existance of counter example before calling MrBean",
						"exhaustive-before-MrBean",
						"First do an exhaustive check to see if a counter example exists and if it exists do a random walk to find it",
						subTreeIfTrue, subTreeIfFalse, false);
			}
		}

		BooleanOption mrBeanOnlyIfExists;
		IntegerOption maxTraceLength;
		IntegerOption maxTraceNumber;// null if reset is not allowed

		public MrBeanOptionItem(GenericMultiArgChoiceOption<?> parent) {
			super("ask MrBean to find a counter example (random walk)",
					"--mrBean", parent);
			List<OptionTree> randomWalkOptions = new ArrayList<>();
			maxTraceLength = new IntegerOption("--maxcelength",
					"maximum length of one random walk (from a reset if there is a reset or for all the walk)",
					100);
			randomWalkOptions.add(maxTraceLength);
			if (resetAllowed) {
				maxTraceNumber = new IntegerOption("--maxceresets",
						"maximum number of reset i.e. maximum number of random walk from initial state for oracle.",
						10);
				randomWalkOptions.add(maxTraceNumber);
			}
			mrBeanOnlyIfExists = new BooleanOption(
					"check existance of counter example before calling MrBean",
					"exhaustive-before-MrBean",
					"First do an exhaustive check to see if a counter example exists and if it exists do a random walk to find it",
					new ArrayList<OptionTree>(), randomWalkOptions, false) {
				@Override
				public String getSubTreeTitle() {
					return isEnabled() ? ""
							: "options for random walk when there is no pre-check on the automaton";
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

	}

	public MultiArgChoiceOptionItem shortest;
	public MrBeanOptionItem mrBean;
	public MultiArgChoiceOptionItem interactive;

	public OracleOption(boolean resetAllowed) {
		this.resetAllowed = resetAllowed;
		shortest = new MultiArgChoiceOptionItem("use shortest counter example",
				"--shortestCE", this);

		mrBean = new MrBeanOptionItem(this);
		interactive = new MultiArgChoiceOptionItem(
				"prompt user each time a CE is needed", "--interactiveCE",
				this);
		addChoice(shortest);
		addChoice(mrBean);
		addChoice(interactive);
	}

	public boolean isResetAllowed() {
		return resetAllowed;
	}

}
