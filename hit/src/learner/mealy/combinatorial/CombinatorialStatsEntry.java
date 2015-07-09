package learner.mealy.combinatorial;

import drivers.Driver;
import drivers.mealy.MealyDriver;
import stats.StatsEntry;
import stats.attribute.Attribute;

public class CombinatorialStatsEntry implements StatsEntry {
	private static final Attribute<?>[] attributes = new Attribute<?>[]{
		Attribute.TRACE_LENGTH,
		Attribute.INPUT_SYMBOLS,
		Attribute.OUTPUT_SYMBOLS
		};

	private	int traceLength;
	private int inputSymbols;
	private int outputSymbols;
	
	protected CombinatorialStatsEntry(int traceLength, MealyDriver d, Conjecture c) {
		this.traceLength = traceLength;
		this.inputSymbols = d.getInputSymbols().size();
		this.outputSymbols = d.getOutputSymbols().size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T get(Attribute<T> a) {
		if (a == Attribute.TRACE_LENGTH)
			return (T) new Integer(traceLength);
		if (a == Attribute.INPUT_SYMBOLS)
			return (T) new Integer(inputSymbols);
		if (a ==Attribute.OUTPUT_SYMBOLS)
			return (T) new Integer(outputSymbols);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");
	}

	@Override
	public Attribute<?>[] getAttributes() {
		return attributes;
	}
}
