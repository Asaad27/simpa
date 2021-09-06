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
 ********************************************************************************/
package tools.loggers;

import automata.State;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;
import automata.mealy.GenericInputSequence;
import automata.mealy.distinctionStruct.DistinctionStruct;
import learner.efsm.table.LiControlTable;
import learner.efsm.table.LiDataTable;
import learner.efsm.tree.ZXObservationNode;
import learner.mealy.LmConjecture;
import learner.mealy.table.LmControlTable;
import learner.mealy.tree.ZObservationNode;
import options.OptionsGroup;

import java.util.List;
import java.util.Map;

public interface ILogger {
    default void logControlTable(LiControlTable ct) {
    }

    default void logControlTable(LmControlTable ct) {

    }

    default void logDataTable(LiDataTable dt) {

    }

    default void logEnd() {

    }

    default void logReset() {

    }

    default void logError(String s) {

    }

    default void logException(String s, Exception e) {

    }

    default void logFatalError(String s) {

    }

    default void logInfo(String s) {

    }

    default void logWarning(String s) {

    }

    default void logRequest(ParameterizedInput pi, ParameterizedOutput po) {

    }

    default void logRequest(String input, String ouput, int n) {

    }

    default void logRequest(String input, String output, int n, State before,
                            State after) {

    }

    default void logStart() {

    }

    default void logStat(String s) {

    }

    default void logStep(int step, Object o) {

    }

    default void logData(String data) {

    }

    default void logTransition(String trans) {

    }

    default void logLine() {

    }

    default void logImage(String path) {

    }

    default void logConcrete(String data) {

    }

    default void logParameters(Map<String, Integer> params) {

    }

    default void logObservationTree(ZObservationNode root) {

    }

    default void logXObservationTree(ZXObservationNode root) {

    }

    default void logConjecture(LmConjecture conjecture) {
    }

    default void logH(GenericInputSequence h) {
    }

    default void logW(DistinctionStruct<? extends GenericInputSequence, ?
            extends GenericInputSequence.GenericOutputSequence> w) {
    }

    default void startNewSubInference() {
    }

    default void startNewInference() {
    }

    default void logCLIOptions(OptionsGroup allOptions) {
    }

    default void logUndefinedRequest(String input, int n, State s) {
    }

    default void inputAlphabetChanged(List<String> inputAlphabet) {
        logInfo("New input alphabet: " + inputAlphabet);
    }
}
