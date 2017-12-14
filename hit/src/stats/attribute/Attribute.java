package stats.attribute;

import stats.Units;

public class Attribute <T extends Comparable<T>> {
	public final static Attribute<Integer> W_SIZE = 				new Attribute<Integer>(	"Size of W",						Units.SEQUENCES,false,	true,	false);
	public final static Attribute<Integer> W1_LENGTH = 				new Attribute<Integer>(	"Length of first W element",		Units.SYMBOLS,	false,	true,	false);
	public final static Attribute<Integer> LOCALIZER_CALL_NB = 		new Attribute<Integer>(	"Number of call to localizer",		Units.FUNCTION_CALL,false,false,false);
	public final static Attribute<Integer> LOCALIZER_SEQUENCE_LENGTH = new Attribute<Integer>("Length of localizer sequence",	Units.SYMBOLS,	false,	false,	false);
	public final static Attribute<Integer> TRACE_LENGTH = 			new Attribute<Integer>(	"length of trace",					Units.SYMBOLS,	true,	false,	false);
	public final static Attribute<Integer> MIN_TRACE_LENGTH = 		new Attribute<Integer>(	"sufficient length of trace",		Units.SYMBOLS,	true,	false,	false);
	public final static Attribute<Integer> INPUT_SYMBOLS = 			new Attribute<Integer>(	"number of input symbols",			Units.SYMBOLS,	false,	true,	false);
	public final static Attribute<Integer> OUTPUT_SYMBOLS = 		new Attribute<Integer>(	"number of output symbols",			Units.SYMBOLS,	false,	true,	false);
	public final static Attribute<Integer> STATE_NUMBER = 			new Attribute<Integer>(	"number of states",					Units.STATES,	false,	true,	false);
	public final static Attribute<Integer> STATE_NUMBER_BOUND = 	new Attribute<Integer>(	"bound of state number",			Units.STATES,	false,	true,	false);
	public final static Attribute<Integer> STATE_BOUND_OFFSET = 	new Attribute<Integer>(	"difference between bound and real state number",Units.STATES,false,true,true);
	public final static Attribute<Integer> LOOP_RATIO = 			new Attribute<Integer>(	"percentage of loop transitions",	Units.PERCENT,	false,	true,	false);
	public final static Attribute<Float> DURATION = 				new Attribute<Float>(	"duration of learning",				Units.SECONDS,	false,	false,	false);
	public final static Attribute<String> AUTOMATA =				new Attribute<String>(	"infered automata",					Units.NO_UNITS,	false, 	true, 	false);
	public static final Attribute<Integer> MEMORY = 				new Attribute<Integer>(	"used memory", 						Units.BYTE, 	false, 	false, 	false);
	public final static Attribute<Integer> NODES_NB =				new Attribute<Integer>(	"number of nodes",					Units.NODES,	true,	false,	false);
	public final static Attribute<Integer> RESET_CALL_NB =			new Attribute<Integer>(	"number of call to reset",			Units.FUNCTION_CALL,false,false,false);
	public final static Attribute<Integer>HOMING_SEQUENCE_LENGTH =	new Attribute<Integer>(	"length of homing sequence",		Units.SYMBOLS,	false,	true,	false);
	public final static Attribute<Integer>LEARNER_NUMBER =			new Attribute<Integer>(	"number of sub-learner",			Units.LEARNER,	false,	true,	false);
	public final static Attribute<Boolean>WITH_SPEEDUP =			new Attribute<Boolean>(	"inference with speed up",			Units.BOOLEAN,	false,	true,	false);
	
	
	
	private String name;
	private Units units;
	private boolean isParameter;
	private boolean isVirtual;
	private boolean useLogScale;

	private Attribute(String name, Units units, boolean useLogScale,boolean isParameter,boolean isVirtual) {
		this.units = units;
		this.name = name;
		this.useLogScale = useLogScale;
		this.isParameter = isParameter;
		this.isVirtual = isVirtual;
	}
	/**
	 * @return a human-readable name of this attribute.
	 */
	public String getName(){
		return name;
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
