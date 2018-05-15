package automata.mealy.distinctionStruct;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmTrace;

/**
 * This interface represent either a fixed or an adaptive set of sequences used
 * to distinguish states (a W-set).
 * 
 * The sequences in the set can be fixed or adaptive.
 * 
 * @author Nicolas BREMOND
 *
 * @param <InputSeq>
 *            the type of inputSequences to use
 * @param <OutputSeq>
 *            the type of answers expected from InputSeq
 */
public interface DistinctionStruct<InputSeq extends GenericInputSequence, OutputSeq extends GenericOutputSequence> {

	/**
	 * Indicate whether the distinction structure is empty or not.
	 * 
	 * @return true if there is no sequence to apply to characterize a state.
	 */
	public boolean isEmpty();

	/**
	 * Get a new empty {@link Characterization} for this distinction structure.
	 * 
	 * @return a characterization with no response known
	 */
	public Characterization<InputSeq, OutputSeq> getEmptyCharacterization();

	/**
	 * refine this structure by adding a new sequence at a given position.
	 * Notice that this may break several characterizations and their created
	 * Iterators (especially with adaptive because refining may cause huge
	 * changes in the tree).
	 * 
	 * @param characterization
	 *            the position which should be refined (this characterization
	 *            will not be complete after refining)
	 * @param newSeq
	 *            the sequence which should be in characterization at the end of
	 *            this method. This sequence should not already be in
	 *            characterization
	 */
	public void refine(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization,
			LmTrace newSeq);

	/**
	 * Append a String representation of this structure to a String builder.
	 * 
	 * @param s
	 *            the StringBuilder which should be extended.
	 * @return {@code s}, the StringBuilder given in parameter.
	 */
	public StringBuilder toString(StringBuilder s);

	@Override
	public String toString();

	public DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> clone();
}
