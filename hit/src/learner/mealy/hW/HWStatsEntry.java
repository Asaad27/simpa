package learner.mealy.hW;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.hW.dataManager.FullyQualifiedState;
import learner.mealy.hW.dataManager.SimplifiedDataManager;
import main.simpa.Options;
import options.RandomOption;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsEntry_OraclePart;
import stats.attribute.Attribute;

public class HWStatsEntry extends StatsEntry {
	public static final Attribute<Integer>MAX_W_TOTAL_LENGTH = 		Attribute.MAX_W_TOTAL_LENGTH;
	public static final Attribute<Float>AVERAGE_W_SIZE = 			Attribute.AVERAGE_W_SIZE;
	public static final Attribute<Integer>MAX_W_SIZE = 				Attribute.MAX_W_SIZE;
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
	public static final Attribute<Boolean> ADD_I_IN_W = Attribute.ADD_I_IN_W;
	public static final Attribute<Boolean>CHECK_3rd_INCONSISTENCY =	Attribute.CHECK_3rd_INCONSISTENCY;
	public static final Attribute<Boolean>REUSE_HZXW =				Attribute.REUSE_HZXW;
	public static final Attribute<Boolean>PRECOMPUTED_W =			Attribute.PRECOMPUTED_W;
	public static final Attribute<Boolean>USE_ADAPTIVE_H = 			Attribute.USE_ADAPTIVE_H;
	public static final Attribute<Boolean>USE_ADAPTIVE_W = 			Attribute.USE_ADAPTIVE_W;
	public static final Attribute<Boolean>USE_RESET =	 			Attribute.USE_RESET;
	public static final Attribute<Float> AVG_NB_TRIED_W =			Attribute.AVG_NB_TRIED_W;
	public static final Attribute<Integer>RESET_CALL_NB =			Attribute.RESET_CALL_NB;
	public static final Attribute<Integer>ORACLE_RESET_NB =			Attribute.ORACLE_RESET_NB;
	public static final Attribute<Float> ORACLE_TRACE_PERCENTAGE =	Attribute.ORACLE_TRACE_PERCENTAGE;

	
	// TODO : remove the USE_SPEEDUP which not used by hW.
	private static Attribute<?>[] attributes = new Attribute<?>[]{
			MAX_W_TOTAL_LENGTH,
			AVERAGE_W_SIZE,
			MAX_W_SIZE,
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
			ADD_I_IN_W,
			USE_ADAPTIVE_H,
			USE_ADAPTIVE_W,
			USE_RESET,
			AVG_NB_TRIED_W,
			RESET_CALL_NB,
			ORACLE_RESET_NB,
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

	private int maxWTotalLength = -1;
	private float avgWSize = -1;
	private int maxWSize = -1;
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
	private int hInconsistencies = 0;
	private int wInconsistencies = 0;
	private int subInferenceNb = 0;
	private String searchCEInTrace;
	private boolean add_h_in_w=false;
	private boolean check_3rd_inconsistency=false;
	private boolean reuse_hzxw = false;
	private boolean precomputedW = false;
	private boolean add_I_in_W = false;
	private boolean useAdaptiveH = false;
	private boolean useAdaptiveW = false;
	private boolean useReset = false;
	private float avgNbTriedWSuffixes = -1;
	private int resetCallNb = 0;

	final StatsEntry_OraclePart oracle;

	/**
	 * rebuild a HWStats object from a CSV line
	 * @param line the line to parse
	 */
	public HWStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		maxWTotalLength = Integer.parseInt(st.nextToken());
		avgWSize = Float.parseFloat(st.nextToken());
		maxWSize = Integer.parseInt(st.nextToken());
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
		st.nextToken();
		hInconsistencies = Integer.parseInt(st.nextToken());
		wInconsistencies = Integer.parseInt(st.nextToken());
		st.nextToken();
		st.nextToken();
		st.nextToken();
		subInferenceNb = Integer.parseInt(st.nextToken());
		searchCEInTrace = st.nextToken();
		add_h_in_w = Boolean.parseBoolean(st.nextToken());
		check_3rd_inconsistency = Boolean.parseBoolean(st.nextToken());
		reuse_hzxw = Boolean.parseBoolean(st.nextToken());
		precomputedW = Boolean.parseBoolean(st.nextToken());
		add_I_in_W = Boolean.parseBoolean(st.nextToken());
		useAdaptiveH = Boolean.parseBoolean(st.nextToken());
		useAdaptiveW = Boolean.parseBoolean(st.nextToken());
		useReset = Boolean.parseBoolean(st.nextToken());
		avgNbTriedWSuffixes = Float.parseFloat(st.nextToken());
		resetCallNb = Integer.parseInt(st.nextToken());
		st.nextToken();//Oracle reset nb
		
		st = new StringTokenizer(line, ",");
		oracle = new StatsEntry_OraclePart(st, getAttributes());

	}

