package drivers;

/**
 * An interface for driver enumerating all possibles automata matching given
 * requirements.
 * 
 * @author Nicolas BREMOND
 *
 */
public interface EnumeratedDriver {
	/**
	 * Get the seed used to build the automata.
	 * 
	 * @return the seed used to build the automata.
	 */
	long getSeed();
}
