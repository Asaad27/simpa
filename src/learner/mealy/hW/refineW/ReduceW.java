package learner.mealy.hW.refineW;

import automata.mealy.InputSequence;
import automata.mealy.distinctionStruct.TotallyFixedW;
import learner.mealy.LmConjecture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReduceW implements WSetOptimization {

    @Override
    public Collection<InputSequence> computeSmallerWSet(TotallyFixedW wSet, LmConjecture conjecture) {
        List<InputSequence> tentativeW = new ArrayList<>(wSet);
        var conjectureProxy = new ConjectureWrapper(conjecture);
        tentativeW = reduceW(conjectureProxy, tentativeW);
        return tentativeW;
    }

    public static List<InputSequence> reduceW(ConjectureWrapper conjectureProxy, List<InputSequence> tentativeW) {
        while (true) {
            var smallerW = oneSeqOut(tentativeW).filter(conjectureProxy::isWSet).findAny();
            if (smallerW.isEmpty()) break;
            tentativeW = smallerW.get();
        }

        while (true) {
            var smallerW = oneEventOut(tentativeW).filter(conjectureProxy::isWSet).findAny();
            if (smallerW.isEmpty()) break;
            tentativeW = smallerW.get();
        }
        return tentativeW;
    }

    private static Stream<List<InputSequence>> oneSeqOut(List<InputSequence> w) {
        return IntStream.range(0, w.size()).mapToObj(i -> {
            var res = new ArrayList<>(w.subList(0, i));
            res.addAll(w.subList(i + 1, w.size()));
            return res;
        });
    }

    private static Stream<List<InputSequence>> oneEventOut(List<InputSequence> w) {
        return IntStream.range(0, w.size()).mapToObj(i -> {
            var res = new ArrayList<>(w);
            var shorterSequence = new ArrayList<>(w.get(i).sequence.subList(0, w.get(i).getLength() - 1));
            res.set(i, new InputSequence(shorterSequence));
            return res;
        });
    }
}
