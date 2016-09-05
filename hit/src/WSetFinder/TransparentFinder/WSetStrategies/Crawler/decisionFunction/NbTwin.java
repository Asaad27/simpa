package WSetFinder.TransparentFinder.WSetStrategies.Crawler.decisionFunction;

import WSetFinder.TransparentFinder.WSetStrategies.Crawler.StatePair;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.States;
import automata.State;

import java.util.ArrayList;
import java.util.List;

/**
 * priority:
 *  nb twin  > nb Singleton > nb Cluster> wordLength
 *  assert before that the number of cluster is growing
 */
public class NbTwin extends ValueFunction {
    public static int nbCall = 0;
    public int countTwin(List<List<StatePair>> statePairs){
        int nbTwin = 0;
        for(List<StatePair> statePairList : statePairs){
            List<State> occupiedStates = new ArrayList<>();
            for(StatePair sp : statePairList){
                if (occupiedStates.contains(sp.getLast())){
                    nbTwin++;
                }else{
                    occupiedStates.add(sp.getLast());
                }
            }
        }
        return nbTwin;
    }
    @Override
    public boolean sup(States s1, States s2,States current) {
        nbCall++;
        if(s2 == null)
            return true;
        if(s1 == null)
            return false;
        int nbTwin1 = countTwin(s1.getStatePairs()) + countTwin(s1.getStuckPairs());
        int nbTwin2 = countTwin(s2.getStatePairs()) + countTwin(s2.getStuckPairs());
        int nbCluster = current.getNbSingleton() + current.getStatePairs().size() + current.getStuckPairs().size();
        int nbCluster1 = s1.getNbSingleton() + s1.getStatePairs().size() + s1.getStuckPairs().size();
        int nbCluster2 = s2.getNbSingleton() + s2.getStatePairs().size() + s2.getStuckPairs().size();
        //an important thing is the number of cluster MUST be increassing
        if(nbCluster == nbCluster1){
            return false;
        }
        if(nbCluster == nbCluster2){
            return true;
        }
        //true progress need dedication
        if(nbTwin1 == nbTwin2) {
            if (s2.getNbSingleton() == s1.getNbSingleton()){
                if(nbCluster1 == nbCluster2){
                    return s1.getWordLength() <= s2.getWordLength();
                }
                else return nbCluster1 > nbCluster2;
            }
            else
                return s1.getNbSingleton() > s2.getNbSingleton();
        } else
            return nbTwin1 < nbTwin2;
    }
}
