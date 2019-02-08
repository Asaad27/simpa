package tools;

import java.util.Random;

import main.simpa.Options;
import options.valueHolders.SeedHolder;

/**
 * This class is a temporary class for random utilities.
 * 
 * Anywhere this class is used, it should be replaced by a
 * {@link options.RandomOption} integrated into a main
 * {@link options.OptionTree} in order to let user choose the seed.
 * 
 * @author Nicolas BREMOND
 *
 */
public class StandaloneRandom extends SeedHolder {
	static Random seedProvider = null;

	static void setSeed(long s) {
		new StandaloneRandom();
		seedProvider.setSeed(s);
	}

	public StandaloneRandom() {
		super("");
		if (seedProvider == null) {
			seedProvider = new Random();
			seedProvider.setSeed(Options.SEED);
		}
		setValue(seedProvider.nextLong());
		initRandom();
	}
}
