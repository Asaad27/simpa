package learner.mealy.combinatorial;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsEntry_OraclePart;
import stats.attribute.Attribute;

public class CombinatorialStatsEntry extends StatsEntry {
	public final static Attribute<Integer> TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public final static Attribute<Integer> INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public final static Attribute<Integer> OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public final static Attribute<Integer> STATE_NUMBER = Attribute.STATE_NUMBER;
	public final static Attribute<Float> DURATION = Attribute.DURATION;
	public final static Attribute<Integer> NODES_NB = Attribute.NODES_NB;
	public final static Attribute<String>  AUTOMATA = Attribute.AUTOMATA;
	public final static Attribute<Integer> ASKED_COUNTER_EXAMPLE = Attribute.ASKED_COUNTER_EXAMPLE;
	public final static Attribute<String> ORACLE_USED = Attribute.ORACLE_USED;
	public final static Attribute<Float> ORACLE_DURATION = Attribute.ORACLE_DURATION;
	public final static Attribute<Integer> ORACLE_TRACE_LENGTH = Attribute.ORACLE_TRACE_LENGTH;

	private static final Attribute<?>[] attributes = new Attribute<?>[]{
		TRACE_LENGTH,
		INPUT_SYMBOLS,
		OUTPUT_SYMBOLS,
		STATE_NUMBER,
		DURATION,
		NODES_NB,
		AUTOMATA,
		ASKED_COUNTER_EXAMPLE,
		ORACLE_USED,
		ORACLE_DURATION,
		ORACLE_TRACE_LENGTH,
	};

	protected int traceLength;
	private int inputSymbols;
	private int outputSymbols;
	private int state_number;
	private float duration;
	private int nodesNB;
	private String automata;
	final StatsEntry_OraclePart oracle;

	public static String getCSVHeader_s(){
		return makeCSVHeader(attributes);
	}

	protected CombinatorialStatsEntry(MealyDriver d,
			CombinatorialOptions options) {
		this.inputSymbols = d.getInputSymbols().size();
		automata = d.getSystemName();
		nodesNB = 0;
		oracle = new StatsEntry_OraclePart(options.getOracleOption());
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
		automata = st.nextToken();

		st = new StringTokenizer(line, ",");
		oracle = new StatsEntry_OraclePart(st, getAttributes());
	}

	public void setDuration(float d){
		duration = d;
	}

	public void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	public void updateWithConjecture(Conjecture c){
		state_number=c.getStates().size();
		Set<String> outputSymbols = new HashSet<>();
		for (MealyTransition t : c.getTransitions()) {
			outputSymbols.add(t.getOutput());
		}
		this.outputSymbols = outputSymbols.size();
	}

	public void addNode(){
		nodesNB++;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == TRACE_LENGTH)
			return (T) Integer.valueOf(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) Integer.valueOf(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) Integer.valueOf(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) Integer.valueOf(state_number);
		if (a == DURATION)
			return (T) Float.valueOf(duration);
		if (a == NODES_NB)
			return (T) Integer.valueOf(nodesNB);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == ASKED_COUNTER_EXAMPLE)
			return (T) Integer.valueOf(oracle.getAskedCE());
		if (a == ORACLE_USED)
			return (T) oracle.getName();
		if (a == ORACLE_DURATION)
			return (T) Float.valueOf(oracle.getDuration());
		if (a == ORACLE_TRACE_LENGTH)
			return (T) Integer.valueOf(oracle.getTraceLength());
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");
	}

	@Override
	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (
				a == ASKED_COUNTER_EXAMPLE ||
				a == ORACLE_TRACE_LENGTH ||
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == NODES_NB)
			return ((Integer) get(a)).floatValue();
		if (
				a == DURATION ||
				a == ORACLE_DURATION)
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
