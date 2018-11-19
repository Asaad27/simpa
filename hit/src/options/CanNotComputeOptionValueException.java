package options;

/**
 * This class is used for exception which happens during learning but which is
 * related to a wrong option.
 * 
 * Wrong options which can be detected BEFORE inference should be checked using
 * {@link OptionValidator}.
 * 
 * @author Nicolas BREMOND
 *
 */
public class CanNotComputeOptionValueException extends RuntimeException {
	private static final long serialVersionUID = -418674591263988294L;

	public CanNotComputeOptionValueException(String what) {
		super(what);
	}

}
