package learner.mealy.tree;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;

public class ZStatsEntry extends StatsEntry {
	public static final Attribute<Integer> TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer> INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer> OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer> STATE_NUMBER = Attribute.STATE_NUMBER;
	public static final Attribute<Integer> LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Long> SEED = Attribute.SEED;
	public final static Attribute<Integer> ASKED_COUNTER_EXAMPLE = Attribute.ASKED_COUNTER_EXAMPLE;
	public final static Attribute<String> ORACLE_USED = Attribute.ORACLE_USED;
	public final static Attribute<Integer> ORACLE_TRACE_LENGTH = Attribute.ORACLE_TRACE_LENGTH;
	public final static Attribute<Float> ORACLE_DURATION = Attribute.ORACLE_DURATION;
	public final static Attribute<Integer> RESET_NB = Attribute.RESET_CALL_NB;
	public final static Attribute<Integer> ORACLE_RESET_NB = Attribute.ORACLE_RESET_NB;

	private static Attribute<?>[] attributes = new Attribute<?>[]{
			TRACE_LENGTH,
			INPUT_SYMBOLS,
			OUTPUT_SYMBOLS,
			STATE_NUMBER,
			LOOP_RATIO,
			AUTOMATA,
			DURATION,
			SEED,
			ASKED_COUNTER_EXAMPLE,
			ORACLE_USED,
			ORACLE_TRACE_LENGTH,
			ORACLE_DURATION,
			RESET_NB,
			ORACLE_RESET_NB,
	};
	
	public static String getCSVHeader_s() {
		return makeCSVHeader(attributes);
	}

	/**
	 * a static version of {@link#getAttributes}
	 * 
	 * @return the attributes of this class;
	 */
	public static Attribute<?>[] getAttributes_s() {
		return attributes;
	}

	protected Attribute<?>[] getAttributesIntern() {
		return attributes;
	}

	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private long seed;
	private int askedCE = 0;
	private String oracleUsed = "Unknown";
	private int oracleTraceLength = 0;
	private float oracleDuration = 0;
	private int resetNb;
	private int oracleResetNb = 0;

	/**
	 * rebuild a StatsEntry object from a CSV line
	 * 
	 * @param line
	 *            the line to parse
	 */
	public ZStatsEntry(String line) {
		StringTokenizer st = new StringTokenizer(line, ",");
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		seed = Long.parseLong(st.nextToken());
		askedCE = Integer.parseInt(st.nextToken());
		oracleUsed = st.nextToken();
		oracleTraceLength = Integer.parseInt(st.nextToken());
		oracleDuration = Float.parseFloat(st.nextToken());
		resetNb = Integer.parseInt(st.nextToken());
		oracleResetNb = Integer.parseInt(st.nextToken());

	}

	public ZStatsEntry(MealyDriver d) {
		this.inputSymbols = d.getInputSymbols().size();
		this.automata = d.getSystemName();
		this.seed = Options.SEED;
		oracleUsed = (Options.USE_DT_CE ? "distinctionTree + " : "") + "MrBean";

	}

	public void increaseOracleCallNb(int traceLength, int resetNb,
			float duration) {
		askedCE++;
		oracleTraceLength += traceLength;
		oracleDuration += duration;
		oracleResetNb += resetNb;
	}

	public void finalUpdate(LmConjecture conjecture, float duration,
			int traceLength, int resets) {
		updateWithConjecture(conjecture);
		this.duration = duration;
		this.traceLength = traceLength;
		this.resetNb = resets;
	}

	public void updateWithConjecture(LmConjecture conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions = 0;
		Set<String> outputSymbols = new HashSet<>();
		for (MealyTransition t : conjecture.getTransitions()) {
			outputSymbols.add(t.getOutput());
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		loopTransitionPercentage = ((100 * loopTransitions)
				/ conjecture.getTransitionCount());
		this.outputSymbols = outputSymbols.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == TRACE_LENGTH)
			return (T) new Integer(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) new Integer(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) new Integer(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) new Integer(statesNumber);
		if (a == LOOP_RATIO)
			return (T) new Integer(loopTransitionPercentage);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == DURATION)
			return (T) new Float(duration);
		if (a == SEED)
			return (T) new Long(seed);
		if (a == ASKED_COUNTER_EXAMPLE)
			return (T) new Integer(askedCE);
		if (a == ORACLE_USED)
			return (T) oracleUsed;
		if (a == ORACLE_TRACE_LENGTH)
			return (T) new Integer(oracleTraceLength);
		if (a == ORACLE_DURATION)
			return (T) new Float(oracleDuration);
		if (a == RESET_NB)
			return (T) new Integer(resetNb);
		if (a == ORACLE_RESET_NB)
			return (T) new Integer(oracleResetNb);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == LOOP_RATIO ||
				a == ASKED_COUNTER_EXAMPLE||
				a == RESET_NB ||
				a == ORACLE_RESET_NB ||
				a == ORACLE_TRACE_LENGTH)
			return ((Integer) get(a)).floatValue();
		if (a == SEED)
			return ((Long) get(a)).floatValue();
		if (a == DURATION || a == ORACLE_DURATION)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");

	}

	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new GraphGenerator() {
			@Override
			public void generate(StatsSet s) {
			}
		};
	}
}
