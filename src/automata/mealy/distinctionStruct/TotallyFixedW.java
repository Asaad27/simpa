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
package automata.mealy.distinctionStruct;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

import static java.util.function.Predicate.not;

public class TotallyFixedW extends ArrayList<InputSequence>
        implements DistinctionStruct<InputSequence, OutputSequence> {
    private static final long serialVersionUID = 38357699697464527L;

    public static TotallyFixedW deserialize(String input) {
        TotallyFixedW W = new TotallyFixedW();
        Scanner s = new Scanner(input);
        s.useDelimiter("[,\\s]+");
        s.skip("[\\[\\s]*"); //skip leading white space and "["
        while (s.hasNext()) {
            W.add(InputSequence.deserialize(s.next()));
        }
        return W;
    }

    private class FixedCharacterization
            implements Characterization<InputSequence, OutputSequence> {
        Map<InputSequence, OutputSequence> WResponses;

        public FixedCharacterization() {
            WResponses = new HashMap<>();
        }

        @Override
        public boolean acceptNextPrint(LmTrace print) {
            return !WResponses.containsKey(print.getInputsProjection());
        }

        @Override
        public void addPrint(LmTrace print) {
            addPrint(print.getInputsProjection(), print.getOutputsProjection());
        }

        @Override
        public void addPrint(GenericInputSequence wGeneric,
                             GenericOutputSequence wResponseGeneric) {
            addPrint((InputSequence) wGeneric,
                    (OutputSequence) wResponseGeneric);
        }

        public void addPrint(InputSequence w, OutputSequence wResponse) {
            assert w.hasAnswer(new LmTrace(w, wResponse));
            if (!TotallyFixedW.this.contains(w)) {
                throw new RuntimeException("Invalid print: The input sequence " + w + " is not in W.");
            }
            WResponses.put(w, wResponse);
        }

        @Override
        public Iterable<LmTrace> knownResponses() {
            return TotallyFixedW.this.stream()
                    .filter(WResponses::containsKey)
                    .map(w -> new LmTrace(w, WResponses.get(w)))
                    .collect(Collectors.toList());
        }

        @Override
        public Iterable<InputSequence> unknownPrints() {
            return getUnknownPrints();
        }

        @Override
        public List<InputSequence> getUnknownPrints() {
            return TotallyFixedW.this.stream()
                    .filter(not(WResponses::containsKey))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isComplete() {
            return TotallyFixedW.this.stream().allMatch(WResponses::containsKey);
        }

        @Override
        public boolean contains(LmTrace trace) {
            assert isComplete();
            return WResponses.keySet().stream().anyMatch(w -> w.hasPrefix(trace));
        }

        @Override
        public boolean equals(Object o) {
            assert o instanceof FixedCharacterization;
            return equals((FixedCharacterization) o);
        }

        public boolean equals(FixedCharacterization o) {
            return WResponses.equals(o.WResponses);
        }

        @Override
        public int hashCode() {
            return WResponses.hashCode();
        }

        @Override
        public String toString() {
            return "[" +
                    TotallyFixedW.this.stream().map(inSeq -> {
                        if (WResponses.containsKey(inSeq)) {
                            return inSeq.buildTrace(WResponses.get(inSeq)).toString();
                        } else {
                            return inSeq.toString() + "/not executed yet";
                        }
                    }).collect(Collectors.joining(", "))
                    + "]";
        }
    }

    public TotallyFixedW() {
    }

    public TotallyFixedW(List<InputSequence> sequences) {
        super(sequences);
    }

    @Override
    public FixedCharacterization getEmptyCharacterization() {
        return new FixedCharacterization();
    }

    @Override
    public void refine(
            Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization,
            LmTrace newSeq) {
        assert characterization.isComplete()
                && !characterization.contains(newSeq);
        boolean extended = false;
        for (InputSequence w : this) {
            assert !w.hasPrefix(newSeq);
            if (newSeq.startsWith(w) || w.getLength() == 0) {
                set(indexOf(w), newSeq.getInputsProjection());
                extended = true;
                LogManager.logInfo("removing " + w
                        + " from W-set because it's a prefix of new inputSequence");
                break;// because W is only extended with this method, there is
                // at most one prefix of newSeq in W (otherwise, one
                // prefix of newSeq in W is also a prefix of the other
                // prefix of newSeq in W, which is not possible by
                // construction of W)
            }
        }
        if (!extended)
            add(newSeq.getInputsProjection());
    }

    @Override
    public StringBuilder toString(StringBuilder s) {
        if (isEmpty()) {
            s.append("empty set");
            return s;
        }
        s.append("[");
        for (InputSequence seq : this) {
            s.append(seq);
            s.append(", ");
        }
        s.setLength(s.length() - 2);
        s.append("]");
        return s;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    @Override
    public TotallyFixedW clone() {
        TotallyFixedW clone = (TotallyFixedW) super.clone();
        return clone;
    }
}
