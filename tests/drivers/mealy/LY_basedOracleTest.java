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
package drivers.mealy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.Automata;
import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.GenericInputSequence.Iterator;
import automata.mealy.distinctionStruct.TotallyAdaptiveW;
import automata.mealy.distinctionStruct.TotallyAdaptiveW.AdaptiveCharacterization;
import automata.mealy.multiTrace.MultiTrace;
import automata.mealy.splittingTree.LY_SplittingTree;
import automata.mealy.splittingTree.smetsersSplittingTree.SplittingTree;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

/**
 * This class aims to check a conjecture with a pseudo-checking sequence. The
 * pseudo-checking sequence is made by applying a {@link LY_SplittingTree
 * distinction tree} after each transition of the conjecture.
 * 
 * @author Nicolas BREMOND
 *
 */
public class LY_basedOracleTest {
}
