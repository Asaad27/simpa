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
package learner.mealy.hW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import drivers.mealy.MealyDriver;
import learner.mealy.hW.refineW.GenWPair;
import learner.mealy.hW.refineW.PassThroughWSet;
import learner.mealy.hW.refineW.ReduceW;
import learner.mealy.hW.refineW.WSetOptimization;
import options.*;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.automataOptions.TransparentDriverValidator;
import options.learnerOptions.OracleOption;

import static options.FileOption.FileExistance.NO_CHECK;
import static options.FileOption.FileSelectionMode.FILES_ONLY;

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
            super("use a computed W-set", "TMhW_with_computed_W",
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
                    AcceptedValues.NONE, "--MhW_without_computed_W", this);
        }

        void updateWithDriver(MealyDriver d) {
            driverValidator.setLastDriver(d);
            validateSelectedTree();
        }
    }

    ;

    public final BooleanOption addHInW;
    public final BooleanOption useReset;
    public final BooleanOption searchCeInTrace;
    public final BooleanOption checkInconsistenciesHMapping;
    public final BooleanOption useDictionary;
    private final PreComputedW usePrecomputedW;
    private final BooleanOption addIInW;
    private final BooleanOption useAdaptiveH;
    private final BooleanOption useAdaptiveW;
    private final FileOption transferSequencesRecord;
    public final TextOption initialW;
    public TextOption initialH;
    private final GenericOneArgChoiceOption<OneArgChoiceOptionItem> wRefinement;

    public final OracleOption getOracleOption() {
        if (useReset.isEnabled())
            return oracleWhenUsingReset;
        else
            return oracleWithoutReset;
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
    private final OracleOption oracleWithoutReset;
    private final GenericOneArgChoiceOption<OneArgChoiceOptionItem> findPathStrategy;

    public String getFindPathStrategy() {
        return findPathStrategy.getSelectedItem().argValue;
    }

    public WSetOptimization getWRefinement() {
        switch (wRefinement.getSelectedItem().argValue) {
            case "none":
                return new PassThroughWSet();
            case "reduceW":
                return new ReduceW();
            case "genWPair":
                return new GenWPair();
            default:
                throw new IllegalArgumentException("There is no W-Optimization strategy \"" + wRefinement.getSelectedItem().argValue + "\"");
        }
    }

    public Optional<Path> getTransferSequenceRecord() {
        if (getFindPathStrategy().equals("interactive-record"))
            return Optional.of(transferSequencesRecord.getcompletePath().toPath());
        else
            return Optional.empty();
    }

    public HWOptions(GenericOneArgChoiceOption<?> parent) {
        super("hW", "MhW", parent);
        checkInconsistenciesHMapping = new BooleanOption(
                "search 3rd inconsistencies", "MhW_hC_inc",
                "Search inconsistencies between homing sequence and conjecture.",
                Collections.emptyList(), Collections.emptyList(), true) {
            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        AcceptedValues.NONE, "--MhW_no_hC_inc", this);
            }
        };
        searchCeInTrace = new BooleanOption("search counter example in trace",
                "MhW_trace_ce",
                "Try to execute the traces observed on conjecture to see if it makes a counter example.",
                Collections.emptyList(), Collections.emptyList(), true) {
            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        AcceptedValues.NONE, "--MhW_no_trace_CE", this);
            }
        };
        useDictionary = new BooleanOption("use dictionary", "Mhw_use_dict",
                "Record the sequences of form 'h z x w' and 'h w' to avoid re-executing them on the SUI.",
                Collections.emptyList(), Collections.emptyList(), true) {
            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        AcceptedValues.NONE, "--MhW_no_dict", this);
            }
        };
        usePrecomputedW = new PreComputedW();
        addIInW = new BooleanOption("add input symbols in W", "MhW_add_I_in_W",
                "Before starting inference, all inputs symbols of SUI are added to W-set as new input sequences.") {
            @Override
            public String getDisableHelp() {
                return "Start with an empty W-set.";
            }

            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        AcceptedValues.NONE, "--MhW_do_not_add_I_in_W", this);
            }
        };
        useAdaptiveH = new BooleanOption("use adaptive homing sequence",
                "Mhw_adaptive_h",
                "Use an adaptive homing sequence instead of a preset sequence.",
                Collections.emptyList(), Collections.emptyList(), true) {
            @Override
            public String getDisableHelp() {
                return "Use a preset homing sequence.";
            }

            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        ArgumentDescriptor.AcceptedValues.NONE,
                        "--MhW_preset_h", this);
            }
        };
        useAdaptiveW = new BooleanOption("use adaptive W-tree",
                "MhW_adaptive_W",
                "Use an adaptive W-tree instead of a preset W-set.",
                new ArrayList<OptionTree>(), Arrays.asList(addIInW), true) {
            @Override
            public String getDisableHelp() {
                return "Use a preset W-set.";
            }

            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        ArgumentDescriptor.AcceptedValues.NONE,
                        "--MhW_preset_W", this);
            }
        };
        addHInW = new BooleanOption("heuristic add h in W", "MhW_add_h_in_W",
                "Add homing sequence in W-set.", new ArrayList<OptionTree>(),
                Arrays.asList(useAdaptiveH, useAdaptiveW), false) {
            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        ArgumentDescriptor.AcceptedValues.NONE,
                        "--MhW_do_not_add_h_in_W", this);
            }

        };
        initialW = new TextOption("--MhW_initial_W", "", "initial W-set", "W-set to" +
                " use " +
                "for the first subinference (default: empty set)");
        initialH = new TextOption("--MhW_initial_h", "", "intial homing sequence", "homing sequence to use in the " +
                "first subinference (default: empty sequence)");
        oracleWithoutReset = new OracleOption(false);
        oracleWhenUsingReset = new OracleOption(true);

        useReset = new BooleanOption("use reset", "MhW_use_reset",
                "Allows the algorithm to use reset when it seems to be necessary "
                        + "(the oracle will also use reset to check validity of conjecture).",
                Arrays.asList((OptionTree) oracleWhenUsingReset),
                Arrays.asList((OptionTree) oracleWithoutReset), false) {
            @Override
            public String getDisableHelp() {
                return "Infer without reseting the driver (assuming that the SUL is connected).";
            }

            @Override
            protected void makeArgumentDescriptors(String argument) {
                super.makeArgumentDescriptors(argument);
                disableArgumentDescriptor = new ArgumentDescriptor(
                        ArgumentDescriptor.AcceptedValues.NONE,
                        "--MhW_without_reset", this);
            }
        };
        findPathStrategy = new GenericOneArgChoiceOption<>("--transferOracle", "Transfer Oracle",
                "Strategy to choose a path from the current state to a not fully" +
                        " characterized state.") {
            {
                OneArgChoiceOptionItem shortestPathChoiceItem = new OneArgChoiceOptionItem("lexicographic", "lexicographic", this);
                addChoice(shortestPathChoiceItem);
                addChoice(new OneArgChoiceOptionItem("interactive", "interactive", this));
                addChoice(new OneArgChoiceOptionItem("interactive-record", "interactive-record", this));
                setDefaultItem(shortestPathChoiceItem);
            }
        };
        wRefinement = new GenericOneArgChoiceOption<>("--wRefinement", "refine W-set after subinference",
                "Strategy that attempts to reduce the size of W after every subinference.") {
            {
                OneArgChoiceOptionItem defaultChoice = new OneArgChoiceOptionItem("none", "none", this);
                addChoice(defaultChoice);
                addChoice(new OneArgChoiceOptionItem("reduceW", "reduceW", this));
                addChoice(new OneArgChoiceOptionItem("genWPair", "genWPair", this));
            //    addChoice(new OneArgChoiceOptionItem("reduceW3", "reduceW3", this));
             //   addChoice(new OneArgChoiceOptionItem("genWFromConjecture", "genWFromConjecture", this));
                setDefaultItem(defaultChoice);
            }
        };
        transferSequencesRecord = new FileOption("--transferSequencesRecord", "Transfer Record", FILES_ONLY, NO_CHECK);


        subTrees.add(useReset);
        subTrees.add(usePrecomputedW);
        subTrees.add(addHInW);
        subTrees.add(useDictionary);
        subTrees.add(checkInconsistenciesHMapping);
        subTrees.add(searchCeInTrace);
        subTrees.add(initialW);
        subTrees.add(initialH);
        subTrees.add(findPathStrategy);
        subTrees.add(transferSequencesRecord);
        subTrees.add(wRefinement);
        for (OptionTree option : subTrees)
            option.setCategoryIfUndef(OptionCategory.ALGO_HW);
    }

    public void updateWithDriver(MealyDriver d) {
        usePrecomputedW.updateWithDriver(d);
        oracleWhenUsingReset.updateWithDriver(d);
        oracleWithoutReset.updateWithDriver(d);
    }
}
