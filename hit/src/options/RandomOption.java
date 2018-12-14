package options;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import tools.loggers.LogManager;

public class RandomOption extends LongOption {
	/**
	 * This seed is used to create seeds of each {@link RandomOption}. It might
	 * be used for deep debugging.
	 */
	static public final long MAIN_SEED;
	/**
	 * The {@link Random} source of seed for each {@link RandomOption}. It is
	 * initialize with {@link #MAIN_SEED}.
	 */
	static private final Random seedGenerator;
	static {
		MAIN_SEED = new Random().nextLong();
		seedGenerator = new Random();
		seedGenerator.setSeed(MAIN_SEED);
	}

	Random rand = null;

	/**
	 * initialize the Random. This must be called before starting to use the
	 * Random in order to set and record the seed.
	 */
	public void init() {
		if (useAutoValue())
			setValue(seedGenerator.nextLong());
		rand = new Random();
		rand.setSeed(getSeed());
		LogManager
				.logInfo("Seed for " + argument.name + " set to " + getSeed());
	}

	/**
	 * get the seed used at the last call to {@link #init()}.
	 * 
	 * @return the seed which can be used to produce the same sequence of
	 *         random.
	 */
	public long getSeed() {
		return getValue();
	}

	public Random getRand() {
		assert rand != null : "rand must be initialized with a call to init()";
		return rand;
	}

	public RandomOption(String argument, String description) {
		super(argument, description, "use a random seed");
		setMinimum(Long.MIN_VALUE);
		setMaximum(Long.MAX_VALUE);
	}

	// the following method were taken from tools.Utils

	public boolean randBoolWithPercent(int p) {
		return rand.nextInt(100) < p;
	}

	public int randIntBetween(int a, int b) {
		if (a == b)
			return a;
		else if (a > b) {
			a -= b;
			b += a;
			a = b - a;
		}
		return rand.nextInt(b - a + 1) + a;
	}

	public <T> T randIn(List<T> l) {
		if (l.isEmpty())
			return null;
		else
			return l.get(rand.nextInt(l.size()));
	}

	public <T> T randIn(T l[]) {
		if (l.length == 0)
			return null;
		else
			return l[rand.nextInt(l.length)];
	}

	public long randLong() {
		return rand.nextLong();
	}

	public int randInt(int max) {
		return rand.nextInt(max);
	}

	public <T> T randIn(Set<T> s) {
		List<T> l = new ArrayList<>(s);
		return randIn(l);
	}

	public String randString() {
		return "random" + randInt(1000);
	}

	public String randAlphaNumString(int size) {
		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder();
		for (int i = 0; i < size; i++) {
			int index = rand.nextInt(charset.length());
			randomString.append(charset.charAt(index));
		}
		;
		return randomString.toString();
	}
}
