package options;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public ParseException(Exception other) {
		super(other);
	}
}
