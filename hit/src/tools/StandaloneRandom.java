package tools;

import java.util.Random;

import main.simpa.Options;
import options.RandomOption;

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
public class StandaloneRandom extends RandomOption {
	static Random seedProvider = null;

	public StandaloneRandom() {
		super("--standaloneRand", "standalone random");
		if (seedProvider == null) {
			seedProvider = new Random();
			seedProvider.setSeed(Options.SEED);
		}
		setValue(seedProvider.nextLong());
		init();
	}
}
