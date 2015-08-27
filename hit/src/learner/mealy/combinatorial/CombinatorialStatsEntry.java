package learner.mealy.combinatorial;

import java.util.StringTokenizer;

import drivers.mealy.MealyDriver;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.attribute.Attribute;

public class CombinatorialStatsEntry extends StatsEntry {
	public final static Attribute<Integer> TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public final static Attribute<Integer> INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public final static Attribute<Integer> OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public final static Attribute<Integer> STATE_NUMBER = Attribute.STATE_NUMBER;
	public final static Attribute<Float> DURATION = Attribute.DURATION;
	public final static Attribute<Integer> NODES_NB = Attribute.NODES_NB;

	private static final Attribute<?>[] attributes = new Attribute<?>[]{
		TRACE_LENGTH,
		INPUT_SYMBOLS,
		OUTPUT_SYMBOLS,
		STATE_NUMBER,
		DURATION,
		NODES_NB,
	};

	private	int traceLength;
	private int inputSymbols;
	private int outputSymbols;
	private int state_number;
	private float duration;
	private int nodesNB;

	public static String getCSVHeader_s(){
		return makeCSVHeader(attributes);
	}

	protected CombinatorialStatsEntry(MealyDriver d) {
		this.inputSymbols = d.getInputSymbols().size();
		this.outputSymbols = d.getOutputSymbols().size();
		nodesNB = 0;
	}

	/**
	 * rebuild a CombinatorialStatEntry object from a CSV line
	 * @param line the line to parse
	 */
	public CombinatorialStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		state_number = Integer.parseInt(st.nextToken());
		duration = Float.parseFloat(st.nextToken());
		nodesNB = Integer.parseInt(st.nextToken());
	}

	public void setDuration(float d){
		duration = d;
	}

	public void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	public void updateWithConjecture(Conjecture c){
		state_number=c.getStates().size();
	}

	public void addNode(){
		nodesNB++;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T get(Attribute<T> a) {
		if (a == TRACE_LENGTH)
			return (T) new Integer(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) new Integer(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) new Integer(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) new Integer(state_number);
		if (a == DURATION)
			return (T) new Float(duration);
		if (a == NODES_NB)
			return (T) new Integer(nodesNB);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");
	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == NODES_NB)
			return ((Integer) get(a)).floatValue();
		if (a == DURATION)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");
	}

	public static Attribute<?>[] getAttributes_s() {
		return attributes;
	}

	@Override
	protected Attribute<?>[] getAttributesIntern() {
		return attributes;
	}

	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new CombinatorialGraphGenerator();
	}

}
