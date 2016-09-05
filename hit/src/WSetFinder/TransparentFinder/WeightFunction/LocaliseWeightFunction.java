package WSetFinder.TransparentFinder.WeightFunction;

import automata.mealy.InputSequence;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jean Bouvattier on 04/07/16.
 * compute the weight if using ICCTSNIUKHN2015 algorithm localizer ( exponential in W-set size)
 */
public class LocaliseWeightFunction extends WeightFunction {

    private int nbState;

    public void setNbState(int nbState) {
        this.nbState = nbState;
    }

    @Override
    public int weight(List<InputSequence> wSet) {
        List<InputSequence> sortedWSet = new LinkedList<>();
        sortedWSet.addAll(wSet);
        Collections.sort(sortedWSet, (s, t1) -> s.getLength() - t1.getLength());
        List<Integer> sequenceLength = sortedWSet.stream().map(InputSequence::getLength).collect(Collectors.toList());
        return recursiveWeigh(sequenceLength);
    }

    private int recursiveWeigh(List<Integer> wSetSizes){
        switch (wSetSizes.size()){
            case 0:
                return 0;
            case 1:
                return wSetSizes.get(0);
            default:
                List<Integer> z1 = new LinkedList<>();
                List<Integer> z2 = new LinkedList<>();
                for(int i = 0; i < wSetSizes.size(); i++){
                    if(i != wSetSizes.size() - 1){
                        z1.add(wSetSizes.get(i));
                    }
                    if(i != wSetSizes.size() -  2){
                        z2.add(wSetSizes.get(i));
                    }
                }
                return recursiveWeigh(z1) * (2 * nbState - 1) + recursiveWeigh(z2);
        }
    }

    public static void main(String[] args){
        Integer[][] samplesTest ={
                {1},{10},{100},
                {1,1},{2,2},{1,10},{10,10},
                {1,1,1},{2,2,2},{1,10,10},
                {1,1,1,1},{2,2,2,2},{10,10,10,10},
                {1,1,1,1,1}
        };
        LocaliseWeightFunction function = new LocaliseWeightFunction();
        function.setNbState(20);
        for( Integer[] array : samplesTest){
            List<Integer> sample = Arrays.asList(array);
            int weight = function.recursiveWeigh(sample);
            System.out.println(sample + " -> " + weight);
        }
    }
}
