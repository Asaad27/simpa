package stats;

public class Units {
	public final static Units PERCENT = new Units("%");
	public final static Units SYMBOLS = new Units("symbols");
	public final static Units SEQUENCES = new Units("sequences");
	public final static Units STATES = new Units("states");
	public final static Units FUNCTION_CALL = new Units("calls");
	public final static Units SECONDS = new Units("s");
	public final static Units NO_UNITS = new Units("");
	private String symbol;

	public Units(String symbol){
		this.symbol = symbol; 
	}

	public String getSymbol(){
		return symbol;
	}

	public String toString(){
		return getSymbol();
	}
}