	public HWStatsEntry(MealyDriver d, HWOptions options) {
		this.inputSymbols = d.getInputSymbols().size();
		this.automata = d.getSystemName();
		this.seed = RandomOption.MAIN_SEED;
		this.reuse_hzxw = options.useDictionary.isEnabled();
		this.precomputedW = options.usePrecomputedW();
		this.useAdaptiveH = options.useAdaptiveH();
		this.useAdaptiveW = options.useAdaptiveW();
		this.useReset = options.useReset.isEnabled();
		this.add_h_in_w = options.addHInW.isEnabled();
		this.add_I_in_W = options.addIInW();
		this.searchCEInTrace = options.searchCeInTrace.isEnabled() ? "simple"
				: "none";
		this.check_3rd_inconsistency = options.checkInconsistenciesHMapping
				.isEnabled();
		oracle = new StatsEntry_OraclePart(options.getOracleOption());
	}

//	protected void setLocalizeSequenceLength(int length){
//		localizeSequenceLength = length;
//	}

	protected void increaseHInconsitencies(){
		hInconsistencies++;
	}
	protected void increaseWInconsistencies() {
		wInconsistencies++;
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
		maxWTotalLength = 0;
		maxWSize = 0;
		int sumWSize = 0;
		int statesNb = 0;
		for (FullyQualifiedState q : dataManager.getStates()) {
			statesNb++;
			int WTotalLength = 0;
			int WSize = 0;
			for (LmTrace seq : q.getWResponses().knownResponses()) {
				WTotalLength += seq.size();
				WSize += 1;
			}
			if (WSize > maxWSize)
				maxWSize = WSize;
			sumWSize += WSize;
			if (WTotalLength > maxWTotalLength)
				maxWTotalLength = WTotalLength;
		}
		avgWSize = sumWSize / statesNb;

		hResponses = dataManager.getHResponsesNb();
		hMaxLength = dataManager.h.getMaxLength();
		resetCallNb = dataManager.getTotalResetNb();
		traceLength -= oracle.getLastTraceLength();
		duration -= oracle.getLastDuration();
		resetCallNb -= oracle.getLastResetNb();
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
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == MAX_W_TOTAL_LENGTH)
			return (T) new Integer(maxWTotalLength);
		if (a == MAX_W_SIZE)
			return (T) new Integer(maxWSize);
		if (a == AVERAGE_W_SIZE)
			return (T) new Float(avgWSize);
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
		if (a == ASKED_COUNTER_EXAMPLE)
			return (T) new Integer(oracle.getAskedCE());
		if (a == H_INCONSISTENCY_FOUND)
			return (T) new Integer(hInconsistencies);
		if (a==W_INCONSISTENCY_FOUND)
			return (T) new Integer(wInconsistencies);
		if (a==SUB_INFERANCE_NB)
			return (T) new Integer(subInferenceNb);
		if (a == ORACLE_USED)
			return (T) oracle.getName();
		if (a == ORACLE_TRACE_LENGTH)
			return (T) new Integer(oracle.getTraceLength());
		if (a == ORACLE_DURATION)
			return (T) new Float(oracle.getDuration());
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
		if (a == ADD_I_IN_W)
			return (T) new Boolean(add_I_in_W);
		if (a == USE_ADAPTIVE_H)
			return (T) new Boolean(useAdaptiveH);
		if (a == USE_ADAPTIVE_W)
				return (T) new Boolean(useAdaptiveW);
		if (a == USE_RESET)
				return (T) new Boolean(useReset);
		if (a == ORACLE_TRACE_PERCENTAGE)
			return (T) new Float(
					100. * oracle.getTraceLength() / traceLength);
		if (a == AVG_NB_TRIED_W)
			return (T) new Float(avgNbTriedWSuffixes);
		if (a == RESET_CALL_NB)
			return (T) new Integer(resetCallNb);
		if (a == ORACLE_RESET_NB)
			return (T) new Integer(oracle.getResetNb());
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == MAX_W_TOTAL_LENGTH || 
				a == MAX_W_SIZE ||
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
				a == RESET_CALL_NB ||
				a == ORACLE_RESET_NB ||
				a == ORACLE_TRACE_LENGTH)
			return ((Integer) get(a)).floatValue();
		if (a == SEED)
			return ((Long) get(a)).floatValue();
		if (a == DURATION || a == ORACLE_DURATION || a == AVERAGE_W_SIZE
				|| a == ORACLE_TRACE_PERCENTAGE || a == AVG_NB_TRIED_W)
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

	public void setAvgTriedWSuffixes(float f) {
		avgNbTriedWSuffixes = f;
	}
}
