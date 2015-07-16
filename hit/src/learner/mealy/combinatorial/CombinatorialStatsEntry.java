package learner.mealy.combinatorial;

import stats.GraphGenerator;
import stats.StatsEntry;
import stats.attribute.Attribute;
import drivers.mealy.MealyDriver;

public class CombinatorialStatsEntry extends StatsEntry {
	public final static Attribute<Integer> TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public final static Attribute<Integer> INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public final static Attribute<Integer> OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public final static Attribute<Float> DURATION = Attribute.DURATION;
	
	private static final Attribute<?>[] attributes = new Attribute<?>[]{
		TRACE_LENGTH,
		INPUT_SYMBOLS,
		OUTPUT_SYMBOLS,
		DURATION,
		};

	private	int traceLength;
	private int inputSymbols;
	private int outputSymbols;
	private float duration;
	
	protected CombinatorialStatsEntry(int traceLength, MealyDriver d, Conjecture c) {
		this.traceLength = traceLength;
		this.inputSymbols = d.getInputSymbols().size();
		this.outputSymbols = d.getOutputSymbols().size();
	}
	
	public void setDuration(float d){
		duration = d;
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
		if (a == DURATION)
			return (T) new Float(duration);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");
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
		return null;
	}
}
