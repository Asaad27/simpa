package automata.mealy.distinctionStruct;

import java.util.List;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmTrace;

/**
 * This class represent the characterization of a state regarding of sequences
 * recorded in a {@link DistinctionStruct}. The characterization can be either
 * complete or partial and thus, a characterization can be seen as an iterator
 * on the sequences to apply from a given {@link DistinctionStruct}.
 * 
 * Please keep in mind that for some implementation, the characterizations can
 * be broken when the distinction structure is refined.
 * 
 * @author Nicolas BREMOND
 *
 * @param <InputSeq>
 *            the type of input sequences
 * @param <OutputSeq>
 *            the type of responses to input sequences.
 */
public interface Characterization<InputSeq extends GenericInputSequence, OutputSeq extends GenericOutputSequence> {
	/**
	 * Indicate whether this print is accepted by one sequence in
	 * {@link #getUnknownPrints()}
	 * 
	 * @param print
	 *            the print to test
	 * @return true if one sequence in {@link #getUnknownPrints()} accept this
	 *         print.
	 */
	public boolean acceptNextPrint(LmTrace print);

	/**
	 * add a print to improve the characterization.
	 * 
	 * @param print
	 *            the trace observed after applying one sequence from
	 *            {@link #getUnknownPrints()}.
	 * @see #addPrint(GenericInputSequence, GenericOutputSequence)
	 *      addPrint(GenericInputSequence, GenericOutputSequence) which can be
	 *      more efficient for some implementations
	 */
	public void addPrint(LmTrace print);

	/**
	 * add a print to improve the characterization.
	 * 
	 * @param w
	 *            the input sequence used to improve characterization.
	 * @param wResponse
	 *            the answer observed after {@code w}
	 */
	public void addPrint(GenericInputSequence w,
			GenericOutputSequence wResponse);

	/**
	 * get an object to iterate over the responses recorded in this
	 * characterization
	 * 
	 * @return an Iterable object
	 */
	public Iterable<LmTrace> knownResponses();

	/**
	 * get an object to iterate over the sequences needed to complete this
	 * characterization.
	 * 
	 * @return an Itarable object
	 */
	public Iterable<InputSeq> unknownPrints();

	/**
	 * get the list of sequences needed to complete the characterization.
	 * 
	 * @return the current list of sequences needed to complete the
	 *         characterization.
	 */
	public List<InputSeq> getUnknownPrints();

	/**
	 * Indicate whether the characterization is complete or not.
	 * 
	 * @return true if there is no more input sequence to apply
	 */
	public boolean isComplete();

	/**
	 * Indicate if this characterization will be split by the given print. This
	 * method should only be called on complete characterizations.
	 * 
	 * @param trace
	 *            a trace which might distinguish two states which have this
	 *            characterization.
	 * @return true if the given trace belongs to (or is prefix of ) the
	 *         recorded prints.
	 */
	public boolean contains(LmTrace trace);

}