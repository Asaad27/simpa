package learner.mealy.hW;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.hW.dataManager.SimplifiedDataManager;
import main.simpa.Options;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.attribute.Attribute;

public class HWStatsEntry extends StatsEntry {
	public static final Attribute<Integer>W_SIZE = Attribute.W_SIZE;
	public static final Attribute<Integer>W_TOTAL_LENGTH = Attribute.W_TOTAL_LENGTH;
	public static final Attribute<Integer>MAX_W_LENGTH = Attribute.MAX_W_LENGTH;
	public static final Attribute<Float>AVERAGE_W_LENGTH = Attribute.AVERAGE_W_LENGTH;
	public static final Attribute<Integer> H_MAX_LENGTH = Attribute.H_MAX_LENGTH;
	public final static Attribute<Integer> H_ANSWERS_NB =	Attribute.H_ANSWERS_NB;
	public static final Attribute<Integer>LOCALIZER_CALL_NB = Attribute.LOCALIZER_CALL_NB;
	public static final Attribute<Integer>TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer>MIN_TRACE_LENGTH = Attribute.MIN_TRACE_LENGTH;
	public static final Attribute<Integer>INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer>OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer>STATE_NUMBER = Attribute.STATE_NUMBER;
	public static final Attribute<Integer>LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Integer>MEMORY = Attribute.MEMORY;
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
	public static final Attribute<Boolean>CHECK_3rd_INCONSISTENCY =	Attribute.CHECK_3rd_INCONSISTENCY;
	public static final Attribute<Boolean>REUSE_HZXW =				Attribute.REUSE_HZXW;
	public static final Attribute<Boolean>PRECOMPUTED_W =			Attribute.PRECOMPUTED_W;
	public static final Attribute<Boolean>USE_ADAPTIVE_H = 			Attribute.USE_ADAPTIVE_H;
	public static final Attribute<Float> ORACLE_TRACE_PERCENTAGE =	Attribute.ORACLE_TRACE_PERCENTAGE;

	
	private static Attribute<?>[] attributes = new Attribute<?>[]{
			W_SIZE,
			W_TOTAL_LENGTH,
			MAX_W_LENGTH,
			AVERAGE_W_LENGTH,
			H_MAX_LENGTH,
			H_ANSWERS_NB,
			LOCALIZER_CALL_NB,
			TRACE_LENGTH,
			INPUT_SYMBOLS,
			OUTPUT_SYMBOLS,
			STATE_NUMBER,
			LOOP_RATIO,
			AUTOMATA,
			DURATION,
			MEMORY,
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
			CHECK_3rd_INCONSISTENCY,
			REUSE_HZXW,
			PRECOMPUTED_W,
			USE_ADAPTIVE_H,
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
	private int hMaxLength = -1;
	private int hResponses;
	private int localizeCallNb = 0;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private int memory = 0;
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
	private boolean check_3rd_inconsistency=false;
	private boolean reuse_hzxw = false;
	private boolean precomputedW = false;
	private boolean useAdaptiveH = false;

	/**
	 * rebuild a HWStats object from a CSV line
	 * @param line the line to parse
	 */
	public HWStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		WSize = Integer.parseInt(st.nextToken());
		WTotalLength = Integer.parseInt(st.nextToken());
		maxWLength = Integer.parseInt(st.nextToken());
		hMaxLength = Integer.parseInt(st.nextToken());
		hResponses = Integer.parseInt(st.nextToken());
		localizeCallNb = Integer.parseInt(st.nextToken());
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		memory = Integer.parseUnsignedInt(st.nextToken());
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
		check_3rd_inconsistency = Boolean.parseBoolean(st.nextToken());
		reuse_hzxw = Boolean.parseBoolean(st.nextToken());
		precomputedW = Boolean.parseBoolean(st.nextToken());
		useAdaptiveH = Boolean.parseBoolean(st.nextToken());
		
	}

	public HWStatsEntry( MealyDriver d){
//		WSize = W.size();
//		w1Length = (W.size()==0)?0:W.get(0).getLength();
		this.inputSymbols = d.getInputSymbols().size();
//		this.n = n;
		this.automata = d.getSystemName();
		this.seed=Options.SEED;
		this.reuse_hzxw = Options.REUSE_HZXW;
		this.precomputedW = Options.HW_WITH_KNOWN_W;
		this.useAdaptiveH = Options.ADAPTIVE_H;
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
		hMaxLength = dataManager.h.getMaxLength();
	}

	
	public void updateWithConjecture(LmConjecture conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions=0;
		Set<String> outputSymbols=new HashSet<>();
		for (MealyTransition t : conjecture.getTransitions()){
			outputSymbols.add(t.getOutput());
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		loopTransitionPercentage = ((100*loopTransitions)/conjecture.getTransitionCount());
		this.outputSymbols = outputSymbols.size();
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
		if (a == H_MAX_LENGTH)
			return (T) new Integer(hMaxLength);
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
		if (a==CHECK_3rd_INCONSISTENCY)
			return (T) new Boolean(check_3rd_inconsistency);
		if (a==REUSE_HZXW)
				return (T) new Boolean(reuse_hzxw);
		if (a == PRECOMPUTED_W)
			return (T) new Boolean(precomputedW);
		if (a == USE_ADAPTIVE_H)
			return (T) new Boolean(useAdaptiveH);
		if (a == ORACLE_TRACE_PERCENTAGE)
			return (T) new Float(100. * oracleTraceLength / traceLength);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == W_SIZE || 
				a == W_TOTAL_LENGTH ||
				a == MAX_W_LENGTH ||
				a == H_MAX_LENGTH ||
				a == LOCALIZER_CALL_NB ||
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == MEMORY ||
				a == MIN_TRACE_LENGTH ||
				a == MAX_RECKONED_STATES ||
				a == MAX_FAKE_STATES ||
				a == H_ANSWERS_NB ||
				a == W_INCONSISTENCY_FOUND ||
				a == SUB_INFERANCE_NB ||
				a == LOOP_RATIO ||
				a == ASKED_COUNTER_EXAMPLE||
				a == H_INCONSISTENCY_FOUND ||
				a == ORACLE_TRACE_LENGTH)
			return ((Integer) get(a)).floatValue();
		if (a == SEED)
			return ((Long) get(a)).floatValue();
		if (a == DURATION || a == AVERAGE_W_LENGTH || a == ORACLE_DURATION
				|| a == ORACLE_TRACE_PERCENTAGE)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");

	}
	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new HWGraphGenerator();
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

	protected void setCheck3rdInconsistency(boolean b) {
		check_3rd_inconsistency = b;
	}

}
