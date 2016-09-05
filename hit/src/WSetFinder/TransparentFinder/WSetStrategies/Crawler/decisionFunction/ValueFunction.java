package WSetFinder.TransparentFinder.WSetStrategies.Crawler.decisionFunction;


import WSetFinder.TransparentFinder.WSetStrategies.Crawler.States;

/**
 * used to determine from two state evolution which one is the most interesting
 */
public abstract class ValueFunction {
    /**
     * @param s1
     * @param s2
     * @return s1 > s2
     */
    public abstract boolean sup(States s1, States s2,States current);
}