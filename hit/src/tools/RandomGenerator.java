package tools;

import java.util.List;
import java.util.Random;
import java.util.Set;

import options.PercentageOption;

public interface RandomGenerator {

	/**
	 * get the seed used at the last call to {@link #initRandom()}.
	 * 
	 * @return the seed which can be used to produce the same sequence of
	 *         random.
	 */
	public long getSeed();

	public Random getRand();

	public boolean randBoolWithPercent(PercentageOption percent);

	// the following method were taken from tools.Utils

	public boolean randBoolWithPercent(int p);

	public int randIntBetween(int a, int b);

	public <T> T randIn(List<T> l);

	public <T> T randIn(T l[]);

	public long randLong();

	public int randInt(int max);

	public <T> T randIn(Set<T> s);

	public String randString();

	public String randAlphaNumString(int size);

}
