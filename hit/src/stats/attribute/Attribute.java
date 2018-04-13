package stats.attribute;

import stats.Units;

public class Attribute <T extends Comparable<T>> {

	public final static Attribute<Integer>	W_SIZE = 					new Attribute<Integer>(	"Size of W",									true,	Units.SEQUENCES,	false,	false,	false);
	public final static Attribute<Integer>	W_TOTAL_LENGTH = 			new Attribute<Integer>(	"sum of length of W sequences",					true,	Units.SYMBOLS,		false,	false,	false);
	public final static Attribute<Integer>	W1_LENGTH = 				new Attribute<Integer>(	"Length of first W element",					true,	Units.SYMBOLS,		false,	false,	false);
	public final static Attribute<Integer>	MAX_W_LENGTH = 				new Attribute<Integer>(	"Length of longest W element",					true,	Units.SYMBOLS,		false,	false,	false);
	public final static Attribute<Float>	AVERAGE_W_LENGTH = 			new Attribute<Float>(	"average length of W element",					true,	Units.SYMBOLS,		false,	false,	true);
	public final static Attribute<Integer>	H_LENGTH = 					new Attribute<Integer>(	"Length of homing sequence",					true,	Units.SYMBOLS,		false,	false,	false);
	public final static Attribute<Integer>	H_ANSWERS_NB =				new Attribute<Integer>(	"number of answers to h",						false,	Units.SEQUENCES,	false,	false,	false);
	public final static Attribute<Integer>	LOCALIZER_CALL_NB = 		new Attribute<Integer>(	"Number of call to localizer",					false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	LOCALIZER_SEQUENCE_LENGTH = new Attribute<Integer>(	"Length of localizer sequence",					true,	Units.SYMBOLS,		false,	false,	false);
	public final static Attribute<Integer>	TRACE_LENGTH = 				new Attribute<Integer>(	"length of trace",								true,	Units.SYMBOLS,		true,	false,	false);
	public final static Attribute<Integer>	MIN_TRACE_LENGTH = 			new Attribute<Integer>(	"sufficient length of trace",					true,	Units.SYMBOLS,		true,	false,	false);
	public final static Attribute<Integer>	INPUT_SYMBOLS = 			new Attribute<Integer>(	"number of input symbols",						false,	Units.SYMBOLS,		false,	true,	false);
	public final static Attribute<Integer>	OUTPUT_SYMBOLS = 			new Attribute<Integer>(	"number of output symbols",						false,	Units.SYMBOLS,		false,	true,	false);
	public final static Attribute<Integer>	STATE_NUMBER = 				new Attribute<Integer>(	"number of states",								false,	Units.STATES,		true,	true,	false);
	public final static Attribute<Integer>	STATE_NUMBER_BOUND = 		new Attribute<Integer>(	"bound of state number",						true,	Units.STATES,		false,	true,	false);
	public final static Attribute<Integer>	STATE_BOUND_OFFSET = 		new Attribute<Integer>(	"difference between bound and real state number",true,	Units.STATES,		false,	true,	true);
	public final static Attribute<Integer>	LOOP_RATIO = 				new Attribute<Integer>(	"percentage of loop transitions",				true,	Units.PERCENT,		false,	true,	false);
	public final static Attribute<Float>	DURATION = 					new Attribute<Float>(	"duration of learning",							true,	Units.SECONDS,		false,	false,	false);
	public final static Attribute<String>	AUTOMATA =					new Attribute<String>(	"infered automata",								false,	Units.NO_UNITS,		false, 	true, 	false);
	public static final Attribute<Integer>	MEMORY = 					new Attribute<Integer>(	"used memory", 									true,	Units.BYTE, 		false, 	false, 	false);
	public final static Attribute<Integer>	NODES_NB =					new Attribute<Integer>(	"number of nodes",								false,	Units.NODES,		true,	false,	false);
	public final static Attribute<Integer>	RESET_CALL_NB =				new Attribute<Integer>(	"number of call to reset",						false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	HOMING_SEQUENCE_LENGTH =	new Attribute<Integer>(	"length of homing sequence",					true,	Units.SYMBOLS,		false,	true,	false);
	public final static Attribute<Integer>	LEARNER_NUMBER =			new Attribute<Integer>(	"number of sub-learner",						false,	Units.LEARNER,		false,	true,	false);
	public final static Attribute<Boolean>	WITH_SPEEDUP =				new Attribute<Boolean>(	"inference with speed up",						false,	Units.BOOLEAN,		false,	true,	false);
	public final static Attribute<Integer>	MAX_RECKONED_STATES =		new Attribute<Integer>(	"maximum number of reckoned states",			true,	Units.STATES,		false,	false,	false);
	public final static Attribute<Integer>	MAX_FAKE_STATES =			new Attribute<Integer>(	"maximum number of fake states",				true,	Units.STATES,		false,	false,	false);
	public final static Attribute<Long>		SEED =						new Attribute<Long>(	"seed used to create automaton",				false,	Units.NO_UNITS,		false,	false,	false);
	public final static Attribute<Integer>	ASKED_COUNTER_EXAMPLE =		new Attribute<Integer>(	"number of counter example asked",				false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	H_INCONSISTENCY_FOUND =		new Attribute<Integer>(	"number of inconsistency on h",					false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	W_INCONSISTENCY_FOUND =		new Attribute<Integer>(	"number of inconsistency on w",					false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	SUB_INFERANCE_NB =			new Attribute<Integer>(	"number of sub-inference",						false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<String>	ORACLE_USED =				new Attribute<String>(	"oracle giving counter examples",				false,	Units.NO_UNITS,		false, 	true, 	false);
	public final static Attribute<Integer>	ORACLE_TRACE_LENGTH = 		new Attribute<Integer>(	"length of trace used by oracle",				true,	Units.SYMBOLS,		true,	false,	false);
	public final static Attribute<Float>	ORACLE_DURATION = 			new Attribute<Float>(	"duration of oracle",							true,	Units.SECONDS,		false,	false,	false);
	public final static Attribute<String> 	SEARCH_CE_IN_TRACE =		new Attribute<String>(	"using trace for counter-example",				false,	Units.NO_UNITS,		false, 	true, 	false);
	public final static Attribute<Boolean>	ADD_H_IN_W =				new Attribute<Boolean>(	"add homing sequence in W-set",					false,	Units.BOOLEAN,		false,	true,	false);
	public final static Attribute<Boolean>	CHECK_3rd_INCONSISTENCY =	new Attribute<Boolean>(	"check inconsistency between h mapping and conjecture",false,Units.BOOLEAN,	false,	true,	false);
	public final static Attribute<Boolean>	REUSE_HZXW =				new Attribute<Boolean>(	"reuse hzxw sequences",							false,	Units.BOOLEAN,		false,	true,	false);
	public final static Attribute<Boolean>	PRECOMPUTED_W =				new Attribute<Boolean>(	"using a known W-set",							false,	Units.BOOLEAN,		false,	true,	false);
	public final static Attribute<Boolean>	RS_WITH_GIVEN_H =			new Attribute<Boolean>(	"homing sequence is known",						false,	Units.BOOLEAN,		false,	true,	false);
	public final static Attribute<Integer>	FAILED_PROBALISTIC_SEARCH=	new Attribute<Integer>(	"number of failed probalistic search",			false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Integer>	SUCCEEDED_PROBALISTIC_SEARCH=new Attribute<Integer>("number of succeeded probalistic search",		false,	Units.FUNCTION_CALL,false,	false,	false);
	public final static Attribute<Float>	ORACLE_TRACE_PERCENTAGE=	new Attribute<Float>(	"percentage of trace used by oracle",			true,	Units.PERCENT,		false,	false,	true);
	
	private String name;
	private Units units;
	private boolean isParameter;
	private boolean isVirtual;
	private boolean useLogScale;
	private boolean displayUnits;

	public Attribute(String name,boolean displayUnits, Units units, boolean useLogScale,boolean isParameter,boolean isVirtual) {
		this.units = units;
		this.name = name;
		this.useLogScale = useLogScale;
		this.isParameter = isParameter;
		this.isVirtual = isVirtual;
		this.displayUnits=displayUnits;
	}
	/**
	 * @return a human-readable name of this attribute.
	 */
	public String getName(){
		return name;
	}
	public boolean shouldDisplayUnits(){
		return displayUnits;
	}
	/**
	 * @return the units of this attribute.
	 */
	public Units getUnits(){
		return units;
	}

	/**
	 * define if this attribute is a parameter of the studied algorithm.
	 */
	public boolean isParameter(){
		return isParameter;
	}
	/**
	 * define if this attribute can be considered as a parameter of the studied algorithm.
	 * For example the difference between two parameter is called a virtual parameter.
	 * @see #getRelatedAttributes()
	 */
	public boolean isVirtualParameter(){
		return isVirtual;
	}
	/**
	 * if this attribute is virtual, that means it depends of others attributes.
	 * @return the others Attributes which define this one.
	 */
	public Attribute<?>[] getRelatedAttributes(){
		return new Attribute<?>[]{};
	}

	/**
	 * indicate if using a logarithmic scale for this attribute is a good idea.
	 */
	public boolean useLogScale(){
		return useLogScale;
	}
	
	public String toString(){
		return getName();
	}
}
