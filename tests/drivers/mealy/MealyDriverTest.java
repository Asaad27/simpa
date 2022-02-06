/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 *     Roland GROZ
 *     Lingxiao WANG
 ********************************************************************************/
package drivers.mealy;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.GenericInputSequence.Iterator;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.MultiTrace;
import automata.mealy.multiTrace.NoRecordMultiTrace;
import drivers.DriverTest;
import drivers.mealy.transparent.TransparentMealyDriverTest;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import options.learnerOptions.OracleOption;
import stats.StatsEntry_OraclePart;
import tools.RandomGenerator;
import tools.StandaloneRandom;
import tools.loggers.LogManager;

public abstract class MealyDriverTest{
	
}
