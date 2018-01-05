package learner.mealy.noReset;

import java.util.StringTokenizer;

import automata.Transition;
import automata.mealy.InputSequence;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.noReset.dataManager.SimplifiedDataManager;
import main.simpa.Options;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.attribute.Attribute;

public class NoResetStatsEntry extends StatsEntry {
	public static final Attribute<Integer>W_SIZE = Attribute.W_SIZE;
	public static final Attribute<Integer>W_TOTAL_LENGTH = Attribute.W_TOTAL_LENGTH;
	public static final Attribute<Integer>MAX_W_LENGTH = Attribute.MAX_W_LENGTH;
	public static final Attribute<Float>AVERAGE_W_LENGTH = Attribute.AVERAGE_W_LENGTH;
	//public static final Attribute<Integer>W1_LENGTH = Attribute.W1_LENGTH;
	public static final Attribute<Integer>H_LENGTH = Attribute.H_LENGTH;
	public final static Attribute<Integer> H_ANSWERS_NB =	Attribute.H_ANSWERS_NB;
	public static final Attribute<Integer>LOCALIZER_CALL_NB = Attribute.LOCALIZER_CALL_NB;
//	public static final Attribute<Integer>LOCALIZER_SEQUENCE_LENGTH = Attribute.LOCALIZER_SEQUENCE_LENGTH;
	public static final Attribute<Integer>TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer>MIN_TRACE_LENGTH = Attribute.MIN_TRACE_LENGTH;
	public static final Attribute<Integer>INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer>OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer>STATE_NUMBER = Attribute.STATE_NUMBER;
//	public static final Attribute<Integer>STATE_NUMBER_BOUND = Attribute.STATE_NUMBER_BOUND;
//	public static final Attribute<Integer>STATE_BOUND_OFFSET = Attribute.STATE_BOUND_OFFSET;
	public static final Attribute<Integer>LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Integer>MEMORY = Attribute.MEMORY;
	public static final Attribute<Boolean>WITH_SPEEDUP = Attribute.WITH_SPEEDUP;
	
	public static final Attribute<Integer>MAX_RECKONED_STATES = Attribute.MAX_RECKONED_STATES;
	public static final Attribute<Integer>MAX_FAKE_STATES = Attribute.MAX_FAKE_STATES;
	public static final Attribute<Long>SEED = Attribute.SEED;
	
