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

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

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
        List<OutputSequence> WResponses;

        public FixedCharacterization() {
            WResponses = new ArrayList<>(TotallyFixedW.this.size());
        }

        @Override
        public boolean acceptNextPrint(LmTrace print) {
            // start with heuristics checking to save time :
            if (isComplete())
                return false;
            if (TotallyFixedW.this.size() > WResponses.size()
                    && TotallyFixedW.this.get(WResponses.size())
                    .equals(print.getInputsProjection()))
                return true;
            // Otherwise, perform a complete checking.
            for (int i = 0; i < TotallyFixedW.this.size(); i++) {
                if (TotallyFixedW.this.get(i)
                        .equals(print.getInputsProjection())
                        && (WResponses.size() < i || WResponses.get(i) == null))
                    return true;
            }

            return false;
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
            // heuristic add
            if (TotallyFixedW.this.size() > WResponses.size()
                    && TotallyFixedW.this.get(WResponses.size()).equals(w)) {
                WResponses.add(wResponse);
                return;
            }
            // complete checking for adding
            for (int i = 0; i < TotallyFixedW.this.size(); i++) {
                if (TotallyFixedW.this.get(i).equals(w)
                        && (WResponses.size() <= i
                        || WResponses.get(i) == null)) {
                    for (int j = WResponses.size(); j <= i; j++)
                        WResponses.add(null);
                    WResponses.set(i, wResponse);
                    return;
                }
            }
            throw new RuntimeException("invalid print");
        }

        @Override
        public Iterable<LmTrace> knownResponses() {
            class KnownResponsesIterator
                    implements java.util.Iterator<LmTrace> {
                int pos = 0;

                @Override
                public boolean hasNext() {
                    while (pos < WResponses.size()) {
                        if (WResponses.get(pos) != null)
                            return true;
                        pos++;
                    }
                    return false;
                }

                @Override
                public LmTrace next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    assert TotallyFixedW.this.get(pos).getLength() == WResponses
                            .get(pos).getLength();
                    LmTrace trace = new LmTrace(TotallyFixedW.this.get(pos),
                            WResponses.get(pos));
                    pos++;
                    return trace;
                }
            }

            return new Iterable<LmTrace>() {
                @Override
                public Iterator<LmTrace> iterator() {
                    return new KnownResponsesIterator();
                }

            };
        }

        @Override
        public Iterable<InputSequence> unknownPrints() {
            class UnknownResponsesIterator
                    implements java.util.Iterator<InputSequence> {
                int pos = 0;

                @Override
                public boolean hasNext() {
                    while (pos < TotallyFixedW.this.size()) {
                        if (pos >= WResponses.size()
                                || WResponses.get(pos) == null)
                            return true;
                        pos++;
                    }
                    return false;

                }

                @Override
                public InputSequence next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    return TotallyFixedW.this.get(pos++);
                }

            }
            return new Iterable<InputSequence>() {

                @Override
                public Iterator<InputSequence> iterator() {
                    return new UnknownResponsesIterator();
                }

            };
        }

        @Override
        public List<InputSequence> getUnknownPrints() {
            List<InputSequence> res = new ArrayList<>();
            for (int i = 0; i < TotallyFixedW.this.size(); i++) {
                if (i >= WResponses.size() || WResponses.get(i) == null)
                    res.add(TotallyFixedW.this.get(i));
            }
            return res;
        }

        @Override
        public boolean isComplete() {
            return WResponses.size() == TotallyFixedW.this.size()
                    && !WResponses.contains(null);
        }

        @Override
        public boolean contains(LmTrace trace) {
            assert isComplete();
            for (InputSequence w : TotallyFixedW.this) {
                if (w.hasPrefix(trace))
                    return true;
            }
            return false;
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
            int i = 0;
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (InputSequence inSeq : TotallyFixedW.this) {
                if (WResponses.size() <= i || WResponses.get(i) == null) {
                    s.append(inSeq);
                    s.append("/not executed yet");
                } else {
                    s.append(inSeq.buildTrace(WResponses.get(i)));
                }
                s.append(", ");
                i++;
            }
            if (i != 0) {
                s.setLength(s.length() - 2);
            }
            s.append("]");
            return s.toString();
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
