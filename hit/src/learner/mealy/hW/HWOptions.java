package learner.mealy.hW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import drivers.mealy.MealyDriver;
import options.BooleanOption;
import options.GenericOneArgChoiceOption;
import options.OneArgChoiceOptionItem;
import options.OptionCategory;
import options.OptionTree;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.automataOptions.TransparentDriverValidator;
import options.learnerOptions.OracleOption;

public class HWOptions extends OneArgChoiceOptionItem {
	class PreComputedW extends BooleanOption {
		private final TransparentDriverValidator driverValidator = new TransparentDriverValidator() {
			@Override
			public void check() {
				if (PreComputedW.this.isEnabled())
					super.check();
				else
					clear();
			}
		};

		public PreComputedW() {
			super("use a computed W-set", "hW_with-computed-W",
					"Compute a W-set before starting inference. This needs a transparent driver.",
					Collections.emptyList(), Collections.emptyList(), false);
			addValidator(driverValidator);
		}

		@Override
		public String getDisableHelp() {
			return "Do not use a glass-box driver to compute a W-set.";
		}

		@Override
		protected void makeArgumentDescriptors(String argument) {
			super.makeArgumentDescriptors(argument);
			disableArgumentDescriptor = new ArgumentDescriptor(
					AcceptedValues.NONE, "--hW_without-computed-W", this);
		}

		void updateWithDriver(MealyDriver d) {
			driverValidator.setLastDriver(d);
			validateSelectedTree();
		}
	};

	public final BooleanOption addHInW;
	public final BooleanOption useReset;
	public final BooleanOption searchCeInTrace;
	public final BooleanOption checkInconsistenciesHMapping;
	public final BooleanOption useDictionary;
	private final PreComputedW usePrecomputedW;
	private final BooleanOption addIInW;
	private final BooleanOption useAdaptiveH;
	private final BooleanOption useAdaptiveW;

	public final OracleOption getOracleOption() {
		if (useReset.isEnabled())
			return oracleWhenUsingReset;
		else
			return oracleWhithoutReset;
	}

	public boolean useAdaptiveH() {
		return !addHInW.isEnabled() && useAdaptiveH.isEnabled();
	}

	public boolean addIInW() {
		return !useAdaptiveW() && addIInW.isEnabled();
	}

	public boolean useAdaptiveW() {
		return !addHInW.isEnabled() && useAdaptiveW.isEnabled();
	}

	public boolean usePrecomputedW() {
		return usePrecomputedW.isEnabled();
	}

	private final OracleOption oracleWhenUsingReset;
	private final OracleOption oracleWhithoutReset;

	public HWOptions(GenericOneArgChoiceOption<?> parent) {
		super("hW", "MhW", parent);
		checkInconsistenciesHMapping = new BooleanOption(
				"search 3rd inconsistencies", "3rd-inconsistency",
				"Search inconsistencies between homing sequence and conjecture.");
		searchCeInTrace = new BooleanOption("search counter example in trace",
				"try-trace-CE",
				"Try to execute the traces observed on conjecture to see if it makes a counter example.");
		useDictionary = new BooleanOption("use dictionary", "use-dictionary",
				"Record the sequences of form 'h z x w' and 'h w' to avoid re-executing them on the SUI.");
		usePrecomputedW = new PreComputedW();
		addIInW = new BooleanOption("add input symbols in W", "add-I-in-W",
				"Before starting inference, all inputs symbols of SUI are added to W-set as new input sequences.") {
			@Override
			public String getDisableHelp() {
				return "Start with an empty W-set.";
			}
		};
		useAdaptiveH = new BooleanOption("use adaptive homing sequence",
				"adaptive-h",
				"Use an adaptive homing sequence instead of a preset sequence.") {
			@Override
			public String getDisableHelp() {
				return "Use a preset homing sequence (not adaptive).";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE, "--preset-h",
						this);
			}
		};
		useAdaptiveW = new BooleanOption("use adaptive W-tree", "adaptive-W",
				"Use an adaptive W-tree instead of a preset W-set.",
				new ArrayList<OptionTree>(), Arrays.asList(addIInW)) {
			@Override
			public String getDisableHelp() {
				return "Use a preset W-set.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE, "--preset-W",
						this);
			}
		};
		addHInW = new BooleanOption("heuristic add h in W", "add-h-in-W",
				"Add homing sequence in W-set.", new ArrayList<OptionTree>(),
				Arrays.asList((OptionTree) useAdaptiveH,
						(OptionTree) useAdaptiveW));
		oracleWhithoutReset = new OracleOption(false);
		oracleWhenUsingReset = new OracleOption(true);

		useReset = new BooleanOption("use reset", "use-reset",
				"Allow the algorithm to use reset when it seems to be necessary "
						+ "(the oracle will also use reset to check validity of conjecture).",
				Arrays.asList((OptionTree) oracleWhenUsingReset),
				Arrays.asList((OptionTree) oracleWhithoutReset)) {
			@Override
			public String getDisableHelp() {
				return "Infer whithout reseting the driver (assuming that the SUL is connected).";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						ArgumentDescriptor.AcceptedValues.NONE, "--no-reset",
						this);
			}
		};
		subTrees.add(useReset);
		subTrees.add(usePrecomputedW);
		subTrees.add(addHInW);
		subTrees.add(useDictionary);
		subTrees.add(checkInconsistenciesHMapping);
		subTrees.add(searchCeInTrace);
		for (OptionTree option : subTrees)
			option.setCategoryIfUndef(OptionCategory.ALGO_HW);
	}

	public void updateWithDriver(MealyDriver d) {
		usePrecomputedW.updateWithDriver(d);
		oracleWhenUsingReset.updateWithDriver(d);
		oracleWhithoutReset.updateWithDriver(d);
	}
}