	public final static Attribute<Integer>ASKED_COUNTER_EXAMPLE =	Attribute.ASKED_COUNTER_EXAMPLE;
	public final static Attribute<Integer>H_INCONSISTENCY_FOUND =	Attribute.H_INCONSISTENCY_FOUND;
	public final static Attribute<Integer>W_INCONSISTENCY_FOUND =	Attribute.W_INCONSISTENCY_FOUND;
	public final static Attribute<Integer>SUB_INFERANCE_NB =		Attribute.SUB_INFERANCE_NB;
	public final static Attribute<String> ORACLE_USED =				Attribute.ORACLE_USED;
	public final static Attribute<Integer>ORACLE_TRACE_LENGTH = 	Attribute.ORACLE_TRACE_LENGTH;
	public final static Attribute<Float>  ORACLE_DURATION = 		Attribute.ORACLE_DURATION;
	public final static Attribute<String> SEARCH_CE_IN_TRACE =		Attribute.SEARCH_CE_IN_TRACE;
	public static final Attribute<Boolean>ADD_H_IN_W = 				Attribute.ADD_H_IN_W;

	
	private static Attribute<?>[] attributes = new Attribute<?>[]{
			W_SIZE,
			W_TOTAL_LENGTH,
			MAX_W_LENGTH,
			AVERAGE_W_LENGTH,
//			W1_LENGTH,
			H_LENGTH,
			H_ANSWERS_NB,
			LOCALIZER_CALL_NB,
//			LOCALIZER_SEQUENCE_LENGTH,
			TRACE_LENGTH,
			INPUT_SYMBOLS,
			OUTPUT_SYMBOLS,
			STATE_NUMBER,
//			STATE_NUMBER_BOUND,
//			STATE_BOUND_OFFSET,
			LOOP_RATIO,
			AUTOMATA,
			DURATION,
			MEMORY,
			WITH_SPEEDUP,
			MIN_TRACE_LENGTH,
			MAX_RECKONED_STATES,
			MAX_FAKE_STATES,
			SEED,
			ASKED_COUNTER_EXAMPLE,
			H_INCONSISTENCY_FOUND,
			W_INCONSISTENCY_FOUND,
			ORACLE_USED,
			ORACLE_TRACE_LENGTH,
			ORACLE_DURATION,
			SUB_INFERANCE_NB,
			SEARCH_CE_IN_TRACE,
			ADD_H_IN_W,
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

	private int WSize=-1;
	private int WTotalLength=-1;
	private int maxWLength=-1;
	private int hLength=-1;
	private int hResponses;
//	private int w1Length;
	private int localizeCallNb = 0;
//	private int localizeSequenceLength;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
//	private int n;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private int memory = 0;
	private boolean with_speedup;
	private int minTraceLength = -1;
	private int maxReckonedStates = -1;
	private int maxFakeStates = -1;
	private long seed;
	private int askedCE=0;
	private int hInconsistencies=0;
	private int wInconsistencies=0;
	private String oracleUsed="Unknown";
	private int oracleTraceLength=0;
	private float oracleDuration=0;
	private int subInferenceNb=0;
	private String searchCEInTrace;
	private boolean add_h_in_w=false;


	/**
	 * rebuild a NoResetStats object from a CSV line
	 * @param line the line to parse
	 */
	public NoResetStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		WSize = Integer.parseInt(st.nextToken());
		WTotalLength = Integer.parseInt(st.nextToken());
		maxWLength = Integer.parseInt(st.nextToken());
		hLength = Integer.parseInt(st.nextToken());
		hResponses = Integer.parseInt(st.nextToken());
//		w1Length = Integer.parseInt(st.nextToken());
		localizeCallNb = Integer.parseInt(st.nextToken());
//		localizeSequenceLength = Integer.parseInt(st.nextToken());
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
//		n = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		memory = Integer.parseUnsignedInt(st.nextToken());
		with_speedup = Boolean.parseBoolean(st.nextToken());
		minTraceLength = Integer.parseInt(st.nextToken());
		maxReckonedStates = Integer.parseInt(st.nextToken());
		maxFakeStates = Integer.parseInt(st.nextToken());
		seed = Long.parseLong(st.nextToken());
		askedCE = Integer.parseInt(st.nextToken());
		hInconsistencies = Integer.parseInt(st.nextToken());
		wInconsistencies = Integer.parseInt(st.nextToken());
		oracleUsed = st.nextToken();
		oracleTraceLength = Integer.parseInt(st.nextToken());
		oracleDuration = Float.parseFloat(st.nextToken());
		subInferenceNb = Integer.parseInt(st.nextToken());
		searchCEInTrace = st.nextToken();
		add_h_in_w = Boolean.parseBoolean(st.nextToken());
		
	}

	public NoResetStatsEntry( MealyDriver d){
//		WSize = W.size();
//		w1Length = (W.size()==0)?0:W.get(0).getLength();
		this.inputSymbols = d.getInputSymbols().size();
		this.outputSymbols = d.getOutputSymbols().size();
//		this.n = n;
		this.automata = d.getSystemName();
		this.with_speedup = !Options.ICTSS2015_WITHOUT_SPEEDUP;
		this.seed=Options.SEED;
	}

//	protected void setLocalizeSequenceLength(int length){
//		localizeSequenceLength = length;
//	}

	protected void setOracle(String oracle) {
		oracleUsed = oracle;
	}

	protected void increaseHInconsitencies(){
		hInconsistencies++;
	}
	protected void increaseWInconsistencies() {
		wInconsistencies++;
	}

	protected void increaseOracleCallNb(int traceLength, float duration) {
		askedCE++;
		oracleTraceLength += traceLength;
		oracleDuration += duration;
	}

	protected void increaseLocalizeCallNb(){
		localizeCallNb ++;
	}

	protected void increaseTraceLength(int traceLength) {
		this.traceLength += traceLength;
	}
	protected void setMinTraceLength(int minTraceLength) {
		this.minTraceLength = minTraceLength;
	}

	protected void setStatesNumber(int statesNumber) {
		this.statesNumber = statesNumber;
	}

	protected void updateMaxReckonedStates(int reckonedStates){
		if (reckonedStates > maxReckonedStates)
			maxReckonedStates=reckonedStates;
	}
	protected void updateMaxFakeStates(int fakeStates){
		if (fakeStates > maxFakeStates)
			maxFakeStates=fakeStates;
	}

