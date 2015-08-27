package learner.mealy.rivestSchapire;

import java.util.StringTokenizer;

import automata.Transition;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import stats.Graph;
import stats.Graph.PlotStyle;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.RangeRestriction;

public class RivestSchapireStatsEntry extends StatsEntry {
	public static final Attribute<Integer>RESET_CALL_NB = Attribute.RESET_CALL_NB;
	public static final Attribute<Integer>HOMING_SEQUENCE_LENGTH = Attribute.HOMING_SEQUENCE_LENGTH;
	public static final Attribute<Integer>LEARNER_NB = Attribute.LEARNER_NUMBER;
	public static final Attribute<Integer>TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer>INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer>OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer>STATE_NUMBER = Attribute.STATE_NUMBER;
	public static final Attribute<Integer>LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Integer>MEMORY = Attribute.MEMORY;

	private static Attribute<?>[] attributes = new Attribute<?>[]{
		RESET_CALL_NB,
		HOMING_SEQUENCE_LENGTH,
		LEARNER_NB,
		TRACE_LENGTH,
		INPUT_SYMBOLS,
		OUTPUT_SYMBOLS,
		STATE_NUMBER,
		LOOP_RATIO,
		AUTOMATA,
		DURATION,
		MEMORY,
	};

	public static String getCSVHeader_s(){
		return makeCSVHeader(attributes);
	}

	/**
	 * a static version of {@link#getAttributes}
	 * @return the attributes of this class;
	 */
	public static Attribute<?>[] getAttributes_s() {
		return attributes;
	}	

	protected Attribute<?>[] getAttributesIntern() {
		return attributes;
	}

	private int resetCallNb = 0;
	private int hommingSequenceLength;
	private int learnerNb;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private int memory = 0;

	/**
	 * rebuild a NoResetStats object from a CSV line
	 * @param line the line to parse
	 */
	public RivestSchapireStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		resetCallNb = Integer.parseInt(st.nextToken());
		hommingSequenceLength = Integer.parseInt(st.nextToken());
		learnerNb = Integer.parseInt(st.nextToken());
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		memory = Integer.parseUnsignedInt(st.nextToken());
		resetCallNb = 0;
	}

	public RivestSchapireStatsEntry(MealyDriver d,InputSequence h){
		hommingSequenceLength = h.getLength();
		this.inputSymbols = d.getInputSymbols().size();
		this.outputSymbols = d.getOutputSymbols().size();
		this.automata = d.getSystemName();
		memory = 0;
	}

	protected void increaseresetCallNb(){
		resetCallNb ++;
	}

	protected void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	protected void setLearnerNumber(int LearnerNumber) {
		this.learnerNb = LearnerNumber;
	}

	public void updateWithConjecture(Mealy conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions=0;
		for (Transition t : conjecture.getTransitions()){
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		loopTransitionPercentage = ((100*loopTransitions)/conjecture.getTransitionCount());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T get(Attribute<T> a) {
		if (a == RESET_CALL_NB)
			return (T) new Integer(resetCallNb);
		if (a == HOMING_SEQUENCE_LENGTH)
			return (T) new Integer(hommingSequenceLength);
		if (a == LEARNER_NB)
			return (T) new Integer(learnerNb);
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
		if (a == MEMORY)
			return (T) new Integer(memory);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == RESET_CALL_NB || 
				a == HOMING_SEQUENCE_LENGTH ||
				a == LEARNER_NB ||
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == LOOP_RATIO ||
				a == MEMORY)
			return ((Integer) get(a)).floatValue();
		if (a == DURATION)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");

	}
	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new GraphGenerator(){

			@Override
			public void generate(StatsSet s) {
				
				Graph<Integer, Integer> g1 = new Graph<Integer,Integer>(INPUT_SYMBOLS, TRACE_LENGTH);
				StatsSet s1 = new StatsSet(s);
				//s1 .restrict(new EqualsRestriction<Integer>(OUTPUT_SYMBOLS, 5));
				//s1.restrict(new EqualsRestriction<Integer>(STATE_NUMBER, 12));
				g1.plot(s1, Graph.PlotStyle.POINTS);
				g1.setForceOrdLogScale(false);
				g1.setFileName("influence_of_input_symbols");
				g1.export();
				
				Graph<Integer, Integer> g2 = new Graph<Integer,Integer>(OUTPUT_SYMBOLS, TRACE_LENGTH);
				StatsSet s2 = new StatsSet(s);
				//s2 .restrict(new EqualsRestriction<Integer>(OUTPUT_SYMBOLS, 5));
				//s2.restrict(new EqualsRestriction<Integer>(STATE_NUMBER, 12));
				g2.plot(s2, Graph.PlotStyle.POINTS);
				//g2.setForceOrdLogScale(false);
				g2.setFileName("influence_of_output_symbols");
				g2.export();
				
				Graph<Integer, Float> g8 = new Graph<Integer,Float>(TRACE_LENGTH, DURATION);
				StatsSet s8 = new StatsSet(s);
				g8.plot(s8, Graph.PlotStyle.POINTS);
				g8.setFileName("similarity_between_duration_and_trace_length");
				g8.export();
				
				Graph<Integer, Integer> g_locker = new Graph<>(INPUT_SYMBOLS, TRACE_LENGTH);
				StatsSet s_locker = new StatsSet(s);
				s_locker.restrict(new RangeRestriction<Integer>(STATE_NUMBER, 0, 5));
				g_locker.plotGroup(s_locker, AUTOMATA, PlotStyle.POINTS);
				g_locker.setFileName("lockers");
				g_locker.export();
			}};
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void updateMemory(int currentMemory) {
		if (currentMemory > memory)
			memory = currentMemory;
	}

}
