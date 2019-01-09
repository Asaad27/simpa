package learner.mealy.rivestSchapire;

import java.util.StringTokenizer;

import automata.Transition;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import stats.Graph;
import stats.GraphGenerator;
import stats.LineStyle;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;

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
	public static final Attribute<Boolean> H_IS_GIVEN = Attribute.RS_WITH_GIVEN_H;
	public static final Attribute<Integer> FAILED_PROBABILISTIC_SEARCH = Attribute.FAILED_PROBALISTIC_SEARCH;
	public static final Attribute<Integer> SUCCEEDED_PROBALISTIC_SEARCH = Attribute.SUCCEEDED_PROBALISTIC_SEARCH;
	public final static Attribute<String> ORACLE_USED = Attribute.ORACLE_USED;
	public static final Attribute<Integer> ASKED_CE = Attribute.ASKED_COUNTER_EXAMPLE;

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
		H_IS_GIVEN,
		FAILED_PROBABILISTIC_SEARCH,
		SUCCEEDED_PROBALISTIC_SEARCH,
		ORACLE_USED,
		ASKED_CE,
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
	private boolean hIsGiven = false;
	private int failedProbalisticSearch = 0;
	private int succeededProbabilisticSearch = 0;
	private String oracleUsed;
	private int askedCE = 0;

	/**
	 * rebuild a HWStats object from a CSV line
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
		hIsGiven = Boolean.parseBoolean(st.nextToken());
		failedProbalisticSearch = Integer.parseUnsignedInt(st.nextToken());
		succeededProbabilisticSearch = Integer.parseUnsignedInt(st.nextToken());
		oracleUsed = st.nextToken();
		askedCE = Integer.parseUnsignedInt(st.nextToken());
	}

	public RivestSchapireStatsEntry(MealyDriver d, boolean hIsGiven,
			RivestSchapireOptions options) {
		this.hIsGiven = hIsGiven;
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

	protected void updateWithHomingSequence(InputSequence h) {
		hommingSequenceLength = h.getLength();
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
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == RESET_CALL_NB)
			return (T) Integer.valueOf(resetCallNb);
		if (a == HOMING_SEQUENCE_LENGTH)
			return (T) Integer.valueOf(hommingSequenceLength);
		if (a == LEARNER_NB)
			return (T) Integer.valueOf(learnerNb);
		if (a == TRACE_LENGTH)
			return (T) Integer.valueOf(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) Integer.valueOf(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) Integer.valueOf(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) Integer.valueOf(statesNumber);
		if (a == LOOP_RATIO)
			return (T) Integer.valueOf(loopTransitionPercentage);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == DURATION)
			return (T) Float.valueOf(duration);
		if (a == MEMORY)
			return (T) Integer.valueOf(memory);
		if (a == H_IS_GIVEN)
			return (T) Boolean.valueOf(hIsGiven);
		if (a == FAILED_PROBABILISTIC_SEARCH)
			return (T) Integer.valueOf(failedProbalisticSearch);
		if (a == SUCCEEDED_PROBALISTIC_SEARCH)
			return (T) Integer.valueOf(succeededProbabilisticSearch);
		if (a == ORACLE_USED)
			return (T) oracleUsed;
		if (a == ASKED_CE)
			return (T) Integer.valueOf(askedCE);
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
				a == FAILED_PROBABILISTIC_SEARCH ||
				a == SUCCEEDED_PROBALISTIC_SEARCH ||
				a == ASKED_CE ||
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
				
				StatsSet RandomCounter = new StatsSet(s);
				RandomCounter.restrict(new EqualsRestriction<String>(AUTOMATA, new RandomMealyDriver().getSystemName()));
				Graph<Integer, Integer> g2 = new Graph<Integer,Integer>(OUTPUT_SYMBOLS, TRACE_LENGTH);
				StatsSet s2 = new StatsSet(RandomCounter);
				s2.restrict(new EqualsRestriction<Integer>(STATE_NUMBER,5));
				s2.restrict(new EqualsRestriction<Integer>(INPUT_SYMBOLS,5));
				s2.restrict(new EqualsRestriction<Integer>(HOMING_SEQUENCE_LENGTH,2));
				g2.plotGroup(s2, LEARNER_NB, Graph.PlotStyle.MEDIAN);
				g2.setForceOrdLogScale(false);
				g2.setFileName("influence_of_output_symbols");
				g2.export();
				
				RandomCounter.restrict(new EqualsRestriction<Integer>(OUTPUT_SYMBOLS, 5));

				Graph<Integer, Integer> g1 = new Graph<Integer,Integer>(INPUT_SYMBOLS, TRACE_LENGTH);
				StatsSet s1 = new StatsSet(RandomCounter);
				s1.restrict(new EqualsRestriction<Integer>(STATE_NUMBER, 5));
				s1.restrict(new EqualsRestriction<Integer>(LEARNER_NB, 5));
				s1.restrict(new EqualsRestriction<Integer>(HOMING_SEQUENCE_LENGTH, 2));
				g1.plot(s1, Graph.PlotStyle.MEDIAN);
				g1.plotFunc("100*x**2", "$100x\\\\^\\\\{2\\\\}$", LineStyle.APPROXIMATION);
				g1.setForceAbsLogScale(true);
				g1.forceAbsRange(null, 15);
				g1.setFileName("influence_of_input_symbols_log");
				g1.export();
				
				g1 = new Graph<Integer,Integer>(INPUT_SYMBOLS, TRACE_LENGTH);
				g1.plot(s1, Graph.PlotStyle.MEDIAN);
				g1.plotFunc("100*x**2", "$100x\\\\^\\\\{2\\\\}$", LineStyle.APPROXIMATION);
				g1.setForceOrdLogScale(false);
				g1.setFileName("influence_of_input_symbols");
				g1.export();
				
//				Graph<Integer,Float> g1_d = new Graph<Integer,Float>(INPUT_SYMBOLS, DURATION);
//				g1_d.plot(s1, PlotStyle.MEDIAN);
//				g1_d.plotFunc("0.5*x**2", "0.5x\\\\^2");
//				g1_d.setFileName("influence_of_input_symbols_duration");
//				g1_d.export();
				
//				Graph<Integer, Integer> g3 = new Graph<Integer,Integer>(LEARNER_NB, TRACE_LENGTH);
//				StatsSet s3 = new StatsSet(RandomCounter);
//				//s3.restrict(new EqualsRestriction<Integer>(STATE_NUMBER, 12));
//				g3.plotGroup(s3, HOMING_SEQUENCE_LENGTH, Graph.PlotStyle.POINTS);
//				g3.setForceOrdLogScale(false);
//				g3.export();
				
				Graph<Integer, Integer> g4 = new Graph<Integer,Integer>(STATE_NUMBER, TRACE_LENGTH);
				StatsSet s4 = new StatsSet(RandomCounter);
				s4.restrict(new EqualsRestriction<Integer>(INPUT_SYMBOLS, 5));
				g4.plot(s4, Graph.PlotStyle.MEDIAN);
				g4.setFileName("influence_of_states_number_on_length");
				g4.plotFunc("65*x**2", "$65x\\\\^2$",LineStyle.APPROXIMATION);
				g4.setForceOrdLogScale(false);
				g4.export();
				g4 = new Graph<Integer,Integer>(STATE_NUMBER, TRACE_LENGTH);
				g4.plot(s4, Graph.PlotStyle.MEDIAN);
				g4.plotFunc("65*x**2", "$65x\\\\^2$",LineStyle.APPROXIMATION);
				g4.setFileName("influence_of_states_number_log");
				g4.setForceAbsLogScale(true);
				g4.export();
				
				
//				g4 = new Graph<Integer,Integer>(STATE_NUMBER, TRACE_LENGTH);
//				g4.plotGroup(s4,LEARNER_NB, Graph.PlotStyle.MEDIAN);
//				//g4.plotFunc("50*x**2", "50x^2");
//				g4.setFileName("influence_of_states_number_learner");
//				g4.setForceOrdLogScale(false);
//				//g4.setForceAbsLogScale(true);
//				g4.export();

				
//				Graph<Integer,Float> g4_d = new Graph<Integer,Float>(STATE_NUMBER, DURATION);
//				g4_d.plotGroup(s4, HOMING_SEQUENCE_LENGTH, PlotStyle.MEDIAN);
//				g4_d.setFileName("influence_of_state_number_duration");
//				g4_d.export();
				
				Graph<Integer, Integer> g5 = new Graph<Integer,Integer>(HOMING_SEQUENCE_LENGTH, LEARNER_NB);
				StatsSet s5 = new StatsSet(RandomCounter);
				g5.plotGroup(s5, STATE_NUMBER, Graph.PlotStyle.POINTS);
				g5.export();
				
				
				
//				Graph<Integer, Float> g8 = new Graph<Integer,Float>(TRACE_LENGTH, DURATION);
//				StatsSet s8 = new StatsSet(s);
//				s8.restrict(new EqualsRestriction<Integer>(HOMING_SEQUENCE_LENGTH, 3));
//				s8.restrict(new EqualsRestriction<Integer>(LEARNER_NB, 5));
//				g8.plot(s8, Graph.PlotStyle.POINTS);
//				g8.setFileName("similarity_between_duration_and_trace_length");
//				g8.export();
				
				

				
//				Graph<Integer, Integer> g_locker = new Graph<>(INPUT_SYMBOLS, TRACE_LENGTH);
//				StatsSet s_locker = new StatsSet(s);
//				s_locker.restrict(new RangeRestriction<Integer>(STATE_NUMBER, 0, 5));
//				g_locker.plotGroup(s_locker, AUTOMATA, PlotStyle.MEDIAN);
//				g_locker.setFileName("lockers");
//				g_locker.export();
			}};
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void updateMemory(int currentMemory) {
		if (currentMemory > memory)
			memory = currentMemory;
	}
	public void increaseFailedProbabilisticSearch(){
		failedProbalisticSearch++;
	}
	public void increaseSucceededProbabilisticSearch(){
		succeededProbabilisticSearch++;
	}

	public void counterExampleCalled() {
		askedCE++;
	}

	public void setOracle(String oracle) {
		oracleUsed = oracle;
	}

}