	protected void increaseWithDataManager(SimplifiedDataManager dataManager) {
		subInferenceNb++;
		updateMaxReckonedStates(dataManager.getConjecture().getStateCount());
		updateMaxFakeStates(dataManager.getIdentifiedFakeStates().size());
		increaseTraceLength(dataManager.traceSize());
	}	
	protected void finalUpdate(SimplifiedDataManager dataManager) {
		updateWithConjecture(dataManager.getConjecture());
		WSize=dataManager.getW().size();
		WTotalLength=0;
		maxWLength=0;
		for (InputSequence w:dataManager.getW()){
			int length=w.getLength();
			WTotalLength+=length;
			if (length>maxWLength){
				maxWLength=length;
			}
		}
			hResponses=dataManager.getHResponsesNb();
			hLength=dataManager.h.getLength();
	}

	
	public void updateWithConjecture(LmConjecture conjecture) {
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
		if (a == W_SIZE)
			return (T) new Integer(WSize);
		if (a==W_TOTAL_LENGTH)
			return (T) new Integer(WTotalLength);
		if (a==MAX_W_LENGTH)
			return (T) new Integer(maxWLength);
		if (a==AVERAGE_W_LENGTH)
			return (T)(new Float((float)WTotalLength/WSize));
		if (a==H_LENGTH)
			return (T) new Integer(hLength);
		if (a==H_ANSWERS_NB)
			return (T) new Integer(hResponses);
//		if (a == W1_LENGTH)
//			return (T) new Integer(w1Length);
		if (a == LOCALIZER_CALL_NB)
			return (T) new Integer(localizeCallNb);
//		if (a == LOCALIZER_SEQUENCE_LENGTH)
//			return (T) new Integer(localizeSequenceLength);
		if (a == TRACE_LENGTH)
			return (T) new Integer(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) new Integer(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) new Integer(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) new Integer(statesNumber);
//		if (a == STATE_NUMBER_BOUND)
//			return (T) new Integer(n);
//		if (a == STATE_BOUND_OFFSET)
//			return (T) new Integer(n - statesNumber);
		if (a == LOOP_RATIO)
			return (T) new Integer(loopTransitionPercentage);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == DURATION)
			return (T) new Float(duration);
		if (a == MEMORY)
			return (T) new Integer(memory);
		if (a == WITH_SPEEDUP)
			return (T) new Boolean(with_speedup);
		if (a == MIN_TRACE_LENGTH)
			return (T) new Integer(minTraceLength);
		if (a == MAX_RECKONED_STATES)
			return (T) new Integer(maxReckonedStates);
		if (a == MAX_FAKE_STATES)
			return (T) new Integer(maxFakeStates);
		if (a == SEED)
			return (T) new Long(seed);
		if (a==ASKED_COUNTER_EXAMPLE)
			return (T) new Integer(askedCE);
		if (a==H_INCONSISTENCY_FOUND)
			return (T) new Integer(hInconsistencies);
		if (a==W_INCONSISTENCY_FOUND)
			return (T) new Integer(wInconsistencies);
		if (a==SUB_INFERANCE_NB)
			return (T) new Integer(subInferenceNb);
		if (a == ORACLE_USED)
			return (T) oracleUsed;
		if (a == ORACLE_TRACE_LENGTH)
			return (T) new Integer(oracleTraceLength);
		if (a == ORACLE_DURATION)
			return (T) new Float(oracleDuration);
		if (a == SEARCH_CE_IN_TRACE)
			return (T) searchCEInTrace;
		if (a == ADD_H_IN_W)
			return (T) new Boolean(add_h_in_w);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == W_SIZE || 
				a == W_TOTAL_LENGTH ||
				a == MAX_W_LENGTH ||
				a == H_LENGTH ||
//				a == W1_LENGTH ||
				a == LOCALIZER_CALL_NB ||
//				a == LOCALIZER_SEQUENCE_LENGTH ||
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
//				a == STATE_NUMBER_BOUND ||
//				a == STATE_BOUND_OFFSET ||
				a == MEMORY ||
				a == MIN_TRACE_LENGTH ||
				a == MAX_RECKONED_STATES ||
				a == MAX_FAKE_STATES ||
				a == H_ANSWERS_NB ||
				a == SUB_INFERANCE_NB ||
				a == LOOP_RATIO ||
				a == ASKED_COUNTER_EXAMPLE||
				a == ORACLE_TRACE_LENGTH)
			return ((Integer) get(a)).floatValue();
		if (a == SEED)
			return ((Long) get(a)).floatValue();
		if (a == DURATION || a == AVERAGE_W_LENGTH || a == ORACLE_DURATION)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");

	}
	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new NoResetGraphGenerator();
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void updateMemory(int currentMemory) {
		if (currentMemory > memory)
			memory = currentMemory;
	}

	protected void setSearchCEInTrace(String string) {
		searchCEInTrace = string;
	}
	
	protected void setAddHInW(boolean b) {
		add_h_in_w=b;
	}

}
